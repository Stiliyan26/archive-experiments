package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtCoaBalance extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //COLLATE pg_catalog."default" NOT NULL,
	private BigDecimal ctAmount; // NOT NULL,
	private BigDecimal dtAmount; // NOT NULL,
	private String period; // COLLATE pg_catalog."default" NOT NULL,
	@ManyToOne
	private FChartAccount coaId; // NOT NULL,
	private LocalDate dateUpdated;
}
/*
COMMENT ON TABLE accounting.pt_coa_balances
    IS '! cab !
Chart of accounts balances -- aggregated in hierarhy for period';

COMMENT ON COLUMN accounting.pt_coa_balances.id
    IS 'primary key - not needed, but for safety';

COMMENT ON COLUMN accounting.pt_coa_balances.out_code
    IS 'organization unit code';

COMMENT ON COLUMN accounting.pt_coa_balances.ct_amount
    IS 'credit amount for the period';

COMMENT ON COLUMN accounting.pt_coa_balances.dt_amount
    IS 'debit  amount for the period';
-- Index: cab_date_updated_idx

-- DROP INDEX IF EXISTS accounting.cab_date_updated_idx;

CREATE INDEX IF NOT EXISTS cab_date_updated_idx
    ON accounting.pt_coa_balances USING btree
    (date_updated ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: cab_out_idx

-- DROP INDEX IF EXISTS accounting.cab_out_idx;

CREATE INDEX IF NOT EXISTS cab_out_idx
    ON accounting.pt_coa_balances USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;
 */