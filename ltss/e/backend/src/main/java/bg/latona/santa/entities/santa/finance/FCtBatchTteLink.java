package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.LocalDate;

@Data //auto-create getters and setters
@Audited
@AllArgsConstructor
@NoArgsConstructor
@Entity //JPA persisted class
public class FCtBatchTteLink extends CompanyRecord {

    @ManyToOne
    private CCcOrganizationUnit outCode; //NOT NULL,
    @ManyToOne
    private FCtTransitionType tteId; //NOT NULL,
    @ManyToOne
    private FCtBatchType bteId;
    @ManyToOne
    private FCtInvDealType ideId;
    private Integer orderNum;
    @ManyToOne
    private LoiBatchTteLinkStatus status; // NOT NULL, -- Domain in register.ref_data is STATUS
    private LocalDate activeFromDate; //NOT NULL DEFAULT (now())::date,
    private LocalDate activeToDate; //NOT NULL DEFAULT to_date('01.01.2100'::text, 'dd.mm.yyyy'::text),
}

/* COMMENT ON COLUMN accounting.ct_batch_tte_links.id IS 'Sequence is btk_seq';
COMMENT ON COLUMN accounting.ct_batch_tte_links.status IS 'Domain in register.ref_data is STATUS'; */