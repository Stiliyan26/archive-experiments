package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.entities.person.LegalPerson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data //auto-create getters and setters
@ToString(exclude = {"powerPlantProtocols","agreementsSelfInvoicing", "electricityInvoices", "importValues", "importQuantities", "importValueAndQuantities"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlantProtocols","agreementsSelfInvoicing", "electricityInvoices", "importValues", "importQuantities", "importValueAndQuantities"}) //avoid recursion by Lombok
@AllArgsConstructor
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

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<ImportValue> importValues;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<ImportQuantity> importQuantities;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlant")
	private List<ImportValueAndQuantity> importValueAndQuantities;
}
