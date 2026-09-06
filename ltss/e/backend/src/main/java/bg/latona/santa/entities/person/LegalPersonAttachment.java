package bg.latona.santa.entities.person;

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
public class LegalPersonAttachment extends CompanyRecord {
	
	private String description;
	@ManyToOne
	private LegalPerson person;
	@ManyToOne
	private Attachable attachmentToPerson;
	
	public LegalPersonAttachment() {};
	
	public LegalPersonAttachment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String description, LegalPerson person, Attachable attachmentToPerson) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.description = description;
		this.person = person;
		this.attachmentToPerson = attachmentToPerson;
	}
}
