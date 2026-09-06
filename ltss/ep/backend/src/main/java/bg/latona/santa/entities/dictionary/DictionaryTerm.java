package bg.latona.santa.entities.dictionary;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class DictionaryTerm extends CompanyRecord {

	private String name;
	private String meaning;
	@ManyToOne
	private Dictionary dictionary;

	public DictionaryTerm() {};

	public DictionaryTerm(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String meaning, Dictionary dictionary) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.meaning = meaning;
		this.dictionary = dictionary;
	}
}
