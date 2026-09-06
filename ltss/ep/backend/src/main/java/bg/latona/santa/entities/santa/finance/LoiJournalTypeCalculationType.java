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
@ToString(exclude = {"fJournalTypes"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fJournalTypes"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiJournalTypeCalculationType extends ListOptionItem {

	public static final Long JOURNAL_TYPE_CALCULATION_TYPE_L = 1L;
	public static final Long JOURNAL_TYPE_CALCULATION_TYPE_P = 2L;
	public static final Long JOURNAL_TYPE_CALCULATION_TYPE_S = 3L;
	public static final Long JOURNAL_TYPE_CALCULATION_TYPE_E = 4L;
	public static final Long JOURNAL_TYPE_CALCULATION_TYPE_N = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "type")
	private List<FJournalType> fJournalTypes;

	public LoiJournalTypeCalculationType() {
		super();
	}

	public LoiJournalTypeCalculationType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
