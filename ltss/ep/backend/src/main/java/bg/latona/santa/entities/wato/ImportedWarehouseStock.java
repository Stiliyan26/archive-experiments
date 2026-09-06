package bg.latona.santa.entities.wato;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class ImportedWarehouseStock extends CompanyRecord {

	@ManyToOne
	private ImportedArticle article;
	private String foreignArticleId;
	private String warehouseId;
	private String warehouseName;
	private BigDecimal quantity;
	private BigDecimal quantityReserve;
	private BigDecimal totalInCompany;
	private BigDecimal reserveInCompany;
	private Boolean foreignDeleted;
	private Integer compId;
	private Long updateCountAsBigInt;


	//default empty constructor
	public ImportedWarehouseStock() {}

	//default constructor with all attributes
	public ImportedWarehouseStock(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			ImportedArticle article, String foreignArticleId, String warehouseId, String warehouseName,
			BigDecimal quantity, BigDecimal quantityReserve, BigDecimal totalInCompany, BigDecimal reserveInCompany,
			Boolean foreignDeleted, Integer compId, Long updateCountAsBigInt) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.article = article;
		this.foreignArticleId = foreignArticleId;
		this.warehouseId = warehouseId;
		this.warehouseName = warehouseName;
		this.quantity = quantity;
		this.quantityReserve = quantityReserve;
		this.totalInCompany = totalInCompany;
		this.reserveInCompany = reserveInCompany;
		this.foreignDeleted = foreignDeleted;
		this.compId = compId;
		this.updateCountAsBigInt = updateCountAsBigInt;
	}
}
