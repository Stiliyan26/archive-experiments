package bg.latona.santa.entities.santa.common;
import lombok.Data;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

import javax.persistence.Entity;

import java.util.Date;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import bg.latona.santa.entities.santa.common.CStock;
import bg.latona.santa.entities.santa.common.CMeasure;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class CPriceList extends CompanyRecord{
	@ManyToOne
	private CCcPartner parId; // not null
	@ManyToOne
	private CGoods godId; // not null
	private BigDecimal price1; // not null
	private BigDecimal discount; // not null default 0
	@ManyToOne
	private CCcOrganizationUnit outCode; // not null
	@ManyToOne
	private CCtCurrency currency; //not null
	private LocalDate startDate; // default now
	private LocalDate endDate; // not null
	private String status; // not null
	private BigDecimal basePrice; // not null
	@ManyToOne
	private CDeliveryOfferItem doiId;
	@ManyToOne
	private CStock stkId;
	@ManyToOne
	private CMeasure meeId; // not null
	private BigDecimal price2;
	private BigDecimal price3;
	private BigDecimal price4;
	private BigDecimal price5;



	public CPriceList() {
		super();
	}

	public CPriceList(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
	boolean calculateOnly, ManagedCompany company, CCcPartner parId, CGoods godId, BigDecimal price1, BigDecimal discount, CCcOrganizationUnit outCode,
	CCtCurrency currency, LocalDate startDate, LocalDate endDate, String status, BigDecimal basePrice, CDeliveryOfferItem doiId, CStock stkId, CMeasure meeId,
	BigDecimal price2, BigDecimal price3, BigDecimal price4, BigDecimal price5) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.parId = parId;
		this.godId = godId;
		this.price1 = price1;
		this.discount = discount;
		this.outCode = outCode;
		this.currency = currency;
		this.startDate = startDate;
		this.endDate = endDate;
		this.status = status;
		this.basePrice = basePrice;
		this.doiId = doiId;
		this.stkId = stkId;
		this.meeId = meeId;
		this.price2 = price2;
		this.price3 = price3;
		this.price4 = price4;
		this.price5 = price5;
	}
}