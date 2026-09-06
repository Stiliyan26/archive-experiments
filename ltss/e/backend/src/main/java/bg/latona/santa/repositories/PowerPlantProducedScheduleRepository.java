package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlantProducedSchedule;
import bg.latona.santa.entities.nepal.QPowerPlantProducedSchedule;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDateTime;

public interface PowerPlantProducedScheduleRepository extends CommonRepository<PowerPlantProducedSchedule, QPowerPlantProducedSchedule, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	PowerPlantProducedSchedule findFirstByIdentificationAndStartTSAndCompanyAndDeleted(String identification, LocalDateTime startTS, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	PowerPlantProducedSchedule findFirstByIdentificationAndCompanyAndDeleted(String identification, ManagedCompany company, boolean deleted);
}
