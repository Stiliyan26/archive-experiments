package bg.latona.santa.anvoice.entities;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

import javax.persistence.Entity;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.UserActionEvent;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data // auto-create getters and setters
@Entity // JPA persisted class
public class EventPopulateElectricityInvoice extends UserActionEvent {

    private ZonedDateTime queuedTime;
    private ZonedDateTime startedTime;
    private ZonedDateTime finishedTime;

    private LocalDate periodFrom;
    private LocalDate periodTo;
    private LocalDate taxEventDate;
    private Long documentType;

    public EventPopulateElectricityInvoice() {};

    public EventPopulateElectricityInvoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
            ZonedDateTime queuedTime, ZonedDateTime startedTime, ZonedDateTime finishedTime, 
            LocalDate periodFrom, LocalDate periodTo, LocalDate taxEventDate,
            Long documentType) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, queuedTime, startedTime, finishedTime);
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.taxEventDate = taxEventDate;
        this.documentType = documentType;
    }
}
