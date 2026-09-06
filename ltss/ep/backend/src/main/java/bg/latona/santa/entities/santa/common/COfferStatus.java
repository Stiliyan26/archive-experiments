package bg.latona.santa.entities.santa.common;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //auto-create getters and setters
@Audited
@Entity //JPA persisted class
@AllArgsConstructor
@NoArgsConstructor
public class COfferStatus extends CompanyRecord{
	
	@ManyToOne
	private COffer ofrId;
	@ManyToOne
	private LoiOfferStatus oldStatus; //not null
	@ManyToOne
	private LoiOfferStatus status; //not null
	@Column(length= 3000)
	private String remark; // varying(500)
}
