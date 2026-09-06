package bg.latona.santa.integration;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.data.repository.support.Repositories;

//@Component //automatically picked up by @SpringBootApplication
public class DynamicRouteStarter implements CommandLineRunner { //gets run after all the beans are created and registered

	private static Logger logger = LoggerFactory.getLogger(DynamicRouteStarter.class);

	public static String CRM_HOST;
	public static boolean isTestMode;
	Environment env;
	CamelContext camelContext;
	private Repositories repositories = null;

	//@Autowired
	public DynamicRouteStarter(Repositories repositories, CamelContext camelContext, Environment env) {
		logger.trace("DynamicRouteStarter created with parameters: \n"+repositories+"\n"+camelContext);
		this.repositories = repositories;
		this.camelContext = camelContext;
		this.env = env;
		DynamicRouteStarter.CRM_HOST = env.getProperty("santa.integration.CRM_HOST");
		String propTestMode = env.getProperty("santa.integration.isTestMode");
		DynamicRouteStarter.isTestMode = propTestMode != null && propTestMode.equals("true");
		if(DynamicRouteStarter.isTestMode) {
			logger.warn("Starting routes in test mode");
		} else {
			logger.warn("Starting routes in production mode");
		}
	}

	@Override
	public void run(String... arg0) throws Exception {
		//TODO add possibility to monitor the routes:
		//https://lburgazzoli.github.io/2017/10/13/A-camel-running-in-the-clouds-part-3.html
		//https://github.com/apache/camel/tree/master/examples/camel-example-spring-boot-health-checks
		//try {
			//camelContext.addRoutes(new AuthorizationRouteBuilder(1L)); //auth for WATO company
			//camelContext.addRoutes(new AuthorizationRouteBuilder(2L)); //auth for Ind. Parts company
			
			//while(AuthorizationRouteBuilder.authToken.size() < 1) {
			//	logger.info("Route start is waiting for authorization tokens: "+AuthorizationRouteBuilder.authToken.size()+"/2 available");
			//	TimeUnit.SECONDS.sleep(10);
			//}
			
			//camelContext.addRoutes(new CamelRouteBuilder(1L));
			//camelContext.addRoutes(new CamelRouteBuilder(2L));
			
			//camelContext.addRoutes(new ImportFromDBRouteBuilder(1L));
			//camelContext.addRoutes(new ImportFromDBRouteBuilder(2L));
			//camelContext.addRoutes(new ScheduleValidationRouteBuilder(1L));
			
			//MailAccountRepository mailAccountRepository = ((MailAccountRepository) repositories.getRepositoryFor(MailAccount.class).get());
			
			//camelContext.addRoutes(new SendMailRouteBuilder(1L, mailAccountRepository));
			//camelContext.addRoutes(new SendMailRouteBuilder(2L, mailAccountRepository));
			
//			Iterable<MailAccount> accounts = mailAccountRepository.findAll();
//			for(MailAccount account : accounts) {
//				if(!account.isDeleted()) {
//					logger.debug("DynamicRouteStarter add route for account: "+account.getId()+"; company: "+account.getCompany().getCode()+" "+account.getCompany().getName());
//					camelContext.addRoutes(new ReceiveMailRouteBuilder(account.getCompany().getCode(), mailMessageRepository, account, reportsRepository));
//				}
//			}
		//} catch (FailedToStartRouteException e) {
		//	logger.error(e.getMessage());
		//	e.printStackTrace();
		//}
	}

}
