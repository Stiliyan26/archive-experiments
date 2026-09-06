package bg.latona.santa.entities.santa.finance;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCtCurrency;
import bg.latona.santa.entities.security.SecUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"fPtJournals","fPtPostings"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"fPtJournals","fPtPostings"}) //avoid recursion by Lombok
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class FPtJournal extends CompanyRecord {

	@ManyToOne
	private FPtBatch bahId; //NOT NULL, -- FK to batches
	private LocalDate postDate;//NOT NULL,
	@ManyToOne
	private CCcOrganizationUnit outCode; // NOT NULL, -- organization unit code
	@ManyToOne
	private LoiPtJournalStatus status; //NOT NULL, -- PN - pending, PT - posted, etc?
	private Integer journalNo; //NOT NULL, -- journal number
	@ManyToOne
	private FPtJournal stornoJolId; //-- id journal's storno
	@ManyToOne
	private LoiPtJournalCcStatus ccStatus; //NOT NULL DEFAULT 'U'::bpchar,
	@Column(length = 3000)
	private String descr;
	@ManyToOne
	private CCtCurrency cuyCode; //NOT NULL DEFAULT 'BGN'::character varying,
	private BigDecimal cuyRate; //NOT NULL,
	private Integer cuyUnit; //NOT NULL,
	@ManyToOne
	private FJournalType jteId; //NOT NULL,

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "stornoJolId")
	private List<FPtJournal> fPtJournals;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "jolId")
	private List<FPtPosting> fPtPostings;

	public FPtJournal(SecUser createdBy, Date createdDate, SecUser lastModifiedBy, Date lastModifiedDate, boolean calculateOnly, ManagedCompany company, FPtBatch bahId, LocalDate postDate, CCcOrganizationUnit outCode, LoiPtJournalStatus status, Integer journalNo, FPtJournal stornoJolId, LoiPtJournalCcStatus ccStatus, String descr, CCtCurrency cuyCode, BigDecimal cuyRate, Integer cuyUnit, FJournalType jteId) {
		super(createdBy, createdDate, lastModifiedBy, lastModifiedDate, calculateOnly, company);
		this.bahId = bahId;
		this.postDate = postDate;
		this.outCode = outCode;
		this.status = status;
		this.journalNo = journalNo;
		this.stornoJolId = stornoJolId;
		this.ccStatus = ccStatus;
		this.descr = descr;
		this.cuyCode = cuyCode;
		this.cuyRate = cuyRate;
		this.cuyUnit = cuyUnit;
		this.jteId = jteId;
	}
}

/*COMMENT ON TABLE accounting.pt_journals
IS '! jol !
Journal are the middle level in posting process - schetovdna statiya';
COMMENT ON COLUMN accounting.pt_journals.bah_id IS 'FK to batches';
COMMENT ON COLUMN accounting.pt_journals.out_code IS 'organization unit code';
COMMENT ON COLUMN accounting.pt_journals.status IS 'PN - pending, PT - posted, etc?';
COMMENT ON COLUMN accounting.pt_journals.journal_no IS 'journal number';
COMMENT ON COLUMN accounting.pt_journals.storno_jol_id IS 'id journal''s storno'; */
