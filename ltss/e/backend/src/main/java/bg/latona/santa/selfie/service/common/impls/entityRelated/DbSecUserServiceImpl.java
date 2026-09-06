package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.SecUserRepository;
import bg.latona.santa.security.SantaUser;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class DbSecUserServiceImpl implements DbSecUserService {

    @Override
    public SecUser getCurrentlyAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof SantaUser) {
            return ((SantaUser) principal).getSecUser();
        }

        return null;
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