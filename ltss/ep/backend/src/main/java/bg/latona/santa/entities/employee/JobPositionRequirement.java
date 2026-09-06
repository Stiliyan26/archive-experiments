package bg.latona.santa.entities.employee;

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
public class JobPositionRequirement extends CompanyRecord {
	
	private String comment;
	@ManyToOne
	private JobPosition position;
	@ManyToOne
	private JobRequirement requirement;
	
	public JobPositionRequirement() {};
	
	public JobPositionRequirement(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String comment, JobPosition position, JobRequirement requirement) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.comment = comment;
		this.position = position;
		this.requirement = requirement;
	}
}
