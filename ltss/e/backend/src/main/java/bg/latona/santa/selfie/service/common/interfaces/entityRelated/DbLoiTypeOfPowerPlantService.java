package bg.latona.santa.selfie.service.common.interfaces.entityRelated;


import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;

public interface DbLoiTypeOfPowerPlantService {

    LoiTypeOfPowerPlant getLoiTypeOfPowerPlantByItemCode(Long code);
}
