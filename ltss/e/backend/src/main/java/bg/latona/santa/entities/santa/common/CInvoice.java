package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.invoice.LoiPaymentType;
import bg.latona.santa.entities.invoice.LoiVatExemptionReason;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"cInvoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cInvoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CInvoice extends CommonRecord {

	private BigDecimal invoiceNum;
	private LocalDate invoiceDate;
	private String invoiceCode;
	private String invoiceDescription;
	private Boolean isIssued;
	private Boolean isExported;
	@ManyToOne
	private CCcPartner invoiceCounterParty;
	@ManyToOne
	private LoiPaymentType paymentType;
	@ManyToOne
	private CCtBankAccount bankAccount;
	@ManyToOne
	private LoiVatExemptionReason vatExemptionReason;
	@ManyToOne
	private CCtCurrency invoiceCurrency;
	private BigDecimal taxBaseAmount;
	private BigDecimal taxAmount;
	private BigDecimal totalAmount;
	@ManyToOne
	private SecUser issuedBy;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoice")
	private List<CInvoiceRow> cInvoiceRows;

	public CInvoice() {
	}

	public CInvoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
					boolean calculateOnly, BigDecimal invoiceNum, LocalDate invoiceDate, String invoiceCode,
					String invoiceDescription, Boolean isIssued, Boolean isExported, CCcPartner invoiceCounterParty,
					LoiPaymentType paymentType, CCtBankAccount bankAccount, LoiVatExemptionReason vatExemptionReason,
					CCtCurrency invoiceCurrency, BigDecimal taxBaseAmount, BigDecimal taxAmount, BigDecimal totalAmount, SecUser issuedBy) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly);
		this.invoiceNum = invoiceNum;
		this.invoiceDate = invoiceDate;
		this.invoiceCode = invoiceCode;
		this.invoiceDescription = invoiceDescription;
		this.isIssued = isIssued;
		this.isExported = isExported;
		this.invoiceCounterParty = invoiceCounterParty;
		this.paymentType = paymentType;
		this.bankAccount = bankAccount;
		this.vatExemptionReason = vatExemptionReason;
		this.invoiceCurrency = invoiceCurrency;
		this.taxBaseAmount = taxBaseAmount;
		this.taxAmount = taxAmount;
		this.totalAmount = totalAmount;
		this.issuedBy = issuedBy;
	}
}
