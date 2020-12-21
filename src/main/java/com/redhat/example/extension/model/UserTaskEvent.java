package com.redhat.example.extension.model;

import org.kie.internal.task.api.model.TaskEvent;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
        name = "user-task-event"
)
public class UserTaskEvent {
    @XmlElement(
            name = "event-id"
    )
    private long id;
    @XmlElement(
            name = "event-type"
    )
    private TaskEvent.TaskEventType eventType;

    @XmlElement(
            name = "user-id"
    )
    private String userId;

    @XmlElement(
            name = "log-time"
    )
    private Date logTime;

    @XmlElement(
            name = "message"
    )
    private String message;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TaskEvent.TaskEventType getEventType() {
        return eventType;
    }

    public void setEventType(TaskEvent.TaskEventType eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
