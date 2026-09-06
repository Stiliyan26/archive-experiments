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
public class Vendor extends CompanyRecord {

	@ManyToOne
	private LegalPerson person;
	private String vendorCategory;
	private String paymentMethod;
	
	public Vendor() {}

	public Vendor(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			LegalPerson person, String vendorCategory, String paymentMethod) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.person = person;
		this.vendorCategory = vendorCategory;
		this.paymentMethod = paymentMethod;
	}
}
