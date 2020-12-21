package com.redhat.example.extension.utils;

import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsVariableList;
import com.redhat.example.extension.model.ProcessInstanceUserTaskDetailsWithVariables;
import org.jbpm.services.api.model.UserTaskInstanceWithPotOwnerDesc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConvertUtils {

    public static ProcessInstanceUserTaskDetailsVariableList convertToUserTaskWithVariablesList(List<UserTaskInstanceWithPotOwnerDesc> queryUserTasksByVariables) {
        List<ProcessInstanceUserTaskDetailsWithVariables> data = new ArrayList();
        Iterator var2 = queryUserTasksByVariables.iterator();

        while(var2.hasNext()) {
            UserTaskInstanceWithPotOwnerDesc desc = (UserTaskInstanceWithPotOwnerDesc)var2.next();
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
            data.add(var);
        }

        ProcessInstanceUserTaskDetailsVariableList result = new ProcessInstanceUserTaskDetailsVariableList();
        result.setUserTaskDetailsWithVariables((ProcessInstanceUserTaskDetailsWithVariables[])data.parallelStream().toArray((x$0) -> {
            return new ProcessInstanceUserTaskDetailsWithVariables[x$0];
        }));
        return result;
    }
}
