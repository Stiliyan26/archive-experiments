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
@ToString(exclude = {"fInvDdsFiles"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fInvDdsFiles"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiInvDdsFileStatus extends ListOptionItem {

    public static final Long INV_DDS_FILE_STATUS_C = 1L;
    public static final Long INV_DDS_FILE_STATUS_G = 2L;
    public static final Long INV_DDS_FILE_STATUS_F = 3L;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "status")
    private List<FInvDdsFile> fInvDdsFiles;

    public LoiInvDdsFileStatus() {
        super();
    }

    public LoiInvDdsFileStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
    }
}

