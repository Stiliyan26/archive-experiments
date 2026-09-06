package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

@Data //auto-create getters and setters
@ToString(exclude = {"intervals"}) //avoid serialization recursion by Lombok
@EqualsAndHashCode(exclude = {"intervals"}) //avoid recursion by Lombok
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
public class ScheduleTimeSeries extends CompanyRecord {

	private String sendersTimeSeriesIdentification;
	private String sendersTimeSeriesVersion;
	private String businessType;
	private String product;
	private String objectAggregation;
	private String inAreaV;
	private String inAreaCodingScheme;
	private String outAreaV;
	private String outAreaCodingScheme;
	private String inPartyV;
	private String inPartyCodingScheme;
	private String outPartyV;
	private String outPartyCodingScheme;
	private String measurementUnit;
	private String timeInterval;
	private String resolution;
	private String meteringPointIdentificationV;
	private String meteringPointIdentificationCodingScheme;
	@ManyToOne
	private Schedule schedule;

	@JsonIgnore //avoid serialization recursion by Jackson
	@OneToMany(mappedBy = "scheduleTimeSeries")
	private List<Interval> intervals;
}
