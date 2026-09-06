package bg.latona.santa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	@Value("${webResourceUrlSubDir}")
	private String webResourceUrlSubDir;
	
	//This configuration is used to expose the static resources under a "sub-directory"
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		if (!registry.hasMappingForPattern("/"+webResourceUrlSubDir+"/**")) {
			registry.addResourceHandler("/"+webResourceUrlSubDir+"/**")
				.addResourceLocations("classpath:/public/");
		}
	}

	//This configuration is used to expose the static resources under a "sub-directory"
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/"+webResourceUrlSubDir+"/").setViewName("forward:/"+webResourceUrlSubDir+"/index.html");
	}
}
