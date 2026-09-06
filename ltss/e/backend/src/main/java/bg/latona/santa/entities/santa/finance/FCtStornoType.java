package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtStornoType extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //COLLATE pg_catalog."default" NOT NULL,
	@ManyToOne
	private LoiCtStornoTypeType type; //COLLATE pg_catalog."default" NOT NULL,
	private Boolean isDefault; //COLLATE pg_catalog."default" NOT NULL DEFAULT 'N'::bpchar,
}
/*
COMMENT ON TABLE accounting.ct_storno_types
    IS '! ste !
storno types';
-- Index: ste_out_idx

-- DROP INDEX IF EXISTS accounting.ste_out_idx;

CREATE INDEX IF NOT EXISTS ste_out_idx
    ON accounting.ct_storno_types USING btree
    (out_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: tr_ste_bu

-- DROP TRIGGER IF EXISTS tr_ste_bu ON accounting.ct_storno_types;

CREATE TRIGGER tr_ste_bu
    BEFORE UPDATE
    ON accounting.ct_storno_types
    FOR EACH ROW
    EXECUTE FUNCTION accounting.set_date_updated();
 */
