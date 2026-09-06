package bg.latona.santa.entities;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class Income extends Attachable {

	private String incomeDesc;
	private Date incomeDate;
	@ManyToOne
	private LegalPerson incomePayer;

	public Income() {};
	
	public Income(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			String incomeDesc, Date incomeDate, LegalPerson incomePayer) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.incomeDesc = incomeDesc;
		this.incomeDate = incomeDate;
		this.incomePayer = incomePayer;
	}
}
