package bg.latona.santa.entities.wato;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ImportedSantaPartner extends CompanyRecord {

	@ManyToOne
	private LegalPerson legalPerson;
	private String code;
	private String name;
	private String bulstat;
	private String egn;
	private String address;
	private String partnerType;
	private String phone;
	private String email;
	private String mol;
	private Calendar activeFromDate;
	private Calendar activeToDate;
	private String organizationUnitId;
	private Integer partnerGroupId;
	private String legalStatusId;
	private Integer dateUpdated;

	//default empty constructor
	public ImportedSantaPartner() {}

	//default constructor with all attributes
	public ImportedSantaPartner(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			LegalPerson legalPerson, String code, String name, String bulstat,
			String egn, String address, String partnerType, String phone, String email, String mol,
			Calendar activeFromDate, Calendar activeToDate, String organizationUnitId, Integer partnerGroupId,
			String legalStatusId, Integer dateUpdated) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.legalPerson = legalPerson;
		this.code = code;
		this.name = name;
		this.bulstat = bulstat;
		this.egn = egn;
		this.address = address;
		this.partnerType = partnerType;
		this.phone = phone;
		this.email = email;
		this.mol = mol;
		this.activeFromDate = activeFromDate;
		this.activeToDate = activeToDate;
		this.organizationUnitId = organizationUnitId;
		this.partnerGroupId = partnerGroupId;
		this.legalStatusId = legalStatusId;
		this.dateUpdated = dateUpdated;
	}
}
