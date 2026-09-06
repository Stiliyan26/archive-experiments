package bg.latona.santa.entities.task;

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
public class TaskAttachment extends CompanyRecord {
	
	private String description;
	@ManyToOne
	private Task task;
	@ManyToOne
	private Attachable attachment;
	
	public TaskAttachment() {};
	
	public TaskAttachment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String description, Task task, Attachable attachment) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.description = description;
		this.task = task;
		this.attachment = attachment;
	}
}
