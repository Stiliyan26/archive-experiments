package bg.latona.santa.entities.security;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"permissions","secUserRoles"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"permissions","secUserRoles"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class SecRole extends CompanyRecord {
	
	private String name;
	private Long code;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "role")
	private List<SecRolePermission> permissions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "role")
	private List<SecUserRole> secUserRoles;
	
	public SecRole() {}

	public SecRole(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
	};
}
