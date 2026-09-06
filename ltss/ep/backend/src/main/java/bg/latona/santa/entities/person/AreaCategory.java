package bg.latona.santa.entities.person;

import bg.latona.santa.entities.wato.ImportedLegalPersonGroup;
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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"customers","importedLegalPersonGroups"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"customers","importedLegalPersonGroups"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class AreaCategory extends CompanyRecord {

	String name;
	private String code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "area")
	private List<Customer> customers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "area")
	private List<ImportedLegalPersonGroup> importedLegalPersonGroups;

	public AreaCategory() {}

	public AreaCategory(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}

}
