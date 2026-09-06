package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.nepal.PowerPlant;

import java.util.List;
import java.util.Optional;

public interface DbPowerPlantService {

    Optional<PowerPlant> getPowerPlantByAccessPoint(String accessPoint);


    List<PowerPlant> getPowerPlantsByAccessPointIn(List<String> accessPoints);


    List<PowerPlant> getAllPowerPlants();
}
