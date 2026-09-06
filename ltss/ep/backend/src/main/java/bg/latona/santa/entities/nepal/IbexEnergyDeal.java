package bg.latona.santa.entities.nepal;


import bg.latona.santa.entities.CompanyRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"legs"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"legs"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class IbexEnergyDeal extends CompanyRecord {

	//REST Historical private trades
	private String updatedAt;
	private String tradeId;
	private String tradeTime;
	private String state;
	private String currency;
	private String eventSequenceNo;
	private String revisionNo;
	private String mediumDisplayName;
	private String selfTrade;
	private Boolean companyTrade;
	private Boolean isDistributedToSchedules;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "ibexEnergyDeal")
	private List<IbexEnergyDealLeg> legs; //array of objects


}
