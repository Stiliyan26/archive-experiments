package bg.latona.santa.entities;

import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.MappedSuperclass;

import lombok.Data;

import bg.latona.santa.entities.security.SecUser;

@MappedSuperclass
@Data // auto-create getters and setters
public abstract class UserActionEvent extends CompanyRecord {

    private ZonedDateTime queuedTime;
    private ZonedDateTime startedTime;
    private ZonedDateTime finishedTime;
    private byte[] executionDetails;

    public UserActionEvent() {};

    public UserActionEvent(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
            ZonedDateTime queuedTime, ZonedDateTime startedTime, ZonedDateTime finishedTime) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.queuedTime = queuedTime;
        this.startedTime = startedTime;
        this.finishedTime = finishedTime;
    }
}
