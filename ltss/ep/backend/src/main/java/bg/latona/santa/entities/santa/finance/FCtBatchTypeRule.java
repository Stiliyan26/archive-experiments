package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.security.SecUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Date;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtBatchTypeRule extends CompanyRecord {

	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- organization unit
	private Integer orderNum; //NOT NULL, -- order number of processing
	@ManyToOne
	private LoiBatchTypeRuleAmountType amountType; //NOT NULL, -- which amount to take - 0 = amount, 1 = amount_outstanding, 2 = amount2, 3 = amount3
	private LocalDate activeFromDate; //NOT NULL DEFAULT ('now'::text)::date,
	private LocalDate activeToDate; //NOT NULL DEFAULT '2100-01-01'::date,
	@ManyToOne
	private LoiBatchTypeRuleDependenceType dependenceType; // -- Domain in register.ref_date DEPENDENCE_TYPE
	@ManyToOne
	private FCtTransitionType tteId; //NOT NULL,
	@ManyToOne
	private FCtRule rueId; //NOT NULL,
	@ManyToOne
	private FJournalType jteId; //NOT NULL,
	@ManyToOne
	private FChartAccount coaIdCt; //NOT NULL,
	@ManyToOne
	private FChartAccount coaIdDt; //NOT NULL,
	private Long dependenceId;
	private String dependenceIdFake;
	
	public Long dependenceTypeStringToCode(String text) {
		switch(text) {
			case "BAK": 
				return LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_BAK;
			case "CAH": 
				return LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_CAH;
			case "PRT": 
				return LoiBatchTypeRuleDependenceType.BATCH_TYPE_RULE_DEPENDENCE_TYPE_PRT;
		}
		
		return null;
	}

	public FCtBatchTypeRule(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, 
			CCcOrganizationUnit outCode, Integer orderNum,
			LoiBatchTypeRuleAmountType amountType, LocalDate activeFromDate, LocalDate activeToDate,
			LoiBatchTypeRuleDependenceType dependenceType, FCtTransitionType tteId, FCtRule rueId, FJournalType jteId,
			FChartAccount coaIdCt, FChartAccount coaIdDt, Long dependenceId, String dependenceIdFake) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.outCode = outCode;
		this.orderNum = orderNum;
		this.amountType = amountType;
		this.activeFromDate = activeFromDate;
		this.activeToDate = activeToDate;
		this.dependenceType = dependenceType;
		this.tteId = tteId;
		this.rueId = rueId;
		this.jteId = jteId;
		this.coaIdCt = coaIdCt;
		this.coaIdDt = coaIdDt;
		this.dependenceId = dependenceId;
		this.dependenceIdFake = dependenceIdFake;
	}
}
/* COMMENT ON TABLE accounting.ct_batch_type_rules
IS '! btr !
Rules configuration for batch processing - i.e. configruation for rules. There can be many rows for one pair batch/rule';
COMMENT ON COLUMN accounting.ct_batch_type_rules.out_code IS 'organization unit';
COMMENT ON COLUMN accounting.ct_batch_type_rules.order_num IS 'order number of processing';
COMMENT ON COLUMN accounting.ct_batch_type_rules.amount_type IS 'which amount to take - 0 = amount, 1 = amount_outstanding, 2 = amount2, 3 = amount3';
COMMENT ON COLUMN accounting.ct_batch_type_rules.dependence_type IS 'Domain in register.ref_date DEPENDENCE_TYPE'; */