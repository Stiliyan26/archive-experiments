package bg.latona.santa.entities;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.offer.OfferToClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.article.Currency;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"invoices","offerToClients"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"invoices","offerToClients"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class BankAccount extends CompanyRecord {

	@ManyToOne
	private LegalPerson bankAccountOwner;
	private String description;
	private String iban;
	private String bankName;
	private String bic;
	@ManyToOne
	private Currency currency;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bankAccount")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "bankAccount")
	private List<OfferToClient> offerToClients;
	public BankAccount() {};
	
	public BankAccount(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			LegalPerson bankAccountOwner, String description, String iban, String bankName, String bic, Currency currency) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.bankAccountOwner = bankAccountOwner;
		this.description = description;
		this.iban = iban;
		this.bankName = bankName;
		this.bic = bic;
		this.currency = currency;
	}
}
