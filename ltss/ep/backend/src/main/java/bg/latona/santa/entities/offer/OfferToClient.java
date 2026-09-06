package bg.latona.santa.entities.offer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.Attachable;
import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.HashTag;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.wato.ImportedOrder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"offerLines","importedOrders","offerToClients"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"offerLines","importedOrders","offerToClients"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"offerCode"})) //for all unique constraints should be put Drools UNIQUE rules too
public class OfferToClient extends Attachable {
	
	private String offerCode;
	private int revision;
	@ManyToOne
	private OfferToClient originalOffer;
	private LocalDate validToDate;
	private BigDecimal discountPercent;
	private BigDecimal vatPercent;
	@ManyToOne
	private Currency currency;
	@ManyToOne
	private BankAccount bankAccount;
	@ManyToOne
	private LegalPerson person;
	@Column(length= 3000)
	private String deliveryTerms;
	@Column(length= 3000)
	private String discountCondition;
	@Column(length= 3000)
	private String guaranteeTerms;
	@Column(length= 3000)
	private String notes;
	private String foreignId;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "offerToClient")
	private List<OfferLine> offerLines;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "offer")
	private List<ImportedOrder> importedOrders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "originalOffer")
	private List<OfferToClient> offerToClients;


	public OfferToClient() {};
	
	public OfferToClient(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, List<HashTag> hashTags, 
			String offerCode, int revision, OfferToClient originalOffer, LocalDate validToDate, BigDecimal discountPercent, BigDecimal vatPercent, Currency currency, BankAccount bankAccount, LegalPerson person,
			String deliveryTerms, String discountCondition, String guaranteeTerms, String notes, String foreignId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, hashTags);
		this.offerCode = offerCode;
		this.revision = revision;
		this.originalOffer = originalOffer;
		this.validToDate = validToDate;
		this.discountPercent = discountPercent;
		this.vatPercent = vatPercent;
		this.currency = currency;
		this.bankAccount = bankAccount;
		this.person = person;
		this.deliveryTerms = deliveryTerms;
		this.discountCondition = discountCondition;
		this.guaranteeTerms = guaranteeTerms;
		this.notes = notes;
		this.foreignId = foreignId;
	}
}
