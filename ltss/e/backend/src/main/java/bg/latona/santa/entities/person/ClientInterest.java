package bg.latona.santa.entities.person;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.ArticleProduct;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ClientInterest extends CompanyRecord {

	@ManyToOne
	private Customer customer;
	@ManyToOne
	private ArticleProduct articleProduct;
	private Boolean interested;
	
	public ClientInterest() {}

	public ClientInterest(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			Customer legalPerson, ArticleProduct articleProduct, Boolean interested) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.customer = legalPerson;
		this.articleProduct = articleProduct;
		this.interested = interested;
	};
}
