package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Date;


@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Builder
@Entity //JPA persisted class
public class AccountingPeriod extends CompanyRecord {

    @Column
    private String month;

    @Column
    private String code;

    @Column(columnDefinition = "boolean default false")
    private Boolean isActive;

	public AccountingPeriod(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String month, String code) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.month = month;
		this.code = code;
	}
}
