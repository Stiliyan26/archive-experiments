package bg.latona.santa.entities.mail;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.HashTag;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"mailAttachments"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"mailAttachments"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class MailMessage extends Attachable {
	@Column(length= 3000)
	private String fromAddress;
	@Column(length= 3000)
	private String mailToRecipient;
	@Column(length= 3000)
	private String replyTo;
	private Date receivedDate;
	private Date sentDate;
	@Column(length= 3000)
	private String mailSubject;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String headers;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String mailContent;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "mail")
	private List<MailAttachment> mailAttachments;
	
	
	public MailMessage() {};
	
	public MailMessage(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			String fromAddress, String mailToRecipient, String replyTo, Date receivedDate, Date sentDate, String mailSubject, String headers, String mailContent) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.fromAddress = fromAddress;
		this.mailToRecipient = mailToRecipient;
		this.replyTo = replyTo;
		this.receivedDate = receivedDate;
		this.sentDate = sentDate;
		this.mailSubject = mailSubject;
		this.headers = headers;
		this.mailContent = mailContent;
	}
}
