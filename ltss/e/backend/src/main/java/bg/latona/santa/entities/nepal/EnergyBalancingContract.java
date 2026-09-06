package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.person.LegalPerson;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class EnergyBalancingContract extends CompanyRecord {

	private String contractId;
	private LocalDate contractDate;
	private String annex;
	private LocalDate term;
	private String identificationNumber;
	private String distributionNetwork;
	private String condition;
	private String name;
	private String traderEic;
	private String producerEic;
	private String address;
	private String contactPerson;
	private String accessPoint;
	@Column(precision = 19, scale = 5)
	private BigDecimal installedPowerMw;
	private BigDecimal value;
	private BigDecimal valueSec;
	private BigDecimal minPriceMWh;

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
	private LoiContractPrice loiContractPrice;
	@ManyToOne
	private LoiContractFee loiContractFee;

}
