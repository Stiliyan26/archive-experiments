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
@ToString(exclude = {"powerPlantProfiles"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlantProfiles"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiMaxLoadWeather extends ListOptionItem {

	public static final Long SUNNY = 1L;
	public static final Long CLOUDY = 2L;
	public static final Long FOG = 3L;
	public static final Long RAINING = 4L;
	public static final Long SNOWING = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiMaxLoadMWWeather")
	private List<PowerPlantProfile> powerPlantProfiles;

	public LoiMaxLoadWeather() {
	}

	public LoiMaxLoadWeather(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
