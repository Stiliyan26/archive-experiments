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
public class ImportedInvoicePayment extends CompanyRecord {

	@ManyToOne
	private ImportedInvoice invoice;
	@ManyToOne
	private ImportedLegalPerson contragent;
	private String foreignId;
	private String docNum;
	private Date docDate;
	private String companyName;
	private String foreignContragentId;
	private String orderNumber;
	private BigDecimal totalAmount;
	private BigDecimal totalNoVAT;
	private BigDecimal totalPayed;
	private Boolean foreignDeleted;
	private Integer compId;


	//default empty constructor
	public ImportedInvoicePayment() {}

	//default constructor with all attributes
	public ImportedInvoicePayment(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			ImportedInvoice invoice, ImportedLegalPerson contragent, String foreignId,
			String docNum, Date docDate, String companyName, String foreignContragentId, String orderNumber,
			BigDecimal totalAmount, BigDecimal totalNoVAT, BigDecimal totalPayed, Boolean foreignDeleted,
			Integer compId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.invoice = invoice;
		this.contragent = contragent;
		this.foreignId = foreignId;
		this.docNum = docNum;
		this.docDate = docDate;
		this.companyName = companyName;
		this.foreignContragentId = foreignContragentId;
		this.orderNumber = orderNumber;
		this.totalAmount = totalAmount;
		this.totalNoVAT = totalNoVAT;
		this.totalPayed = totalPayed;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
	}
}
