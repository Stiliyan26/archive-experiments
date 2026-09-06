package bg.latona.santa.entities.employee;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class EmployeeAttachment extends CompanyRecord {
	
	private String description;
	@ManyToOne
	private Employee employee;
	@ManyToOne
	private Attachable attachmentToEmployee;
	
	public EmployeeAttachment() {};
	
	public EmployeeAttachment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String description, Employee employee, Attachable attachmentToEmployee) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.description = description;
		this.employee = employee;
		this.attachmentToEmployee = attachmentToEmployee;
	}
}
