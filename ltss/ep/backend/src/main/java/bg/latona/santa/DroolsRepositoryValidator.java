package bg.latona.santa;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
//import java.util.concurrent.TimeUnit;

import org.apache.camel.CamelContext;
import org.kie.api.KieServices;
import org.kie.api.definition.rule.Rule;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.context.PersistentEntities;
//import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;

import bg.latona.santa.entities.*;
//import bg.latona.santa.entities.santa.common.COrder;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.reports.CustomPersistentEntityResourceAssembler;
import bg.latona.santa.security.SantaUser;
import bg.latona.santa.DroolsRuleProcedures;

public class DroolsRepositoryValidator implements Validator {

	//don't use together with logging.level.org.drools.core.phreak.RuleExecutor=TRACE, there is some logger fight happening
	private static Logger logger = LoggerFactory.getLogger(DroolsRepositoryValidator.class);
	
	public class TrackingAgendaEventListener extends DefaultAgendaEventListener {
		@Override
		public void afterMatchFired(AfterMatchFiredEvent event) {
			Rule rule = event.getMatch().getRule();

			String ruleName = rule.getName();
			Map<String, Object> ruleMetaDataMap = rule.getMetaData();

			//matchList.add(event.getMatch());
			StringBuilder sb = new StringBuilder("Rule fired: " + ruleName);

			if (ruleMetaDataMap.size() > 0) {
				sb.append("\n  With [" + ruleMetaDataMap.size() + "] meta-data:");
				for (String key : ruleMetaDataMap.keySet()) {
					sb.append("\n	key=" + key + ", value="
							+ ruleMetaDataMap.get(key));
				}
			}

			logger.trace(sb.toString());
		}
	}
	
	public class Result {
		private List<FieldError> fieldErrors = new LinkedList<FieldError>();
		private List<String> warnings = new LinkedList<String>();
		private List<String> infos = new LinkedList<String>();
		
		public boolean hasErrors() {
			return fieldErrors.size() > 0;
		}
		
		public List<FieldError> getFieldErrors() {
			return fieldErrors;
		}
		
		public void rejectValue(String objectName, String field,String code, Object value) {
			String[] codes = {code};
			fieldErrors.add(new FieldError(objectName,field,value,false,codes,null,null)); 
		}
		
		public void add(String message, boolean isWarning) {
			if(!isWarning) {
				infos.add(message);
			} else {
				warnings.add(message);
			}
		}
		
		public void handleResults() {
			for(FieldError fieldError : fieldErrors) {
				logger.debug(fieldError.toString());
			}
			for(String warning : warnings) {
				logger.warn(warning);
			}
			for(String info : infos) {
				logger.debug(info);
			}
		}
	}

	private KieContainer kieContainer;
	private StatelessKieSession kieSession;
	private DroolsRuleProcedures droolsRuleProcedures;
	private MappingJackson2HttpMessageConverter halJacksonHttpMessageConverter;
	private PersistentEntities persistentEntities;
	private Associations associations;
	//private SelfLinkProvider selfLinkProvider;
	private CamelContext camelContext;
	
	private String validationEvent;
	
	public DroolsRepositoryValidator(String validationEvent, WebApplicationContext appContext) {
		if(this.kieContainer == null) {
			KieServices kieServices = KieServices.Factory.get();
			this.kieContainer = kieServices.getKieClasspathContainer();
			this.kieSession = this.kieContainer.newStatelessKieSession("santaStatelessSession");
			if(logger.isTraceEnabled()) {
				AgendaEventListener agendaEventListener = new TrackingAgendaEventListener();
				this.kieSession.addEventListener(agendaEventListener);
			}
		}
		if(appContext == null) logger.error("DroolsRepositoryValidator: appContext is null!!!");
		this.validationEvent = validationEvent;
		this.droolsRuleProcedures = appContext.getBean(DroolsRuleProcedures.class);
		if(droolsRuleProcedures == null) {
			throw new RuntimeException("Cannot get RepositoryResourceMappings bean");
		}
		this.halJacksonHttpMessageConverter = appContext.getBean("halJacksonHttpMessageConverter", MappingJackson2HttpMessageConverter.class);

		this.persistentEntities = appContext.getBean("persistentEntities", PersistentEntities.class);
		this.associations = appContext.getBean(Associations.class);
		//this.selfLinkProvider = appContext.getBean("selfLinkProvider", SelfLinkProvider.class);
		this.camelContext = appContext.getBean(CamelContext.class);
	}
	
	@Override
	public boolean supports(Class<?> clazz) {
		//return WebsiteUser.class.equals(clazz);
		return true; //validate everything!!!
	}
	
