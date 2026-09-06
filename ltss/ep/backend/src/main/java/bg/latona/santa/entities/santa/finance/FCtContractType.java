package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fCcContracts"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCcContracts"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FCtContractType extends CompanyRecord {

	private String code;
	@ManyToOne
	private CCcOrganizationUnit outCode;
	private String name;
	@Column(length = 3000)
	private String descr;
	private String kind;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "cteId")
	private List<FCcContract> fCcContracts;
}
