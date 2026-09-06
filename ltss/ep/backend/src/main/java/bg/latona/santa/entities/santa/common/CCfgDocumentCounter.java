package bg.latona.santa.entities.santa.common;


import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"cCfgDocumentPatterns"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cCfgDocumentPatterns"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CCfgDocumentCounter extends CompanyRecord{
	
	private String name;
	private String description;
	private Integer initialValue;
	private Integer nextValue;
	//@ManyToOne
	//private CCcOrganizationUnit outCreated;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cfgDcr")
	private List<CCfgDocumentPattern> cCfgDocumentPatterns;

	public CCfgDocumentCounter() {}

	public CCfgDocumentCounter(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
				String name, String description, Integer initialValue, Integer nextValue) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.initialValue = initialValue;
		this.nextValue = nextValue;
		this.description = description;
	}
}
