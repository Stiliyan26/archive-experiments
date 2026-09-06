package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLoiAgreementStatusService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.selfie.LoiAgreementStatus;
import bg.latona.santa.repositories.LoiAgreementStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbLoiAgreementStatusServiceImpl implements DbLoiAgreementStatusService {

    private final DbSecUserService dbSecUserService;
    private final LoiAgreementStatusRepository loiAgreementStatusRepository;

    @Override
    public LoiAgreementStatus getLoiAgreementStatusByListOptionItemCode(Long code) {
        return loiAgreementStatusRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
                code, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}