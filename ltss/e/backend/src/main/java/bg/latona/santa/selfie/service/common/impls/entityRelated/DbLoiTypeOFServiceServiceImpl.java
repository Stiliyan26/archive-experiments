package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLoiTypeOFServiceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.selfie.LoiTypeOFService;
import bg.latona.santa.repositories.LoiTypeOFServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbLoiTypeOFServiceServiceImpl implements DbLoiTypeOFServiceService {

    private final DbSecUserService dbSecUserService;
    private final LoiTypeOFServiceRepository loiTypeOFServiceRepository;

    @Override
    public LoiTypeOFService getLoiTypeOFServiceByListOptionItemCode(Long code) {
        return loiTypeOFServiceRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
                code, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}



