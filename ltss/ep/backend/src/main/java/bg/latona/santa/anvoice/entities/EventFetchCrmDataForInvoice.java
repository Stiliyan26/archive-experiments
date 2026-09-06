package bg.latona.santa.anvoice.entities;

import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.UserActionEvent;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import lombok.Data;

@Data // auto-create getters and setters
@Entity // JPA persisted class
public class EventFetchCrmDataForInvoice extends UserActionEvent {

    @ManyToOne
    private ElectricityInvoice electricityInvoice;

    public EventFetchCrmDataForInvoice() {};

    public EventFetchCrmDataForInvoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
            ZonedDateTime queuedTime, ZonedDateTime startedTime, ZonedDateTime finishedTime,
            ElectricityInvoice electricityInvoice) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, queuedTime, startedTime, finishedTime);
        this.electricityInvoice = electricityInvoice;
    }
}
