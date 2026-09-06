package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cBlockedQuantities","cDeliveryDetails","cDeliveryOfferItems","cGoods","cOfferDetails","cOrderDetails","cPriceLists"
,"cRequestDetails","cReserveQuantities","cSaleDetails","cServices","cStocks","cMeasures"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cBlockedQuantities","cDeliveryDetails","cDeliveryOfferItems","cGoods","cOfferDetails","cOrderDetails","cPriceLists"
,"cRequestDetails","cReserveQuantities","cSaleDetails","cServices","cStocks","cMeasures"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CMeasure extends CompanyRecord{
	private String name;
	private String code;
	private BigDecimal cofficient; //TODO rename this to be correct
	@ManyToOne
	private CMeasure meeId; //in old system it shouldn't be null and points to itself

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CBlockedQuantity> cBlockedQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CDeliveryDetail> cDeliveryDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "measure")
	private List<CDeliveryOfferItem> cDeliveryOfferItems;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CGoods> cGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<COfferDetail> cOfferDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<COrderDetail> cOrderDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CPriceList> cPriceLists;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CRequestDetail> cRequestDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CReserveQuantity> cReserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CSaleDetail> cSaleDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CService> cServices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CStock> cStocks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "meeId")
	private List<CMeasure> cMeasures;
	
	public CMeasure() {
		super();
	}

	public CMeasure(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String name, String code, BigDecimal cofficient, CMeasure meeId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
		this.cofficient = cofficient;
		this.meeId = meeId;
	}

	
}
