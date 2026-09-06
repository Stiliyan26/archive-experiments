package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class CInvoiceRow extends CompanyRecord {

	@ManyToOne
	private CInvoice invoice;
	@ManyToOne
	private CGoods article;
	private String invoiceRowDescription;
	private BigDecimal quantity;
	private BigDecimal priceRate;


	public CInvoiceRow() {}

	public CInvoiceRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
					   boolean calculateOnly, ManagedCompany company, CInvoice invoice, CGoods article,
					   String invoiceRowDescription, BigDecimal quantity, BigDecimal priceRate) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.invoice = invoice;
		this.article = article;
		this.invoiceRowDescription = invoiceRowDescription;
		this.quantity = quantity;
		this.priceRate = priceRate;
	}
}
