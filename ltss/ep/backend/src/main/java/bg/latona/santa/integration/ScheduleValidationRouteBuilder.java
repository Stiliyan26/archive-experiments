package bg.latona.santa.integration;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bg.latona.santa.entities.nepal.IbexEnergyDeal;


public class ScheduleValidationRouteBuilder extends RouteBuilder  {

	private static Logger logger = LoggerFactory.getLogger(ScheduleValidationRouteBuilder.class);
	private Long companyCode;

	public ScheduleValidationRouteBuilder(Long companyCode) {
		this.companyCode = companyCode;


	}


	@Override
	public void configure() throws Exception {

		JacksonDataFormat ibexEnergyDealFormat = new JacksonDataFormat(IbexEnergyDeal.class);
		ibexEnergyDealFormat.useList();  // Indicate that this is a list of IbexEnergyDeal objects

		//The route is triggered when launch the application and when restart the route scheduleValidation1
		from("direct:undistributedDeals")
				.routeId("undistributedDeals" + companyCode)
				.log("Starting route undistributedDeals for company code: " + companyCode)
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
		//		.log("Authorization token set. - undistributedDeals")
				.removeHeaders("Authorization")
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		//		.log("HTTP Method set to GET. - undistributedDeals")
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
		//		.log("Authorization token set again. - undistributedDeals")
				.setHeader(Exchange.HTTP_METHOD, constant("GET"))
				.to("https4://" + DynamicRouteStarter.CRM_HOST + "/api/reports/getUndistributedDeals")
				.removeHeaders("Authorization")
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
		//		.to("log:receivedUndistributedIbexDeal?level=INFO&showAll=true&multiline=true")
				.unmarshal(ibexEnergyDealFormat)  // Convert the response to List<IbexEnergyDeal>
		//		.log("Body before seda: ${body}")
				.to("seda:energyDistribution") // Pass to the next route
				.end();


		// Trigger the route during Camel startup
		getContext().getExecutorServiceManager().newSingleThreadExecutor(this, "Startup").execute(() -> {
			try {
				getContext().createProducerTemplate().sendBody("direct:undistributedDeals", null);
			} catch (Exception e) {
				log.error("Error while triggering route at startup", e);
			}
		});



		from("timer:scheduleValidation?period=900000") // 15 minutes
				.routeId("scheduleValidation" + companyCode)
				.log("Checking for trades on IBEX")
		//		.log("Starting route with companyCode: ${header.companyCode}")
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
		//		.log("Authorization token set.")
				.removeHeaders("Authorization")
				.setHeader(Exchange.HTTP_METHOD, constant("GET"))
		//		.log("HTTP Method set to GET.")
				.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
		//		.log("Authorization token set again.")
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.to("https4://" + DynamicRouteStarter.CRM_HOST + "/api/reports/getIbexEnergyDeal")
		//		.to("log:receivedIbexDeal?level=INFO&showAll=true&multiline=true")
				.unmarshal(ibexEnergyDealFormat)  // Convert the response to List<IbexEnergyDeal>
		//		.log("Unmarshalled deals: ${body}")
				.to("seda:energyDistribution")  // Pass to the next route
				.end();


		from("seda:energyDistribution")
				.routeId("energyDistribution" + companyCode)
				.log("Processing MQ to distribute trades from IBEX")
				.process(exchange -> {
					// Extract the list of IbexEnergyDeal
					List<IbexEnergyDeal> deals = exchange.getIn().getBody(List.class);

					// Transform the list of IbexEnergyDeal into a list of Long IDs
					List<Long> idList = deals.stream()
							.filter(deal -> deal != null && deal.getId() != null)
							.map(IbexEnergyDeal::getId)  // Assuming there's a getId()
							.collect(Collectors.toList());
					exchange.getIn().setBody(idList);
					// Check if idListLong is null or empty before proceeding
					//	log.info("idListLong size - " + idListLong.size());
					if (idList.size() != 0) {
						exchange.getIn().setHeader("isNotEmpty", true);
					} else {
						log.error("ID list is null or empty - routeId: energyDistribution");
					}
				})
				.filter().header("isNotEmpty")
				.removeHeaders("isNotEmpty")
		//		.log("List of IDs: ${header.idListLong}")  // Log the list of IDs
				.setHeader(Exchange.HTTP_METHOD, constant("POST"))
				.marshal().json(JsonLibrary.Jackson)
				.to("https4://" + DynamicRouteStarter.CRM_HOST + "/api/reports/energyDistributionCamelProcess")
		//		.log("Sent list of IDs: ${header.idListLong}")
				.end();


		}

	}
