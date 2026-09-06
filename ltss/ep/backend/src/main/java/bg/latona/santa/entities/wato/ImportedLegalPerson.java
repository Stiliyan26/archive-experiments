package bg.latona.santa.entities.wato;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"importedInvoicePayments"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedInvoicePayments"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ImportedLegalPerson extends CompanyRecord {

	@ManyToOne
	private LegalPerson legalPerson;
	private String foreignId;
	private String clName;
	private String bulstat;
	private String companyName;
	private String vatNumber;
	private String email;
	private String phoneNumber;
	private Boolean supplier;
	private Boolean client;
	private Boolean manufacturer;
	private Boolean subcontractor;
	private String molName;
	private String regCountry;
	private String regCity;
	private String regAddress;
	private String currCountry;
	private String currCity;
	private String currAddress;
	private Integer compId;
	private Boolean foreignDeleted;
	private Long updateCountAsBigInt;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "contragent")
	private List<ImportedInvoicePayment> importedInvoicePayments;

	//default empty constructor
	public ImportedLegalPerson() {}

	//default constructor with all attributes
	public ImportedLegalPerson(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			LegalPerson legalPerson, String foreignId, String clName, String bulstat, String companyName, String vatNumber,
			String email, String phoneNumber, Boolean supplier, Boolean client, Boolean manufacturer, Boolean subcontractor,
			String molName, String regCountry, String regCity, String regAddress, String currCountry, String currCity, String currAddress,
			Integer compId, Boolean foreignDeleted, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.legalPerson = legalPerson;
		this.foreignId = foreignId;
		this.clName = clName;
		this.bulstat = bulstat;
		this.companyName = companyName;
		this.vatNumber = vatNumber;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.supplier = supplier;
		this.client = client;
		this.manufacturer = manufacturer;
		this.subcontractor = subcontractor;
		this.molName = molName;
		this.regCountry = regCountry;
		this.regCity = regCity;
		this.regAddress = regAddress;
		this.currCountry = currCountry;
		this.currCity = currCity;
		this.currAddress = currAddress;
		this.compId = compId;
		this.foreignDeleted = foreignDeleted;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
	
}
