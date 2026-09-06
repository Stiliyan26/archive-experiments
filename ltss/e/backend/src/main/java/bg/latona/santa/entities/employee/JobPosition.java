package bg.latona.santa.entities.employee;

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
@ToString(exclude = {"employees","JobPositionRequirement"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"employees","JobPositionRequirement"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class JobPosition extends CompanyRecord {

	private String name;
	private String code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "position")
	private List<Employee> employees;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "position")
	private List<JobPositionRequirement> jobPositionRequirements;
	
	public JobPosition() {
	}

	public JobPosition(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	}
}
