package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fBreTransitions"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fBreTransitions"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiBreTransitionResult extends ListOptionItem {

	public static final Long BRE_TRANSITION_RESULT_WAITING = -1L;
	public static final Long BRE_TRANSITION_RESULT_OK = 0L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_CREATE_JOURNAL = 1L;
	public static final Long BRE_TRANSITION_RESULT_CHECK_RULES = 2L;
	public static final Long BRE_TRANSITION_RESULT_UNKNOWN_FUNCTION = 4L;
	public static final Long BRE_TRANSITION_RESULT_NO_EXCHANGE_RATE = 8L;
	public static final Long BRE_TRANSITION_RESULT_NO_BATCH_TYPE = 10L;
	public static final Long BRE_TRANSITION_RESULT_NO_LINK_BATCH = 11L;
	public static final Long BRE_TRANSITION_RESULT_NO_TTE = 16L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_CREATE_BATCH = 32L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_UPDATE_BATCH = 64L;
	public static final Long BRE_TRANSITION_RESULT_CHECK_COST_CENTERS = 100L;
	public static final Long BRE_TRANSITION_RESULT_MANUAL_PROCESS = 101L;
	public static final Long BRE_TRANSITION_RESULT_CHECK_BTE_TTE_TYPE = 102L;
	public static final Long BRE_TRANSITION_RESULT_REJECTED = 103L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_CREATE_BATCH_LINK = 128L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_UPDATE_BATCH_AMOUNT = 256L;
	public static final Long BRE_TRANSITION_RESULT_FAIL_UPDATE_LINK_BATCH_AMOUNT = 512L;
	public static final Long BRE_TRANSITION_RESULT_NO_BATCH_TYPE_RULE = 1024L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "result")
	private List<FBreTransition> fBreTransitions;

	public LoiBreTransitionResult() {
	}

	public LoiBreTransitionResult(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
