package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlantMeterReading;
import bg.latona.santa.entities.nepal.PowerPlantProducedSchedule;
import bg.latona.santa.entities.nepal.QPowerPlantMeterReading;
import org.springframework.data.rest.core.annotation.RestResource;

import java.time.LocalDateTime;

public interface PowerPlantMeterReadingRepository extends CommonRepository<PowerPlantMeterReading, QPowerPlantMeterReading, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	PowerPlantMeterReading findFirstByIdentificationAndCompanyAndDeleted(String identification, ManagedCompany company, boolean deleted);

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	PowerPlantMeterReading findFirstByIdentificationAndStartTSAndCompanyAndDeleted(String identification, LocalDateTime startTS, ManagedCompany company, boolean deleted);
}
