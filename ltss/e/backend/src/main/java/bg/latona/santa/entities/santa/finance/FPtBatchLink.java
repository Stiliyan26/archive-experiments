package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCtCurrency;
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
public class FPtBatchLink extends CompanyRecord {

	@ManyToOne
	private FPtBatch bahId1; //NOT NULL,
	@ManyToOne
	private FPtBatch bahId2; //NOT NULL,
	@ManyToOne
	private LoiLinkedFlag flag1; //COLLATE pg_catalog."default" NOT NULL,
	@ManyToOne
	private LoiLinkedFlag flag2; //COLLATE pg_catalog."default" NOT NULL,
	private BigDecimal amount; //NOT NULL,
	private BigDecimal amountCurrency; //NOT NULL,
	@ManyToOne
	private CCtCurrency cuyCode; // COLLATE pg_catalog."default" NOT NULL,
	private BigDecimal cuyRate; //NOT NULL,
	private Integer cuyUnit; //NOT NULL,
	@ManyToOne
	private LoiPtBatchLinkStatus status; //COLLATE pg_catalog."default" NOT NULL,
}

/*
COMMENT ON COLUMN accounting.pt_batch_links.id
    IS 'Sequence is blk_seq';

COMMENT ON COLUMN accounting.pt_batch_links.flag1
    IS 'For bah_id1. Domain in ref_date LINKED_FLAG';

COMMENT ON COLUMN accounting.pt_batch_links.flag2
    IS 'For bah_id2. Domain in ref_date LINKED_FLAG';

COMMENT ON COLUMN accounting.pt_batch_links.status
    IS 'Domain in register.ref_date STATUS';
-- Index: blk_bah1_idx

-- DROP INDEX IF EXISTS accounting.blk_bah1_idx;

CREATE INDEX IF NOT EXISTS blk_bah1_idx
    ON accounting.pt_batch_links USING btree
    (bah_id1 ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: blk_bah2_idx

-- DROP INDEX IF EXISTS accounting.blk_bah2_idx;

CREATE INDEX IF NOT EXISTS blk_bah2_idx
    ON accounting.pt_batch_links USING btree
    (bah_id2 ASC NULLS LAST)
    TABLESPACE pg_default;
-- Index: blk_cuy_idx

-- DROP INDEX IF EXISTS accounting.blk_cuy_idx;

CREATE INDEX IF NOT EXISTS blk_cuy_idx
    ON accounting.pt_batch_links USING btree
    (cuy_code COLLATE pg_catalog."default" ASC NULLS LAST)
    TABLESPACE pg_default;

-- Trigger: tr_blk_bu

-- DROP TRIGGER IF EXISTS tr_blk_bu ON accounting.pt_batch_links;

CREATE TRIGGER tr_blk_bu
    BEFORE UPDATE
    ON accounting.pt_batch_links
    FOR EACH ROW
    EXECUTE FUNCTION accounting.set_date_updated();
 */
