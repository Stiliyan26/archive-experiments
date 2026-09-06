package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.person.LegalPerson;

import java.util.Optional;
import java.util.Set;

public interface DbLegalPersonService {

    Set<LegalPerson> getAllDistinctLegalPersonsByAccessPoints(Set<String> accessPoints);

    Optional<LegalPerson> getLegalPersonByEik(String eik);

    LegalPerson getLegalPersonByName(String name);

    LegalPerson getLoggedLegalPerson(boolean isSender);

    Set<LegalPerson> getAllLegalPeople();
}
