package bg.latona.santa.entities.waste;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class WasteArea extends CompanyRecord {


	private String name;
	private String address;
	private String phone;
	private String contactPerson;
	private String email;

	public WasteArea() {};

	public WasteArea(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String address, String phone, String contactPerson, String email) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.address = address;
		this.phone = phone;
		this.contactPerson = contactPerson;
		this.email = email;
	}
}
