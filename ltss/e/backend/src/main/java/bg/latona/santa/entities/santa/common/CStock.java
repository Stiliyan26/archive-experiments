package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;

import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"blockedQuantities","reserveQuantities","cDeliveryDetails","cPriceLists","cSaleDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"blockedQuantities","reserveQuantities","cDeliveryDetails","cPriceLists","cSaleDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "ix_CStock_godId", columnList = "god_id_id"),
		@Index(name = "ix_CStock_ddlId", columnList = "ddl_id_id"),
		@Index(name = "ix_CStock_meeId", columnList = "mee_id_id"),
		@Index(name = "ix_CStock_outCode", columnList = "out_code_id"),
		@Index(name = "ix_CStock_parId", columnList = "par_id_id")
	})
public class CStock extends CompanyRecord{
	
	@ManyToOne
	private CGoods godId; // not null
	@ManyToOne
	private CDeliveryDetail ddlId; // not null
	private BigDecimal initialQuantity; // not null
	private BigDecimal quantity; // not null
	@ManyToOne
	private CMeasure meeId; // not null
	private BigDecimal price1; //not null
	private BigDecimal price2;
	private BigDecimal price3;
	private BigDecimal price4;
	private BigDecimal price5;
	@ManyToOne
	private CCcOrganizationUnit outCode; // not null
	private BigDecimal reserveQuantity; // not null Default 0 - should be the aggregate from CReserveQuantity
	private BigDecimal cost; // not null Default 0
	private Date stockDate; // not null
	private BigDecimal ddlCost; // Default 0
	private BigDecimal costQuantity;
	@ManyToOne
	private CCcPartner parId;
	private BigDecimal blockedQuantity; // not null default 0
	private BigDecimal consignmentQuantity; // not null default 0
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stkId")
	private List<CBlockedQuantity> blockedQuantities;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stkId")
	private List<CReserveQuantity> reserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stkId")
	private List<CDeliveryDetail> cDeliveryDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stkId")
	private List<CPriceList> cPriceLists;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stkId")
	private List<CSaleDetail> cSaleDetails;



	public CStock() {
		super();
	}

	public CStock(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, CGoods godId, CDeliveryDetail ddlId,
			BigDecimal initialQuantity, BigDecimal quantity, CMeasure meeId, BigDecimal price1, BigDecimal price2,
			BigDecimal price3, BigDecimal price4, BigDecimal price5, CCcOrganizationUnit outCode,
			BigDecimal reserveQuantity, BigDecimal cost, Date stockDate, BigDecimal ddlCost, BigDecimal costQuantity,
			CCcPartner parId, BigDecimal blockedQuantity, BigDecimal consignmentQuantity) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.godId = godId;
		this.ddlId = ddlId;
		this.initialQuantity = initialQuantity;
		this.quantity = quantity;
		this.meeId = meeId;
		this.price1 = price1;
		this.price2 = price2;
		this.price3 = price3;
		this.price4 = price4;
		this.price5 = price5;
		this.outCode = outCode;
		this.reserveQuantity = reserveQuantity;
		this.cost = cost;
		this.stockDate = stockDate;
		this.ddlCost = ddlCost;
		this.costQuantity = costQuantity;
		this.parId = parId;
		this.blockedQuantity = blockedQuantity;
		this.consignmentQuantity = consignmentQuantity;
	}
}
