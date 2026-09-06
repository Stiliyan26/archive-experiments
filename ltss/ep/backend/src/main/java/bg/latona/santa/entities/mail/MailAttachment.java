package bg.latona.santa.entities.mail;

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
public class MailAttachment extends CompanyRecord {

	@ManyToOne
	private MailMessage mail;
	@ManyToOne
	private DBFile attachment;
	
	public MailAttachment() {};
	
	public MailAttachment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			MailMessage mail, DBFile attachment) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.mail = mail;
		this.attachment = attachment;
	}
}
