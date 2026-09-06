package bg.latona.santa.entities.article;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class ArticlePriceRate extends CompanyRecord {
	
	private String vendor;
	@ManyToOne
	private Article article;
	private BigDecimal price;
	@ManyToOne
	private Currency currency;
	private LocalDate validFromDate;
	private LocalDate validToDate;
	
	public ArticlePriceRate() {};
	
	public ArticlePriceRate(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String vendor, Article article,
			BigDecimal price, Currency currency, LocalDate validFromDate, LocalDate validToDate) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.vendor = vendor;
		this.article = article;
		this.price = price;
		this.currency = currency;
		this.validFromDate = validFromDate;
		this.validToDate = validToDate;
	}
}
