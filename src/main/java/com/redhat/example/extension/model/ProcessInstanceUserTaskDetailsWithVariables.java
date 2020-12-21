package com.redhat.example.extension.model;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
        name = "process-instance-task-details-with-vars"
)
public class ProcessInstanceUserTaskDetailsWithVariables {
    @XmlElement(
            name = "id"
    )
    private Long id;
    @XmlElement(
            name = "name"
    )
    private String name;
    @XmlElement(
            name = "actual-owner"
    )
    private String actualOwner;
    @XmlElement(
            name = "correlation-key"
    )
    private String correlationKey;
    @XmlElement(
            name = "potential-owners"
    )
    private List<String> potentialOwners;
    @XmlElement(
            name = "process-definition-id"
    )
    private String processDefinitionId;
    @XmlElement(
            name = "process-instance-id"
    )
    private Long processInstanceId;
    @XmlElement(
            name = "task-instance-input-variables"
    )
    private Map<String, Object> inputVariables;
    @XmlElement(
            name = "process-variables"
    )
    private Map<String, Object> processVariables;
    @XmlElement(
            name = "status"
    )
    private String status;

    @XmlElement(
            name = "created-by"
    )
    private String createdBy;
    @XmlElement(
            name = "created-on"
    )
    private Date createdOn;
    @XmlElement(
            name = "activation-time"
    )
    private Date activationTime;
    @XmlElement(
            name = "due-date"
    )
    private Date dueDate;
    @XmlElement(
            name = "priority"
    )
    private Integer priority;
    @XmlElement(
            name = "sla-due-date"
    )
    private Date slaDueDate;
    @XmlElement(
            name = "sla-compliance"
    )
    private Integer slaCompliance;

    @XmlElementWrapper(name="task-events")
    @XmlElement(name="task-event")
    private List<UserTaskEvent> taskEvents;

    public ProcessInstanceUserTaskDetailsWithVariables() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActualOwner() {
        return this.actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public String getCorrelationKey() {
        return this.correlationKey;
    }

    public void setCorrelationKey(String correlationKey) {
        this.correlationKey = correlationKey;
    }

    public List<String> getPotentialOwners() {
        return this.potentialOwners;
    }

    public void setPotentialOwners(List<String> potentialOwners) {
        this.potentialOwners = potentialOwners;
    }

    public String getProcessDefinitionId() {
        return this.processDefinitionId;
    }

    public void setProcessDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
    }

    public Long getProcessInstanceId() {
        return this.processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Map<String, Object> getInputVariables() {
        return this.inputVariables;
    }

    public void setInputVariables(Map<String, Object> inputVariables) {
        this.inputVariables = inputVariables;
    }

    public Map<String, Object> getProcessVariables() {
        return this.processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables) {
        this.processVariables = processVariables;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getActivationTime() {
        return activationTime;
    }

    public void setActivationTime(Date activationTime) {
        this.activationTime = activationTime;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Date getSlaDueDate() {
        return slaDueDate;
    }

    public void setSlaDueDate(Date slaDueDate) {
        this.slaDueDate = slaDueDate;
    }

    public Integer getSlaCompliance() {
        return slaCompliance;
    }

    public void setSlaCompliance(Integer slaCompliance) {
        this.slaCompliance = slaCompliance;
    }

    public List<UserTaskEvent> getTaskEvents() {
        return taskEvents;
    }

    public void setTaskEvents(List<UserTaskEvent> taskEvents) {
        this.taskEvents = taskEvents;
    }

    public String toString() {
        return "ProcessInstanceUserTaskDetailsWithVariables [id=" + this.id + ", name=" + this.name + ", correlationKey=" + this.correlationKey + ", processDefinitionId=" + this.processDefinitionId + ", processInstanceId=" + this.processInstanceId + "]";
    }
}
