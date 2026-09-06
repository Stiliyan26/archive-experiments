package bg.latona.santa.entities.mail;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import bg.latona.santa.entities.nepal.PowerPlantProtocol;
import bg.latona.santa.entities.nepal.Schedule;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.HashTag;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.Contact;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"schedules","powerPlantProtocols"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"schedules","powerPlantProtocols"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class SendMailMessage extends Attachable {
	@ManyToOne
	private MailAccount fromAccount;

	private String sendMailSubject;
	private String sendMailToRecipient;
	@ManyToMany
	private List<Contact> sendMailContacts; //not checking for foreign key
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String sendMailContent;
	@ManyToMany
	private List<DBFile> attachments; //not checking for foreign key
	private Boolean sent;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "sendMailMessage")
	private List<Schedule> schedules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "sendMailMessage")
	private List<PowerPlantProtocol> powerPlantProtocols;
	
	public SendMailMessage() {};
	
	public SendMailMessage(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			MailAccount fromAccount, String sendMailSubject, String sendMailToRecipient, List<Contact> sendMailContacts, String sendMailContent, 
			List<DBFile> attachments, Boolean sent) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.fromAccount = fromAccount;
		this.sendMailSubject = sendMailSubject;
		this.sendMailToRecipient = sendMailToRecipient;
		this.sendMailContacts = sendMailContacts;
		this.sendMailContent = sendMailContent;
		this.attachments = attachments;
		this.sent = sent;
	}
}
