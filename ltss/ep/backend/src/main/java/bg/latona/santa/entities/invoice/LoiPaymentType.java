package bg.latona.santa.entities.invoice;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.santa.common.CDelivery;
import bg.latona.santa.entities.santa.common.CInvoice;
import bg.latona.santa.entities.santa.common.CSale;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"deliveries","cSales","invoices","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"deliveries","cSales","invoices","cInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiPaymentType extends ListOptionItem {

	public static final Long PAYMENT_TYPE_CASH = 1L;
	public static final Long PAYMENT_TYPE_WIRE_TRANSFER = 2L;
	public static final Long PAYMENT_TYPE_CDC = 3L;
	public static final Long PAYMENT_TYPE_CD = 4L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "paymentType")
	private List<CDelivery> deliveries;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "paymentType")
	private List<CSale> cSales;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "paymentType")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "paymentType")
	private List<CInvoice> cInvoices;
	
	public LoiPaymentType() {};
	
	public LoiPaymentType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
