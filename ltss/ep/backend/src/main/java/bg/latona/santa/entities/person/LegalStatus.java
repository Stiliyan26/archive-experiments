package bg.latona.santa.entities.person;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"legalPersonList"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"legalPersonList"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "code"})) //for all unique constraints should be put Drools UNIQUE rules too
public class LegalStatus extends CompanyRecord {
	public static final Long LEGAL_STATUS_BG_COMPANY_VAT = Long.valueOf(1);
	public static final Long LEGAL_STATUS_BG_COMPANY_NO_VAT = Long.valueOf(2);
	public static final Long LEGAL_STATUS_BG_PHYS = Long.valueOf(3);
	public static final Long LEGAL_STATUS_NON_BG_COMPANY = Long.valueOf(4);
	public static final Long LEGAL_STATUS_NON_BG_PHYS = Long.valueOf(5);
	public static final Long LEGAL_STATUS_OTHER = Long.valueOf(6);
	
	private String name;
	private Long code;
	private String foreignId;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "legalStatus")
	private List<LegalPerson> legalPersonList;
	
	public LegalStatus() {};
	
	public LegalStatus(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, Long code, String foreignId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.code = code;
		this.foreignId = foreignId;
	}
}
