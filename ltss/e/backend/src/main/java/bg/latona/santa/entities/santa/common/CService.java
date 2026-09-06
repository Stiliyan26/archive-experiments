package bg.latona.santa.entities.santa.common;

import javax.persistence.ManyToOne;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import lombok.ToString;
import org.hibernate.envers.Audited;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Data //auto-create getters and setters
@ToString(exclude = {"cDeliveryDetails","cOfferDetails","cSaleDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cDeliveryDetails","cOfferDetails","cSaleDetails"}) //avoid recursion by Lombok
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class CService extends CompanyRecord{

	private String code;
	private String name;
	@ManyToOne
	private CMeasure meeId;
	private String description;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	@ManyToOne
	private LoiServiceType serviceType;
	//parent of outcode
	//private String out_mask

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "seeId")
	private List<CDeliveryDetail> cDeliveryDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "seeId")
	private List<COfferDetail> cOfferDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "seeId")
	private List<CSaleDetail> cSaleDetails;
	
	public CService(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String code, String name, CMeasure meeId, String description, CCcOrganizationUnit outCode, LoiServiceType serviceType) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.meeId = meeId;
		this.description = description;
		this.outCode = outCode;
		this.serviceType = serviceType;
	}

}