	@Override
	public void validate(Object obj, Errors errorsForTheValidatedEntity) {
		//synchronized to avoid parallel updates and validations - until we find a proper DB locking solution OR solution by splitting the rules into synchronized (all that depend on other objects, e.g. unique checks, foreign key checks, aggregate calculations) and non-synchronized sets (e.g. simple defaults and validations)
		synchronized (DroolsRepositoryValidator.class) {
			//for testing of parallel autoinc
	//		if(obj instanceof COrder) {
	//			try {
	//				TimeUnit.SECONDS.sleep(5);
	//			} catch (InterruptedException e1) {
	//				// TODO Auto-generated catch block
	//				e1.printStackTrace();
	//			}
	//		}
			if(logger.isDebugEnabled()) {
				try {
					ObjectMapper objectMapperTmp = new ObjectMapper();
					logger.debug("Validation event "+this.validationEvent+" for object "+obj.getClass().toString()+": "+objectMapperTmp.writeValueAsString(obj));
					//Thread.dumpStack();
				} catch(com.fasterxml.jackson.core.JsonProcessingException e) {
					logger.debug("Validation event "+this.validationEvent+" for object: "+obj.toString());
				}
			}
			Result warnings = new Result();
			SecUser currentUser = null;
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	
			if (authentication != null && authentication.isAuthenticated()) {
				currentUser = ((SantaUser) authentication.getPrincipal()).getSecUser();
			} else if(obj instanceof CommonRecord && ((CommonRecord) obj).getCreatedBy() != null){
				currentUser = ((CommonRecord) obj).getCreatedBy();
			} else {
				throw new DroolsRuleException("No creator user for the validated object",warnings);
			}
			
			StatelessKieSession kSession = kieSession; //stateless session should be reusable (optimize without the slow creation)
	
			DroolsRuleProcedures.CleanEmWrapper cleanEmWrapper = this.droolsRuleProcedures.getCleanEmWrapper();
			kSession.setGlobal("camelContext", camelContext);
			kSession.setGlobal("cleanEmWrapper", cleanEmWrapper);
			kSession.setGlobal("validationEvent", this.validationEvent);
			kSession.setGlobal("droolsRuleProcedures", this.droolsRuleProcedures);
			kSession.setGlobal("triggerObject", obj);
			Instant currentInstant = Calendar.getInstance().toInstant();
			kSession.setGlobal("currentDate", Date.from(currentInstant));
			kSession.setGlobal("currentLocalDate", currentInstant.atZone(ZoneId.systemDefault()).toLocalDate());
			kSession.setGlobal("currentUser", currentUser);
			kSession.setGlobal("warnings", warnings);
			
			
			List<CommonRecord> entitiesToBeSaved = new LinkedList<CommonRecord>(); //TODO: check if one entity can be more than once in the list
			kSession.setGlobal("entitiesToBeSaved", entitiesToBeSaved);
			
			kSession.execute(obj);
			cleanEmWrapper.release();
			warnings.handleResults();
			if(warnings.hasErrors()) {
				//response seems to be based on org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
				//maybe MethodArgumentNotValidException is more appropriate
				StringBuilder errMsg = new StringBuilder();
				errMsg.append("[");
				//get JSON mapper that is copy of the default
				ObjectMapper objectMapper = halJacksonHttpMessageConverter.getObjectMapper().copy();
				for(FieldError err: warnings.getFieldErrors()) {
					try {
						logger.trace("Drools RejectedValue: "+err.getRejectedValue()+" "+persistentEntities);
						String rejectedValue = "null";
						if(!(err.getRejectedValue() == null || err.getRejectedValue() == "")) {
							if(err.getRejectedValue() instanceof CommonRecord) {
								CustomPersistentEntityResourceAssembler assembler = new CustomPersistentEntityResourceAssembler(null, persistentEntities, associations, null);
								EntityModel entityModel = assembler.toModel(err.getRejectedValue());
								rejectedValue = objectMapper.writeValueAsString(entityModel);
							}
						}
						errMsg.append("{\"error\": \"").append(err.getCode())
							.append("\", \"field\": \"").append(err.getField())
							.append("\", \"value\": ").append(rejectedValue)
							.append("},");
					} catch(com.fasterxml.jackson.core.JsonProcessingException e) {
						logger.trace("Drools errors' JsonProcessingException: "+e.toString());
						errMsg.append("{ \"error\": \"").append(err.getCode())
							.append("\", \"field\": \"").append(err.getField())
							.append("\", \"value\": \"").append(err.getRejectedValue() == null || err.getRejectedValue() == "" ? "null" : err.getRejectedValue().toString().replace("\"", "\\\""))
							.append("\"},");
					}
				}
				errMsg.append("{}");
				errMsg.append("]");
				logger.trace("DroolsRuleException: "+errMsg.toString());
				if( currentUser.getName().startsWith("camel") ) {
					logger.error("Camel initiated DroolsRuleException: "+errMsg.toString());
				}
				throw new DroolsRuleException(errMsg.toString(), warnings);
			}
			//TODO test if it is better to save here (at this point) the entities edited/created by the rules, because now the created entities are saved before all validations are passed
			for(CommonRecord entity : entitiesToBeSaved) {
				logger.trace("Entity saved: "+entity);
				droolsRuleProcedures.save(entity);
			}
			if(logger.isDebugEnabled()) {
				try {
					ObjectMapper objectMapperTmp = new ObjectMapper();
					logger.debug("End validation event "+this.validationEvent+" for object "+obj.getClass().toString()+": "+objectMapperTmp.writeValueAsString(obj));
					//Thread.dumpStack();
				} catch(com.fasterxml.jackson.core.JsonProcessingException e) {
					logger.debug("End validation event "+this.validationEvent+" for object: "+obj.toString());
				}
			}
		}
	}
}