package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.repositories.LegalPersonRepository;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLegalPersonService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class DbLegalPersonServiceImpl implements DbLegalPersonService {

    private final DbSecUserService dbSecUserService;
    private final LegalPersonRepository legalPersonRepository;

    @Override
    public Set<LegalPerson> getAllDistinctLegalPersonsByAccessPoints(Set<String> accessPoints) {
        return legalPersonRepository.findDistinctByPowerPlantsAccessPointInAndCompanyAndDeleted(
                accessPoints, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }

    @Override
    public Optional<LegalPerson> getLegalPersonByEik(String eik) {
        return Optional.ofNullable(legalPersonRepository.findFirstByEikAndCompanyAndDeleted(
                eik, dbSecUserService.getCurrentUserManagedCompany(), false
        ));
    }

    @Override
    public LegalPerson getLegalPersonByName(String name) {
        return legalPersonRepository.findFirstByNameAndCompanyAndDeleted(
                name, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }

    @Override
    public LegalPerson getLoggedLegalPerson(boolean isSender) {
        return legalPersonRepository.findFirstByIsSenderAndCompanyAndDeleted(
                isSender, dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }

    @Override
    public Set<LegalPerson> getAllLegalPeople() {
        return legalPersonRepository.findAllByCompanyAndDeleted(
                dbSecUserService.getCurrentUserManagedCompany(), false
        );
    }
}

