package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"deliveryDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveryDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(indexes = {
	@Index(name = "ix_COrderDetail_godId", columnList = "god_id_id"),
	@Index(name = "ix_COrderDetail_orrId", columnList = "orr_id_id"),
	@Index(name = "ix_COrderDetail_meeId", columnList = "mee_id_id"),
	@Index(name = "ix_COrderDetail_doiId", columnList = "doi_id_id")
})
public class COrderDetail extends AllocationOrigin{
	@ManyToOne
	private CGoods godId;
	@ManyToOne 
	private COrder orrId;
	private BigDecimal quantity;
	private BigDecimal price;
	@ManyToOne
	private CMeasure meeId;
	private BigDecimal priceConfirm;
	private BigDecimal quantityConfirm;
	@ManyToOne
	private CDeliveryOfferItem doiId;
	private BigDecimal rqyQuantity; //the part of the order that is for the reserved quantities
	private BigDecimal stkQuantity; //the part of the order that is for replenishing the stocks to the maximum
	private BigDecimal ddlQuantity; //how much was delivered - only from deliveries generated from this order detail
	private BigDecimal plnQuantity;
	private BigDecimal cancelledQuantity; //not delivered and cancelled
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orlId")
	private List<CDeliveryDetail> deliveryDetails;
	
	public COrderDetail() {
		super();
	}

	public COrderDetail(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CGoods godId, COrder orrId, BigDecimal quantity,
			BigDecimal price, CMeasure meeId, BigDecimal priceConfirm, BigDecimal quantityConfirm,
			CDeliveryOfferItem doiId, BigDecimal rqyQuantity, BigDecimal stkQuantity, BigDecimal ddlQuantity,
			BigDecimal plnQuantity, BigDecimal cancelledQuantity) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.godId = godId;
		this.orrId = orrId;
		this.quantity = quantity;
		this.price = price;
		this.meeId = meeId;
		this.priceConfirm = priceConfirm;
		this.quantityConfirm = quantityConfirm;
		this.doiId = doiId;
		this.rqyQuantity = rqyQuantity;
		this.stkQuantity = stkQuantity;
		this.ddlQuantity = ddlQuantity;
		this.plnQuantity = plnQuantity;
		this.cancelledQuantity = cancelledQuantity;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return getQuantityConfirm();
	}
}
