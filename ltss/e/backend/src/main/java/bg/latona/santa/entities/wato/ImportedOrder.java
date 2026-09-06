package bg.latona.santa.entities.wato;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.offer.OfferToClient;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"importedOrderRows","importedExpeditionLists","importedInvoices"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedOrderRows","importedExpeditionLists","importedInvoices"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ImportedOrder extends CompanyRecord {

	@ManyToOne
	private OfferToClient offer;
	private String foreignId;
	private String docNum;
	private Date docDate;
	private BigDecimal total;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "order")
	private List<ImportedOrderRow> importedOrderRows;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "order")
	private List<ImportedExpeditionList> importedExpeditionLists;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "order")
	private List<ImportedInvoice> importedInvoices;

	//default empty constructor
	public ImportedOrder() {}

	//default constructor with all attributes
	public ImportedOrder(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			OfferToClient offer, String foreignId, String docNum, Date docDate, BigDecimal total,
			Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.offer = offer;
		this.foreignId = foreignId;
		this.docNum = docNum;
		this.docDate = docDate;
		this.total = total;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
