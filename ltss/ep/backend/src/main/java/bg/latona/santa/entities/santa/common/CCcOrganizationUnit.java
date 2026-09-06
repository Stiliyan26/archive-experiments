package bg.latona.santa.entities.santa.common;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import bg.latona.santa.entities.santa.finance.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"deliveries","outIdDeliveries","blocks","cCcGoodsTypes","cCcPartners","cCfgDocumentPatterns","cCtBankAccounts",
		"cCtPartnerGroups","cCtPartnerGroupsOutMask","cDeliveryOffers","cGoodMarks","cGoods","cOffers","cOrders","cPmtCurrencyRates","cPriceLists"
,"cRequests","outIdCRequests","cReserveQuantities","cSales","outIdCSales","cServices","cStocks","fChartAccounts","fCtJteDefaults","fDiscounts"
,"fJournalTypes","cCcOrganizationUnits","fCtRepresentatives","fInvDdsFiles","fCtBatchTypeRules","fCtRules","fCtBatchTteTypes","fCtBatchTteLinks","fCtBatchJteDefaults",
"fCtGoods","fPtJournals","fCtContractTypes","fCcContracts","fPtBatches","fCcEbks","fCcFinsources","fCcFunctions","fCcPrograms","fCcReserve1s","fCcReserve2s","fPtPostings",
"ccOutIdFPtPostings","fBreTransitions","ccOutIdFBreTransitions","ccOutIdFPtBatchCcDetails","fPtBatchCcDetails","fPtClosingAccounts","fHpCcBalances","ccOutIdFHpCcBalances"
,"fPtCcBalances","ccOutIdFPtCcBalances","fInvInvoices","ccOutIdFInvInvoices","fPtCoaBalances","fCtStornoTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries","outIdDeliveries","blocks","cCcGoodsTypes","cCcPartners","cCfgDocumentPatterns","cCtBankAccounts",
		"cCtPartnerGroups","cCtPartnerGroupsOutMask","cDeliveryOffers","cGoodMarks","cGoods","cOffers","cOrders","cPmtCurrencyRates","cPriceLists"
,"cRequests","outIdCRequests","cReserveQuantities","cSales","outIdCSales","cServices","cStocks","fChartAccounts","fCtJteDefaults","fDiscounts"
,"fJournalTypes","cCcOrganizationUnits","fCtRepresentatives","fInvDdsFiles","fCtBatchTypeRules","fCtRules","fCtBatchTteTypes","fCtBatchTteLinks","fCtBatchJteDefaults",
"fCtGoods","fPtJournals","fCtContractTypes","fCcContracts","fPtBatches","fCcEbks","fCcFinsources","fCcFunctions","fCcPrograms","fCcReserve1s","fCcReserve2s","fPtPostings",
"ccOutIdFPtPostings","fBreTransitions","ccOutIdFBreTransitions","ccOutIdFPtBatchCcDetails","fPtBatchCcDetails","fPtClosingAccounts","fHpCcBalances","ccOutIdFHpCcBalances"
,"fPtCcBalances","ccOutIdFPtCcBalances","fInvInvoices","ccOutIdFInvInvoices","fPtCoaBalances","fCtStornoTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"company_id", "code"}),@UniqueConstraint(columnNames = {"company_id", "bulstat"})})
public class CCcOrganizationUnit extends CompanyRecord {

	private String code;
	private String name;
	private String abbreviation;
	private String bulstat;
	private String address;
	private String organizationUnitType;
	@ManyToOne
	private CCcOrganizationUnit outId;
	private String sebraCode;//this is no longer used and needed
	private LocalDate activeFrom;
	private LocalDate activeTo;
	private String vatNumber;
	private LocalDate ddsRegDate;
	private LocalDate ddsCloseRegDate;
	private String mainOutCode;
	private String nameEn;
	private String addressEn;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CDelivery> deliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outId")
	private List<CDelivery> outIdDeliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CBlock> blocks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CCcGoodsType> cCcGoodsTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CCcPartner> cCcPartners;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CCfgDocumentPattern> cCfgDocumentPatterns;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CCtBankAccount> cCtBankAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CCtPartnerGroup> cCtPartnerGroups;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outMask")
	private List<CCtPartnerGroup> cCtPartnerGroupsOutMask;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CDeliveryOffer> cDeliveryOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CGoodMark> cGoodMarks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CGoods> cGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<COffer> cOffers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<COrder> cOrders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CPmtCurrencyRate> cPmtCurrencyRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CPriceList> cPriceLists;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CRequest> cRequests;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outId")
	private List<CRequest> outIdCRequests;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outId")
	private List<CReserveQuantity> cReserveQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CSale> cSales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outId")
	private List<CSale> outIdCSales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CService> cServices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<CStock> cStocks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FChartAccount> fChartAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtJteDefault> fCtJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FDiscount> fDiscounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FJournalType> fJournalTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outId")
	private List<CCcOrganizationUnit> cCcOrganizationUnits;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtRepresentative> fCtRepresentatives;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FInvDdsFile> fInvDdsFiles;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtBatchTypeRule> fCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtRule> fCtRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtBatchTteType> fCtBatchTteTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtBatchTteLink> fCtBatchTteLinks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtBatchJteDefault> fCtBatchJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtGood> fCtGoods;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtJournal> fPtJournals;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtContractType> fCtContractTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcContract> fCcContracts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtBatch> fPtBatches;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcEbk> fCcEbks;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcFinsource> fCcFinsources;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcFunction> fCcFunctions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcProgram> fCcPrograms;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcReserve1> fCcReserve1s;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCcReserve2> fCcReserve2s;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtPosting> fPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FPtPosting> ccOutIdFPtPostings;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FBreTransition> ccOutIdFBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FPtBatchCcDetail> ccOutIdFPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtClosingAccount> fPtClosingAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FHpCcBalance> fHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FHpCcBalance> ccOutIdFHpCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtCcBalance> fPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FPtCcBalance> ccOutIdFPtCcBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FInvInvoice> fInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccOutId")
	private List<FInvInvoice> ccOutIdFInvInvoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FPtCoaBalance> fPtCoaBalances;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "outCode")
	private List<FCtStornoType> fCtStornoTypes;

	public CCcOrganizationUnit() {
		super();
	}

	public CCcOrganizationUnit(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, String code, String name, String abbreviation,
			String bulstat, String address, String organizationUnitType, CCcOrganizationUnit outId, String sebraCode,
			LocalDate activeFrom, LocalDate activeTo, String vatNumber, LocalDate ddsRegDate, LocalDate ddsCloseRegDate, String mainOutCode,
			String nameEn, String addressEn) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.abbreviation = abbreviation;
		this.bulstat = bulstat;
		this.address = address;
		this.organizationUnitType = organizationUnitType;
		this.outId = outId;
		this.sebraCode = sebraCode;
		this.activeFrom = activeFrom;
		this.activeTo = activeTo;
		this.vatNumber = vatNumber;
		this.ddsRegDate = ddsRegDate;
		this.ddsCloseRegDate = ddsCloseRegDate;
		this.mainOutCode = mainOutCode;
		this.nameEn = nameEn;
		this.addressEn = addressEn;
	}

}
