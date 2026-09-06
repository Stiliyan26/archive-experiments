package bg.latona.santa.entities.article;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import bg.latona.santa.entities.AttachableRevenuesAndExpenses;
import bg.latona.santa.entities.invoice.InvoiceRow;
import bg.latona.santa.entities.offer.OfferLine;
import bg.latona.santa.entities.wato.ImportedArticle;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.entities.task.PlannedIncomeOrExpense;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"articlePriceRates","plannedIncomeOrExpenses","invoiceRows","offerLines","importedArticles"
,"attachableRevenuesAndExpenses"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"articlePriceRates","plannedIncomeOrExpenses","invoiceRows","offerLines","importedArticles"
,"attachableRevenuesAndExpenses"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"company_id", "name"})) //for all unique constraints should be put Drools UNIQUE rules too
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) 
//https://www.thoughts-on-java.org/complete-guide-inheritance-strategies-jpa-hibernate/
//https://docs.oracle.com/javaee/6/tutorial/doc/bnbqn.html#bnbqs
public abstract class Article extends CompanyRecord {

	@Column(length= 3000)
	private String name;
	private String foreignId;
	private String measureForeignId;
	private String measure;
	private String measureShort;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<ArticlePriceRate> articlePriceRates;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<PlannedIncomeOrExpense> plannedIncomeOrExpenses;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<InvoiceRow> invoiceRows;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<OfferLine> offerLines;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<ImportedArticle> importedArticles;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "article")
	private List<AttachableRevenuesAndExpenses> attachableRevenuesAndExpenses;
	
	public Article() {};
	
	public Article(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
			String name, String foreignId, String measureForeignId, String measure, String measureShort) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.name = name;
		this.foreignId = foreignId;
		this.measureForeignId = measureForeignId;
		this.measure = measure;
		this.measureShort = measureShort;
	}
}
