
package bg.latona.santa.entities.santa.finance;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
//this is a configuration class
public class FCtJteDefault extends CompanyRecord {
	@ManyToOne
	private FJournalType jte;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	//chart of account for debit
	@ManyToOne
	private FChartAccount coaDt;
	//char of account credit
	@ManyToOne
	private FChartAccount coaCt;

}
