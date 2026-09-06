package bg.latona.santa.entities.article;

import java.util.Date;

import javax.persistence.Entity;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ArticleService extends Article {
	
	
	public ArticleService() {};
	
	public ArticleService(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String name, String foreignId,
			String measureForeignId, String measure, String measureShort) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, foreignId, measureForeignId, measure, measureShort);
	}
}
