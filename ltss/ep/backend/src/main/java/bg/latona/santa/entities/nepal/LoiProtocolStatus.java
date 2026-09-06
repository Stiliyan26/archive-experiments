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
@ToString(exclude = {"powerPlantProtocols"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlantProtocols"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiProtocolStatus extends ListOptionItem {

	public static final Long THERE_IS_NO_PROTOCOL = 1L;
	public static final Long GENERATED_PROTOCOL = 2L;
	public static final Long DOWNLOADED_PROTOCOL = 3L;
	public static final Long SIGNED_PROTOCOL = 4L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiProtocolStatus")
	private List<PowerPlantProtocol> powerPlantProtocols;


	public LoiProtocolStatus() {
	}

	public LoiProtocolStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
