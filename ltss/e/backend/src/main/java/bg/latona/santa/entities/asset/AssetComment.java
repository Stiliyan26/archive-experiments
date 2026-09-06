package bg.latona.santa.entities.asset;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class AssetComment extends CompanyRecord {
	
	private String text;
	@ManyToOne
	private Asset asset;
	
	public AssetComment() {};
	
	public AssetComment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String text, Asset asset) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.text = text;
		this.asset = asset;
	}
}
