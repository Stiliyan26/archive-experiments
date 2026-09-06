package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtClosingAccount extends CompanyRecord {

	@ManyToOne
	private LoiPtClosingAccountPeriodMonth periodMonth; //NOT NULL DEFAULT '12'::character varying, -- In case of closing monthly accounts - last month of the closing period. In case of closing annual accounts - December /by defaults/.
	private String periodYear; //NOT NULL,
	@ManyToOne
	private CCcOrganizationUnit outCode; //NOT NULL, -- The company or office to which refers closing accounts
	@ManyToOne
	private FChartAccount coaId; //NOT NULL, -- The account from chart of accounts into which all balance accounts should to be closed
	@ManyToOne
	private FPtBatch bahId; //-- Corresponded document
	@ManyToOne
	private LoiPtClosingAccountStatus status; //NOT NULL DEFAULT 'S'::character varying, -- If closing accounts is started or ended. Domain CLOSING_ACCOUNTS_STATUS

}
/*
ALTER TABLE accounting.pt_closing_accounts
  OWNER TO accounting;
COMMENT ON COLUMN accounting.pt_closing_accounts.period_month IS 'In case of closing monthly accounts - last month of the closing period. In case of closing annual accounts - December /by defaults/.';
COMMENT ON COLUMN accounting.pt_closing_accounts.out_code IS 'The company or office to which refers closing accounts';
COMMENT ON COLUMN accounting.pt_closing_accounts.coa_id IS 'The account from chart of accounts into which all balance accounts should to be closed';
COMMENT ON COLUMN accounting.pt_closing_accounts.bah_id IS 'Corresponded document';
COMMENT ON COLUMN accounting.pt_closing_accounts.status IS 'If closing accounts is started or ended. Domain CLOSING_ACCOUNTS_STATUS';
 */
