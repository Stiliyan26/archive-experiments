package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtCurrency;
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
public class FBreTransition extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL,
	private LocalDate postDate;  //NOT NULL,
	private String module;  //NOT NULL,
	@ManyToOne
	private FCtTransitionType tteCode; //NOT NULL,
	private String refNo;  //NOT NULL,
	private LocalDate refDate;  //NOT NULL,
	private BigDecimal amountOutstanding;
	private BigDecimal amountTotal;  //NOT NULL,
	private BigDecimal amountDo;
	private BigDecimal amountVat;
	@ManyToOne
	private CCtCurrency cuyCode; //NOT NULL DEFAULT 'BGN'::character varying,
	private BigDecimal cuyRate; //NOT NULL DEFAULT 1,
	private LocalDate dueDate;
	@Column(length = 3000)
	private String descr;
	@ManyToOne
	private LoiBreTransitionResult result;  //NOT NULL DEFAULT '-1'::integer,
	private BigDecimal cost;  //NOT NULL DEFAULT 0.0,
	@ManyToOne
	private FPtBatch bahId;
	@ManyToOne
	private FCtInvDealType ideCode;
	@ManyToOne
	private LoiBatchTypeRuleDependenceType dependenceType; //-- Domain in ref_date DEPENDENCE_TYPE
	private String dependenceCode;
	private String addRefNo; //-- Additional reference number
	private LocalDate addRefDate;
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
//	@ManyToOne
//	private FCahPayment patId;
	private BigDecimal advance;
	
	
	public FBreTransition(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, LocalDate postDate, String module,
			FCtTransitionType tteCode, String refNo, LocalDate refDate, BigDecimal amountOutstanding,
			BigDecimal amountTotal, BigDecimal amountDo, BigDecimal amountVat, CCtCurrency cuyCode, BigDecimal cuyRate,
			LocalDate dueDate, String descr, LoiBreTransitionResult result, BigDecimal cost, FPtBatch bahId, FCtInvDealType ideCode,
			LoiBatchTypeRuleDependenceType dependenceType, String dependenceCode, String addRefNo, LocalDate addRefDate,
			FCcEbk ccEbkId, FCcFunction ccFunId, FCcProgram ccPrmId, FCcFinsource ccFieId, CCcPartner ccParId,
			CCcGoodsType ccGteId, FCcContract ccCotId, CCcOrganizationUnit ccOutId, FCcReserve1 ccRe1Id,
			FCcReserve2 ccRe2Id, BigDecimal advance) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.postDate = postDate;
		this.module = module;
		this.tteCode = tteCode;
		this.refNo = refNo;
		this.refDate = refDate;
		this.amountOutstanding = amountOutstanding;
		this.amountTotal = amountTotal;
		this.amountDo = amountDo;
		this.amountVat = amountVat;
		this.cuyCode = cuyCode;
		this.cuyRate = cuyRate;
		this.dueDate = dueDate;
		this.descr = descr;
		this.result = result;
		this.cost = cost;
		this.bahId = bahId;
		this.ideCode = ideCode;
		this.dependenceType = dependenceType;
		this.dependenceCode = dependenceCode;
		this.addRefNo = addRefNo;
		this.addRefDate = addRefDate;
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
		this.advance = advance;
	}
}

/*
GRANT ALL ON TABLE register.bre_transitions TO register;
GRANT ALL ON TABLE register.bre_transitions TO grandis;
COMMENT ON COLUMN register.bre_transitions.dependence_type IS 'Domain in ref_date DEPENDENCE_TYPE';
COMMENT ON COLUMN register.bre_transitions.add_ref_no IS 'Additional reference number'; */