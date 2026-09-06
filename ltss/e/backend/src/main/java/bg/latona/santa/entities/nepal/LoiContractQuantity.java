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
@ToString(exclude = {"powerPlants","powerPlantProtocols"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlants","powerPlantProtocols"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiContractQuantity extends ListOptionItem {

	public static final Long AMOUNT_PER_ELECTRICITY_METER = 1L;
	public static final Long SCHEDULE_QUANTITY = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiContractQuantity")
	private List<PowerPlant> powerPlants;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiContractQuantity")
	private List<PowerPlantProtocol> powerPlantProtocols;

	public LoiContractQuantity() {
	}

	public LoiContractQuantity(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
