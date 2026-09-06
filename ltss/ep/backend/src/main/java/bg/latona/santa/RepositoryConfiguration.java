package bg.latona.santa;

import javax.persistence.Entity;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import bg.latona.santa.repositories.CommonRepositoryImpl;

//example test call: curl --url https://localhost:8443/api/reports/changelog/Task/442 -k --header "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTUyOTY1NzIzN30.xXU4vnvLcVm4eg27cEjxsMOAP9TRPKWKxaOESyYFQTbwHwW39TiioO_Pvfd_TMfM_5Cvbp0HmlROCQmseVUHzw"
@Configuration
@EnableJpaRepositories(repositoryBaseClass = CommonRepositoryImpl.class)
public class RepositoryConfiguration implements RepositoryRestConfigurer {
	
	//needed for DroolsRepositoryEventListener
	@Autowired
	private WebApplicationContext appContext;

	private static DroolsRepositoryValidator beforeCreateValidator;
	private static DroolsRepositoryValidator beforeSaveValidator;
	private static DroolsRepositoryValidator afterCreateValidator;
	private static DroolsRepositoryValidator afterSaveValidator;
	
	public static DroolsRepositoryValidator getBeforeCreateValidator() {
		return RepositoryConfiguration.beforeCreateValidator;
	}
	
	public static DroolsRepositoryValidator getBeforeSaveValidator() {
		return RepositoryConfiguration.beforeSaveValidator;
	}

	public static DroolsRepositoryValidator getAfterCreateValidator() {
		return RepositoryConfiguration.afterCreateValidator;
	}
	
	public static DroolsRepositoryValidator getAfterSaveValidator() {
		return RepositoryConfiguration.afterSaveValidator;
	}
	
	//register the different databases we will use
	@Primary
	@Bean(name = "springDataSource")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSource springDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name = "registerDataSource")
	@ConfigurationProperties(prefix = "register.datasource")
	public DataSource registerDataSource() {
		return DataSourceBuilder.create().build();
	}
	
	@Bean(name = "workStreamDataSource")
	@ConfigurationProperties(prefix = "workstream.datasource")
	public DataSource workStreamDataSource() {
		return DataSourceBuilder.create().build();
	}
	
//	private Class<?>[] findAllClassesUsingClassLoader(String packageName, String suffix) {
//		System.out.println("findAllClassesUsingClassLoader for package "+packageName);
//		InputStream stream = CommonRecord.class//.getClassLoader()
//			.getResourceAsStream(packageName.replaceAll("[.]", "/"));
//		if(stream == null) {
//			System.out.println("Cannot find resource "+packageName.replaceAll("[.]", "/"));
//			return null;
//		} else {
//			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
//			return reader.lines()
//				.filter(line -> {
//					System.out.println("package line "+line);
//					return line.endsWith(suffix);
//				})
//				.map(line -> getClass(line, packageName))
//				.toArray(Class[]::new);
//		}
//	}
// 
//	private Class getClass(String className, String packageName) {
//		try {
//			if(className.lastIndexOf('.') < 0) {
//				return null;
//			}
//			System.out.println("Adding expose id for class "+className+" from package "+packageName);
//			String fullClassName = packageName + "."
//					+ className.substring(0, className.lastIndexOf('.'));
//			return Class.forName(fullClassName);
//		} catch (ClassNotFoundException e) {
//			// handle the exception
//		}
//		return null;
//	}
	
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
		//findAllClassesUsingClassLoader("","");
		//we need this when using Spring Boot - not to hide the IDs of the instances
//		String rootEntitiesPackageName = CommonRecord.class.getPackage().getName();
//		for(Package aPackage: Package.getPackages()) {
//			if(aPackage.getName().startsWith(rootEntitiesPackageName)) {
//				Class<?>[] entityClasses = findAllClassesUsingClassLoader(
//						rootEntitiesPackageName.equals(aPackage.getName()) ? "" : aPackage.getName().substring(rootEntitiesPackageName.length()+1)
//						,".class");
//				if(entityClasses != null) {
//					config.exposeIdsFor(entityClasses);
//				}
//			}
//		}

//		final RepositoryResourceMappings repositoryResourceMappings = appContext.getBean(RepositoryResourceMappings.class);
//		if(repositoryResourceMappings == null) {
//			throw new RuntimeException("Cannot get RepositoryResourceMappings bean");
//		}
//		repositoryResourceMappings.get().map(resourceMetadata -> {
//			System.out.println("Adding expose id for class "+resourceMetadata.getDomainType());
//			config.exposeIdsFor(resourceMetadata.getDomainType());
//			return resourceMetadata;
//		});
		
		ClassPathScanningCandidateComponentProvider scanner =
				new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
		for (org.springframework.beans.factory.config.BeanDefinition bd : scanner.findCandidateComponents(SantaApplication.class.getPackage().getName())) {
			//System.out.println("Adding expose id for class "+bd.getBeanClassName());
			try {
				config.exposeIdsFor(Class.forName(bd.getBeanClassName()));
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Class not found: "+bd.getBeanClassName()+" while trying to expose Id for REST");
			}
		}
	}
	
	//call Drools rule engine on data modification events of the repositories
	@Override
	public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
		beforeCreateValidator = new DroolsRepositoryValidator("beforeCreate", appContext);
		v.addValidator("beforeCreate", beforeCreateValidator);
		afterCreateValidator = new DroolsRepositoryValidator("afterCreate", appContext);
		v.addValidator("afterCreate", afterCreateValidator);
		beforeSaveValidator = new DroolsRepositoryValidator("beforeSave", appContext);
		v.addValidator("beforeSave", beforeSaveValidator);
		afterSaveValidator = new DroolsRepositoryValidator("afterSave", appContext);
		v.addValidator("afterSave", afterSaveValidator);
		v.addValidator("beforeLinkSave", new DroolsRepositoryValidator("beforeLinkSave", appContext));
		v.addValidator("afterLinkSave", new DroolsRepositoryValidator("afterLinkSave", appContext));
		v.addValidator("beforeDelete", new DroolsRepositoryValidator("beforeDelete", appContext));
		v.addValidator("afterDelete", new DroolsRepositoryValidator("afterDelete", appContext));
	}
}