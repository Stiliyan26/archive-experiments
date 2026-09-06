package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.person.LegalPerson;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Objects;


@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Builder
@Entity //JPA persisted class
public class DocumentRange extends CompanyRecord {

    @Column
    private Integer rangeFrom; // >=

    @Column
    private Integer rangeTo; // <=

    @Column(columnDefinition = "boolean default false")
    private Boolean isActive;

    @Column
    private Integer current;

    @ManyToOne
    private LegalPerson person;


    @PreUpdate
    public void setIsActiveIfCurrentReachesRangeTo() { // if ranges are all used -> make it inactive range
        if (Objects.equals(this.current, this.rangeTo)) {
            this.isActive = false;
        }
    }


    @PrePersist
    public void setInitialCurrentValue() { // set the current initial value to the range from - 1
        if (this.rangeFrom != null)
            this.current = this.rangeFrom - 1;
    }
}
