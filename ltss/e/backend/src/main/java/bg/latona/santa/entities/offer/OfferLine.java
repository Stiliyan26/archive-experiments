package bg.latona.santa.entities.offer;

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
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.wato.ImportedOrderRow;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"importedOrderRows"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedOrderRows"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class OfferLine extends CompanyRecord {

	private String description;
	@ManyToOne
	private Article article;
	private BigDecimal ammount;
	private BigDecimal price;
	private BigDecimal discountPercent;
	private String deliveryDate;
	@ManyToOne
	private OfferToClient offerToClient;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "offerLine")
	private List<ImportedOrderRow> importedOrderRows;
	
	public OfferLine() {};
	
	public OfferLine(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String description, Article article, BigDecimal ammount, BigDecimal price, BigDecimal discountPercent, String deliveryDate, OfferToClient offerToClient) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.article = article;
		this.ammount = ammount;
		this.price = price;
		this.discountPercent = discountPercent;
		this.deliveryDate = deliveryDate;
		this.description = description;
		this.offerToClient = offerToClient;
	}
}
