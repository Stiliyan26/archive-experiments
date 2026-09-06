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
@ToString(exclude = {"fCtBatchTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiBatchCalculationType extends ListOptionItem {

	public static final Long BATCH_CALCULATION_TYPE_N = 1L;
	public static final Long BATCH_CALCULATION_TYPE_L = 2L;
	public static final Long BATCH_CALCULATION_TYPE_C = 3L;
	public static final Long BATCH_CALCULATION_TYPE_P = 4L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<FCtBatchType> fCtBatchTypes;

	public LoiBatchCalculationType() {
		super();
	}

	public LoiBatchCalculationType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
