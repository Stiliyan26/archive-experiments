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
public class LoiMaxLoadMWSeason extends ListOptionItem {

	public static final Long SPRING = 1L;
	public static final Long SUMMER = 2L;
	public static final Long AUTUMN = 3L;
	public static final Long WINTER = 4L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiMaxLoadMWSeason")
	private List<PowerPlantProfile> powerPlantProfiles;

	public LoiMaxLoadMWSeason() {
	}

	public LoiMaxLoadMWSeason(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
