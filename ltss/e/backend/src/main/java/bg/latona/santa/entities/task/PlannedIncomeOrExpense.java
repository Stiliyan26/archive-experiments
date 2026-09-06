package bg.latona.santa.entities.task;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class PlannedIncomeOrExpense extends CompanyRecord {

	@ManyToOne
	private Article article;
	private BigDecimal ammount;
	@ManyToOne
	private Task task;
	
	public PlannedIncomeOrExpense() {};
	
	public PlannedIncomeOrExpense(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Article article, BigDecimal ammount, Task task) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.article = article;
		this.ammount = ammount;
		this.task = task;
	}
}
