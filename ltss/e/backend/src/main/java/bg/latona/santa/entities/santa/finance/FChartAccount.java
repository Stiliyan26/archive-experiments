package bg.latona.santa.entities.santa.finance;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import bg.latona.santa.entities.security.SecUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;

@Data //auto-create getters and setters
@ToString(exclude = {"fCtJteDefaults","coaCtFCtJteDefaults","subAccountsList","coaIdCtfCtBatchTypeRules","coaIdDtfCtBatchTypeRules","fPtPostings",
"coaIdDtFPtPostings","fPtClosingAccounts","fHpCcBalances","fPtCcBalances","fPtCoaBalances"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtJteDefaults","coaCtFCtJteDefaults","subAccountsList","coaIdCtfCtBatchTypeRules","coaIdDtfCtBatchTypeRules","fPtPostings",
"coaIdDtFPtPostings","fPtClosingAccounts","fHpCcBalances","fPtCcBalances","fPtCoaBalances"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FChartAccount extends CompanyRecord{
	
	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String code;
	private String name;
	@ManyToOne
	private CCtCurrency currency;
	private LocalDate dateOpened;
	private LocalDate dateClosed;//defaults to 2100 year
	private Boolean isForeignCurrency;
	@ManyToOne
	private LoiTypeOfFinancialAccount isSynthetic;
	//private Boolean isActive;// not used column //if enabled, then it should have a list of values, like isSynthetic
	@ManyToOne
	private LoiAccountActivityType isBalanced;
	//cost center setting per account
	private Boolean ebkRequired;
	private Boolean funRequired;
	private Boolean prmRequired;
	private Boolean fieRequired;
	private Boolean parRequired;
	private Boolean gteRequired;
	private Boolean cotRequired;
	private Boolean re1Required;
	private Boolean re2Required;
	private Boolean outRequired;
	@ManyToOne
	private FChartAccount coaIdUp;
	// not used field abe_id - reference to ct_account_balances - no, to accounting.ct_acc_balances

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaDt")
	private List<FCtJteDefault> fCtJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaCt")
	private List<FCtJteDefault> coaCtFCtJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaIdUp")
	private List<FChartAccount> subAccountsList;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaIdCt")
	private List<FCtBatchTypeRule> coaIdCtfCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaIdDt")
	private List<FCtBatchTypeRule> coaIdDtfCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaIdCt")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaIdDt")
	private List<FPtPosting> coaIdDtFPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaId")
	private List<FPtClosingAccount> fPtClosingAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaId")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaId")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "coaId")
	private List<FPtCoaBalance> fPtCoaBalances;

	public FChartAccount(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, String code, String name,
			CCtCurrency currency, LocalDate dateOpened, LocalDate dateClosed, Boolean isForeignCurrency,
			LoiTypeOfFinancialAccount isSynthetic, LoiAccountActivityType isBalanced, Boolean ebkRequired,
			Boolean funRequired, Boolean prmRequired, Boolean fieRequired, Boolean parRequired, Boolean gteRequired,
			Boolean cotRequired, Boolean re1Required, Boolean re2Required, Boolean outRequired, FChartAccount coaIdUp) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.code = code;
		this.name = name;
		this.currency = currency;
		this.dateOpened = dateOpened;
		this.dateClosed = dateClosed;
		this.isForeignCurrency = isForeignCurrency;
		this.isSynthetic = isSynthetic;
		this.isBalanced = isBalanced;
		this.ebkRequired = ebkRequired;
		this.funRequired = funRequired;
		this.prmRequired = prmRequired;
		this.fieRequired = fieRequired;
		this.parRequired = parRequired;
		this.gteRequired = gteRequired;
		this.cotRequired = cotRequired;
		this.re1Required = re1Required;
		this.re2Required = re2Required;
		this.outRequired = outRequired;
		this.coaIdUp = coaIdUp;
	}
}
