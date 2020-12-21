package com.redhat.example.extension;

import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsVariableList;
import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsWithVariables;
import com.redhat.example.extension.utils.AuthQueryUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.jbpm.services.api.query.QueryService;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.definition.SearchQueryFilterSpec;
import org.kie.server.api.model.instance.ProcessInstanceUserTaskWithVariablesList;
import org.kie.server.remote.rest.common.Header;
import org.kie.server.remote.rest.common.marker.KieServerEndpoint;
import org.kie.server.remote.rest.common.util.RestUtils;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;


@Api(value="Process queries")
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
            final QueryContext queryContext =  new QueryContext(page, pageSize);
            final AuthQueryUtils authQueryUtils = new AuthQueryUtils(bypassAuthUser, identityProvider, advanceRuntimeDataService, runtimeDataServiceBase);

            final List<ProcessInstanceUserTaskDetailsWithVariables> taskDetailsList = authQueryUtils.getUserTaskInstancePotentialOwnerAware
                    .apply(filter, queryContext)
                    .stream()
                    .map(authQueryUtils.toProcessInstanceUserTaskDetailsWithVariables)
                    .map(taskDetails -> authQueryUtils.addEventsToProcessInstanceUserTaskDetailsWithVariables.apply(taskDetails, runtimeDataServiceBase.getTaskEvents(taskDetails.getId(), null)))
                    .collect(Collectors.toList());

            return RestUtils.createCorrectVariant(new ProcessInstanceUserTaskDetailsVariableList(taskDetailsList), headers, Response.Status.OK, conversationIdHeader);
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
