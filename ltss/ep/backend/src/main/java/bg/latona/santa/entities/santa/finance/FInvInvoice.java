package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtBankAccount;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data //auto-create getters and setters
@ToString(exclude = {"fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fInvInvoices"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FInvInvoice extends CompanyRecord {
	private Integer invNo; //COLLATE pg_catalog."default" NOT NULL,
	private LocalDate invDate; //NOT NULL,
	private LocalDate datePayment;
	private LocalDate dateAccount; //NOT NULL,
	@ManyToOne
	private FInvInvoice ineId;
	@ManyToOne
	private CCcOrganizationUnit outCode; //COLLATE pg_catalog."default" NOT NULL,
	@Column(length = 3000)
	private String withoutVatReason; //COLLATE pg_catalog."default",
	private BigDecimal percentVat;
	private BigDecimal taxBase;
	private BigDecimal vatAmount;
	private BigDecimal totalAmount; //NOT NULL,
	@ManyToOne
	private LoiInvInvoicePaymentType paymentType; //COLLATE pg_catalog."default" NOT NULL,
	@ManyToOne
	private CCtBankAccount batId;
	@Column(length = 3000)
	private String reason; //COLLATE pg_catalog."default" NOT NULL,
	@Column(length = 3000)
	private String notes; //COLLATE pg_catalog."default",
	private String compilerName; //COLLATE pg_catalog."default" NOT NULL,
	private String salePeriod; //COLLATE pg_catalog."default" DEFAULT to_char(('now'::text)::timestamp without time zone, 'MMYYYY'::text),
	private String purchasePeriod; //COLLATE pg_catalog."default" DEFAULT to_char(('now'::text)::timestamp without time zone, 'MMYYYY'::text),
	@ManyToOne
	private LoiBatchJteDefaultSide invType; //COLLATE pg_catalog."default" NOT NULL DEFAULT 'S'::character varying,
	@ManyToOne
	private CCtCurrency cuyCode; //COLLATE pg_catalog."default" NOT NULL,
	private BigDecimal cuyRate; //NOT NULL DEFAULT 1,
	@ManyToOne
	private LoiInvInvoiceStatus status; //COLLATE pg_catalog."default" NOT NULL DEFAULT 'C'::bpchar,
	@ManyToOne
	private FPtBatch vodBahId;
	@ManyToOne
	private FPtBatch bahId;
	private Integer processStatus; // NOT NULL DEFAULT '-1'::integer,
	private Integer cuyUnit;  //NOT NULL DEFAULT 1,
	@ManyToOne
	private FCtTransitionType tteId; //NOT NULL,
	@ManyToOne
	private CCcPartner ccParId; //NOT NULL,
	@ManyToOne
	private FCtInvDealType ideId; //NOT NULL,
	@ManyToOne
	private FCcEbk ccEbkId;
	@ManyToOne
	private FCcFunction ccFunId;
	@ManyToOne
	private FCcProgram ccPrmId;
	@ManyToOne
	private FCcFinsource ccFieId;
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

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ineId")
	private List<FInvInvoice> fInvInvoices;
}

/*
COMMENT ON TABLE accounting.inv_invoices
    IS '!  !
ine';

COMMENT ON COLUMN accounting.inv_invoices.ine_id
    IS 'tuk stoi samo faktura, no ne i kreditno izvestie.';

COMMENT ON COLUMN accounting.inv_invoices.cuy_rate
    IS 'dali da e zadalzhitelno i po podrazbirane da e 1 za leva ili - ne';

COMMENT ON COLUMN accounting.inv_invoices.process_status
    IS 'from register.ct_error_code';
-- Index: ine_bat_idx

-- DROP INDEX IF EXISTS accounting.ine_bat_idx;

CREATE INDEX IF NOT EXISTS ine_bat_idx
    ON accounting.inv_invoices USING btree
    (bat_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_cc_out_idx

-- DROP INDEX IF EXISTS accounting.ine_cc_out_idx;

CREATE INDEX IF NOT EXISTS ine_cc_out_idx
    ON accounting.inv_invoices USING btree
    (cc_out_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_cot_idx

-- DROP INDEX IF EXISTS accounting.ine_cot_idx;

CREATE INDEX IF NOT EXISTS ine_cot_idx
    ON accounting.inv_invoices USING btree
    (cc_cot_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_cuy_idx

-- DROP INDEX IF EXISTS accounting.ine_cuy_idx;

CREATE INDEX IF NOT EXISTS ine_cuy_idx
    ON accounting.inv_invoices USING btree
    (cuy_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_ebk_idx

-- DROP INDEX IF EXISTS accounting.ine_ebk_idx;

CREATE INDEX IF NOT EXISTS ine_ebk_idx
    ON accounting.inv_invoices USING btree
    (cc_ebk_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_fie_idx

-- DROP INDEX IF EXISTS accounting.ine_fie_idx;

CREATE INDEX IF NOT EXISTS ine_fie_idx
    ON accounting.inv_invoices USING btree
    (cc_fie_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_fun_idx

-- DROP INDEX IF EXISTS accounting.ine_fun_idx;

CREATE INDEX IF NOT EXISTS ine_fun_idx
    ON accounting.inv_invoices USING btree
    (cc_fun_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_gte_idx

-- DROP INDEX IF EXISTS accounting.ine_gte_idx;

CREATE INDEX IF NOT EXISTS ine_gte_idx
    ON accounting.inv_invoices USING btree
    (cc_gte_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_ine_idx

-- DROP INDEX IF EXISTS accounting.ine_ine_idx;

CREATE INDEX IF NOT EXISTS ine_ine_idx
    ON accounting.inv_invoices USING btree
    (ine_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_out_idx

-- DROP INDEX IF EXISTS accounting.ine_out_idx;

CREATE INDEX IF NOT EXISTS ine_out_idx
    ON accounting.inv_invoices USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_par_idx

-- DROP INDEX IF EXISTS accounting.ine_par_idx;

CREATE INDEX IF NOT EXISTS ine_par_idx
    ON accounting.inv_invoices USING btree
    (cc_par_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_prm_idx

-- DROP INDEX IF EXISTS accounting.ine_prm_idx;

CREATE INDEX IF NOT EXISTS ine_prm_idx
    ON accounting.inv_invoices USING btree
    (cc_prm_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_re1_idx

-- DROP INDEX IF EXISTS accounting.ine_re1_idx;

CREATE INDEX IF NOT EXISTS ine_re1_idx
    ON accounting.inv_invoices USING btree
    (cc_re1_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_re2_idx

-- DROP INDEX IF EXISTS accounting.ine_re2_idx;

CREATE INDEX IF NOT EXISTS ine_re2_idx
    ON accounting.inv_invoices USING btree
    (cc_re2_id ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: ine_tte_idx

-- DROP INDEX IF EXISTS accounting.ine_tte_idx;

CREATE INDEX IF NOT EXISTS ine_tte_idx
    ON accounting.inv_invoices USING btree
    (tte_id ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: tr_ine_bi

-- DROP TRIGGER IF EXISTS tr_ine_bi ON accounting.inv_invoices;

CREATE TRIGGER tr_ine_bi
    BEFORE INSERT
    ON accounting.inv_invoices
    FOR EACH ROW
    EXECUTE FUNCTION accounting.get_next_invoice_number();

-- Trigger: tr_ine_bu

-- DROP TRIGGER IF EXISTS tr_ine_bu ON accounting.inv_invoices;

CREATE TRIGGER tr_ine_bu
    BEFORE UPDATE
    ON accounting.inv_invoices
    FOR EACH ROW
    EXECUTE FUNCTION accounting.set_date_updated();
 */
