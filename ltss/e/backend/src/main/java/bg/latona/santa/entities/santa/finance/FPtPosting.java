package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.security.SecUser;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data //auto-create getters and setters
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtPosting extends CompanyRecord {

	@ManyToOne
	private FPtJournal jolId; //NOT NULL -- FK to journals
	private LocalDate postDate; //NOT NULL,
	@ManyToOne
	private CCcOrganizationUnit outCode; // NOT NULL, -- organization unit code
	private BigDecimal amount; //NOT NULL,
	private BigDecimal amountCurrency; //NOT NULL,
	@ManyToOne
	private LoiPtPostingCoaStatus coaStatus; //NOT NULL DEFAULT 'U'::bpchar,
	@ManyToOne
	private LoiPtPostingCcStatus ccStatus; //NOT NULL DEFAULT 'U'::bpchar,
	@Column(length = 3000)
	private String descr;
	@ManyToOne
	private FChartAccount coaIdCt; //NOT NULL,
	@ManyToOne
	private FChartAccount coaIdDt; //NOT NULL,
	@ManyToOne
	private FCcEbk ccEbkId;
	@ManyToOne
	private FCcFunction ccFunId;
	@ManyToOne
	private FCcProgram ccPrmId;
	@ManyToOne
	private FCcFinsource ccFieId;
	@ManyToOne
	private CCcPartner ccParId;
	@ManyToOne
	private CCcGoodsType ccGteId;
	@ManyToOne
	private FCcContract ccCotId;
	@ManyToOne
	private CCcOrganizationUnit ccOutId;
	@ManyToOne
	private FCcReserve1 ccRe1Id;
	@ManyToOne
	private FCcReserve2 ccRe2Id;
	
	public FPtPosting(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, FPtJournal jolId, LocalDate postDate,
			CCcOrganizationUnit outCode, BigDecimal amount, BigDecimal amountCurrency, LoiPtPostingCoaStatus coaStatus,
			LoiPtPostingCcStatus ccStatus, String descr, FChartAccount coaIdCt, FChartAccount coaIdDt, FCcEbk ccEbkId,
			FCcFunction ccFunId, FCcProgram ccPrmId, FCcFinsource ccFieId, CCcPartner ccParId, CCcGoodsType ccGteId,
			FCcContract ccCotId, CCcOrganizationUnit ccOutId, FCcReserve1 ccRe1Id, FCcReserve2 ccRe2Id) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.jolId = jolId;
		this.postDate = postDate;
		this.outCode = outCode;
		this.amount = amount;
		this.amountCurrency = amountCurrency;
		this.coaStatus = coaStatus;
		this.ccStatus = ccStatus;
		this.descr = descr;
		this.coaIdCt = coaIdCt;
		this.coaIdDt = coaIdDt;
		this.ccEbkId = ccEbkId;
		this.ccFunId = ccFunId;
		this.ccPrmId = ccPrmId;
		this.ccFieId = ccFieId;
		this.ccParId = ccParId;
		this.ccGteId = ccGteId;
		this.ccCotId = ccCotId;
		this.ccOutId = ccOutId;
		this.ccRe1Id = ccRe1Id;
		this.ccRe2Id = ccRe2Id;
	}

}
/* COMMENT ON COLUMN accounting.pt_postings.jol_id IS 'FK to journals';
COMMENT ON COLUMN accounting.pt_postings.out_code IS 'organization unit code'; */