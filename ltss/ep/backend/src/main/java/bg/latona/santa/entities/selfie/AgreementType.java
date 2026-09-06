package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;


@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
@Builder
public class AgreementType extends CompanyRecord {

    @Column
    private String code;

    @Column
    private String description;

    @Column
    private String text;

    @Column
    private Boolean isValid;
}
