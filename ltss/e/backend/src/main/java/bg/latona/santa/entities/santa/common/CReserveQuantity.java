package bg.latona.santa.entities.santa.common;

import lombok.Data;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class CReserveQuantity extends CompanyRecord{
	
	private BigDecimal initialQuantity; //NOT NULL DEFAULT 0,
	private BigDecimal quantity; //NOT NULL DEFAULT 0,
	@ManyToOne
	private CStock stkId;
	@ManyToOne
	private CCcPartner partner; //par_id if it is reserved for a client
	private Integer retId;
	private LocalDate term; // not null
	@ManyToOne
	private COffer ofrId; // not null
	@ManyToOne
	private CGoods godId; // not null
	@ManyToOne
	private COfferDetail odlId; // not null
	@ManyToOne
	private CMeasure meeId; // not null
	@ManyToOne
	private CCcOrganizationUnit outId; //if it is reserved for another warehouse

	public CReserveQuantity() {
		super();
	}
	
	public CReserveQuantity(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, BigDecimal initialQuantity, BigDecimal quantity,
			CStock stkId, CCcPartner partner, Integer retId, LocalDate term, COffer ofrId, CGoods godId, COfferDetail odlId,
			CMeasure meeId, CCcOrganizationUnit outId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.initialQuantity = initialQuantity;
		this.quantity = quantity;
		this.stkId = stkId;
		this.partner = partner;
		this.retId = retId;
		this.term = term;
		this.ofrId = ofrId;
		this.godId = godId;
		this.odlId = odlId;
		this.meeId = meeId;
		this.outId = outId;
	}
}
