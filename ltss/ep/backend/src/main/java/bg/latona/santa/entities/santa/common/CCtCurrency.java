package bg.latona.santa.entities.santa.common;

import bg.latona.santa.entities.santa.finance.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Data //auto-create getters and setters
@ToString(exclude = {"deliveries","cCtBankAccounts","cDeliveryOfferItems","cOffers","cOrders","cPmtCurrencyRates","cPriceLists","cSales"
,"cSaleDetails","fPtBatches","fPtJournals","fBreTransitions","fPtBatchCcDetails","fPtBatchLinks","fInvInvoices","fChartAccounts","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries","cCtBankAccounts","cDeliveryOfferItems","cOffers","cOrders","cPmtCurrencyRates","cPriceLists","cSales"
,"cSaleDetails","fPtBatches","fPtJournals","fBreTransitions","fPtBatchCcDetails","fPtBatchLinks","fInvInvoices","fChartAccounts","cInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CCtCurrency extends CompanyRecord {
	private String code;
	private String name;
	private Boolean isDefault;
	private String unitEn;
	private String fractionEn;
	private LocalDate activeFrom;
	private LocalDate activeTo;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CDelivery> deliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CCtBankAccount> cCtBankAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CDeliveryOfferItem> cDeliveryOfferItems;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<COffer> cOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<COrder> cOrders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<CPmtCurrencyRate> cPmtCurrencyRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CPriceList> cPriceLists;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CSale> cSales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<CSaleDetail> cSaleDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FPtBatch> fPtBatches;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FPtJournal> fPtJournals;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FPtBatchLink> fPtBatchLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cuyCode")
	private List<FInvInvoice> fInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<FChartAccount> fChartAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoiceCurrency")
	private List<CInvoice> cInvoices;
	
	public CCtCurrency() {
		super();
	}
	
	public CCtCurrency(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, String code, String name, Boolean isDefault, String unitEn,
			String fractionEn, LocalDate activeFrom, LocalDate activeTo) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.isDefault = isDefault;
		this.unitEn = unitEn;
		this.fractionEn = fractionEn;
		this.activeFrom = activeFrom;
		this.activeTo = activeTo;
	}

}
