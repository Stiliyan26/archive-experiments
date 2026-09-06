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
@ToString(exclude = {"fPtBatchCcDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtBatchCcDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiPtBatchCcDetailStatus extends ListOptionItem {

	public static final Long PT_BATCH_CC_DETAIL_STATUS_C = 1L;
	public static final Long PT_BATCH_CC_DETAIL_STATUS_A = 2L;
	public static final Long PT_BATCH_CC_DETAIL_STATUS_F = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "status")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;

	public LoiPtBatchCcDetailStatus() {
	}

	public LoiPtBatchCcDetailStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
