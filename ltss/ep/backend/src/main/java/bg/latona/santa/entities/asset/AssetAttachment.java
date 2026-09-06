package bg.latona.santa.entities.asset;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class AssetAttachment extends CompanyRecord {
	
	private String description;
	@ManyToOne
	private Asset asset;
	@ManyToOne
	private Attachable attachmentToAsset;
	
	public AssetAttachment() {};
	
	public AssetAttachment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String description, Asset asset, Attachable attachmentToAsset) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.description = description;
		this.asset = asset;
		this.attachmentToAsset = attachmentToAsset;
	}
}
