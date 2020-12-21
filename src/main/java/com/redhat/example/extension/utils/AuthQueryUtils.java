package com.redhat.example.extension.utils;

import com.redhat.example.extension.AuthenticatedQueryResource;
import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsVariableList;
import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsWithVariables;
import com.redhat.example.extension.model.UserTaskEvent;
import org.jbpm.services.api.AdvanceRuntimeDataService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.model.definition.SearchQueryFilterSpec;
import org.kie.server.services.jbpm.ConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AuthQueryUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthQueryUtils.class);

    private IdentityProvider identityProvider;

    private RuntimeDataService runtimeDataServiceBase;

    private AdvanceRuntimeDataService advanceRuntimeDataService;

    private boolean bypassAuthUser;

    public AuthQueryUtils(boolean bypassAuthUser, IdentityProvider identityProvider, AdvanceRuntimeDataService advanceRuntimeDataService, RuntimeDataService runtimeDataServiceBase){
        this.bypassAuthUser = bypassAuthUser;
        this.identityProvider = identityProvider;
        this.advanceRuntimeDataService = advanceRuntimeDataService;
        this.runtimeDataServiceBase = runtimeDataServiceBase;
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

    public BiFunction<SearchQueryFilterSpec, QueryContext, List<UserTaskInstanceWithPotOwnerDesc>> executeQuery = (filter, queryContext) -> {
        final List<UserTaskInstanceWithPotOwnerDesc> userTaskInstancesWithPotOwnerDesc =  advanceRuntimeDataService.queryUserTasksByVariables(
                ConvertUtils.convertToServiceApiQueryParam(filter.getAttributesQueryParams()),
                ConvertUtils.convertToServiceApiQueryParam(filter.getTaskVariablesQueryParams()),
                ConvertUtils.convertToServiceApiQueryParam(filter.getProcessVariablesQueryParams()),
                filter.getOwners(),
                queryContext);
        LOGGER.debug("Query executed for {}, found {} tasks", filter.getOwners(), userTaskInstancesWithPotOwnerDesc.size());
        return userTaskInstancesWithPotOwnerDesc;
    };

    private final Comparator<UserTaskInstanceWithPotOwnerDesc> userTaskInstanceWithPotOwnerDescComparator = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return -1;
        if (o2 == null) return 1;
        return Comparator.comparing(UserTaskInstanceWithPotOwnerDesc::getTaskId)
                .compare(o1, o2);
    };

    public BiFunction<SearchQueryFilterSpec, QueryContext, ArrayList<UserTaskInstanceWithPotOwnerDesc>> getUserTaskInstancePotentialOwnerAware = (filter, queryContext) -> {
        final Set<UserTaskInstanceWithPotOwnerDesc> userTaskInstanceWithPotOwnerDescs = new TreeSet<>(userTaskInstanceWithPotOwnerDescComparator);
        getUserAndRoles(filter.getOwners()).stream()
                .map(pot -> {
                    filter.setOwners(Arrays.asList(pot));
                    return filter;
                })
                .map(filterWithOwner -> executeQuery.apply(filterWithOwner, queryContext))
                .forEach(userTaskInstanceWithPotOwnerDescs::addAll);
        return new ArrayList<>(userTaskInstanceWithPotOwnerDescs);
    };

    public ProcessInstanceUserTaskDetailsVariableList convertToUserTaskWithVariablesList(List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables, final Map<Long, List<TaskEvent>> taskEvents) {
        List<ProcessInstanceUserTaskDetailsWithVariables> taskDetailsList = queryUserTasksByVariables.stream()
                .map(toProcessInstanceUserTaskDetailsWithVariables)
                .map(taskDesc -> addEventsToProcessInstanceUserTaskDetailsWithVariables.apply(taskDesc, taskEvents.get(taskDesc.getId())))
                .collect(Collectors.toList());
        return new ProcessInstanceUserTaskDetailsVariableList(taskDetailsList);
    }

    public Function<UserTaskInstanceWithPotOwnerDesc, ProcessInstanceUserTaskDetailsWithVariables> toProcessInstanceUserTaskDetailsWithVariables = (desc) -> {
        ProcessInstanceUserTaskDetailsWithVariables var = new ProcessInstanceUserTaskDetailsWithVariables();
        var.setId(desc.getTaskId());
        var.setName(desc.getName());
        var.setCorrelationKey(desc.getCorrelationKey());
        var.setActualOwner(desc.getActualOwner());
        var.setProcessDefinitionId(desc.getProcessId());
        var.setPotentialOwners(desc.getPotentialOwners());
        var.setProcessInstanceId(desc.getProcessInstanceId());
        var.setProcessVariables(desc.getProcessVariables());
        var.setInputVariables(desc.getInputdata());
        var.setStatus(desc.getStatus());
        var.setCreatedBy(desc.getCreatedBy());
        var.setCreatedOn(desc.getCreatedOn());
        var.setActivationTime(desc.getActivationTime());
        var.setDueDate(desc.getDueDate());
        var.setPriority(desc.getPriority());
        var.setSlaDueDate(desc.getSlaDueDate());
        var.setSlaCompliance(desc.getSlaCompliance());
        return var;
    };

    public Function<TaskEvent, UserTaskEvent> toUserTaskEvent = (taskEvent) -> {
        UserTaskEvent result = new UserTaskEvent();
        result.setId(taskEvent.getId());
        result.setEventType(taskEvent.getType());
        result.setMessage(taskEvent.getMessage());
        result.setUserId(taskEvent.getUserId());
        result.setLogTime(taskEvent.getLogTime());
        return result;
    };

    public BiFunction<ProcessInstanceUserTaskDetailsWithVariables, List<TaskEvent>, ProcessInstanceUserTaskDetailsWithVariables> addEventsToProcessInstanceUserTaskDetailsWithVariables = (taskDesc, events) -> {
        taskDesc.setTaskEvents(events.stream().map(toUserTaskEvent).collect(Collectors.toList()));
        return taskDesc;
    };

}
