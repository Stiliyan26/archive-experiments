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
@ToString(exclude = {"fCtBatchTypeRules"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTypeRules"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiBatchTypeRuleAmountType extends ListOptionItem {

    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_0 = 1L;
    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_1 = 2L;
    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_2 = 3L;
    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_3 = 4L;
    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_4 = 5L;
    public static final Long BATCH_TYPE_RULE_AMOUNT_TYPE_5 = 6L;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "amountType")
    private List<FCtBatchTypeRule> fCtBatchTypeRules;

    public LoiBatchTypeRuleAmountType() {
        super();
    }

    public LoiBatchTypeRuleAmountType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
    }
}

