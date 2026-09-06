package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CService;
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
@ToString(exclude = {"fCtRepresentatives"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtRepresentatives"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiRepresentativeType extends ListOptionItem {

    public static final Long REPRESENTATIVE_TYPE_A = 1L;
    public static final Long REPRESENTATIVE_TYPE_R = 2L;
    public static final Long REPRESENTATIVE_TYPE_M = 3L;
    public static final Long REPRESENTATIVE_TYPE_C = 4L;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "type")
    private List<FCtRepresentative> fCtRepresentatives;

    public LoiRepresentativeType() {
        super();
    }

    public LoiRepresentativeType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
    }
}

