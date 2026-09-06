package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class IbexEnergyDealLeg extends CompanyRecord {

	private String contractId;
	private String side;
	private BigDecimal unitPrice;
	private BigDecimal quantity;
	private String deliveryAreaId;
	private Boolean aggressor;
	private String portfolioId;
	private String orderId;
	private String userId;
	private String deliveryStart;
	private String deliveryEnd;
	private String orderType;
	private String clientOrderId;
	private String text;
	@ManyToOne
	private IbexEnergyDeal ibexEnergyDeal;
}
