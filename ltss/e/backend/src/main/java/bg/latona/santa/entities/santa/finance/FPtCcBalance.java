package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FPtCcBalance extends CompanyRecord {

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
}
/*
COMMENT ON TABLE accounting.pt_cc_balances
    IS '! ccb !
Cost Center Balances  - agregated for period - debit and credit for specific item in chart of accounts';

COMMENT ON COLUMN accounting.pt_cc_balances.id
    IS 'primary key - not needed, but for safety';

COMMENT ON COLUMN accounting.pt_cc_balances.out_code
    IS 'organization unit code';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_ebk_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_ebk_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_fun_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_fun_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_prm_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_prm_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_fie_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_fie_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_par_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_par_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_gte_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_gte_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_cot_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_cot_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_out_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_out_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_re1_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_re1_dt_amount
    IS 'debit  amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_re2_ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_cc_balances.cc_re2_dt_amount
    IS 'debit  amount for the period';
-- Index: ccb_cc_out_idx

-- DROP INDEX IF EXISTS accounting.ccb_cc_out_idx;

CREATE INDEX IF NOT EXISTS ccb_cc_out_idx
    ON accounting.pt_cc_balances USING btree
    (cc_out_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_cot_idx

-- DROP INDEX IF EXISTS accounting.ccb_cot_idx;

CREATE INDEX IF NOT EXISTS ccb_cot_idx
    ON accounting.pt_cc_balances USING btree
    (cc_cot_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_date_updated_idx

-- DROP INDEX IF EXISTS accounting.ccb_date_updated_idx;

CREATE INDEX IF NOT EXISTS ccb_date_updated_idx
    ON accounting.pt_cc_balances USING btree
    (date_updated ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_ebk_idx

-- DROP INDEX IF EXISTS accounting.ccb_ebk_idx;

CREATE INDEX IF NOT EXISTS ccb_ebk_idx
    ON accounting.pt_cc_balances USING btree
    (cc_ebk_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_fie_idx

-- DROP INDEX IF EXISTS accounting.ccb_fie_idx;

CREATE INDEX IF NOT EXISTS ccb_fie_idx
    ON accounting.pt_cc_balances USING btree
    (cc_fie_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_fun_idx

-- DROP INDEX IF EXISTS accounting.ccb_fun_idx;

CREATE INDEX IF NOT EXISTS ccb_fun_idx
    ON accounting.pt_cc_balances USING btree
    (cc_fun_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_gte_idx

-- DROP INDEX IF EXISTS accounting.ccb_gte_idx;

CREATE INDEX IF NOT EXISTS ccb_gte_idx
    ON accounting.pt_cc_balances USING btree
    (cc_gte_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_out_coa_period_fun_idx

-- DROP INDEX IF EXISTS accounting.ccb_out_coa_period_fun_idx;

CREATE INDEX IF NOT EXISTS ccb_out_coa_period_fun_idx
    ON accounting.pt_cc_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST, coa_id ASC NULLS LAST, period COLLATE pg_catalog."default" ASC NULLS LAST, cc_fun_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_out_coa_period_par_idx

-- DROP INDEX IF EXISTS accounting.ccb_out_coa_period_par_idx;

CREATE INDEX IF NOT EXISTS ccb_out_coa_period_par_idx
    ON accounting.pt_cc_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST, coa_id ASC NULLS LAST, period COLLATE pg_catalog."default" ASC NULLS LAST, cc_par_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_out_coa_period_prm_idx

-- DROP INDEX IF EXISTS accounting.ccb_out_coa_period_prm_idx;

CREATE INDEX IF NOT EXISTS ccb_out_coa_period_prm_idx
    ON accounting.pt_cc_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST, coa_id ASC NULLS LAST, period COLLATE pg_catalog."default" ASC NULLS LAST, cc_prm_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_out_idx

-- DROP INDEX IF EXISTS accounting.ccb_out_idx;

CREATE INDEX IF NOT EXISTS ccb_out_idx
    ON accounting.pt_cc_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_par_idx

-- DROP INDEX IF EXISTS accounting.ccb_par_idx;

CREATE INDEX IF NOT EXISTS ccb_par_idx
    ON accounting.pt_cc_balances USING btree
    (cc_par_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_prm_idx

-- DROP INDEX IF EXISTS accounting.ccb_prm_idx;

CREATE INDEX IF NOT EXISTS ccb_prm_idx
    ON accounting.pt_cc_balances USING btree
    (cc_prm_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_re1_idx

-- DROP INDEX IF EXISTS accounting.ccb_re1_idx;

CREATE INDEX IF NOT EXISTS ccb_re1_idx
    ON accounting.pt_cc_balances USING btree
    (cc_re1_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ccb_re2_idx

-- DROP INDEX IF EXISTS accounting.ccb_re2_idx;

CREATE INDEX IF NOT EXISTS ccb_re2_idx
    ON accounting.pt_cc_balances USING btree
    (cc_re2_id ASC NULLS LAST)
    TABLESPACE pg_default;
 */