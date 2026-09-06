package bg.latona.santa.entities.task;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class PlannedTime extends CompanyRecord {

	@ManyToOne
	private SecUser resource;
	private BigDecimal minutes;
	@ManyToOne
	private Task task;
	
	public PlannedTime() {};
	
	public PlannedTime(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			SecUser resource, BigDecimal minutes, Task task) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.resource = resource;
		this.minutes = minutes;
		this.task = task;
	}
}
