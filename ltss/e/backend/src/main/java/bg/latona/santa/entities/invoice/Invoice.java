package bg.latona.santa.entities.invoice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"invoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"invoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Invoice extends AllocationOrigin {

	private BigDecimal invoiceNum;
	private LocalDate invoiceDate;
	private String invoiceCode;
	private String invoiceDescription;
	private Boolean isIssued;
	private Boolean isExported;
	@ManyToOne
	private LegalPerson invoiceCounterParty;
	@ManyToOne
	private LoiPaymentType paymentType;
	@ManyToOne
	private BankAccount bankAccount;
	@ManyToOne
	private LoiVatExemptionReason vatExemptionReason;
	@ManyToOne
	private Currency invoiceCurrency;
	private BigDecimal taxBaseAmount;
	private BigDecimal taxAmount;
	private BigDecimal totalAmount;
	@ManyToOne
	private SecUser issuedBy;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoice")
	private List<InvoiceRow> invoiceRows;


	//default empty constructor
	public Invoice() {}

	//default constructor with all attributes
	public Invoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			BigDecimal invoiceNum, LocalDate invoiceDate, String invoiceCode, String invoiceDescription, Boolean isIssued, Boolean isExported,
			LegalPerson invoiceCounterParty, LoiPaymentType paymentType, BankAccount bankAccount,
			LoiVatExemptionReason vatExemptionReason, Currency invoiceCurrency, BigDecimal taxBaseAmount,
			BigDecimal taxAmount, BigDecimal totalAmount, SecUser issuedBy) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
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
