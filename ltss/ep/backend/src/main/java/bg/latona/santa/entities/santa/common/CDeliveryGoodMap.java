package bg.latona.santa.entities.santa.common;

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
public class CDeliveryGoodMap extends CompanyRecord{
    @ManyToOne
    private CCcPartner parId;            
    private String deyCode;              
    @ManyToOne
    private CGoods godId;                

    public CDeliveryGoodMap() {
        super();
    }

    public CDeliveryGoodMap(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate,
            boolean calculateOnly, ManagedCompany company, CCcPartner parId, String deyCode, CGoods godId) {
        super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
        this.parId = parId;
        this.deyCode = deyCode;
        this.godId = godId;
    }

}