package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.List;


@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Builder
@Entity //JPA persisted class
public class AgreementTypeMapping extends CompanyRecord {

    @ManyToMany
    private List<AgreementType> agreementTypes;

    @Column
    private BigDecimal crmCode;

    @Column
    private String description;
}
