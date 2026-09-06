package bg.latona.santa.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import bg.latona.santa.entities.security.SecUser;

public class SantaUser extends User {

	private static final long serialVersionUID = -7201965506292140125L;
	
	private SecUser secUser = null;

	public SantaUser(String username, String password, Collection<? extends GrantedAuthority> authorities, SecUser secUser) {
		super(username, password, authorities);
		this.secUser = secUser;
	}
	
	public SecUser getSecUser() {
		return this.secUser;
	}
	
	@Override
	public String toString() {
		return this.getUsername();
	}

}
