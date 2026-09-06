package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLoiTypeOfPowerPlantService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;
import bg.latona.santa.repositories.LoiTypeOfPowerPlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbLoiTypeOfPowerPlantServiceImpl implements DbLoiTypeOfPowerPlantService {

    private final DbSecUserService dbSecUserService;
    private final LoiTypeOfPowerPlantRepository loiTypeOfPowerPlantRepository;

    @Override
    public LoiTypeOfPowerPlant getLoiTypeOfPowerPlantByItemCode(Long code) {
        return loiTypeOfPowerPlantRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
                code, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}
