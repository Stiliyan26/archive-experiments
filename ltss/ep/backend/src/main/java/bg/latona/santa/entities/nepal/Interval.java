package bg.latona.santa.entities.nepal;

import bg.latona.santa.entities.CompanyRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import java.math.BigDecimal;

@Data //auto-create getters and setters
@AllArgsConstructor
@NoArgsConstructor
@Audited
@Entity //JPA persisted class
@Table(indexes = {
		@Index(name = "idx_interval_schedule_time_series", columnList = "deleted, company_id, schedule_time_series_id")
	})
public class Interval extends CompanyRecord {

	private BigDecimal pos;
	@Column(precision = 19, scale = 5)
	private BigDecimal qty;

	@ManyToOne
	ScheduleTimeSeries scheduleTimeSeries;

}
