package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.repositories.PowerPlantRepository;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbPowerPlantService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class DbPowerPlantServiceImpl implements DbPowerPlantService {

    private final DbSecUserService dbSecUserService;
    private final PowerPlantRepository powerPlantRepository;

    public Optional<PowerPlant> getPowerPlantByAccessPoint(String accessPoint) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        return Optional.ofNullable(powerPlantRepository.findFirstByAccessPointAndCompanyAndDeleted(
                accessPoint, company, false
        ));
    }

    @Override
    public List<PowerPlant> getPowerPlantsByAccessPointIn(List<String> accessPoints) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        return powerPlantRepository.findAllByAccessPointInAndCompanyAndDeleted(accessPoints, company, false);
    }

    @Override
    public List<PowerPlant> getAllPowerPlants() {
        return powerPlantRepository.findByCompanyAndDeleted(
                dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}