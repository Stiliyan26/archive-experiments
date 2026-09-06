package bg.latona.santa.entities.person;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"interests"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"interests"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Customer extends CompanyRecord {

	@ManyToOne
	private LegalPerson person;
	private Boolean client;
	@ManyToOne
	private SalesStage stage;
	@ManyToOne
	private SecUser assignedSales;
	private Boolean unsubscribed;
	private String segment;
	@ManyToOne
	private DirectionCategory direction;
	@ManyToOne
	private AreaCategory area;
	@ManyToOne
	private BusinessCategory business;
	@ManyToOne
	private GeneralCategory generalCategory;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "customer")
	private List<ClientInterest> interests;
	
	public Customer() {}

	public Customer(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			LegalPerson person, Boolean client, SalesStage stage, SecUser assignedSales, Boolean unsubscribed, String segment, 
			DirectionCategory direction, AreaCategory area, BusinessCategory business, GeneralCategory generalCategory) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.person = person;
		this.client = client;
		this.stage = stage;
		this.assignedSales = assignedSales;
		this.unsubscribed = unsubscribed;
		this.segment = segment;
		this.direction = direction;
		this.area = area;
		this.business = business;
		this.generalCategory = generalCategory;
	}
}
