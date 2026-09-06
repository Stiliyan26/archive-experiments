package bg.latona.santa.entities.santa.common;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;

@Data //auto-create getters and setters
@ToString(exclude = {"stocks","cDeliveryDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"stocks","cDeliveryDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CDeliveryDetail extends AllocationOrigin {

	@ManyToOne(fetch = FetchType.LAZY) //if not LAZY then repository get all creates enormous SQL that gives error 
	private CDelivery deyId; //not null
	@ManyToOne
	private CGoods godId;
	private String batch;
	private String serialNumber;
	private Date expiry;
	private BigDecimal quantity; // not null Default
	@ManyToOne
	private CMeasure meeId;
	private BigDecimal price; // not null
	private BigDecimal vat; // not null
	private BigDecimal valuePrice;
	private BigDecimal discount;
	private BigDecimal valueAll;
	private BigDecimal vatAll;
	private BigDecimal total;
	@ManyToOne(fetch = FetchType.LAZY) //if not LAZY then repository get all creates enormous SQL that gives error 
	private CDeliveryDetail ddlId; //???
	private BigDecimal cost;
	@ManyToOne
	private CService seeId;
	private BigDecimal costBase;
	@ManyToOne(fetch = FetchType.LAZY) //if not LAZY then repository get all creates enormous SQL that gives error 
	private COrderDetail orlId; //ref only if this detail was copied from order
	@ManyToOne(fetch = FetchType.LAZY) //if not LAZY then repository get all creates enormous SQL that gives error 
	private CStock stkId; //only filled by insertintodeliveriesfromsales??? this is the stock in another warehouse that is transferred and generates this delivery detail
	// no var table yet
	private Integer varId; //???

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ddlId")
	private List<CStock> stocks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ddlId")
	private List<CDeliveryDetail> cDeliveryDetails;
	
	
	public CDeliveryDetail() {
		super();
	}

	public CDeliveryDetail(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CDelivery deyId, CGoods godId, String batch,
			String serialNumber, Date expiry, BigDecimal quantity, CMeasure meeId, BigDecimal price, BigDecimal vat,
			BigDecimal valuePrice, BigDecimal discount, BigDecimal valueAll, BigDecimal vatAll, BigDecimal total,
			CDeliveryDetail ddlId, BigDecimal cost, CService seeId, BigDecimal costBase, COrderDetail orlId, CStock stkId,
			Integer varId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.deyId = deyId;
		this.godId = godId;
		this.batch = batch;
		this.serialNumber = serialNumber;
		this.expiry = expiry;
		this.quantity = quantity;
		this.meeId = meeId;
		this.price = price;
		this.vat = vat;
		this.valuePrice = valuePrice;
		this.discount = discount;
		this.valueAll = valueAll;
		this.vatAll = vatAll;
		this.total = total;
		this.ddlId = ddlId;
		this.cost = cost;
		this.seeId = seeId;
		this.costBase = costBase;
		this.orlId = orlId;
		this.stkId = stkId;
		this.varId = varId;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return getQuantity();
	}
}
