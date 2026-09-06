package bg.latona.santa.entities.santa.common;

import java.util.Date;
import java.math.BigDecimal;

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
public class CBlockedQuantity extends CompanyRecord{

    private BigDecimal quantity;
    @ManyToOne
    private CStock stkId;
    @ManyToOne
    private CBlock blkId;
    private Date effectiveDate;
    @ManyToOne
    private CMeasure meeId;


    public CBlockedQuantity() {
        super();
    }

    public CBlockedQuantity(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
    BigDecimal quantity, CStock stkId, CBlock blkId, Date effectiveDate, CMeasure meeId) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.quantity = quantity;
        this.stkId = stkId;
        this.blkId = blkId;
        this.effectiveDate = effectiveDate;
        this.meeId = meeId;
    }
}
