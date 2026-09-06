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
@ToString(exclude = {"fCtBatchJteDefaults","fCtBatchTypes","fCtInvDealTypes","fInvInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchJteDefaults","fCtBatchTypes","fCtInvDealTypes","fInvInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiBatchJteDefaultSide extends ListOptionItem {

	public static final Long BATCH_JTE_DEFAULT_SIDE_P = 1L;
	public static final Long BATCH_JTE_DEFAULT_SIDE_S = 2L;
	public static final Long BATCH_JTE_DEFAULT_SIDE_N = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "side")
	private List<FCtBatchJteDefault> fCtBatchJteDefaults;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "side")
	private List<FCtBatchType> fCtBatchTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "side")
	private List<FCtInvDealType> fCtInvDealTypes;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invType")
	private List<FInvInvoice> fInvInvoices;

	public LoiBatchJteDefaultSide() {
		super();
	}

	public LoiBatchJteDefaultSide(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}

