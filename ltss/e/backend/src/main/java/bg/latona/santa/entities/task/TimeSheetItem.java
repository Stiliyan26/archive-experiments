package bg.latona.santa.entities.task;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class TimeSheetItem extends CompanyRecord {

	@ManyToOne
	private SecUser resource;
	@Temporal(TemporalType.TIMESTAMP)
	private Date fromTime;
	@Temporal(TemporalType.TIMESTAMP)
	private Date toTime;
	private String description;
	@ManyToOne
	private Task task;
	@ManyToOne
	private TimeSheetItemType timeSheetItemType;

	
	public TimeSheetItem() {};
	
	public TimeSheetItem(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			SecUser resource, Date fromTime, Date toTime, String description, Task task) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.resource = resource;
		this.fromTime = fromTime;
		this.toTime = toTime;
		this.description = description;
		this.task = task;
	}
}
