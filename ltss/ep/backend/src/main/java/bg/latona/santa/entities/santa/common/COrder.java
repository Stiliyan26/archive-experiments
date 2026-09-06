package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"orderDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"orderDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "ix_COrder_parId", columnList = "par_id_id"),
		@Index(name = "ix_COrder_outCode", columnList = "out_code_id"),
		@Index(name = "ix_COrder_status", columnList = "status_id"),
		@Index(name = "ix_COrder_currency", columnList = "currency_id")
	})
public class COrder extends CompanyRecord{
	private Date dateOrr;
	private Date timeLimit;
	@ManyToOne
	private CCcPartner parId;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	@ManyToOne
	private LoiOrderStatus status;	
	private BigDecimal total;
	@ManyToOne
	private CCtCurrency currency;
	private Date finishDate;	
	private Integer orderNum;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "orrId")
	private List<COrderDetail> orderDetails;

	public COrder() {
		super();
	}

	public COrder(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
			boolean calculateOnly, ManagedCompany company, Date dateOrr, Date timeLimit, CCcPartner parId,
			CCcOrganizationUnit outCode, LoiOrderStatus status, BigDecimal total, CCtCurrency currency,
			Date finishDate, Integer orderNum) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.dateOrr = dateOrr;
		this.timeLimit = timeLimit;
		this.parId = parId;
		this.outCode = outCode;
		this.status = status;
		this.total = total;
		this.currency = currency;
		this.finishDate = finishDate;
		this.orderNum = orderNum;
	}
}
