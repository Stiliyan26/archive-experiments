package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;

public interface DbSecUserService {

    SecUser getCurrentlyAuthenticatedUser();

    ManagedCompany getCurrentUserManagedCompany();

    Long getCompanyCode();
}
