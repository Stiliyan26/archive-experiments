package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.QScheduleTimeSeries;
import bg.latona.santa.entities.nepal.Schedule;
import bg.latona.santa.entities.nepal.ScheduleTimeSeries;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

public interface ScheduleTimeSeriesRepository extends CommonRepository<ScheduleTimeSeries, QScheduleTimeSeries, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<ScheduleTimeSeries> findByScheduleAndCompanyAndDeleted(Schedule schedule, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	ScheduleTimeSeries findFirstByScheduleAndInPartyVAndOutPartyVAndCompanyAndDeleted(Schedule schedule, String inParty, String outParty, ManagedCompany company, boolean deleted);
}
