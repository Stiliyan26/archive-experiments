package bg.latona.santa.entities.security;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "ix_SecRolePermission_RolePermission", columnList = "role_id ,permission_id")
	},
	uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "role_id", "permission_id"})) //for all unique constraints should be put Drools UNIQUE rules too
public class SecRolePermission extends CompanyRecord {

	@ManyToOne
	private SecRole role;
	@ManyToOne
	private SecPermission permission;
	
	public SecRolePermission() {};
	
	public SecRolePermission(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			SecRole role, SecPermission permission) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.role = role;
		this.permission = permission;
	}
}
