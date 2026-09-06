package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.AgreementType;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.selfie.ElectricityInvoice;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.*;

import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;


@Data //auto-create getters and setters
@ToString(exclude = {"powerPlantProtocols","agreementsSelfInvoicing", "electricityInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlantProtocols","agreementsSelfInvoicing", "electricityInvoices"}) //avoid recursion by Lombok
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class PowerPlant extends CompanyRecord {

	private String name;

	private String traderEic;

	private String producerEic;

	private String address;

	private String contactPerson;

	private String accessPoint;

	@Column(precision = 19, scale = 5)
	private BigDecimal installedPowerMw;

	private String contractId;

	private LocalDate contractDate;

	private String annex;

	private LocalDate term;

	private String identificationNumber;

	private String distributionNetwork;

	private BigDecimal value;

	private BigDecimal valueSec;

	private BigDecimal minPriceMWh;
	private String externalNumber;

	@ManyToOne
	private LoiTypeOfPowerPlant type;

	@ManyToOne
	private LoiGrid grid;

	@ManyToOne
	private PowerPlantProfile powerPlantProfile;

	@ManyToOne
	private LegalPerson owner;

	@ManyToOne
	private LoiContractStatus contractStatus;

	@ManyToOne
	private LoiContractQuantity loiContractQuantity;

	@ManyToOne
	private LoiContractPrice loiContractPrice;

	@ManyToOne
	private LoiContractFee loiContractFee;

	@ManyToOne
	private LoiProtocolCountPerMonth loiProtocolCountPerMonth;

	@ManyToOne
	private LoiProtocolLineCount loiProtocolLineCount;

	@ManyToOne
	private AgreementType agreementType;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<PowerPlantProtocol> powerPlantProtocols;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<AgreementSelfInvoicing> agreementsSelfInvoicing;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<ElectricityInvoice> electricityInvoices;

    public PowerPlant(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String traderEic, String producerEic, String address, String contactPerson, String accessPoint, 
			BigDecimal installedPowerMw, String contractId, LocalDate contractDate, String annex, LocalDate term, 
			String identificationNumber, String distributionNetwork, BigDecimal value, BigDecimal valueSec, BigDecimal minPriceMWh, 
			String externalNumber, LoiTypeOfPowerPlant type, LoiGrid grid, PowerPlantProfile powerPlantProfile, LegalPerson owner, 
			LoiContractStatus contractStatus, LoiContractQuantity loiContractQuantity, LoiContractPrice loiContractPrice, 
			LoiContractFee loiContractFee, LoiProtocolCountPerMonth loiProtocolCountPerMonth, 
			LoiProtocolLineCount loiProtocolLineCount, AgreementType agreementType) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.name = name;
        this.traderEic = traderEic;
        this.producerEic = producerEic;
        this.address = address;
        this.contactPerson = contactPerson;
        this.accessPoint = accessPoint;
        this.installedPowerMw = installedPowerMw;
        this.contractId = contractId;
        this.contractDate = contractDate;
        this.annex = annex;
        this.term = term;
        this.identificationNumber = identificationNumber;
        this.distributionNetwork = distributionNetwork;
        this.value = value;
        this.valueSec = valueSec;
        this.minPriceMWh = minPriceMWh;
        this.externalNumber = externalNumber;
        this.type = type;
        this.grid = grid;
        this.powerPlantProfile = powerPlantProfile;
        this.owner = owner;
        this.contractStatus = contractStatus;
        this.loiContractQuantity = loiContractQuantity;
        this.loiContractPrice = loiContractPrice;
        this.loiContractFee = loiContractFee;
        this.loiProtocolCountPerMonth = loiProtocolCountPerMonth;
        this.loiProtocolLineCount = loiProtocolLineCount;
        this.agreementType = agreementType;
    }
}
