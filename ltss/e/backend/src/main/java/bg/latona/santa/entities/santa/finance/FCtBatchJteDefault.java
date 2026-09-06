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
public class FCtBatchJteDefault extends CompanyRecord {

		@ManyToOne
		private FCtBatchType bteId; //NOT NULL, -- The batch to which refers given defaults
		@ManyToOne
		private CCcOrganizationUnit outCode; //NOT NULL, -- The company or office to which refers given defaults
		@ManyToOne
		private FJournalType jteId; //NOT NULL, -- Default journal
		@ManyToOne
		private LoiBatchJteDefaultSide side; // NOT NULL,
}

/* COMMENT ON COLUMN accounting.ct_batch_jte_defaults.bte_id IS 'The batch to which refers given defaults';
COMMENT ON COLUMN accounting.ct_batch_jte_defaults.out_code IS 'The company or office to which refers given defaults';
COMMENT ON COLUMN accounting.ct_batch_jte_defaults.jte_id IS 'Default journal'; */