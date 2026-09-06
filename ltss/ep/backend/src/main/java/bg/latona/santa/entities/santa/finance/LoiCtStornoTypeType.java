package bg.latona.santa.entities.santa.finance;

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
@ToString(exclude = {"fCtStornoTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtStornoTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiCtStornoTypeType extends ListOptionItem {

	public static final Long CT_STORNO_TYPE_TYPE_N = 1L;
	public static final Long CT_STORNO_TYPE_TYPE_R = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<FCtStornoType> fCtStornoTypes;

	public LoiCtStornoTypeType() {
	}

	public LoiCtStornoTypeType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}