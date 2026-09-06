package bg.latona.santa.entities.wato;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.offer.OfferLine;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"importedExpeditionListRows","importedInvoiceRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedExpeditionListRows","importedInvoiceRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ImportedOrderRow extends CompanyRecord {

	@ManyToOne
	private ImportedOrder order;
	@ManyToOne
	private ImportedArticle article;
	@ManyToOne
	private OfferLine offerLine;
	private String foreignId;
	private String foreignOrderId;
	private String foreignArticleId;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orderRow")
	private List<ImportedExpeditionListRow> importedExpeditionListRows;
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orderRow")
	private List<ImportedInvoiceRow> importedInvoiceRows;

	//default empty constructor
	public ImportedOrderRow() {}

	//default constructor with all attributes
	public ImportedOrderRow(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			ImportedOrder order, ImportedArticle article, OfferLine offerLine, String foreignId, String foreignOrderId,
			String foreignArticleId, Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.order = order;
		this.article = article;
		this.offerLine = offerLine;
		this.foreignId = foreignId;
		this.foreignOrderId = foreignOrderId;
		this.foreignArticleId = foreignArticleId;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
