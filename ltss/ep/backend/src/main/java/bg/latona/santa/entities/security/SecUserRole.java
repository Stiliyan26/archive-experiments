package bg.latona.santa.entities.security;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class SecUserRole extends CompanyRecord {

	@ManyToOne
	private SecUser user;
	@ManyToOne
	private SecRole role;
	
	public SecUserRole() {};
	
	public SecUserRole(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			SecUser user, SecRole role) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.user = user;
		this.role = role;
	}
}
