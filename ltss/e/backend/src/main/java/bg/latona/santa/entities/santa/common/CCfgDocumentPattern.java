package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class CCfgDocumentPattern extends CompanyRecord{

	@ManyToOne
	private CCfgDocumentCounter cfgDcr;
	private String documentCode;
	private String pattern;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String complementarySymbols;
	//@ManyToOne
	//private CCcOrganizationUnit outCreated;
}
