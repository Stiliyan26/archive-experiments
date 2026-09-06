package bg.latona.santa.reports;

import bg.latona.santa.entities.CommonRecord;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.nepal.IbexEnergyDeal;
import bg.latona.santa.entities.nepal.PowerPlantProtocol;
import bg.latona.santa.entities.nepal.Schedule;
import bg.latona.santa.entities.santa.common.CReserveQuantity;
import bg.latona.santa.repositories.DBFileRepository;
import bg.latona.santa.repositories.IbexEnergyDealRepository;
import bg.latona.santa.repositories.ScheduleRepository;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.querydsl.core.Tuple;
import org.apache.camel.CamelContext;
import org.apache.camel.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.support.SelfLinkProvider;
import org.springframework.data.rest.webmvc.mapping.Associations;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@CrossOrigin
@RestController
@RequestMapping(value = "/api/reports")
public class ReportsRestController {

    private final ReportsRepository reportsRepository;
    private final ReportBuilder reportBuilder;
    private final WarehouseProcedures warehouseProcedures;

	private final FinanceProcedures financeProcedures;

	private final NEPALProcedure NEPALProcedure;

	@Autowired
	private RepositoryResourceMappings mappings;

    @Autowired
    private WebApplicationContext appContext;
    private Repositories repositories = null;

	@Autowired
	private CamelContext camelContext;
	
	@Autowired
	ReportsRestController(ReportsRepository reportsRepository, ReportBuilder reportBuilder, WarehouseProcedures warehouseProcedures, FinanceProcedures financeProcedures, NEPALProcedure NEPALProcedure) {
		this.reportsRepository = reportsRepository;
		this.reportBuilder = reportBuilder;
		this.warehouseProcedures = warehouseProcedures;
		this.financeProcedures = financeProcedures;
		this.NEPALProcedure = NEPALProcedure;
	}

