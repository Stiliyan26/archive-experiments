package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"reserveQuantities","cSaleDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"reserveQuantities","cSaleDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class COfferDetail extends AllocationOrigin {
	
	@ManyToOne
	private CGoods godId;
	@ManyToOne
	private COffer ofrId; // not null
	private BigDecimal price1; // noy null
	private BigDecimal price2;
	private BigDecimal price3;
	private BigDecimal price4;
	private BigDecimal price5;
	private BigDecimal discount; //NOT NULL DEFAULT 0,
	private BigDecimal quantity; //NOT NULL,
	@ManyToOne
	private CMeasure meeId; // not null
	@ManyToOne
	private CService seeId;
	private BigDecimal sdlQuantity; // not null default 0 //TODO must be the sum of all reserved quantities
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "odlId")
	private List<CReserveQuantity> reserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "odlId")
	private List<CSaleDetail> cSaleDetails;
	
	
	public COfferDetail() {
		super();
	}


	public COfferDetail(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CGoods godId, COffer ofrId, BigDecimal price1,
			BigDecimal price2, BigDecimal price3, BigDecimal price4, BigDecimal price5, BigDecimal discount,
			BigDecimal quantity, CMeasure meeId, CService seeId, BigDecimal sdlQuantity) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.godId = godId;
		this.ofrId = ofrId;
		this.price1 = price1;
		this.price2 = price2;
		this.price3 = price3;
		this.price4 = price4;
		this.price5 = price5;
		this.discount = discount;
		this.quantity = quantity;
		this.meeId = meeId;
		this.seeId = seeId;
		this.sdlQuantity = sdlQuantity;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return getQuantity();
	}
}
