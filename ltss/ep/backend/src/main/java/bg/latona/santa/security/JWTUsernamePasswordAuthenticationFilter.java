package bg.latona.santa.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static bg.latona.santa.security.SecurityConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;


public class JWTUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	private static Logger logger = LoggerFactory.getLogger(JWTUsernamePasswordAuthenticationFilter.class);
	private AuthenticationManager authenticationManager;
	@Value("$access.control.origin")
	private String accessControlOriginValue;
	@Value("$access.control.origin.methods")
	private String accessControlMethodsValue;
	@Value("$access.control.origin.max.age")
	private String accessControlMaxAgeValue;
	@Value("$access.control.allowed.headers")
	private String accessControlAllowHeadersValue;
	@Value("$access.control.expose.headers")
	private String accessControlExposeHeadersValue;
	
	public JWTUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager) {
		logger.trace("JWTUsernamePasswordAuthenticationFilter");
		this.authenticationManager = authenticationManager;
		this.setFilterProcessesUrl("/api/login");
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest req,
												HttpServletResponse res) throws AuthenticationException {
		logger.trace("JWTUsernamePasswordAuthenticationFilter.attemptAuthentication");
		String password =req.getParameter("password");
		return authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						req.getParameter("username"),
						password,
						new ArrayList<>()
				)
		);

	}

	@Override
	protected void successfulAuthentication(HttpServletRequest req,
											HttpServletResponse res,
											FilterChain chain,
											Authentication auth) throws IOException, ServletException {
		logger.trace("JWTUsernamePasswordAuthenticationFilter.successfulAuthentication start");
		String token = Jwts.builder()
				.setSubject(((User) auth.getPrincipal()).getUsername())
				.setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
				.signWith(SignatureAlgorithm.HS512, SECRET.getBytes())
				.compact();

		res.setHeader("Access-Control-Allow-Credentials", "true");
		res.setHeader("Access-Control-Allow-Origin", accessControlOriginValue);
		res.setHeader("Access-Control-Allow-Methods", accessControlMethodsValue);
		res.setHeader("Access-Control-Max-Age", accessControlMaxAgeValue);
		res.setHeader("Access-Control-Allow-Headers", accessControlAllowHeadersValue);
		res.addHeader("Access-Control-Expose-Headers", accessControlExposeHeadersValue);
		res.getWriter().write("{\"Authorization\": \"Bearer "+ token +"\"}");
		res.setStatus(HttpServletResponse.SC_OK);
		logger.trace("JWTUsernamePasswordAuthenticationFilter.successfulAuthentication end");
	}
}

