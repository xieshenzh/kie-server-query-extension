package com.redhat.example.extension;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.identity.IdentityProvider;
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


@Api(value="Process queries")
@Path("/server/queries/variables/processes")
public class AuthenticatedQueryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedQueryResource.class);

    private KieServerRegistry context;

    private IdentityProvider identityProvider;

    private boolean bypassAuthUser;

    public AuthenticatedQueryResource() {
    }

    public AuthenticatedQueryResource(KieServerRegistry context) {
        this.context = context;
        this.identityProvider = context.getIdentityProvider();
        this.bypassAuthUser = Boolean.parseBoolean(context.getConfig().getConfigItemValue(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));
    }

    @ApiOperation(value = "Queries process tasks by variables logged user aware", response = ProcessInstanceUserTaskWithVariablesList.class)
    @POST
    @Path("authorised-tasks")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @KieServerEndpoint(categories = {KieServerEndpoint.EndpointType.DEFAULT, KieServerEndpoint.EndpointType.HISTORY})
    public Response tasks(@Context HttpHeaders headers, SearchQueryFilterSpec filter,
                          @ApiParam(value = "optional pagination - at which page to start, defaults to 0 (meaning first)", required = false) @QueryParam("page") @DefaultValue("0") Integer page,
                          @ApiParam(value = "optional pagination - size of the result, defaults to 10", required = false) @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        Header conversationIdHeader = RestUtils.buildConversationIdHeader("", context, headers);
        Variant v = RestUtils.getVariant(headers);
        try {
            final AdvanceRuntimeDataService advanceRuntimeDataService = (AdvanceRuntimeDataService) context.getServerExtension("jBPM")
                    .getServices()
                    .stream()
                    .filter(service -> AdvanceRuntimeDataService.class.isAssignableFrom(service.getClass()))
                    .findFirst().get();
            final Set<UserTaskInstanceWithPotOwnerDesc> userTaskInstanceWithPotOwnerDescs = new TreeSet<>(userTaskInstanceWithPotOwnerDescComparator);
            getUserAndRoles(filter.getOwners()).stream().map(pot -> executeQuery(advanceRuntimeDataService, filter, pot, new QueryContext(page, pageSize))).forEach(userTaskInstanceWithPotOwnerDescs::addAll);
            final ProcessInstanceUserTaskWithVariablesList taskVariableSummaryList = ConvertUtils.convertToUserTaskWithVariablesList(new ArrayList<>(userTaskInstanceWithPotOwnerDescs));
            return RestUtils.createCorrectVariant(taskVariableSummaryList, headers, Response.Status.OK, conversationIdHeader);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during processing {}", e.getMessage(), e);
            return RestUtils.internalServerError(RestUtils.errorMessage(e), v, conversationIdHeader);
        }
    }

    private final Comparator<UserTaskInstanceWithPotOwnerDesc> userTaskInstanceWithPotOwnerDescComparator = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return Comparator.comparing(UserTaskInstanceWithPotOwnerDesc::getTaskId)
                .compare(o1, o2);
    };

    private List<UserTaskInstanceWithPotOwnerDesc> executeQuery(AdvanceRuntimeDataService advanceRuntimeDataService, SearchQueryFilterSpec filter, String potOwner, QueryContext queryContext) {
        final List<UserTaskInstanceWithPotOwnerDesc> userTaskInstancesWithPotOwnerDesc =  advanceRuntimeDataService.queryUserTasksByVariables(
                ConvertUtils.convertToServiceApiQueryParam(filter.getAttributesQueryParams()),
                ConvertUtils.convertToServiceApiQueryParam(filter.getTaskVariablesQueryParams()),
                ConvertUtils.convertToServiceApiQueryParam(filter.getProcessVariablesQueryParams()),
                Arrays.asList(potOwner),
                queryContext);
        LOGGER.debug("Query executed for {}, found {} tasks", potOwner, userTaskInstancesWithPotOwnerDesc.size());
        return userTaskInstancesWithPotOwnerDesc;
    }

    protected List<String> getUserAndRoles(List<String> queryParamUser) {
        if (bypassAuthUser) {
            return queryParamUser;
        }
        List<String> rolesAndUsers = new ArrayList<>();
        rolesAndUsers.addAll(identityProvider.getRoles());
        rolesAndUsers.add(identityProvider.getName());
        return rolesAndUsers;
    }
}
