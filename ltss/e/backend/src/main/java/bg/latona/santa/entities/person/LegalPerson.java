package bg.latona.santa.entities.person;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.Expenditure;
import bg.latona.santa.entities.Income;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.allocation.AllocationOrigin;
import bg.latona.santa.entities.selfie.DocumentRange;
import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.nepal.EnergyBalancingContract;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.nepal.PowerPlantProtocol;
import bg.latona.santa.entities.offer.OfferToClient;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.Task;
import bg.latona.santa.entities.transport.Transport;
import bg.latona.santa.entities.transport.TransportOrder;
import bg.latona.santa.entities.wato.ImportedLegalPerson;
import bg.latona.santa.entities.wato.ImportedLegalPersonGroup;
import bg.latona.santa.entities.wato.ImportedSantaPartner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"contacts","tasks","importedLegalPerson","vendors","customers","expenditures","invoices"
		,"offerToClients","legalPersonAttachments","legalPersonComments","transports","transportOrders","transportOrderOrderers"
		,"transportOrderSenders","importedLegalPersonGroups","importedSantaPartners","bankAccounts","incomes","powerPlants","powerPlantProtocols","energyBalancingContracts", "documentRanges"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"contacts","tasks","importedLegalPerson","vendors","customers","expenditures","invoices"
		,"offerToClients","legalPersonAttachments","legalPersonComments","transports","transportOrders","transportOrderOrderers"
		,"transportOrderSenders","importedLegalPersonGroups","importedSantaPartners","bankAccounts","incomes","powerPlants","powerPlantProtocols","energyBalancingContracts", "documentRanges"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LegalPerson extends AllocationOrigin {

	private String name;
	private String eik;
	private String egn;
	private String vatNumber;
	private String mol;
	private String country;
	private String city;
	private String address;
	private String postCode;
	private String currentAddress;

	private String sapNumber;
	private String bulstat;


	private String sendersRecipientID;
	private String street;

	private Boolean isSender;

	@ManyToOne
	private LegalStatus legalStatus;

	@ManyToOne
	private LoiLegalPersonType legalPersonType;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<DocumentRange> documentRanges;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<Contact> contacts;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "counterParty")
	private List<Task> tasks;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalPerson")
	private List<ImportedLegalPerson> importedLegalPerson;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<Vendor> vendors;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<Customer> customers;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "expenseVendor")
	private List<Expenditure> expenditures;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoiceCounterParty")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<OfferToClient> offerToClients;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<LegalPersonAttachment> legalPersonAttachments;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "person")
	private List<LegalPersonComment> legalPersonComments;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orderer")
	private List<Transport> transports;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "transporter")
	private List<TransportOrder> transportOrders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orderOrderer")
	private List<TransportOrder> transportOrderOrderers;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "sender")
	private List<TransportOrder> transportOrderSenders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalPerson")
	private List<ImportedLegalPersonGroup> importedLegalPersonGroups;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalPerson")
	private List<ImportedSantaPartner> importedSantaPartners;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bankAccountOwner")
	private List<BankAccount> bankAccounts;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "incomePayer")
	private List<Income> incomes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "owner")
	private List<PowerPlant> powerPlants;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "owner")
	private List<PowerPlantProtocol> powerPlantProtocols;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "owner")
	private List<EnergyBalancingContract> energyBalancingContracts;
	
	public LegalPerson() {}
	
	public LegalPerson(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String eik, String egn, String vatNumber, String mol, String address, String postCode, 
			String currentAddress, String country, String city, LegalStatus legalStatus, LoiLegalPersonType legalPersonType) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.eik = eik;
		this.egn = egn;
		this.vatNumber = vatNumber;
		this.mol = mol;
		this.country = country;
		this.city = city;
		this.address = address;
		this.postCode = postCode;
		this.currentAddress = currentAddress;
		this.legalStatus = legalStatus;
		this.legalPersonType = legalPersonType;
	}
	
	public BigDecimal getQuantityToAllocate() {
		return new BigDecimal(-1);
	}
}
