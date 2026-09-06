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
@ToString(exclude = {"powerPlants"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlants"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiProtocolCountPerMonth extends ListOptionItem {

	public static final Long FOR_A_WHOLE_MONTH = 1L;
	public static final Long FOR_FIFTEEN_DAYS = 2L;
	public static final Long FOR_TEN_DAYS = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiProtocolCountPerMonth")
	private List<PowerPlant> powerPlants;

	public LoiProtocolCountPerMonth() {
	}

	public LoiProtocolCountPerMonth(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
