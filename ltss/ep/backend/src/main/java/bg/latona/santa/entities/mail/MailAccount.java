package bg.latona.santa.entities.mail;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonProperty;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.ToString;

@Data //auto-create getters and setters
//@JsonIgnoreProperties(value={ "password" }, allowSetters=true) //avoid serialization by Jackson but allow deserialization - this stopped working, use the other approach
@ToString(exclude = {"password","sendMailMessages" }) //avoid serialization by Lombok
@EqualsAndHashCode(exclude = {"sendMailMessages"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class MailAccount extends CompanyRecord {
	private String name;
	private String imapHost;
	private String smtpHost;
	private String username;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY) //avoid serialization by Jackson but allow deserialization
	private String password;
	private int delay;
	private String inboxFolder;
	private String sentFolder;
	private String archiveFolder;
	private Boolean deleteMail;
	private Boolean unseenMail;
	private Boolean recognizedContact;
	private Boolean defaultAccount;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "fromAccount")
	private List<SendMailMessage> sendMailMessages;
	
	
	public MailAccount() {};

	public MailAccount(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			String name, String imapHost, String smtpHost, String username, String password, int delay,
			String inboxFolder, String sentFolder, String archiveFolder, Boolean deleteMail, Boolean unseenMail, 
			Boolean recognizedContact, Boolean defaultAccount) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.imapHost = imapHost;
		this.smtpHost = smtpHost;
		this.username = username;
		this.setPassword(password);
		this.delay = delay;
		this.inboxFolder = inboxFolder;
		this.sentFolder = sentFolder;
		this.archiveFolder = archiveFolder;
		this.deleteMail = deleteMail;
		this.unseenMail = unseenMail;
		this.recognizedContact = recognizedContact;
		this.defaultAccount = defaultAccount;
	}

	public void setPassword(String password) {
		//TODO do symmetric encryption
		this.password = password;
	}
}
