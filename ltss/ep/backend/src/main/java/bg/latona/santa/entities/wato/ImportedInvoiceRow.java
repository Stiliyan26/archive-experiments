package bg.latona.santa.entities.wato;

import java.math.BigDecimal;
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
public class ImportedInvoiceRow extends CompanyRecord {

	@ManyToOne
	private ImportedOrderRow orderRow;
	@ManyToOne
	private ImportedInvoice invoice;
	private String foreignId;
	private String foreignOrderRowId;
	private String foreignInvoiceId;
	private BigDecimal quantity;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;


	//default empty constructor
	public ImportedInvoiceRow() {}

	//default constructor with all attributes
	public ImportedInvoiceRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			ImportedOrderRow orderRow, ImportedInvoice invoice, String foreignId, String foreignOrderRowId, String foreignInvoiceId,
			BigDecimal quantity, Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.orderRow = orderRow;
		this.invoice = invoice;
		this.foreignId = foreignId;
		this.foreignOrderRowId = foreignOrderRowId;
		this.foreignInvoiceId = foreignInvoiceId;
		this.quantity = quantity;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
