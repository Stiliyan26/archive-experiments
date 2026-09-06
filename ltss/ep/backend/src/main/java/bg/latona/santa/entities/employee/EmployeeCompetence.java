package bg.latona.santa.entities.employee;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class EmployeeCompetence extends CompanyRecord {
	
	private String comment;
	@ManyToOne
	private Employee employee;
	@ManyToOne
	private JobRequirement competence;
	@ManyToOne
	private DBFile document;
	
	public EmployeeCompetence() {};
	
	public EmployeeCompetence(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String comment, Employee employee, JobRequirement competence, DBFile document) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.comment = comment;
		this.employee = employee;
		this.competence = competence;
		this.document = document;
	}
}
