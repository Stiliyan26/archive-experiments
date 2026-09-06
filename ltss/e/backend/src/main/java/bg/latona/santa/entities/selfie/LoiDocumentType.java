package bg.latona.santa.entities.selfie;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;


@Data //auto-create getters and setters
@ToString(exclude = {"electricityInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"electricityInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiDocumentType extends ListOptionItem {

	public static final Long INVOICE = 1L;
	public static final Long DEBIT_NOTE = 2L;
	public static final Long CREDIT_NOTE = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiDocumentType")
	private List<ElectricityInvoice> electricityInvoices;

	public LoiDocumentType() {
	}

	public LoiDocumentType(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
