package bg.latona.santa.entities.wato;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.article.Article;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@ToString(exclude = {"importedOrderRows","importedWarehouseStocks"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"importedOrderRows","importedWarehouseStocks"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class ImportedArticle extends CompanyRecord {

	@ManyToOne
	private Article article;
	private String foreignId;
	private Integer number;
	private Boolean foreignDeleted;
	private Integer compId;
	private Short articleType;
	private String predNom;
	private String nomNom;
	private String name;
	private String measureForeignId;
	private String measure;
	private String measureShort;
	private BigDecimal sellPrice;
	private String currency;
	private Long updateCountAsBigInt;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<ImportedOrderRow> importedOrderRows;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<ImportedWarehouseStock> importedWarehouseStocks;


	//default empty constructor
	public ImportedArticle() {}

	//default constructor with all attributes
	public ImportedArticle(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, Boolean calculateOnly, ManagedCompany company,
			Article article, String foreignId, Integer number, Boolean foreignDeleted, Integer compId,
			Short articleType, String predNom, String nomNom, String name, String measureForeignId, String measure, String measureShort,
			BigDecimal sellPrice, String currency, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.article = article;
		this.foreignId = foreignId;
		this.number = number;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.articleType = articleType;
		this.predNom = predNom;
		this.nomNom = nomNom;
		this.name = name;
		this.measureForeignId = measureForeignId;
		this.measure = measure;
		this.measureShort = measureShort;
		this.sellPrice = sellPrice;
		this.currency = currency;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
