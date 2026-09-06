package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"quarterOfHours","powerPlants","energyBalancingContracts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"quarterOfHours","powerPlants","energyBalancingContracts"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class PowerPlantProfile extends CompanyRecord {


	private String name;
	@ManyToOne
	private LoiMaxLoadWeather loiMaxLoadMWWeather;
	@ManyToOne
	private LoiMaxLoadMWSeason loiMaxLoadMWSeason;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlantProfile")
	private List<QuarterOfHour> quarterOfHours;


	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlantProfile")
	private List<PowerPlant> powerPlants;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "powerPlantProfile")
	private List<EnergyBalancingContract> energyBalancingContracts;
}
