package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fPtBatches","dfeId2FPtBatches"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtBatches","dfeId2FPtBatches"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FInvDdsFile extends CompanyRecord {

    @ManyToOne
    private CCcOrganizationUnit outCode; //NOT NULL,
    private String period; //NOT NULL,
    @ManyToOne
    private FCtRepresentative reeId; //NOT NULL,
    @ManyToOne
    private LoiInvDdsFileStatus status; //NOT NULL,
    private BigDecimal vatPayIn;
    private BigDecimal vatRestore1;
    private BigDecimal vatRestore3;
    private BigDecimal vatRestore4;
    private byte[] salesFile;
    private byte[] purchaseFile;
    private byte[] viesFile;
    private byte[] deklarationFile;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "dfeId")
    private List<FPtBatch> fPtBatches;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "dfeId2")
    private List<FPtBatch> dfeId2FPtBatches;

}
