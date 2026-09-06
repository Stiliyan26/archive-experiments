package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class VendorInvoiceRow extends AllocationOrigin {

	@ManyToOne
	private VendorInvoice invoice;
	@ManyToOne
	private CGoods goods;
	private String invoiceRowDescription;
	private BigDecimal quantity;
	private BigDecimal priceRate;


	//default empty constructor
	public VendorInvoiceRow() {}

	//default constructor with all attributes
	public VendorInvoiceRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			VendorInvoice invoice, CGoods goods, String invoiceRowDescription, BigDecimal quantity, BigDecimal priceRate) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.invoice = invoice;
		this.goods = goods;
		this.invoiceRowDescription = invoiceRowDescription;
		this.quantity = quantity;
		this.priceRate = priceRate;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return getQuantity();
	}
}
