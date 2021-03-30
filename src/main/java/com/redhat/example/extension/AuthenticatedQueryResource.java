package com.redhat.example.extension;

import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsVariableList;
import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsWithVariables;
import com.redhat.example.extension.utils.AuthQueryUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.QueryService;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.SearchQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstanceUserTaskWithVariablesList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.util.RestUtils;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.jbpm.services.api.AdvanceRuntimeDataService.PROCESS_ATTR_INSTANCE_ID;
import static org.jbpm.services.api.AdvanceRuntimeDataService.TASK_ATTR_NAME;
import static org.kie.server.api.rest.RestURI.TASK_GET_URI;
import static org.kie.server.api.rest.RestURI.TASK_INSTANCE_ID;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;
import static org.kie.server.remote.rest.common.util.RestUtils.notFound;


@Api(value = "Process queries")
@Path("/server/queries/variables/processes")
public class AuthenticatedQueryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedQueryResource.class);

    private KieServerRegistry context;
    private AdvanceRuntimeDataService advanceRuntimeDataService;
    private RuntimeDataService runtimeDataServiceBase;
    private QueryService queryService;
    private IdentityProvider identityProvider;

    private boolean bypassAuthUser;

    public AuthenticatedQueryResource() {
    }

    public AuthenticatedQueryResource(KieServerRegistry context) {
        this.context = context;
        this.identityProvider = context.getIdentityProvider();
        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
        this.advanceRuntimeDataService = getService(AdvanceRuntimeDataService.class);
        this.runtimeDataServiceBase = getService(RuntimeDataService.class);
        this.queryService = getService(QueryService.class);
    }

    @ApiOperation(value = "Queries process tasks by variables logged user aware", response = ProcessInstanceUserTaskWithVariablesList.class)
    @POST
    @Path("authorised-tasks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @KieServerEndpoint(categories = {KieServerEndpoint.EndpointType.DEFAULT, KieServerEndpoint.EndpointType.HISTORY})
    public Response tasks(@Context HttpHeaders headers,
                          SearchQueryFilterSpec filter,
                          @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
                          @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {
        Header conversationIdHeader = RestUtils.buildConversationIdHeader("", context, headers);
        Variant v = RestUtils.getVariant(headers);
        try {
            final QueryContext queryContext = new QueryContext(page, pageSize);
            final AuthQueryUtils authQueryUtils = new AuthQueryUtils(bypassAuthUser, identityProvider, advanceRuntimeDataService, runtimeDataServiceBase);

            final List<ProcessInstanceUserTaskDetailsWithVariables> taskDetailsList = authQueryUtils.getUserTaskInstancePotentialOwnerAware
                    .apply(filter, queryContext)
                    .stream()
                    .map(t -> authQueryUtils.toProcessInstanceUserTaskDetailsWithVariables.apply(t, null))
                    .map(taskDetails -> authQueryUtils.addEventsToProcessInstanceUserTaskDetailsWithVariables.apply(taskDetails, runtimeDataServiceBase.getTaskEvents(taskDetails.getId(), null)))
                    .collect(Collectors.toList());

            return RestUtils.createCorrectVariant(new ProcessInstanceUserTaskDetailsVariableList(taskDetailsList), headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during processing {}", e.getMessage(), e);
            return RestUtils.internalServerError(RestUtils.errorMessage(e), v, conversationIdHeader);
        }
    }

    @ApiOperation(value = "Returns information about a specified task instance.", response = ProcessInstanceUserTaskDetailsWithVariables.class)
    @GET
    @Path(TASK_GET_URI)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @KieServerEndpoint(categories = {KieServerEndpoint.EndpointType.DEFAULT, KieServerEndpoint.EndpointType.HISTORY})
    public Response getTaskById(@Context HttpHeaders headers,
                                @ApiParam(value = "task id to load task instance", required = true) @PathParam(TASK_INSTANCE_ID) Long taskId) {
        Variant v = getVariant(headers);
        Header conversationIdHeader = RestUtils.buildConversationIdHeader("", context, headers);

        AuthQueryUtils authQueryUtils = new AuthQueryUtils(bypassAuthUser, identityProvider, advanceRuntimeDataService, runtimeDataServiceBase);

        try {
            UserTaskInstanceDesc userTaskInstanceDesc = authQueryUtils.getTask(taskId);

            if (userTaskInstanceDesc == null) {
                throw new TaskNotFoundException("No task found with id " + taskId);
            }

            List<org.jbpm.services.api.query.model.QueryParam> attributesQueryParams = new ArrayList<>(3);
            attributesQueryParams.add(new org.jbpm.services.api.query.model.QueryParam("TABLE", "MODE", Collections.singletonList("HISTORY")));
            attributesQueryParams.add(new org.jbpm.services.api.query.model.QueryParam(PROCESS_ATTR_INSTANCE_ID, "EQUALS_TO", Collections.singletonList(userTaskInstanceDesc.getProcessInstanceId())));
            attributesQueryParams.add(new org.jbpm.services.api.query.model.QueryParam(TASK_ATTR_NAME, "EQUALS_TO", Collections.singletonList(userTaskInstanceDesc.getName())));

            List<UserTaskInstanceWithPotOwnerDesc> taskInstanceWithPotOwnerDescs = advanceRuntimeDataService.queryUserTasksByVariables(attributesQueryParams, null, null, null, new QueryContext(0, 0));
            ProcessInstanceUserTaskDetailsWithVariables taskDetails = taskInstanceWithPotOwnerDescs.stream()
                    .filter(desc -> userTaskInstanceDesc.getTaskId().equals(desc.getTaskId()))
                    .map(t -> authQueryUtils.toProcessInstanceUserTaskDetailsWithVariables.apply(t, userTaskInstanceDesc))
                    .findAny().orElseThrow(() -> new TaskNotFoundException("No task found with id " + taskId));
            return RestUtils.createCorrectVariant(taskDetails, headers, Response.Status.OK, conversationIdHeader);
        } catch (TaskNotFoundException e) {
            return notFound(MessageFormat.format("Could not find task instance with id \"{0}\"", taskId), v, conversationIdHeader);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during processing {}", e.getMessage(), e);
            return RestUtils.internalServerError(RestUtils.errorMessage(e), v, conversationIdHeader);
        }
    }

    private <T> T getService(Class<T> clazz) {
        return (T) context.getServerExtension("jBPM")
                .getServices()
                .stream()
                .filter(service -> clazz.isAssignableFrom(service.getClass()))
                .findFirst().get();
    }
}
