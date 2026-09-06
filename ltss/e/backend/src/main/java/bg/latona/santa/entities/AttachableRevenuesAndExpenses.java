package bg.latona.santa.entities;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;


import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class AttachableRevenuesAndExpenses extends AllocationOrigin {

	@ManyToOne
	private Article article;
	private BigDecimal ammount;
	@ManyToOne
	private Attachable attachable;
	
	public AttachableRevenuesAndExpenses() {};
	
	public AttachableRevenuesAndExpenses(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Article article, BigDecimal ammount, Attachable attachable) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.article = article;
		this.ammount = ammount;
		this.attachable = attachable;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return getAmmount();
	}
}
