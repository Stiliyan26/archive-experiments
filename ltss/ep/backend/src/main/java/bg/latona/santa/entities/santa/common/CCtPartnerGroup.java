package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.santa.finance.FDiscount;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Data //auto-create getters and setters
@ToString(exclude = {"cCcPartners","fDiscounts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCcPartners","fDiscounts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CCtPartnerGroup extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String name;
	private String code;
	private LocalDate activeFrom;
	private LocalDate activeTo;
	@ManyToOne
	private CCcOrganizationUnit outMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "partnerGroup")
	private List<CCcPartner> cCcPartners;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "pgpId")
	private List<FDiscount> fDiscounts;
	
	
	public CCtPartnerGroup() {
		super();
	}
	
	public CCtPartnerGroup(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CCcOrganizationUnit outCode, String name, String code,
			LocalDate activeFrom, LocalDate activeTo, CCcOrganizationUnit outMask) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.name = name;
		this.code = code;
		this.activeFrom = activeFrom;
		this.activeTo = activeTo;
		this.outMask = outMask;
	}
}
