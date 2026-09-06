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
@ToString(exclude = {"fCtBatchTypeRules","fBreTransitions","fPtBatchCcDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTypeRules","fBreTransitions","fPtBatchCcDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiBatchTypeRuleDependenceType extends ListOptionItem {

	public static final Long BATCH_TYPE_RULE_DEPENDENCE_TYPE_BAK = 1L;
	public static final Long BATCH_TYPE_RULE_DEPENDENCE_TYPE_CAH = 2L;
	public static final Long BATCH_TYPE_RULE_DEPENDENCE_TYPE_PRT = 3L;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dependenceType")
	private List<FCtBatchTypeRule> fCtBatchTypeRules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dependenceType")
	private List<FBreTransition> fBreTransitions;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "dependenceType")
	private List<FPtBatchCcDetail> fPtBatchCcDetails;
	
	public LoiBatchTypeRuleDependenceType() {
		super();
	}
	
	public LoiBatchTypeRuleDependenceType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}

