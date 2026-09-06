package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.SecUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbSecUserServiceImpl implements DbSecUserService {

    private final SecUserRepository secUserRepository;

    @Override
    public SecUser getCurrentlyAuthenticatedUser() {
        String currentUser = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getPrincipal().toString();
        }

        return secUserRepository.findFirstByName(currentUser);
    }

    @Override
    public ManagedCompany getCurrentUserManagedCompany() {
        SecUser secUser = getCurrentlyAuthenticatedUser();

        if (secUser.getCompany() == null) {
            throw new SecurityException("No managed company found for current user");
        }

        return secUser.getCompany();
    }

    @Override
    public Long getCompanyCode() {
        return getCurrentUserManagedCompany().getCode();
    }
}