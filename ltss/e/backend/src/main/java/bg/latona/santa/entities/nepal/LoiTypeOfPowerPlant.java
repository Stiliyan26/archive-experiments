package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"powerPlants","energyBalancingContracts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlants","energyBalancingContracts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiTypeOfPowerPlant extends ListOptionItem {

	public static final Long SOLAR_POWER_PLANT = 914050000L;
	public static final Long WIND_FARM_POWER_PLANT = 914050001L;
	public static final Long HYDROELECTRIC_POWER_PLANT = 914050002L;
	public static final Long BIOMASS_POWER_PLANT = 914050003L;
	public static final Long COGENERATION_POWER_PLANT = 914050004L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<PowerPlant> powerPlants;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<EnergyBalancingContract> energyBalancingContracts;

	public LoiTypeOfPowerPlant() {
	}

	public LoiTypeOfPowerPlant(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
