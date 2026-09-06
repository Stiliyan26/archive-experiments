package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.ListOptionItem;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"powerPlants","energyBalancingContracts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"powerPlants","energyBalancingContracts"}) //avoid recursion by Lombok
@Audited
@Entity //JPA persisted class
public class LoiContractFee extends ListOptionItem {

	public static final Long FEE_PERCENTAGE = 1L;
	public static final Long FIXED_FEE = 2L;
	public static final Long NO_FEE = 3L;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiContractFee")
	private List<PowerPlant> powerPlants;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "loiContractFee")
	private List<EnergyBalancingContract> energyBalancingContracts;

	public LoiContractFee() {
	}

	public LoiContractFee(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, String listOptionItemName, Long listOptionItemCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company, listOptionItemName, listOptionItemCode);
	}
}
