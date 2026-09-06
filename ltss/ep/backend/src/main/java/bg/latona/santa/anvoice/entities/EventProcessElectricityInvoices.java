package bg.latona.santa.anvoice.entities;

import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.Entity;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.UserActionEvent;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data // auto-create getters and setters
@Entity // JPA persisted class
public class EventProcessElectricityInvoices extends UserActionEvent {

    private ZonedDateTime queuedTime;
    private ZonedDateTime startedTime;
    private ZonedDateTime finishedTime;

    public EventProcessElectricityInvoices() {};

    public EventProcessElectricityInvoices(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
            ZonedDateTime queuedTime, ZonedDateTime startedTime, ZonedDateTime finishedTime) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, queuedTime, startedTime, finishedTime);
    }
}
