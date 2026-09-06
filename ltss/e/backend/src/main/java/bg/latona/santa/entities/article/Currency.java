package bg.latona.santa.entities.article;

import java.util.Date;
import java.util.List;


import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.invoice.Invoice;
import bg.latona.santa.entities.offer.OfferToClient;
import bg.latona.santa.entities.task.TimeChargeRate;
import bg.latona.santa.entities.transport.Transport;
import bg.latona.santa.entities.transport.TransportOrder;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"currencyArticlePriceRates","invoices","offerToClients","timeChargeRates","transports","transportOrders","bankAccounts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"currencyArticlePriceRates","invoices","offerToClients","timeChargeRates","transports","transportOrders","bankAccounts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class Currency extends Article {

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<ArticlePriceRate> currencyArticlePriceRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "invoiceCurrency")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<OfferToClient> offerToClients;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<TimeChargeRate> timeChargeRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "paymentCurrency")
	private List<Transport> transports;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orderPaymentCurrency")
	private List<TransportOrder> transportOrders;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "currency")
	private List<BankAccount> bankAccounts;

	public Currency() {};
	
	public Currency(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String foreignId, String measureForeignId, String measure, String measureShort) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, name, foreignId, measureForeignId, measure, measureShort);
	}
}
