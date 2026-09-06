package bg.latona.santa.entities.dictionary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"dictionaryClassificationPolicies","dictionaryTerms"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"dictionaryClassificationPolicies","dictionaryTerms"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Dictionary extends CompanyRecord {

	private String name;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dictionary")
	private List<DictionaryClassificationPolicy> dictionaryClassificationPolicies;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dictionary")
	private List<DictionaryTerm> dictionaryTerms;

	public Dictionary() {};

	public Dictionary(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
	}
}
