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
@ToString(exclude = {"fPtBatchLinksFlag1","fPtBatchLinksFlag2"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtBatchLinksFlag1","fPtBatchLinksFlag2"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiLinkedFlag extends ListOptionItem {

	public static final Long LINKED_SIDE_L = 1L;
	public static final Long LINKED_SIDE_P = 2L;
	public static final Long LINKED_SIDE_R = 3L;
	public static final Long LINKED_SIDE_D = 4L;
	public static final Long LINKED_SIDE_I = 5L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "flag1")
	private List<FPtBatchLink> fPtBatchLinksFlag1;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "flag2")
	private List<FPtBatchLink> fPtBatchLinksFlag2;

	public LoiLinkedFlag() {
	}

	public LoiLinkedFlag(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
