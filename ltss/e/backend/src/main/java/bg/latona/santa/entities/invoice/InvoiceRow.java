package bg.latona.santa.entities.invoice;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.transport.Transport;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class InvoiceRow extends CompanyRecord {

	@ManyToOne
	private Invoice invoice;
	@ManyToOne
	private Article article;
	private String invoiceRowDescription;
	private BigDecimal quantity;
	private BigDecimal priceRate;
	@ManyToOne
	private Transport transport;


	//default empty constructor
	public InvoiceRow() {}

	//default constructor with all attributes
	public InvoiceRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			Invoice invoice, Article article, String invoiceRowDescription, BigDecimal quantity, BigDecimal priceRate, Transport transport) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.invoice = invoice;
		this.article = article;
		this.invoiceRowDescription = invoiceRowDescription;
		this.quantity = quantity;
		this.priceRate = priceRate;
		this.transport = transport;
	}
}
