package bg.latona.santa.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import bg.latona.santa.entities.security.SecUser;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
	private UserDetailsService userDetailsService;
	@Value("$access.control.origin")
	private String accessControlOriginValue;

	public SecurityConfiguration(UserDetailsService userDetailsService) {
		logger.trace("SecurityConfiguration");
		this.userDetailsService = userDetailsService;
	}
	
	public class WebSecurity {
		public boolean checkEndpointId(Authentication authentication, String roleName) {
			logger.trace("SecurityConfiguration.WebSecurity.checkEndpointId");
			for(GrantedAuthority authority: authentication.getAuthorities()) {
				if(((SimpleGrantedAuthority)authority).getAuthority().equals(roleName)) {
					logger.debug("Granted permission: "+roleName);
					return true;
				}
			}
			logger.info("No permission "+roleName+" for user "+authentication.getName());
			return false;
		}
	}
	
	@Bean(name = "webSecurity")
	public WebSecurity getWebSecurity() {
		logger.trace("SecurityConfiguration.WebSecurity.getWebSecurity");
		return new WebSecurity();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		logger.trace("SecurityConfiguration.configure(HttpSecurity http)");
		http
			//can't disable anonymous authentication - needed for static content
			//.anonymous().disable()
			//set to return 401 (not 403) when not authenticated
			.exceptionHandling().authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)).and()
			.cors().and()
			.addFilter(new JWTUsernamePasswordAuthenticationFilter(authenticationManager()))
			.addFilter(new JWTTokenAuthenticationFilter(authenticationManager(), this.userDetailsService))
			.authorizeRequests()
				//per method access to each repository
				.antMatchers(HttpMethod.DELETE, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('DELETE_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_DELETE_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.GET, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('GET_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_GET_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.HEAD, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('HEAD_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_HEAD_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.OPTIONS, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('OPTIONS_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_OPTIONS_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.PATCH, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('PATCH_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_PATCH_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.POST, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('POST_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_POST_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.PUT, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('PUT_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_PUT_'.concat(#endpointId)))")
				.antMatchers(HttpMethod.TRACE, "/api/{endpointId}/**").access("isAuthenticated() and (hasRole('TRACE_ANY') or @webSecurity.checkEndpointId(authentication,'ROLE_TRACE_'.concat(#endpointId)))")
				.antMatchers("/**", "/*.*").permitAll()
				.anyRequest().authenticated()
				.and()
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
			.csrf().disable(); //TODO check if CSRF and CrossOrigin can be enabled when frontend is hosted by the backend
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		logger.trace("SecurityConfiguration.configure(AuthenticationManagerBuilder auth)");
		auth.userDetailsService(userDetailsService).passwordEncoder(SecUser.PASSWORD_ENCODER);
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		logger.trace("SecurityConfiguration.corsConfigurationSource");
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		config.applyPermitDefaultValues();
		config.addAllowedMethod(HttpMethod.PUT);
		config.addAllowedMethod(HttpMethod.DELETE);
		config.addAllowedMethod(HttpMethod.OPTIONS);
		config.addAllowedMethod(HttpMethod.PATCH);
		config.addAllowedMethod(HttpMethod.TRACE);
		config.addAllowedOrigin(accessControlOriginValue);
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
