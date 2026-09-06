package bg.latona.santa.entities.person;

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
public class Contact extends CompanyRecord {

	private String name;
	private String description;
	private String phone;
	private String fax;
	private String email;
	private String webSite;

	@ManyToOne
	private LegalPerson person;
	@ManyToOne
	private ContactType type;
	
	public Contact() {};
	
	public Contact(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String description,
			String phone, String fax, String email, String webSite, LegalPerson person, ContactType type) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.description = description;
		this.phone = phone;
		this.fax = fax;
		this.email = email;
		this.webSite = webSite;
		this.person = person;
		this.type = type;
	}
}
