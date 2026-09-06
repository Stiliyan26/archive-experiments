package bg.latona.santa.entities.santa.common;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"invoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"invoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class VendorInvoice extends CompanyRecord {

	private BigDecimal invoiceNum;
	private LocalDate invoiceDate;
	private String invoiceCode;
	private String invoiceDescription;
	private Boolean isExported;
	@ManyToOne
	private CCcPartner invoiceCounterParty;
	@ManyToOne
	private CCtCurrency invoiceCurrency;
	@Column(precision=19, scale=5)
	private BigDecimal invoiceExchangeRate;
	private BigDecimal taxBaseAmount;
	private BigDecimal taxAmount;
	private BigDecimal totalAmount;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoice")
	private List<VendorInvoiceRow> invoiceRows;


	//default empty constructor
	public VendorInvoice() {}

	//default constructor with all attributes
	public VendorInvoice(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			BigDecimal invoiceNum, LocalDate invoiceDate, String invoiceCode, String invoiceDescription, Boolean isExported,
			CCcPartner invoiceCounterParty, CCtCurrency invoiceCurrency, BigDecimal invoiceExchangeRate, BigDecimal taxBaseAmount,
			BigDecimal taxAmount, BigDecimal totalAmount) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.invoiceNum = invoiceNum;
		this.invoiceDate = invoiceDate;
		this.invoiceCode = invoiceCode;
		this.invoiceDescription = invoiceDescription;
		this.isExported = isExported;
		this.invoiceCounterParty = invoiceCounterParty;
		this.invoiceCurrency = invoiceCurrency;
		this.invoiceExchangeRate = invoiceExchangeRate;
		this.taxBaseAmount = taxBaseAmount;
		this.taxAmount = taxAmount;
		this.totalAmount = totalAmount;
	}
}
