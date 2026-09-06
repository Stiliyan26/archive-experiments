package bg.latona.santa.entities.person;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"contacts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"contacts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class ContactType extends CompanyRecord {
	public static final Long CONTACT_TYPE_GENERAL = Long.valueOf(1);
	
	private String name;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<Contact> contacts;
	
	public ContactType() {};
	
	public ContactType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}
}
