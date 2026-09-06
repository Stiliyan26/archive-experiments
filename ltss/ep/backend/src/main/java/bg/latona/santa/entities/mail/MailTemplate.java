package bg.latona.santa.entities.mail;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.HashTag;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class MailTemplate extends Attachable {

	public static final Long MAIL_TEMPLATE_FOR_ESO = 1L;
	public static final Long MAIL_TEMPLATE_FOR_PROTOCOLS = 2L;

	private String templateSubject;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String templateContent;
	private String templateToRecipient;
	private Long templateCode;

	public MailTemplate() {};

	public MailTemplate(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String name,
						List<HashTag> hashTags, String templateSubject, String templateContent, String templateToRecipient, Long templateCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.templateSubject = templateSubject;
		this.templateContent = templateContent;
		this.templateToRecipient = templateToRecipient;
		this.templateCode = templateCode;
	}
}
