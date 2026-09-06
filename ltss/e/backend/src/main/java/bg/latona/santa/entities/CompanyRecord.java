package bg.latona.santa.entities;

import java.util.Date;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@MappedSuperclass
@Audited
@Data //auto-create getters and setters
//@ToString(callSuper=true) //to get the inherited fields in the debug print
public class CompanyRecord extends CommonRecord {
	//TODO create interface for editing the companies
	@ManyToOne(fetch = FetchType.LAZY)
	ManagedCompany company;

	public CompanyRecord() {
		super();
	}

	public CompanyRecord(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly,
			ManagedCompany company) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly);
		this.company = company;
	}

}
