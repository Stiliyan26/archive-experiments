package bg.latona.santa.entities.invoice;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import bg.latona.santa.entities.santa.common.CInvoice;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"invoices","cInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"invoices","cInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiVatExemptionReason extends ListOptionItem {

	public static final Long VAT_EXEMPTION_REASON_NONE = 0L;
	public static final Long VAT_EXEMPTION_REASON_EXPORT = 1L;
	public static final Long VAT_EXEMPTION_REASON_REVERSE = 2L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "vatExemptionReason")
	private List<Invoice> invoices;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "vatExemptionReason")
	private List<CInvoice> cInvoices;
	
	public LoiVatExemptionReason() {};
	
	public LoiVatExemptionReason(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
