package bg.latona.santa.entities.santa.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import org.hibernate.envers.Audited;

import bg.latona.santa.entities.CompanyRecord;

import javax.persistence.Column;
import javax.persistence.Entity;

import java.util.Date;
import java.util.List;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Data //auto-create getters and setters
@ToString(exclude = {"cRequestDetails","cRequests"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"cRequestDetails","cRequests"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class CRequest extends CompanyRecord{
	
	private Date dateOdy; //not null
	private Date term;
	@ManyToOne
	private CCcPartner partner; //par_id
	@ManyToOne
	private CCcOrganizationUnit outCode; // NOT NULL
	@ManyToOne
	private CRequest retId;
	private String numberOdy; // not null
	private String status;
	@Column(length= 3000)
	private String remark;
	@ManyToOne
	private CCcOrganizationUnit outId;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "retId")
	private List<CRequestDetail> cRequestDetails;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "retId")
	private List<CRequest> cRequests;
}
