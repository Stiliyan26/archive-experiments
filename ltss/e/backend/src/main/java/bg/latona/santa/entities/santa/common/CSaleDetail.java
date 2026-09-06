package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data //auto-create getters and setters
@ToString(exclude = {"saleDetails"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"saleDetails"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class CSaleDetail extends CompanyRecord{

    @ManyToOne
    private CStock stkId;
    private BigDecimal quantity;
    private String serialNumber;
    private String batch;
    @ManyToOne
    private CMeasure meeId;
    private BigDecimal price;
    private BigDecimal discount;
    private BigDecimal vat;
    private BigDecimal priceVat;
    private BigDecimal totalWhtVat;
    private BigDecimal totalVat;
    private BigDecimal total;
    @ManyToOne
    private CCtCurrency currency;
	@Column(precision=19, scale=5)
    private BigDecimal rateExchange;
    @ManyToOne
    private CSaleDetail sdlId;
    @ManyToOne
    private CSale saeId;
    @ManyToOne
    private CService seeId;
    @ManyToOne
    private COfferDetail odlId;
    private BigDecimal cost;
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "sdlId")
	private List<CSaleDetail> saleDetails;

    public CSaleDetail() {
        super();
    }

    public CSaleDetail(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, CStock stkId, BigDecimal quantity,String serialNumber,String batch,
    CMeasure meeId,BigDecimal price,BigDecimal discount,BigDecimal vat,BigDecimal priceVat,BigDecimal totalWhtVat,BigDecimal totalVat,BigDecimal total,CCtCurrency currency,BigDecimal rateExchange,CSaleDetail sdlId,CSale saeId,CService seeId,COfferDetail odlId,BigDecimal cost) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.stkId = stkId;
        this.quantity = quantity;
        this.serialNumber = serialNumber;
        this.batch = batch;
        this.meeId = meeId;
        this.price = price;
        this.discount = discount;
        this.vat = vat;
        this.priceVat = priceVat;
        this.totalWhtVat = totalWhtVat;
        this.totalVat = totalVat;
        this.total = total;
        this.currency = currency;
        this.rateExchange = rateExchange;
        this.sdlId = sdlId;
        this.saeId = saeId;
        this.seeId = seeId;
        this.odlId = odlId;
        this.cost = cost;
    }

}
