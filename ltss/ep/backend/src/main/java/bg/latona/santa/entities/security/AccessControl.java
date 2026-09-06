package bg.latona.santa.entities.security;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CommonRecord;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class AccessControl extends CommonRecord {

	private Long commonRecordId;
	@ManyToOne
	private SecUser secUser;
	private Boolean read;
	private Boolean write;
	private Boolean administer;
	
	public AccessControl() {
	}

	public AccessControl(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly,
			Long commonRecordId, SecUser secUser, Boolean read, Boolean write, Boolean administer) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly);
		this.commonRecordId = commonRecordId;
		this.secUser = secUser;
		this.read = read;
		this.write = write;
		this.administer = administer;
	}
}
