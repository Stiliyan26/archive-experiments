package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@NoArgsConstructor
@Entity //JPA persisted class
public class FHpCcBalance extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //COLLATE pg_catalog."default" NOT NULL,
	private BigDecimal ccEbkCtAmount;
	private BigDecimal ccEbkDtAmount;
	private BigDecimal ccFunCtAmount;
	private BigDecimal ccFunDtAmount;
	private BigDecimal ccPrmCtAmount;
	private BigDecimal ccPrmDtAmount;
	private BigDecimal ccFieCtAmount;
	private BigDecimal ccFieDtAmount;
	private BigDecimal ccParCtAmount;
	private BigDecimal ccParDtAmount;
	private BigDecimal ccGteCtAmount;
	private BigDecimal ccGteDtAmount;
	private BigDecimal ccCotCtAmount;
	private BigDecimal ccCotDtAmount;
	private BigDecimal ccOutCtAmount;
	private BigDecimal ccOutDtAmount;
	private BigDecimal ccRe1CtAmount;
	private BigDecimal ccRe1DtAmount;
	private BigDecimal ccRe2CtAmount;
	private BigDecimal ccRe2DtAmount;
	private String period; //COLLATE pg_catalog."default" NOT NULL,
	@ManyToOne
	private FChartAccount coaId; //NOT NULL,
	@ManyToOne
	private FCcEbk ccEbkId;
	@ManyToOne
	private FCcFunction ccFunId;
	@ManyToOne
	private FCcFinsource ccFieId;
	@ManyToOne
	private FCcProgram ccPrmId;
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

	public FHpCcBalance(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, BigDecimal ccEbkCtAmount, BigDecimal ccEbkDtAmount, BigDecimal ccFunCtAmount, 
			BigDecimal ccFunDtAmount, BigDecimal ccPrmCtAmount, BigDecimal ccPrmDtAmount, BigDecimal ccFieCtAmount, 
			BigDecimal ccFieDtAmount, BigDecimal ccParCtAmount, BigDecimal ccParDtAmount, BigDecimal ccGteCtAmount, 
			BigDecimal ccGteDtAmount, BigDecimal ccCotCtAmount, BigDecimal ccCotDtAmount, BigDecimal ccOutCtAmount, 
			BigDecimal ccOutDtAmount, BigDecimal ccRe1CtAmount, BigDecimal ccRe1DtAmount, BigDecimal ccRe2CtAmount, 
			BigDecimal ccRe2DtAmount, String period, FChartAccount coaId, FCcEbk ccEbkId, FCcFunction ccFunId, FCcFinsource ccFieId, 
			FCcProgram ccPrmId, CCcPartner ccParId, CCcGoodsType ccGteId, FCcContract ccCotId, CCcOrganizationUnit ccOutId, 
			FCcReserve1 ccRe1Id, FCcReserve2 ccRe2Id) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.ccEbkCtAmount = ccEbkCtAmount;
		this.ccEbkDtAmount = ccEbkDtAmount;
		this.ccFunCtAmount = ccFunCtAmount;
		this.ccFunDtAmount = ccFunDtAmount;
		this.ccPrmCtAmount = ccPrmCtAmount;
		this.ccPrmDtAmount = ccPrmDtAmount;
		this.ccFieCtAmount = ccFieCtAmount;
		this.ccFieDtAmount = ccFieDtAmount;
		this.ccParCtAmount = ccParCtAmount;
		this.ccParDtAmount = ccParDtAmount;
		this.ccGteCtAmount = ccGteCtAmount;
		this.ccGteDtAmount = ccGteDtAmount;
		this.ccCotCtAmount = ccCotCtAmount;
		this.ccCotDtAmount = ccCotDtAmount;
		this.ccOutCtAmount = ccOutCtAmount;
		this.ccOutDtAmount = ccOutDtAmount;
		this.ccRe1CtAmount = ccRe1CtAmount;
		this.ccRe1DtAmount = ccRe1DtAmount;
		this.ccRe2CtAmount = ccRe2CtAmount;
		this.ccRe2DtAmount = ccRe2DtAmount;
		this.period = period;
		this.coaId = coaId;
		this.ccEbkId = ccEbkId;
		this.ccFunId = ccFunId;
		this.ccFieId = ccFieId;
		this.ccPrmId = ccPrmId;
		this.ccParId = ccParId;
		this.ccGteId = ccGteId;
		this.ccCotId = ccCotId;
		this.ccOutId = ccOutId;
		this.ccRe1Id = ccRe1Id;
		this.ccRe2Id = ccRe2Id;
	}
}

/*
COMMENT ON COLUMN accounting.hp_cc_balances.id
    IS 'Sequence is cbc_seq';
-- Index: cbc_cc_out_idx

-- DROP INDEX IF EXISTS accounting.cbc_cc_out_idx;

CREATE INDEX IF NOT EXISTS cbc_cc_out_idx
    ON accounting.hp_cc_balances USING btree
    (cc_out_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_cot_idx

-- DROP INDEX IF EXISTS accounting.cbc_cot_idx;

CREATE INDEX IF NOT EXISTS cbc_cot_idx
    ON accounting.hp_cc_balances USING btree
    (cc_cot_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_ebk_idx

-- DROP INDEX IF EXISTS accounting.cbc_ebk_idx;

CREATE INDEX IF NOT EXISTS cbc_ebk_idx
    ON accounting.hp_cc_balances USING btree
    (cc_ebk_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_fie_idx

-- DROP INDEX IF EXISTS accounting.cbc_fie_idx;

CREATE INDEX IF NOT EXISTS cbc_fie_idx
    ON accounting.hp_cc_balances USING btree
    (cc_fie_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_fun_idx

-- DROP INDEX IF EXISTS accounting.cbc_fun_idx;

CREATE INDEX IF NOT EXISTS cbc_fun_idx
    ON accounting.hp_cc_balances USING btree
    (cc_fun_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_gte_idx

-- DROP INDEX IF EXISTS accounting.cbc_gte_idx;

CREATE INDEX IF NOT EXISTS cbc_gte_idx
    ON accounting.hp_cc_balances USING btree
    (cc_gte_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_out_idx

-- DROP INDEX IF EXISTS accounting.cbc_out_idx;

CREATE INDEX IF NOT EXISTS cbc_out_idx
    ON accounting.hp_cc_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_par_idx

-- DROP INDEX IF EXISTS accounting.cbc_par_idx;

CREATE INDEX IF NOT EXISTS cbc_par_idx
    ON accounting.hp_cc_balances USING btree
    (cc_par_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_prm_idx

-- DROP INDEX IF EXISTS accounting.cbc_prm_idx;

CREATE INDEX IF NOT EXISTS cbc_prm_idx
    ON accounting.hp_cc_balances USING btree
    (cc_prm_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_re1_idx

-- DROP INDEX IF EXISTS accounting.cbc_re1_idx;

CREATE INDEX IF NOT EXISTS cbc_re1_idx
    ON accounting.hp_cc_balances USING btree
    (cc_re1_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cbc_re2_idx

-- DROP INDEX IF EXISTS accounting.cbc_re2_idx;

CREATE INDEX IF NOT EXISTS cbc_re2_idx
    ON accounting.hp_cc_balances USING btree
    (cc_re2_id ASC NULLS LAST)
    TABLESPACE pg_default;
 */
