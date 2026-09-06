package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.mail.SendMailMessage;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data //auto-create getters and setters
@ToString(exclude = {"scheduleTimeSeries"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"scheduleTimeSeries"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "idx_schedule_msg_ident", columnList = "deleted, company_id, messageIdentification, isPPS, messageVersion")
	})
public class Schedule extends CompanyRecord {


	private String messageIdentification; //msgIdtfc
	private BigDecimal messageVersion; //msgVersion
	private String messageType; //msgType
	private String processType; //gpt
	private String scheduleClassificationType;
	private String senderIdentificationV; //senderID
	private String senderIdentificationCodingScheme;
	private String senderRole;
	private String receiverIdentificationV; //receiverID
	private String receiverIdentificationCodingScheme; //receiverID
	private String receiverRole;
	private String messageDateTime;
	private String scheduleTimeInterval;
	private LocalDateTime scheduleTimeStart;
	private LocalDateTime scheduleTimeEnd;
	private Boolean isPPS;
	private Boolean isSent;

	@ManyToOne
	private SendMailMessage sendMailMessage;
	@ManyToOne
	private DBFile dbFile;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "schedule")
	private List<ScheduleTimeSeries> scheduleTimeSeries;

}
