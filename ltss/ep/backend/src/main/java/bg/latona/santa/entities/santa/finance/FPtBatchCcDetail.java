package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcGoodsType;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtBatchCcDetail extends CompanyRecord {

	@ManyToOne
	private FPtBatch bahId;  //NOT NULL,
	@ManyToOne
	private FCtTransitionType tteId;  //NOT NULL,
	@ManyToOne
	private CCtCurrency cuyCode; //NOT NULL,
	private LocalDate postDate;  //NOT NULL,
	@ManyToOne
	private LoiPtBatchCcDetailStatus status; //NOT NULL DEFAULT 'C'::character varying, -- Domain in ref_data INVOICE_STATUS
	private Long result;  //NOT NULL DEFAULT '-1'::integer,
	private BigDecimal amountTotal;  //NOT NULL,
	private BigDecimal amountDo;
	private BigDecimal amountVat;
	@ManyToOne
	private FCcEbk ccEbkId;
	@ManyToOne
	private FCcFunction ccFunId;
	@ManyToOne
	private FCcProgram ccPrmId;
	@ManyToOne
	private FCcFinsource ccFieId;
	@ManyToOne
	private CCcPartner ccParId;
	@ManyToOne
	private CCcGoodsType ccGteId;
	@ManyToOne
	private FCcContract ccCotId;
	@ManyToOne
	private CCcOrganizationUnit ccOutId;
	@ManyToOne
	private FCcReserve1 ccRe1Id;
	@ManyToOne
	private FCcReserve2 ccRe2Id;
	@ManyToOne
	private LoiBatchTypeRuleDependenceType dependenceType; //-- Domain in register.ref_data DEPENDENCE_TYPE
	private Long dependenceId;
	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL
}
/*
ALTER TABLE accounting.pt_batch_cc_details
  OWNER TO accounting;
COMMENT ON COLUMN accounting.pt_batch_cc_details.id IS 'Sequence is bcl_seq';
COMMENT ON COLUMN accounting.pt_batch_cc_details.status IS 'Domain in ref_data INVOICE_STATUS';
COMMENT ON COLUMN accounting.pt_batch_cc_details.dependence_type IS 'Domain in register.ref_data DEPENDENCE_TYPE';
 */