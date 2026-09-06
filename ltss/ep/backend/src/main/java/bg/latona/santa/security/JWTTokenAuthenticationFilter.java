package bg.latona.santa.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static bg.latona.santa.security.SecurityConstants.HEADER_STRING;
import static bg.latona.santa.security.SecurityConstants.SECRET;
import static bg.latona.santa.security.SecurityConstants.TOKEN_PREFIX;

import java.io.IOException;

public class JWTTokenAuthenticationFilter extends BasicAuthenticationFilter {

	private static Logger logger = LoggerFactory.getLogger(JWTTokenAuthenticationFilter.class);
	private UserDetailsService userDetailsService;
	
	public JWTTokenAuthenticationFilter(AuthenticationManager authenticationManager, UserDetailsService userDetailsService) {
		super(authenticationManager);
		logger.trace("JWTTokenAuthenticationFilter");
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req,
									HttpServletResponse res,
									FilterChain chain) throws IOException, ServletException {
		logger.trace("JWTTokenAuthenticationFilter.doFilterInternal start for request "
//				+req.getContentLength()+" "
//				+req.getContentType()+" "
//				+req.getContextPath()+" "
//				+req.getLocalAddr()+" "
//				+req.getLocalName()+" "
//				+req.getLocalPort()+" "
//				+req.getMethod()+" "
//				+req.getPathInfo()+" "
//				+req.getPathTranslated()+" "
//				+req.getProtocol()+" "
//				+req.getQueryString()+" "
//				+req.getRemoteAddr()+" "
//				+req.getRemoteHost()+" "
//				+req.getRemotePort()+" "
//				+req.getRemoteUser()+" "
//				+req.getRequestedSessionId()+" "
//				+req.getRequestURI()+" "
//				+req.getScheme()+" "
//				+req.getServerName()+" "
//				+req.getServerPort()+" "
				+req.getServletPath()+" "
			);
		String header = req.getHeader(HEADER_STRING);

		if (header == null || !header.startsWith(TOKEN_PREFIX)) {
			logger.trace("JWTTokenAuthenticationFilter.doFilterInternal No header or wrong header: "+header);
			chain.doFilter(req, res);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = getAuthentication(req);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(req, res);
		logger.trace("JWTTokenAuthenticationFilter.doFilterInternal end");
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		logger.trace("JWTTokenAuthenticationFilter.getAuthentication start");
		String token = request.getHeader(HEADER_STRING);
		if (token != null) {
			logger.trace("JWTTokenAuthenticationFilter.getAuthentication Has token");
			try {
				// parse the token.
				String user = Jwts.parser()
						.setSigningKey(SECRET.getBytes())
						.parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
						.getBody()
						.getSubject();
				if (user != null) {
					logger.trace("JWTTokenAuthenticationFilter.getAuthentication Has user");
					UserDetails userDetails = this.userDetailsService.loadUserByUsername(user);
					return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
				}
			} catch(ExpiredJwtException e) {
				//TODO handle expiration properly
				return null;
			}
			logger.trace("JWTTokenAuthenticationFilter.getAuthentication end2");
			return null;
		}
		logger.trace("JWTTokenAuthenticationFilter.getAuthentication end1");
		return null;
	}
}
