package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.security.SecUser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fCtBatchTypeRules"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fCtBatchTypeRules"}) //avoid recursion by Lombok
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtRule extends CompanyRecord {

	private String code; //NOT NULL, -- unique
	private String name; //NOT NULL,
	@Column(length = 3000)
	private String expression; // NOT NULL, -- - rule expression - java, sql, jython, ?
	@Column(length = 3000)
	private String descr; //NOT NULL,
	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL,
	
	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "rueId")
	private List<FCtBatchTypeRule> fCtBatchTypeRules;

	public FCtRule(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			String code, String name, String expression, String descr, CCcOrganizationUnit outCode) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.code = code;
		this.name = name;
		this.expression = expression;
		this.descr = descr;
		this.outCode = outCode;
	}

}

/* COMMENT ON TABLE accounting.ct_rules
IS '! rue !
Rules for automatic/automated postings';
COMMENT ON COLUMN accounting.ct_rules.code IS 'unique';
COMMENT ON COLUMN accounting.ct_rules.expression IS '- rule expression - java, sql, jython, ?'; */