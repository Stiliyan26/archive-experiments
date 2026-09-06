package bg.latona.santa.entities.santa.common;

import lombok.Data;
import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

import javax.persistence.Column;
import javax.persistence.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
public class CPmtCurrencyRate extends CompanyRecord {
	
	@ManyToOne
	private CCcOrganizationUnit outCode; // not sure about that
	@ManyToOne
	private CCtCurrency cuyCode;
	private LocalDate dateFrom;
	private LocalDate dateTo;
	private Integer unitOfCuy;
	@Column(precision=19, scale=5)
	private BigDecimal inMainCuy;
	private BigDecimal perMainCuy;
	private String triggerIns;

	public CPmtCurrencyRate() {
		super();
	}

	public CPmtCurrencyRate(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company,
							CCcOrganizationUnit outCode, CCtCurrency cuyCode, LocalDate dateFrom, LocalDate dateTo, Integer unitOfCuy, BigDecimal inMainCuy, BigDecimal perMainCuy, String triggerIns) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.cuyCode = cuyCode;
		this.dateFrom = dateFrom;
		this.dateTo = dateTo;
		this.unitOfCuy = unitOfCuy;
		this.inMainCuy = inMainCuy;
		this.perMainCuy = perMainCuy;
		this.triggerIns = triggerIns;
	}
}