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
@ToString(exclude = {"fPtJournals"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtJournals"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiPtJournalCcStatus extends ListOptionItem {

	public static final Long PT_JOURNAL_CC_STATUS_U = 1L;
	public static final Long PT_JOURNAL_CC_STATUS_P = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ccStatus")
	private List<FPtJournal> fPtJournals;

	public LoiPtJournalCcStatus() {
		super();
	}

	public LoiPtJournalCcStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
