package bg.latona.santa.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.Tuple;

import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.reports.ReportsRepository;

import java.util.ArrayList;
import java.util.List;

@Component
public class AppUserDetailsService implements UserDetailsService {
	
	private static Logger logger = LoggerFactory.getLogger(AppUserDetailsService.class);
	@Autowired
	private ReportsRepository reportsRepository;

	@Transactional(propagation=Propagation.REQUIRED, readOnly=true) //required to avoid the error: "failed to lazily initialize a collection could not initialize proxy - no Session" for user.getRoles() 
	@Override
	public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
		logger.trace("AppUserDetailsService.loadUserByUsername start");
		List<Tuple> authorityCodes = reportsRepository.getUsernameAuthorities(name);
		
		if(authorityCodes.isEmpty()) {
			throw new UsernameNotFoundException(String.format("The username %s doesn't exist or has no permissions", name));
		}

		List<GrantedAuthority> authorities = new ArrayList<>();
		for(Tuple code: authorityCodes) {
			authorities.add(new SimpleGrantedAuthority(code.get(2,String.class)));
		}
		
		SecUser secUser = reportsRepository.findSecUserByName(name);
		secUser.getCompany().getName(); //countering the lazy loading that causes the same error as above appears when in the DRL you put currentUser.getCompany().getName(): org.hibernate.LazyInitializationException: could not initialize proxy [bg.latona.santa.entities.ManagedCompany#1] - no Session]

		UserDetails userDetails = new SantaUser(authorityCodes.get(0).get(0,String.class), authorityCodes.get(0).get(1,String.class), authorities, secUser);

		logger.trace("AppUserDetailsService.loadUserByUsername end");
		return userDetails;
	}
}