    Repositories getRepositories() {
        if (repositories == null) {
            repositories = new Repositories(appContext);
        }
        return repositories;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/wasteQuantity")
    @ResponseBody
    List<Object[]> wasteQuantity() {
        return reportsRepository.getWasteQuantities();
    }

    @RequestMapping(method = RequestMethod.POST, value = "/timeSheetsPerDay/{fromDate}/{toDate}")
    @ResponseBody
    List<Object[]> timeSheetsPerDay(@PathVariable @DateTimeFormat(pattern = "ddMMyyyy") Date fromDate, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") Date toDate) {
        return reportsRepository.timeSheetsPerDay(fromDate, toDate);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/subTasksPlannedTime/{Id}")
    @ResponseBody
    List<PlannedTimeDTO> reportSubTasksPlannedTime(@PathVariable Long Id) {
        return reportsRepository.reportSubTasksPlannedTime(Arrays.asList(Id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/subTasksPlannedIncomeOrExpense/{Id}")
    @ResponseBody
    List<PlannedIncomeOrExpenseDTO> reportSubTasksPlannedIncomeOrExpense(@PathVariable Long Id) {
        return reportsRepository.reportSubTasksPlannedIncomeOrExpense(Arrays.asList(Id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/subTasksActualTime/{Id}")
    @ResponseBody
    List<ActualTimeDTO> reportSubTasksActualTime(@PathVariable Long Id) {
        return reportsRepository.reportSubTasksActualTime(Arrays.asList(Id));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/subTasksActualRevenuesAndExpenses/{Id}")
    @ResponseBody
    List<ActualRevenuesAndExpensesDTO> reportSubTasksActualRevenuesAndExpenses(@PathVariable Long Id) {
        return reportsRepository.reportSubTasksActualRevenuesAndExpenses(Arrays.asList(Id));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/requestOfferContractOpportunityAnalysis")
    @ResponseBody
    List<Object[]> reportRequestTasksOpportunityAnalysisDTO(@RequestBody AnalysisRequest analysisRequest) {
        return reportsRepository.reportRequestOfferContractOpportunityAnalysis(analysisRequest.getFromDate());
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getCurrentStock")
    @ResponseBody
    List<Map<String, Object>> getCurrentStocks(@RequestBody String[] goodsCodes) {
        return warehouseProcedures.getCurrentStocks(goodsCodes);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/makeRequest/{offrId}/{pUser}/{remark}")
    @ResponseBody
    List<CReserveQuantity> makeRequest(@PathVariable Long offrId, @PathVariable String pUser, @PathVariable String remark) {
        return warehouseProcedures.makeRequest(offrId, pUser, remark);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getStocksDate/{outCodeId}/{parId}/{toDate}")
    @ResponseBody
    List<Map<String, Object>> getStocksDate(@PathVariable Long outCodeId, @PathVariable Long parId, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") LocalDate toDate) {
        return warehouseProcedures.getStocksDate(outCodeId, parId, toDate);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getStocks/{outCodeId}/{fromDate}/{toDate}")
    @ResponseBody
    List<Map<String, Object>> getStocks(@PathVariable Long outCodeId, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") LocalDate fromDate, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") LocalDate toDate) {
        return warehouseProcedures.getStocks(outCodeId, fromDate, toDate);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getOrderGoods/{charVar}")
    @ResponseBody
    Json getOrderGoods(@PathVariable Long charVar) throws JsonProcessingException {
        //get JSON mapper that is copy of the default
        ObjectMapper mapper = halJacksonHttpMessageConverter.getObjectMapper().copy();
        return new Json(mapper.writeValueAsString(warehouseProcedures.getOrderGoods((charVar == null || charVar <= 0 ? null : charVar))));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/makeOrder/{parId}/{OutCode}")
    @ResponseBody
    Map<String, List<Object>> makeOrder(@PathVariable Long parId, @PathVariable Long OutCode) {
        return warehouseProcedures.makeOrder(parId, OutCode);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/blockStocks/{pdeyId}/{pOutCode}")
    @ResponseBody
    List<Object> blockStocks(@PathVariable Long pdeyId, @PathVariable Long pOutCode) {
        return warehouseProcedures.blockStocks(pdeyId, pOutCode);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/modifyStocks/{pdeyId}/{pOutCode}")
    @ResponseBody
    List<Object> modifyStocks(@PathVariable Long pdeyId, @PathVariable Long pOutCode) {
        return warehouseProcedures.modifyStocks(pdeyId, pOutCode);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/makeSale/{offrId}/{pUser}/{remark}")
    @ResponseBody
    Map<String, Object> makeSale(@PathVariable Long offrId, @PathVariable String pUser, @PathVariable String remark) {
        return warehouseProcedures.makeSale(offrId, pUser, remark);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/postToBridge/{pOutCode}/{pStartDate}/{pEndDate}/{pFlags}")
    @ResponseBody
    Map<String, Object> postToBridge(@PathVariable String pOutCode, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") LocalDate pStartDate, @PathVariable @DateTimeFormat(pattern = "ddMMyyyy") LocalDate pEndDate, @PathVariable String pFlags) {
        String currentUser = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            currentUser = authentication.getPrincipal().toString();
        }
        return warehouseProcedures.postToBridge(pOutCode, currentUser, pStartDate, pEndDate, pFlags);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/bridgeDataProcessUi/{pCode}/{pTrnId}")
    @ResponseBody
    Map<String, Object> bridgeDataProcessUi(@PathVariable String pCode, @PathVariable Long pTrnId) {
        return financeProcedures.bridgeDataProcessUi(pCode, pTrnId);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/createDefaultJournals/{pBahId}/{pIsCenerate}")
    @ResponseBody
    Map<String, Object> createDefaultJournals(@PathVariable Long pBahId, @PathVariable String pIsCenerate) {
        return financeProcedures.createDefaultJournals(pBahId, pIsCenerate);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/bahEnforceRulesUi/{pBahId}/{pId}/{pFlag}")
    @ResponseBody
    Map<String, Object> bahEnforceRulesUi(@PathVariable Long pBahId, @PathVariable Long pId, @PathVariable int pFlag) {
        return financeProcedures.bahEnforceRulesUi(pBahId, pId, pFlag);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/stornoJournalUi/{pJolId}/{pStornoType}")
    @ResponseBody
    Map<String, Object> stornoJournalUi(@PathVariable Long pJolId, @PathVariable Long pStornoType) {
        return financeProcedures.stornoJournalUi(pJolId, pStornoType);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/accountingAggregateJournalsUi/{pJolId}")
    @ResponseBody
    Integer stornoJournalUi(@PathVariable Long pJolId) {
        return financeProcedures.accountingAggregateJournalsUi(pJolId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getOfferState/{offrId}")
    @ResponseBody
    List<Map<String, Object>> getOfferState(@PathVariable Long offrId) {
        return warehouseProcedures.getOfferState(offrId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/coaBalanceAllByMonth/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pSConsolidated}/{pSCoaType}")
    @ResponseBody
    List<Map<String, Object>> coaBalanceAllByMonth(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable String pSConsolidated, @PathVariable Long pSCoaType) {
        return financeProcedures.coaBalanceAllByMonth(pSOutCode, pSDateFrom, pSDateTo, pSConsolidated, pSCoaType);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/parBalanceByMonths/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pICoaId}/{pSConsolidated}/{pIParId}")
    @ResponseBody
    List<Map<String, Object>> parBalanceByMonths(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable Integer pICoaId, @PathVariable String pSConsolidated, @PathVariable Integer pIParId) {
        return financeProcedures.parBalanceByMonths(pSOutCode, pSDateFrom, pSDateTo, pICoaId, pSConsolidated, pIParId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/parBalanceByMonths2/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pICoaId}/{pSConsolidated}/{pIParId}")
    @ResponseBody
    List<Map<String, Object>> parBalanceByMonths2(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable Integer pICoaId, @PathVariable String pSConsolidated, @PathVariable Integer pIParId) {
        return financeProcedures.parBalanceByMonths2(pSOutCode, pSDateFrom, pSDateTo, pICoaId, pSConsolidated, pIParId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/parBalanceByMonthsNull/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pICoaId}/{pSConsolidated}/{pIParId}")
    @ResponseBody
    List<Map<String, Object>> parBalanceByMonthsNull(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable Integer pICoaId, @PathVariable String pSConsolidated, @PathVariable Integer pIParId) {
        return financeProcedures.parBalanceByMonthsNull(pSOutCode, pSDateFrom, pSDateTo, pICoaId, pSConsolidated, pIParId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/outBalanceByMonths/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pICoaId}/{pSConsolidated}/{pICcoutId}")
    @ResponseBody
    List<Map<String, Object>> outBalanceByMonths(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable Integer pICoaId, @PathVariable String pSConsolidated, @PathVariable Integer pICcoutId) {
        return financeProcedures.outBalanceByMonths(pSOutCode, pSDateFrom, pSDateTo, pICoaId, pSConsolidated, pICcoutId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/outBalanceByMonths2/{pSOutCode}/{pSDateFrom}/{pSDateTo}/{pICoaId}/{pSConsolidated}/{pICcoutId}")
    @ResponseBody
    List<Map<String, Object>> outBalanceByMonths2(@PathVariable String pSOutCode, @PathVariable String pSDateFrom, @PathVariable String pSDateTo, @PathVariable Integer pICoaId, @PathVariable String pSConsolidated, @PathVariable Integer pICcoutId) {
        return financeProcedures.outBalanceByMonths2(pSOutCode, pSDateFrom, pSDateTo, pICoaId, pSConsolidated, pICcoutId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/unpaidDocsByPar/{pSOutCode}/{pSBteSide}/{pSCons}")
    @ResponseBody
    List<Map<String, Object>> unpaidDocsByPar(@PathVariable String pSOutCode, @PathVariable Integer pSBteSide, @PathVariable String pSCons) {
        return financeProcedures.unpaidDocsByPar(pSOutCode, pSBteSide, pSCons);
    }

	@RequestMapping(method = RequestMethod.GET, value = "/unpaidDocsByParSum/{pSOutCode}/{pSBteSide}/{pSCons}")
	@ResponseBody
	List<Map<String,Object>> unpaidDocsByParSum(@PathVariable String pSOutCode, @PathVariable Integer pSBteSide, @PathVariable String pSCons) {
		return financeProcedures.unpaidDocsByParSum(pSOutCode, pSBteSide, pSCons);
	}

    //NEPAL
    @RequestMapping(method = RequestMethod.POST, value = "/importXML/{dbFileId}")
    @ResponseBody
    List<Map<String, Object>> importXML(@PathVariable Long dbFileId) {
        DBFile dbFile = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get()).findById(dbFileId).get();

		List<Map<String, Object>> mapList = NEPALProcedure.importXML(dbFile);
		
		camelContext.createProducerTemplate().sendBody("direct:undistributedDeals", null);

		return mapList;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getIbexEnergyDeal")
    @ResponseBody
    List<IbexEnergyDeal> getIbexEnergyDeal(Long minutes) throws IOException {
        return NEPALProcedure.getIbexEnergyDeal(minutes);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/energyDistributionCamelProcess")
    @ResponseBody
    List<Map<String, Object>> energyDistributionCamelProcess(@RequestBody List<Long> ibexEnergyDealIdList) throws IOException, ParserConfigurationException {
        List<IbexEnergyDeal> ibexEnergyDealList = new ArrayList<>();
        for (Long id : ibexEnergyDealIdList) {
            IbexEnergyDeal ibexEnergyDeal = ((IbexEnergyDealRepository) getRepositories().getRepositoryFor(IbexEnergyDeal.class).get()).findFirstByIdAndIsDistributedToSchedulesAndCompanyAndDeleted(id, false, NEPALProcedure.getSecUser().getCompany(), false);
            ibexEnergyDealList.add(ibexEnergyDeal);
        }
        return NEPALProcedure.energyDistributionCamelProcess(ibexEnergyDealList);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/sendMail/{scheduleIdList}")
    @ResponseBody
    List<Map<String, Object>> sendMail(@PathVariable List<Long> scheduleIdList) throws ParserConfigurationException {
        List<Schedule> scheduleList = new ArrayList<>();
        for (Long id : scheduleIdList) {
            Schedule schedule = ((ScheduleRepository) getRepositories().getRepositoryFor(Schedule.class).get()).findFirstByIdAndCompanyAndDeleted(id, NEPALProcedure.getSecUser().getCompany(), false);
            scheduleList.add(schedule);
        }
		return 	NEPALProcedure.sendScheduleMail(scheduleList);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importXLSXProducedSchedule/{dbFileId}")
    @ResponseBody
    List<Map<String, Object>> importXLSXProducedSchedule(@PathVariable Long dbFileId) {
        DBFile dbFile = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get()).findById(dbFileId).get();
        return NEPALProcedure.importXLSXProducedSchedule(dbFile);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importXLSXMeterReading/{dbFileId}")
    @ResponseBody
    List<Map<String, Object>> importXLSXMeterReading(@PathVariable Long dbFileId) {
        DBFile dbFile = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get()).findById(dbFileId).get();
        return NEPALProcedure.importXLSXMeterReading(dbFile);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/importIbexPriceDAM/{dbFileId}")
    @ResponseBody
    List<Map<String, Object>> importIbexPriceDAM(@PathVariable Long dbFileId) {
        DBFile dbFile = ((DBFileRepository) getRepositories().getRepositoryFor(DBFile.class).get()).findById(dbFileId).get();
        return NEPALProcedure.importIbexPriceDAM(dbFile);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/savePowerPlantProtocols/{powerPlantProtocols}")
    @ResponseBody
    List<Map<String, Object>> savePowerPlantProtocols(@PathVariable List<PowerPlantProtocol> powerPlantProtocols) {

        return NEPALProcedure.savePowerPlantProtocols(powerPlantProtocols);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getAuctionPrices")
    @ResponseBody
    List<Map<String, Object>> getAuctionPrices() throws IOException {
        return NEPALProcedure.getAuctionPrices();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/sumKwh/{pSDateFrom}/{pSDateTo}")
    @ResponseBody
    List<Map<String, Object>> sqlQuery(@PathVariable String pSDateFrom, @PathVariable String pSDateTo) {
        return NEPALProcedure.sumKwh(pSDateFrom, pSDateTo);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/routes")
    @ResponseBody
    public List<Map<String, Object>> listRoutes() {
        return Collections.singletonList(camelContext.getRoutes().stream()
                .collect(Collectors.toMap(Route::getId, route -> camelContext.getRouteStatus(route.getId()).name())));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/start/{routeId}")
    @ResponseBody
    public List<Map<String, Object>> startRoute(@PathVariable String routeId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if (routeId.equals("scheduleValidation1")) {
                camelContext.startRoute(routeId);
                camelContext.startRoute("undistributedDeals1");
				camelContext.createProducerTemplate().sendBody("direct:undistributedDeals", null);
                resultMap.put("Route " + routeId + " started.", routeId);
                resultMap.put("Route " + "undistributedDeals1" + " started.", "undistributedDeals1");
                result.add(resultMap);
            } else {
                camelContext.startRoute(routeId);
                resultMap.put("Route " + routeId + " started.", routeId);
                result.add(resultMap);
            }
            return result;
        } catch (Exception e) {
            resultMap.put("Failed to start route " + routeId + ": " + e.getMessage(), routeId);
            result.add(resultMap);
            return result;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/stop/{routeId}")
    @ResponseBody
    public List<Map<String, Object>> stopRoute(@PathVariable String routeId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();

        try {
            if (routeId.equals("scheduleValidation1")) {
                camelContext.stopRoute(routeId);
                camelContext.stopRoute("undistributedDeals1");
                resultMap.put("Route " + routeId + " stopped.", routeId);
                resultMap.put("Route " + "undistributedDeals1" + " stopped.", "undistributedDeals1");
                result.add(resultMap);
            } else {
                camelContext.stopRoute(routeId);
                resultMap.put("Route " + routeId + " stopped.", routeId);
                result.add(resultMap);
            }
            return result;
        } catch (Exception e) {
            resultMap.put("Failed to stop route " + routeId + ": " + e.getMessage(), routeId);
            result.add(resultMap);
            return result;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/startAll")
    @ResponseBody
    public String startAllRoutes() {
        try {
            List<Route> routes = camelContext.getRoutes();
            for (Route route : routes) {
                camelContext.startRoute(route.getId());
            }
            return "All routes started.";
        } catch (Exception e) {
            return "Failed to start all routes: " + e.getMessage();
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/stopAll")
    @ResponseBody
    public String stopAllRoutes() {
        try {
            List<Route> routes = camelContext.getRoutes();
            for (Route route : routes) {
                camelContext.stopRoute(route.getId());
            }
            return "All routes stopped.";
        } catch (Exception e) {
            return "Failed to stop all routes: " + e.getMessage();
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getUndistributedDeals")
    @ResponseBody
    List<IbexEnergyDeal> getUndistributedDeals() {
        return NEPALProcedure.getUndistributedDeals();
    }

//	@JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
//	public abstract class SecUserMixin {
//	    // Other settings if required such as @JsonProperty on abstract methods.
//	}

    @RequestMapping(method = RequestMethod.GET, value = "/changelog/{entityName}/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    List<Object> getChangeLog(@PathVariable String entityName, @PathVariable Long id) {
        return reportsRepository.findRevisions(entityName, id);
    }
//	public Json getChangeLog(@PathVariable String entityName, @PathVariable Long id) throws JsonProcessingException {
//		List<Object> objects = reportsRepository.findRevisions(entityName, id);
//		ObjectMapper mapper = new ObjectMapper();
//		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
//		mapper.addMixIn(SecUser.class, SecUserMixin.class);
//		return new Json(mapper.writeValueAsString(objects));
//	}
//	public ResponseEntity<CollectionModel<PersistentEntityResource>> getChangeLog(@PathVariable String entityName, @PathVariable Long id, PersistentEntityResourceAssembler persistentEntityResourceAssembler) {
//		List<Object> objects = reportsRepository.findRevisions(entityName, id);
//		return ResponseEntity.ok(new CollectionModel<PersistentEntityResource>(objects.stream()
//				.map(persistentEntityResourceAssembler::toModel)
//				.collect(Collectors.toList())));
//	}


    @Autowired
    private PagedResourcesAssembler pagedResourcesAssembler;
    @Autowired
    private PersistentEntities persistentEntities;
    @Autowired
    private Associations associations;
    @Autowired
    private SelfLinkProvider selfLinkProvider;

    class Json {
        private final String value;

        public Json(String value) {
            this.value = value;
        }

        @JsonValue
        @JsonRawValue
        public String value() {
            return value;
        }
    }

    @Autowired
    private MappingJackson2HttpMessageConverter halJacksonHttpMessageConverter;//mappingJackson2HttpMessageConverter,jacksonHttpMessageConverter,halJacksonHttpMessageConverter,alpsJsonHttpMessageConverter;

    public class ItemSerializer extends StdSerializer<CommonRecord> {

        public ItemSerializer() {
            this(null);
        }

        public ItemSerializer(Class<CommonRecord> t) {
            super(t);
        }

        @Override
        public void serialize(CommonRecord value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            jgen.writeStartObject();
            jgen.writeNumberField("id", value.getId());
            //jgen.writeObjectField("createdBy", value.getCreatedBy());
            jgen.writeObjectField("createdDate", value.getCreatedDate());
            //jgen.writeObjectField("lastModifiedBy", value.getLastModifiedBy());
            jgen.writeObjectField("lastModifiedDate", value.getLastModifiedDate());
            jgen.writeBooleanField("deleted", value.isDeleted());
            jgen.writeEndObject();
        }
    }

    public class SimpleResourceAssembler<T> implements RepresentationModelAssembler<T, EntityModel<T>> {

        private ReportDefinition reportDef;

        public SimpleResourceAssembler(ReportDefinition reportDef) {
            this.reportDef = reportDef;
        }

        @Override
        public EntityModel toModel(T entity) {
            Class<?> entityType;
            try {
                //build the query
                entityType = ReportsRepository.getClassFromRestUriOrClassName(mappings, reportDef.from);
                Object curEntity = ((Tuple) entity).get(0, entityType);
                EntityModel resource = EntityModel.of(curEntity);
                //return wrap(entity, entity).build();
                return resource;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            //EntityModel<T> resource = new EntityModel<T>(entity);
            //return resource;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/builder/{reportId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
        //PagedModel<PersistentEntityResource> reportBuilder(Pageable pageable, @PathVariable Long reportId) {
    Json reportBuilder(Pageable pageable, @PathVariable Long reportId, @RequestParam MultiValueMap<String, String> params) throws JsonProcessingException {
        //TODO get custom user report by reportId if != 0
        //TODO parse report definition from JSON
        //TODO merge report definition with URL params

        //get report data
        Page<javax.persistence.Tuple> resources = reportsRepository.reportBuilder(pageable, params);

        //get assembler imitating the default
        CustomPersistentEntityResourceAssembler assembler = new CustomPersistentEntityResourceAssembler(params, persistentEntities, associations, selfLinkProvider);
        //SimpleResourceAssembler assembler = new SimpleResourceAssembler(reportDef);

        //return pagedResourcesAssembler.toModel(resources, assembler);

        //get JSON mapper that is copy of the default
        ObjectMapper mapper = halJacksonHttpMessageConverter.getObjectMapper().copy();
        //SimpleModule module = new SimpleModule();
        //custom JSON serialization of the results
        //module.addSerializer(CommonRecord.class, new ItemSerializer());
        //mapper.registerModule(module);
        //ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new Json(mapper.writeValueAsString(pagedResourcesAssembler.toModel(resources, assembler)));
        //return new Json(mapper.writeValueAsString(reportsRepository.reportBuilder(pageable, reportDef)));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/native", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
        //PagedModel<PersistentEntityResource> reportBuilder(Pageable pageable, @PathVariable Long reportId) {
    Json nativeReport(Pageable pageable, @RequestParam MultiValueMap<String, String> params) throws JsonProcessingException, NoSuchFieldException {
        //TODO get custom user report by reportId if != 0
        //TODO parse report definition from JSON
        //TODO merge report definition with URL params

        //get report data
        Page<ObjectNode> resources = reportBuilder.nativeReport(pageable, params);

        //get assembler imitating the default
        //CustomPersistentEntityResourceAssembler assembler = new CustomPersistentEntityResourceAssembler(params, persistentEntities, associations, selfLinkProvider);
        //SimpleResourceAssembler assembler = new SimpleResourceAssembler(reportDef);

        //return pagedResourcesAssembler.toModel(resources, assembler);

        //get JSON mapper that is copy of the default
        ObjectMapper mapper = halJacksonHttpMessageConverter.getObjectMapper().copy();
        //SimpleModule module = new SimpleModule();
        //custom JSON serialization of the results
        //module.addSerializer(CommonRecord.class, new ItemSerializer());
        //mapper.registerModule(module);
        //ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new Json(mapper.writeValueAsString(pagedResourcesAssembler.toModel(resources)));
        //return new Json(mapper.writeValueAsString(reportsRepository.reportBuilder(pageable, reportDef)));
    }
}
