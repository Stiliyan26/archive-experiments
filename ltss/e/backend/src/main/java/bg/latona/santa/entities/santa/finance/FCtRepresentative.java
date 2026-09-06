package bg.latona.santa.entities.santa.finance;


import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fInvDdsFiles"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fInvDdsFiles"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtRepresentative extends CompanyRecord {

    @ManyToOne
    private CCcOrganizationUnit outCode; //NOT NULL,
    private String ideNo; //NOT NULL,
    private String name; //NOT NULL,
    private String city; //NOT NULL,
    @Column(length = 3000)
    private String address;
    private String zipCode;
    @ManyToOne
    private LoiRepresentativeType type;
    private String position;
    private String phone;
    private String mobile1;
    private String mobile2;
    private String email;
    @Column(length = 3000)
    private String notes;
    private String country;
    @ManyToOne
    private CCcPartner parId;
    private String nameEn;

    @JsonIgnore //avoid serialization recursion by Jackson
    @OneToMany(mappedBy = "reeId")
    private List<FInvDdsFile> fInvDdsFiles;
}
