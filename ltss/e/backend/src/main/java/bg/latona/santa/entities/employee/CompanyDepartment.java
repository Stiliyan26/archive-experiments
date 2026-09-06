package bg.latona.santa.entities.employee;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
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
@ToString(exclude = {"companyDepartments","employees"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"companyDepartments","employees"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CompanyDepartment extends CompanyRecord {

	private String name;
	@ManyToOne
	CompanyDepartment parentDepartment;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "parentDepartment")
	private List<CompanyDepartment> companyDepartments;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "department")
	private List<Employee> employees;
	
	public CompanyDepartment() {}

	public CompanyDepartment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, CompanyDepartment parentDepartment) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.parentDepartment = parentDepartment;
	}

}
