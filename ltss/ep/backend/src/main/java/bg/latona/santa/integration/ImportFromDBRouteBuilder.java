package bg.latona.santa.integration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class ImportFromDBRouteBuilder extends RouteBuilder {

	public interface RowToEntityMapper {
		public void map(Map<String, Object> importRow, ObjectNode newEntityNode);
	}
	public interface RowToFilterStringMapper {
		public String map(Map<String, Object> importRow);
	}

	private static Logger logger = LoggerFactory.getLogger(ImportFromDBRouteBuilder.class);
	private Long companyCode;
	
	public ImportFromDBRouteBuilder(Long companyCode) {
		this.companyCode = companyCode;
	}
	
	@Override
	public void configure() throws Exception {
//		registerPartners();
		workstreamLegalPersons();
		workstreamLegalPersonGroups();
		workstreamArticles();
		workstreamImport(
				"ImportedWarehouseStock",
				"importedWarehouseStocks",
				180000,
				"SELECT *,CONVERT(BIGINT, CONVERT(VARBINARY, UpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vOnhandArticle"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, UpdateCount, 1))",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ArticleID") != null) newEntity.put("foreignArticleId",(String) importRow.get("ArticleID"));
					if(importRow.get("Quantity") != null) newEntity.put("quantity",(BigDecimal) importRow.get("Quantity"));
					if(importRow.get("QuantityReserve") != null) newEntity.put("quantityReserve",(BigDecimal) importRow.get("QuantityReserve"));
					if(importRow.get("ObjectID") != null) newEntity.put("warehouseId",(String) importRow.get("ObjectID"));
					if(importRow.get("Store") != null) newEntity.put("warehouseName",(String) importRow.get("Store"));
					if(importRow.get("TotalInCompany") != null) newEntity.put("totalInCompany",(BigDecimal) importRow.get("TotalInCompany"));
					if(importRow.get("ReserveInCompany") != null) newEntity.put("reserveInCompany",(BigDecimal) importRow.get("ReserveInCompany"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				(Map<String, Object> importRow) -> {
					return "foreignArticleId="+((String) importRow.get("ArticleID"))+"&warehouseId="+((String) importRow.get("ObjectID"));
				}
		);
		workstreamImport(
				"ImportedOrder",
				"importedOrders",
				180000,
				"SELECT DISTINCT OrderID AS ID,OrderDocNum,OrderDate,OrderTotal,OrderDeleted AS Deleted,CompID,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, OrderUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, OrderUpdateCount, 1))",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderDocNum") != null) newEntity.put("docNum",(String) importRow.get("OrderDocNum"));
					Calendar docDate = null;
					if(importRow.get("OrderDate") != null) {
						docDate = Calendar.getInstance();
						docDate.setTime((Date) importRow.get("OrderDate"));
						newEntity.put("docDate",docDate.getTimeInMillis());
					} 
					if(importRow.get("OrderTotal") != null) newEntity.put("total",(BigDecimal) importRow.get("OrderTotal"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		workstreamImport(
				"ImportedOrderRow",
				"importedOrderRows",
				180000,
				"SELECT DISTINCT OrderRowID AS ID,OrderID,ArticleID,CompID,OrderDeleted AS Deleted,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, OrderRowUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, OrderRowUpdateCount, 1))"
					+ " AND OrderRowID IS NOT NULL",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderID") != null) newEntity.put("foreignOrderId",(String) importRow.get("OrderID"));
					if(importRow.get("ArticleID") != null) newEntity.put("foreignArticleId",(String) importRow.get("ArticleID"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		workstreamImport(
				"ImportedExpeditionList",
				"importedExpeditionLists",
				180000,
				"SELECT DISTINCT ExpID AS ID,OrderID,CompID,ExpDocNum,ExpDocDate,ExpDeleted AS Deleted,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, expUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, expUpdateCount, 1))"
					+ " AND ExpID IS NOT NULL",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderID") != null) newEntity.put("foreignOrderId",(String) importRow.get("OrderID"));
					if(importRow.get("ExpDocNum") != null) newEntity.put("docNum",(String) importRow.get("ExpDocNum"));
					Calendar docDate = null;
					if(importRow.get("ExpDocDate") != null) {
						docDate = Calendar.getInstance();
						docDate.setTime((Date) importRow.get("ExpDocDate"));
						newEntity.put("docDate",docDate.getTimeInMillis());
					} 
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		workstreamImport(
				"ImportedExpeditionListRow",
				"importedExpeditionListRows",
				180000,
				"SELECT DISTINCT ExpRowID AS ID,OrderRowID,CompID,ExpID,ExpQty,ExpDeleted AS Deleted,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, expRowUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, expRowUpdateCount, 1))"
					+ " AND ExpRowID IS NOT NULL",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderRowID") != null) newEntity.put("foreignOrderRowId",(String) importRow.get("OrderRowID"));
					if(importRow.get("ExpID") != null) newEntity.put("foreignExpeditionListId",(String) importRow.get("ExpID"));
					if(importRow.get("ExpQty") != null) newEntity.put("quantity",(BigDecimal) importRow.get("ExpQty"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		workstreamImport(
				"ImportedInvoice",
				"importedInvoices",
				180000,
				"SELECT DISTINCT InvID AS ID,OrderID,CompID,InvDocNum,InvDocDate,InvDeleted AS Deleted,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, InvUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, InvUpdateCount, 1))"
					+ " AND InvID IS NOT NULL",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderID") != null) newEntity.put("foreignOrderId",(String) importRow.get("OrderID"));
					if(importRow.get("InvDocNum") != null) newEntity.put("docNum",(String) importRow.get("InvDocNum"));
					Calendar docDate = null;
					if(importRow.get("InvDocDate") != null) {
						docDate = Calendar.getInstance();
						docDate.setTime((Date) importRow.get("InvDocDate"));
						newEntity.put("docDate",docDate.getTimeInMillis());
					} 
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		workstreamImport(
				"ImportedInvoiceRow",
				"importedInvoiceRows",
				180000,
				"SELECT DISTINCT InvRowID AS ID,OrderRowID,CompID,InvID,InvQty,InvDeleted AS Deleted,"
					+ "CONVERT(BIGINT, CONVERT(VARBINARY, InvRowUpdateCount, 1)) UpdateCountAsBigInt"
					+ " FROM dbo.vInvoiceTracking"
					+ " WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, InvRowUpdateCount, 1))"
					+ " AND InvRowID IS NOT NULL",
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("OrderRowID") != null) newEntity.put("foreignOrderRowId",(String) importRow.get("OrderRowID"));
					if(importRow.get("InvID") != null) newEntity.put("foreignInvoiceId",(String) importRow.get("InvID"));
					if(importRow.get("InvQty") != null) newEntity.put("quantity",(BigDecimal) importRow.get("InvQty"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
					if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));
				},
				null
		);
		//TODO this will import all payments every time - find a way to optimize it - for now we put it on lower frequency
		workstreamImport(
				"ImportedInvoicePayment",
				"importedInvoicePayments",
				1800000, //30 min
				"SELECT ID,DocNum,DocDate,CompanyName,CompID,Deleted,TotalAmount,TotalNoVAT,TotalPayed,ContragentID,OrderNumber"
					+ " FROM dbo.vInvoicePayments"
					+ " WHERE CompID = "+companyCode,
				(Map<String, Object> importRow, ObjectNode newEntity) -> {
					if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
					if(importRow.get("ContragentID") != null) newEntity.put("foreignContragentId",(String) importRow.get("ContragentID"));
					if(importRow.get("DocNum") != null) newEntity.put("docNum",(String) importRow.get("DocNum"));
					Calendar docDate = null;
					if(importRow.get("DocDate") != null) {
						docDate = Calendar.getInstance();
						docDate.setTime((Date) importRow.get("DocDate"));
						newEntity.put("docDate",docDate.getTimeInMillis());
					} 
					if(importRow.get("CompanyName") != null) newEntity.put("companyName",(String) importRow.get("CompanyName"));
					if(importRow.get("OrderNumber") != null) newEntity.put("orderNumber",(String) importRow.get("OrderNumber"));
					if(importRow.get("TotalAmount") != null) newEntity.put("totalAmount",(BigDecimal) importRow.get("TotalAmount"));
					if(importRow.get("TotalNoVAT") != null) newEntity.put("totalNoVAT",(BigDecimal) importRow.get("TotalNoVAT"));
					if(importRow.get("TotalPayed") != null) newEntity.put("totalPayed",(BigDecimal) importRow.get("TotalPayed"));
					if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
					if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
				},
				null
		);
	}
	
	private void registerPartners() {
		from("timer:registerPartners?period=180000")
			.routeId("registerPartners"+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak
			//get last modified
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedSantaPartners?page=0&size=1&sort=dateUpdated,desc")
			.setHeader("dateUpdated").jsonpath("$._embedded.importedSantaPartners[0].dateUpdated",true)
			.choice()
				.when(header("dateUpdated").isNull())
					.setHeader("dateUpdated").constant("0")
			.end()
			//.to("log:import_last_modified?level=INFO&showAll=true&multiline=true")
			//TODO fix for cases where many rows have same threshold: first call query once with row limit, get min and max thresholds and then call it again with inclusive interval of the found min and max threshold, but without row limit
			.setBody(simple("select *, cast(extract(epoch from coalesce(date_updated,date_created)) as integer) calc_date_updated from register.cc_partners\n" + 
					"where cast(extract(epoch from coalesce(date_updated,date_created)) as integer) > ${header.dateUpdated}\n" + 
					"order by date_updated asc,date_created asc\n" + 
					"fetch first 1000 rows only"))
			//.to("log:import_retrieve_query?level=INFO&showAll=true&multiline=true")
			.to("jdbc:registerDataSource?useHeadersAsParameters=true")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					ArrayList<Map<String, Object>> partners = exchange.getIn().getBody(ArrayList.class);
					ArrayList<String> partnersJSON = new ArrayList<String>();
					ObjectMapper mapper = new ObjectMapper();
					for(Map<String, Object> partner : partners) {
						Calendar activeFromDate = null;
						if(partner.get("active_from_date") != null) {
							activeFromDate = Calendar.getInstance();
							activeFromDate.setTime((Date) partner.get("active_from_date"));
						}
						Calendar activeToDate = null;
						if(partner.get("active_to_date") != null) {
							activeToDate = Calendar.getInstance();
							activeToDate.setTime((Date) partner.get("active_to_date"));
						}
						//create JSON with only the available data (not to overwrite existing data when patching)
						ObjectNode newPartner = mapper.createObjectNode();
						if(partner.get("code") != null) newPartner.put("code",(String) partner.get("code"));
						if(partner.get("name") != null) newPartner.put("name",(String) partner.get("name"));
						if(partner.get("bulstat") != null) newPartner.put("bulstat",(String) partner.get("bulstat"));
						if(partner.get("egn") != null) newPartner.put("egn",(String) partner.get("egn"));
						if(partner.get("address") != null) newPartner.put("address",(String) partner.get("address"));
						if(partner.get("partner_type") != null) newPartner.put("partnerType",(String) partner.get("partner_type"));
						if(partner.get("tel") != null) newPartner.put("phone",(String) partner.get("tel"));
						if(partner.get("email") != null) newPartner.put("email",(String) partner.get("email"));
						if(partner.get("mol") != null) newPartner.put("mol",(String) partner.get("mol"));
						if(activeFromDate != null) newPartner.put("activeFromDate",activeFromDate.getTimeInMillis());
						if(activeToDate != null) newPartner.put("activeToDate",activeToDate.getTimeInMillis());
						if(partner.get("out_code") != null) newPartner.put("organizationUnitId",(String) partner.get("out_code"));
						if(partner.get("pgp_id") != null) newPartner.put("partnerGroupId",(Integer) partner.get("pgp_id"));
						if(partner.get("legal_status") != null) newPartner.put("legalStatusId",(String) partner.get("legal_status"));
						if(partner.get("calc_date_updated") != null) newPartner.put("dateUpdated",(Integer) partner.get("calc_date_updated"));

						//check if exists
						String authToken = (String) exchange.getIn().getHeader("Authorization");
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders headers = new HttpHeaders();
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
						headers.add(HttpHeaders.AUTHORIZATION, authToken);
						HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

						String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/importedSantaPartners?page=0&size=1"
								+ "&organizationUnitId="+partner.get("out_code")
								+ "&code="+partner.get("code"), 
								HttpMethod.GET, 
								entity, 
								String.class).getBody();
						
						JsonNode entityNode = mapper.readTree(jsonData).path("_embedded").path("importedSantaPartners").path(0);
						
						if(!entityNode.isNull() && !entityNode.isMissingNode()) {
							logger.debug("Node found, id: "+entityNode.path("id").asLong());
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "PATCH");
							exchange.getOut().setHeader("EntityID", entityNode.path("id").asLong());
						} else {
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
						}
						
						//output
						String json = mapper.writeValueAsString(newPartner);
						partnersJSON.add(json);
					}
					exchange.getOut().setBody(partnersJSON);
				}
			})
			.split(body())
			//.to("log:import_save_entity?level=INFO&showAll=true&multiline=true")
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedSantaPartners/${header.EntityID}")); //dynamic URI
	}
	
	private void workstreamLegalPersons() {
		from("timer:workstreamLegalPersons?period=180000")
			.routeId("workstreamLegalPersons"+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak
			//get last modified
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedLegalPersons?page=0&size=1&sort=updateCountAsBigInt,desc")
			.setHeader("updateCountAsBigInt").jsonpath("$._embedded.importedLegalPersons[0].updateCountAsBigInt",true)
			.choice()
				.when(header("updateCountAsBigInt").isNull())
					.setHeader("updateCountAsBigInt").constant("0")
			.end()
			//.to("log:import_last_modified?level=INFO&showAll=true&multiline=true")
			//retrieve updated rows
			.setBody(simple("SELECT *, CONVERT(BIGINT, CONVERT(VARBINARY, ContragentUpdateCount, 1)) UpdateCountAsBigInt FROM dbo.vContragents WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, ContragentUpdateCount, 1)) ORDER BY ContragentUpdateCount"))
			//.to("log:import_retrieve_query?level=INFO&showAll=true&multiline=true")
			.to("jdbc:workStreamDataSource?useHeadersAsParameters=true")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					ArrayList<Map<String, Object>> partners = exchange.getIn().getBody(ArrayList.class);
					ArrayList<String> partnersJSON = new ArrayList<String>();
					ObjectMapper mapper = new ObjectMapper();
					for(Map<String, Object> partner : partners) {
						//create JSON with only the available data (not to overwrite existing data when patching)
						ObjectNode newPartner = mapper.createObjectNode();
						if(partner.get("ID") != null) newPartner.put("foreignId",(String) partner.get("ID"));
						if(partner.get("CLName") != null) newPartner.put("clName",(String) partner.get("CLName"));
						if(partner.get("BULSTAT") != null) newPartner.put("bulstat",(String) partner.get("BULSTAT"));
						if(partner.get("CompanyName") != null) newPartner.put("companyName",(String) partner.get("CompanyName"));
						if(partner.get("VATNumber") != null) newPartner.put("vatNumber",(String) partner.get("VATNumber"));
						if(partner.get("Email") != null) newPartner.put("email",(String) partner.get("Email"));
						if(partner.get("PhoneNumber") != null) newPartner.put("phoneNumber",(String) partner.get("PhoneNumber"));
						if(partner.get("IsSupplier") != null) newPartner.put("supplier",(Boolean) partner.get("IsSupplier"));
						if(partner.get("IsClient") != null) newPartner.put("client",(Boolean) partner.get("IsClient"));
						if(partner.get("IsManufacturer") != null) newPartner.put("manufacturer",(Boolean) partner.get("IsManufacturer"));
						if(partner.get("IsSubContractor") != null) newPartner.put("subcontractor",(Boolean) partner.get("IsSubContractor"));
						if(partner.get("MolName") != null) newPartner.put("molName",(String) partner.get("MolName"));
						if(partner.get("RegCountry") != null) newPartner.put("regCountry",(String) partner.get("RegCountry"));
						if(partner.get("RegCity") != null) newPartner.put("regCity",(String) partner.get("RegCity"));
						if(partner.get("RegAddress") != null) newPartner.put("regAddress",(String) partner.get("RegAddress"));
						if(partner.get("CurrCountry") != null) newPartner.put("currCountry",(String) partner.get("CurrCountry"));
						if(partner.get("CurrCity") != null) newPartner.put("currCity",(String) partner.get("CurrCity"));
						if(partner.get("CurrAddress") != null) newPartner.put("currAddress",(String) partner.get("CurrAddress"));
						if(partner.get("CompID") != null) newPartner.put("compId",(Integer) partner.get("CompID"));
						if(partner.get("Deleted") != null) newPartner.put("foreignDeleted",(Boolean) partner.get("Deleted"));
						if(partner.get("UpdateCountAsBigInt") != null) newPartner.put("updateCountAsBigInt",(Long) partner.get("UpdateCountAsBigInt"));

						//check if exists
						String authToken = (String) exchange.getIn().getHeader("Authorization");
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders headers = new HttpHeaders();
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
						headers.add(HttpHeaders.AUTHORIZATION, authToken);
						HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
				
						String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/importedLegalPersons?page=0&size=1"
								+ "&foreignId="+partner.get("ID"), 
								HttpMethod.GET, 
								entity, 
								String.class).getBody();
						
						JsonNode entityNode = mapper.readTree(jsonData).path("_embedded").path("importedLegalPersons").path(0);
						
						if(!entityNode.isNull() && !entityNode.isMissingNode()) {
							logger.debug("Node found, id: "+entityNode.path("id").asLong());
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "PATCH");
							exchange.getOut().setHeader("EntityID", entityNode.path("id").asLong());
						} else {
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
						}
						
						//output
						String json = mapper.writeValueAsString(newPartner);
						partnersJSON.add(json);
					}
					exchange.getOut().setBody(partnersJSON);
				}
			})
			.split(body())
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedLegalPersons/${header.EntityID}")); //dynamic URI
	}

	private void mapImportedLegalPersonGroups(Map<String, Object> importRow, ObjectNode newEntityNode) {
		if(importRow.get("SetGroupID") != null) newEntityNode.put("foreignId",(String) importRow.get("SetGroupID"));
		if(importRow.get("ID") != null) newEntityNode.put("contragentID",(String) importRow.get("ID"));
		if(importRow.get("MainGroupID") != null) newEntityNode.put("mainGroupID",(String) importRow.get("MainGroupID"));
		if(importRow.get("GroupID") != null) newEntityNode.put("groupID",(String) importRow.get("GroupID"));
		if(importRow.get("MainGroupCode") != null) newEntityNode.put("mainGroupCode",(String) importRow.get("MainGroupCode"));
		if(importRow.get("MainGroupName") != null) newEntityNode.put("mainGroupName",(String) importRow.get("MainGroupName"));
		if(importRow.get("GroupCode") != null) newEntityNode.put("groupCode",(String) importRow.get("GroupCode"));
		if(importRow.get("GroupName") != null) newEntityNode.put("groupName",(String) importRow.get("GroupName"));
		if(importRow.get("CompID") != null) newEntityNode.put("compId",(Integer) importRow.get("CompID"));
		Long updateCountAsBigInt = 0L;
		if(importRow.get("MainGroupUpdateCountAsBigInt") != null) updateCountAsBigInt = Math.max((Long) importRow.get("MainGroupUpdateCountAsBigInt"), updateCountAsBigInt);
		if(importRow.get("GroupUpdateCountAsBigInt") != null) updateCountAsBigInt = Math.max((Long) importRow.get("GroupUpdateCountAsBigInt"), updateCountAsBigInt);
		if(importRow.get("UpdateCountAsBigInt") != null) updateCountAsBigInt = Math.max((Long) importRow.get("UpdateCountAsBigInt"), updateCountAsBigInt);
		newEntityNode.put("updateCountAsBigInt", updateCountAsBigInt);
	}
	
	private void workstreamLegalPersonGroups() {
		final String endpoint = "importedLegalPersonGroups";
		final String routeName = "workstreamLegalPersonGroups";
		//must have UpdateCountAsBigInt
		final String selectSQL = 
			"SELECT cg.*,c.CompID\n" + 
			", CONVERT(BIGINT, CONVERT(VARBINARY, cg.MainGroupUpdateCount, 1)) MainGroupUpdateCountAsBigInt\n" + 
			", CONVERT(BIGINT, CONVERT(VARBINARY, cg.GroupUpdateCount, 1)) GroupUpdateCountAsBigInt\n" + 
			", CONVERT(BIGINT, CONVERT(VARBINARY, cg.SetGroupUpdateCount, 1)) UpdateCountAsBigInt FROM dbo.vContragentGroups cg\n" + 
			"INNER JOIN dbo.vContragents c ON c.ID = cg.ID\n" + 
			"WHERE c.CompID = "+companyCode+" AND \n" + 
			"(${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, cg.MainGroupUpdateCount, 1))\n" + 
			"OR ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, cg.GroupUpdateCount, 1))\n" + 
			"OR ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, cg.SetGroupUpdateCount, 1)))\n" + 
			"ORDER BY cg.SetGroupUpdateCount";
		
		from("timer:"+routeName+"?period=180000")
			.routeId(routeName+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak
			//get last modified
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"?page=0&size=1&sort=updateCountAsBigInt,desc")
			.setHeader("updateCountAsBigInt").jsonpath("$._embedded."+endpoint+"[0].updateCountAsBigInt",true)
			.choice()
				.when(header("updateCountAsBigInt").isNull())
					.setHeader("updateCountAsBigInt").constant("0")
			.end()
			//.to("log:import_last_modified?level=INFO&showAll=true&multiline=true")
			//retrieve updated rows
			.setBody(simple(selectSQL))
			//.to("log:import_retrieve_query?level=INFO&showAll=true&multiline=true")
			.to("jdbc:workStreamDataSource?useHeadersAsParameters=true")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					ArrayList<Map<String, Object>> importRows = exchange.getIn().getBody(ArrayList.class);
					ArrayList<String> entityJSON = new ArrayList<String>();
					ObjectMapper mapper = new ObjectMapper();
					for(Map<String, Object> importRow : importRows) {
						//create JSON with only the available data (not to overwrite existing data when patching)
						ObjectNode newEntityNode = mapper.createObjectNode();
						mapImportedLegalPersonGroups(importRow, newEntityNode);

						//check if exists
						String authToken = (String) exchange.getIn().getHeader("Authorization");
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders headers = new HttpHeaders();
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
						headers.add(HttpHeaders.AUTHORIZATION, authToken);
						HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
				
						String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"?page=0&size=1"
								+ "&foreignId="+importRow.get("ID"), 
								HttpMethod.GET, 
								entity, 
								String.class).getBody();
						
						JsonNode entityNode = mapper.readTree(jsonData).path("_embedded").path(endpoint).path(0);
						
						if(!entityNode.isNull() && !entityNode.isMissingNode()) {
							logger.debug("Node found, id: "+entityNode.path("id").asLong());
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "PATCH");
							exchange.getOut().setHeader("EntityID", entityNode.path("id").asLong());
						} else {
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
						}
						
						//output
						String json = mapper.writeValueAsString(newEntityNode);
						entityJSON.add(json);
					}
					exchange.getOut().setBody(entityJSON);
				}
			})
			.split(body())
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"/${header.EntityID}")); //dynamic URI
	}

	private void workstreamArticles() {
		from("timer:workstreamArticles?period=180000")
			.routeId("workstreamArticles"+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak
			//get last modified
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedArticles?page=0&size=1&sort=updateCountAsBigInt,desc")
			.setHeader("updateCountAsBigInt").jsonpath("$._embedded.importedArticles[0].updateCountAsBigInt",true)
			.choice()
				.when(header("updateCountAsBigInt").isNull())
					.setHeader("updateCountAsBigInt").constant("0")
			.end()
			//.to("log:import_last_modified?level=INFO&showAll=true&multiline=true")
			//retrieve updated rows
			.setBody(simple("SELECT *, CONVERT(BIGINT, CONVERT(VARBINARY, UpdateCount, 1)) UpdateCountAsBigInt FROM dbo.vArticles WHERE CompID = "+companyCode+" AND ${header.updateCountAsBigInt} < CONVERT(BIGINT, CONVERT(VARBINARY, UpdateCount, 1))"))
			//.to("log:import_retrieve_query?level=INFO&showAll=true&multiline=true")
			.to("jdbc:workStreamDataSource?useHeadersAsParameters=true")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					ArrayList<Map<String, Object>> importRows = exchange.getIn().getBody(ArrayList.class);
					ArrayList<String> entitiesJSON = new ArrayList<String>();
					ObjectMapper mapper = new ObjectMapper();
					for(Map<String, Object> importRow : importRows) {
						//create JSON with only the available data (not to overwrite existing data when patching)
						ObjectNode newEntity = mapper.createObjectNode();
						if(importRow.get("ID") != null) newEntity.put("foreignId",(String) importRow.get("ID"));
						if(importRow.get("Number") != null) newEntity.put("number",(Integer) importRow.get("Number"));
						if(importRow.get("Deleted") != null) newEntity.put("foreignDeleted",(Boolean) importRow.get("Deleted"));
						if(importRow.get("CompID") != null) newEntity.put("compId",(Integer) importRow.get("CompID"));
						if(importRow.get("ArticleType") != null) newEntity.put("articleType",(Short) importRow.get("ArticleType"));
						if(importRow.get("PredNom") != null) newEntity.put("predNom",(String) importRow.get("PredNom"));
						if(importRow.get("NomNom") != null) newEntity.put("nomNom",(String) importRow.get("NomNom"));
						if(importRow.get("Name") != null) newEntity.put("name",(String) importRow.get("Name"));
						if(importRow.get("MeasureID") != null) newEntity.put("measureForeignId",(String) importRow.get("MeasureID"));
						if(importRow.get("Measure") != null) newEntity.put("measure",(String) importRow.get("Measure"));
						if(importRow.get("MeasureShort") != null) newEntity.put("measureShort",(String) importRow.get("MeasureShort"));
						if(importRow.get("SellPrice") != null) newEntity.put("sellPrice",(BigDecimal) importRow.get("SellPrice"));
						if(importRow.get("Currency") != null) newEntity.put("currency",(String) importRow.get("Currency"));
						if(importRow.get("UpdateCountAsBigInt") != null) newEntity.put("updateCountAsBigInt",(Long) importRow.get("UpdateCountAsBigInt"));

						//check if exists
						String authToken = (String) exchange.getIn().getHeader("Authorization");
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders headers = new HttpHeaders();
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
						headers.add(HttpHeaders.AUTHORIZATION, authToken);
						HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
				
						String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/importedArticles?page=0&size=1"
								+ "&foreignId="+importRow.get("ID"), 
								HttpMethod.GET, 
								entity, 
								String.class).getBody();
						
						JsonNode entityNode = mapper.readTree(jsonData).path("_embedded").path("importedArticles").path(0);
						
						if(!entityNode.isNull() && !entityNode.isMissingNode()) {
							logger.debug("Node found, id: "+entityNode.path("id").asLong());
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "PATCH");
							exchange.getOut().setHeader("EntityID", entityNode.path("id").asLong());
						} else {
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
						}
						
						//output
						String json = mapper.writeValueAsString(newEntity);
						entitiesJSON.add(json);
					}
					exchange.getOut().setBody(entitiesJSON);
				}
			})
			.split(body())
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/importedArticles/${header.EntityID}")); //dynamic URI
	}

	/*
	 * There must be the following:
	 * - updateCountAsBigInt column in entity and select that is used to filter only the rows updated since last import;
	 * - entity should have 'foreignId' field;
	 * - select should have 'ID' field;
	 */
	private void workstreamImport(String className, String endpoint, int period, String selectSQL, RowToEntityMapper rowToEntityMapper,
			RowToFilterStringMapper rowToFilterStringMapper) {
		final RowToFilterStringMapper rowToFilterStringMapperLocal;
		if(rowToFilterStringMapper == null) {
			rowToFilterStringMapperLocal = (Map<String, Object> importRow) -> {return "foreignId="+importRow.get("ID");};
		} else {
			rowToFilterStringMapperLocal = rowToFilterStringMapper;
		}
		from("timer:workstream"+className+"?period="+period)
			.routeId("workstream"+className+companyCode)
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.filter().header("Authorization") //only when there is a token
			.removeHeaders("Authorization") //prevent header leak
			//get last modified
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.to("https4://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"?page=0&size=1&sort=updateCountAsBigInt,desc")
			.setHeader("updateCountAsBigInt").jsonpath("$._embedded."+endpoint+"[0].updateCountAsBigInt",true)
			.choice()
				.when(header("updateCountAsBigInt").isNull())
					.setHeader("updateCountAsBigInt").constant("0")
			.end()
			//.to("log:import_last_modified?level=INFO&showAll=true&multiline=true")
			//retrieve updated rows
			.setBody(simple(selectSQL))
			//.to("log:import_retrieve_query?level=INFO&showAll=true&multiline=true")
			.to("jdbc:workStreamDataSource?useHeadersAsParameters=true")
			.process(new Processor() {
				public void process(Exchange exchange) throws Exception {
					ArrayList<Map<String, Object>> importRows = exchange.getIn().getBody(ArrayList.class);
					ArrayList<String> entitiesJSON = new ArrayList<String>();
					ObjectMapper mapper = new ObjectMapper();
					for(Map<String, Object> importRow : importRows) {
						//create JSON with only the available data (not to overwrite existing data when patching)
						ObjectNode newEntity = mapper.createObjectNode();
						rowToEntityMapper.map(importRow, newEntity);

						//check if exists
						String authToken = (String) exchange.getIn().getHeader("Authorization");
						RestTemplate restTemplate = new RestTemplate();
						HttpHeaders headers = new HttpHeaders();
						headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
						headers.add(HttpHeaders.AUTHORIZATION, authToken);
						HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
				
						String jsonData = restTemplate.exchange("https://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"?page=0&size=1&"+rowToFilterStringMapperLocal.map(importRow), 
								HttpMethod.GET, 
								entity, 
								String.class).getBody();
						
						JsonNode entityNode = mapper.readTree(jsonData).path("_embedded").path(endpoint).path(0);
						
						if(!entityNode.isNull() && !entityNode.isMissingNode()) {
							logger.debug("Node found, id: "+entityNode.path("id").asLong());
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "PATCH");
							exchange.getOut().setHeader("EntityID", entityNode.path("id").asLong());
						} else {
							exchange.getOut().setHeader(Exchange.HTTP_METHOD, "POST");
						}
						
						//output
						String json = mapper.writeValueAsString(newEntity);
						entitiesJSON.add(json);
					}
					exchange.getOut().setBody(entitiesJSON);
				}
			})
			.split(body())
				.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
				.recipientList(simple("https4://"+DynamicRouteStarter.CRM_HOST+"/api/"+endpoint+"/${header.EntityID}")) //dynamic URI
			.end();
	}
}
