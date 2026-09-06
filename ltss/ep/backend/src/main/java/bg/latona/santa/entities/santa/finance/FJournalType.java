package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.security.SecUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.Date;
import java.util.List;


@Data //auto-create getters and setters
@ToString(exclude = {"fCtJteDefaults","fCtBatchTypeRules","fCtBatchJteDefaults","fPtJournals"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtJteDefaults","fCtBatchTypeRules","fCtBatchJteDefaults","fPtJournals"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FJournalType extends CompanyRecord{
	//code of the journal might be skipped
	private String code;
	//name of the journal - for sales invoice, purchase invoice, bank etc.
	private String name;
	private String descr;
	@ManyToOne
	private LoiJournalTypeCalculationType type;
	@ManyToOne
	private CCcOrganizationUnit outCode;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "jte")
	private List<FCtJteDefault> fCtJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "jteId")
	private List<FCtBatchTypeRule> fCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "jteId")
	private List<FCtBatchJteDefault> fCtBatchJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "jteId")
	private List<FPtJournal> fPtJournals;

	public FJournalType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, String code, String name, String descr,
			LoiJournalTypeCalculationType type, CCcOrganizationUnit outCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.descr = descr;
		this.type = type;
		this.outCode = outCode;
	}
}
