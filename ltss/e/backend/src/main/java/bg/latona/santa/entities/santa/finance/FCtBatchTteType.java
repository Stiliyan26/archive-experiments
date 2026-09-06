package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.security.SecUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtBatchTteType extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL,
	@ManyToOne
	private FCtTransitionType tteId; //NOT NULL,
	@ManyToOne
	private FCtBatchType bteId; //NOT NULL,

	public FCtBatchTteType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, FCtTransitionType tteId, FCtBatchType bteId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.tteId = tteId;
		this.bteId = bteId;
	}
}
/* COMMENT ON COLUMN accounting.ct_batch_tte_types.id IS 'Sequence is btt_seq'; */