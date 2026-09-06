package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.Interval;
import bg.latona.santa.entities.nepal.QInterval;
import bg.latona.santa.entities.nepal.ScheduleTimeSeries;

public interface IntervalRepository extends CommonRepository<Interval, QInterval, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<Interval> findByScheduleTimeSeriesAndCompanyAndDeleted(ScheduleTimeSeries scheduleTimeSeries, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	Interval findByIdAndDeleted(Long id, boolean deleted);
}
