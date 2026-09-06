package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.math.BigDecimal;
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
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cOrderDetails","cPriceLists"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cOrderDetails","cPriceLists"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CDeliveryOfferItem extends CompanyRecord{
	@ManyToOne
	private CDeliveryOffer dorId;
	private String code;
	private String name;
	private BigDecimal price;
	@ManyToOne
	private CCtCurrency currency;
	private BigDecimal discount; 
	@ManyToOne
	private CGoods godId;
	private String status;
	@ManyToOne
	private CMeasure measure;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "doiId")
	private List<COrderDetail> cOrderDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "doiId")
	private List<CPriceList> cPriceLists;

	public CDeliveryOfferItem() {
		super();
	}

	public CDeliveryOfferItem(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CDeliveryOffer dorId, String code, String name,
			BigDecimal price, CCtCurrency currency, BigDecimal discount, CGoods godId, String status, CMeasure measure) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.dorId = dorId;
		this.code = code;
		this.name = name;
		this.price = price;
		this.currency = currency;
		this.discount = discount;
		this.godId = godId;
		this.status = status;
		this.measure = measure;
	}
}
