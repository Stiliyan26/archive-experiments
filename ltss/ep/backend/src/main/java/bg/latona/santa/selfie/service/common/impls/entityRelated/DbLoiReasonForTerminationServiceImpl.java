package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLoiReasonForTerminationService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.selfie.LoiReasonForTermination;
import bg.latona.santa.repositories.LoiReasonForTerminationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbLoiReasonForTerminationServiceImpl implements DbLoiReasonForTerminationService {

    private final DbSecUserService dbSecUserService;
    private final LoiReasonForTerminationRepository loiReasonForTerminationRepository;

    @Override
    public LoiReasonForTermination getLoiReasonForTerminationByListOptionItemCode(Long code) {
        return loiReasonForTerminationRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(
                code, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}

