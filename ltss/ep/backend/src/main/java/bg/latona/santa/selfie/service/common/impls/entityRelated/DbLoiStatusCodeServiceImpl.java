package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLoiStatusCodeService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.selfie.LoiStatusCode;
import bg.latona.santa.repositories.LoiStatusCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbLoiStatusCodeServiceImpl implements DbLoiStatusCodeService {

    private final DbSecUserService dbSecUserService;
    private final LoiStatusCodeRepository loiStatusCodeRepository;

    @Override
    public LoiStatusCode getLoiStatusCodeServiceByListOptionItemCode(Long code) {
        return loiStatusCodeRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
                code, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}
