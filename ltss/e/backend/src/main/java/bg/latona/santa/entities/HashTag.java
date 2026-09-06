package bg.latona.santa.entities;

import java.util.Date;

import javax.persistence.Entity;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class HashTag extends CompanyRecord {
	
	private String text;
	//TODO parent hash tag - for hierarchy like folders
	
	public HashTag() {};
	
	public HashTag(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Attachable attachable, String text) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.text = text;
	}
}
