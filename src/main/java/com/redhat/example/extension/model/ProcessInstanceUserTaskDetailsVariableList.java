package com.redhat.example.extension.model;

import org.kie.server.api.model.ItemList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
        name = "user-task-details-variable-instance-list"
)
public class ProcessInstanceUserTaskDetailsVariableList implements ItemList<ProcessInstanceUserTaskDetailsWithVariables> {
    @XmlElement(
            name = "user-task-details-with-vars-instance"
    )
    private ProcessInstanceUserTaskDetailsWithVariables[] userTaskDetailsWithVariables;

    public ProcessInstanceUserTaskDetailsVariableList() {
    }

    public ProcessInstanceUserTaskDetailsVariableList(ProcessInstanceUserTaskDetailsWithVariables[] variableInstances) {
        this.userTaskDetailsWithVariables = variableInstances;
    }

    public ProcessInstanceUserTaskDetailsVariableList(List<ProcessInstanceUserTaskDetailsWithVariables> variableInstances) {
        this.userTaskDetailsWithVariables = (ProcessInstanceUserTaskDetailsWithVariables[]) variableInstances.toArray(new ProcessInstanceUserTaskDetailsWithVariables[variableInstances.size()]);
    }

    public ProcessInstanceUserTaskDetailsWithVariables[] getUserTaskDetailsWithVariables() {
        return this.userTaskDetailsWithVariables;
    }

    public void setUserTaskDetailsWithVariables(ProcessInstanceUserTaskDetailsWithVariables[] variableInstances) {
        this.userTaskDetailsWithVariables = variableInstances;
    }

    public List<ProcessInstanceUserTaskDetailsWithVariables> getItems() {
        return this.userTaskDetailsWithVariables == null ? Collections.emptyList() : Arrays.asList(this.userTaskDetailsWithVariables);
    }

    public String toString() {
        return "ProcessInstanceUserTaskDetailsVariableList [userTaskDetailsWithVariables=" + Arrays.toString(this.userTaskDetailsWithVariables) + "]";
    }
}