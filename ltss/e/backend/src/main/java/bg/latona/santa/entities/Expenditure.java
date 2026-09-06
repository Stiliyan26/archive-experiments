package bg.latona.santa.entities;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class Expenditure extends Attachable {

	@ManyToOne
	private LoiExpenditureType expenseType;
	private String expenseDesc; //inherited entities shouldn't have same property names as siblings!
	@Column(length= 3000)
	private String expenseNotes; //inherited entities shouldn't have same property names as siblings!
	private Date expenseDate; //inherited entities shouldn't have same property names as siblings!
	private LocalDate expenseFromDate; //inherited entities shouldn't have same property names as siblings!
	private LocalDate expenseToDate; //inherited entities shouldn't have same property names as siblings!
	@ManyToOne
	private LegalPerson expenseVendor; //inherited entities shouldn't have same property names as siblings!

	public Expenditure() {};
	
	public Expenditure(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			LoiExpenditureType expenseType, String expenseDesc, String expenseNotes, Date expenseDate, LocalDate expenseFromDate, LocalDate expenseToDate, LegalPerson expenseVendor) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.expenseType = expenseType;
		this.expenseDesc = expenseDesc;
		this.expenseNotes = expenseNotes;
		this.expenseDate = expenseDate;
		this.expenseFromDate = expenseFromDate;
		this.expenseToDate = expenseToDate;
		this.expenseVendor = expenseVendor;
	}
}
