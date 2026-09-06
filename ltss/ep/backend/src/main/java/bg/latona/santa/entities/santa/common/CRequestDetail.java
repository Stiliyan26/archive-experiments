package bg.latona.santa.entities.santa.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

import javax.persistence.Entity;

import java.math.BigDecimal;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class CRequestDetail extends CompanyRecord{
    
    @ManyToOne
    private CRequest retId;
    @ManyToOne 
    private CGoods godId;
    private BigDecimal quantity;
    @ManyToOne
    private CMeasure meeId;
    private BigDecimal price;
}
