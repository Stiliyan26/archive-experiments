package bg.latona.santa.anvoice;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import bg.latona.santa.anvoice.entities.EventFetchCrmDataForInvoice;
import bg.latona.santa.anvoice.entities.EventGenerateElectricityInvoicePdf;
import bg.latona.santa.anvoice.entities.EventPopulateElectricityInvoice;
import bg.latona.santa.anvoice.entities.EventValidateAndIssueElectricityInvoice;
import bg.latona.santa.entities.UserActionEvent;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.integration.AuthorizationRouteBuilder;

public class AnvoiceRouteBuilder extends RouteBuilder {

    private final AnvoiceProcedures anvoiceProcedures;
    private final String CRM_HOST;

    public AnvoiceRouteBuilder(AnvoiceProcedures anvoiceProcedures, String CRM_HOST) {
        this.anvoiceProcedures = anvoiceProcedures;
        this.CRM_HOST = CRM_HOST;
    }

    public static class ExecutionDetailsAggregationStrategy implements AggregationStrategy {
		 
		public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            // put details together in old exchange by adding the order from new exchange
    
            if (oldExchange == null) {
                // the first time we aggregate we only have the new exchange,
                // so we just return it
                return newExchange;
            }
    
            String oldDetails = oldExchange.getProperty("executionDetails", String.class);
            String newDetails = newExchange.getProperty("executionDetails", String.class);
            
            // put combined details back on old to preserve it
            oldExchange.setProperty("executionDetails",oldDetails+newDetails);
    
            // return old as this is the one that has all the details gathered until now
            return oldExchange;
        }
    }
	@Override
	public void configure() {

        final Long companyCode = 1L;

        //consumes EventPopulateElectricityInvoice
		from("direct:EventPopulateElectricityInvoice")
            .routeId("EventPopulateElectricityInvoice")
			//.to("log:EventPopulateElectricityInvoice?level=INFO&showAll=true&multiline=true")
            .to("direct:setUserActionEventStartedTime")
            //get the params from the event object
            .process(exchange -> {
                EventPopulateElectricityInvoice myObject = exchange.getIn().getBody(EventPopulateElectricityInvoice.class);
                anvoiceProcedures.populateElectricityInvoice(myObject.getPeriodFrom(), myObject.getPeriodTo(), myObject.getTaxEventDate(), myObject.getDocumentType());
            });

        //no starting requirements
        from("direct:EventProcessElectricityInvoices")
            .routeId("EventProcessElectricityInvoices")
            //.to("log:EventProcessElectricityInvoices?level=INFO&showAll=true&multiline=true")
            .to("direct:setUserActionEventStartedTime")
            .doTry()
                .process(exchange -> {
                    exchange.getIn().setBody(anvoiceProcedures.getElectricityInvoicesToGenerate());
                })
                .split(body(), new ExecutionDetailsAggregationStrategy()).streaming()
                    .setProperty("electricityInvoice",body())
                    .setProperty("executionDetails", constant(""))
                    .to("direct:fetchCrmDataForInvoice")
                    .setBody(simple("${exchangeProperty.electricityInvoice}"))
                    .to("direct:validateAndIssueElectricityInvoice")
                    .setBody(simple("${exchangeProperty.electricityInvoice}"))
                    .to("direct:generateElectricityInvoicePdf")
                .end()
                .to("log:EventProcessElectricityInvoices_afterSplit?level=INFO&showAll=true&multiline=true")
                .to("direct:saveEventExecutionDetails")
            .endDoTry()
            .doCatch(Exception.class)
                .log("Exception executing anvoiceProcedures.getElectricityInvoicesToGenerate: ${exception.message}")
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Exception executing anvoiceProcedures.getElectricityInvoicesToGenerate: ${exception.message}\"}],"))
                .to("direct:saveEventExecutionDetails")
            .end();

        //consumes properties UserActionEvent userActionEvent and String executionDetails
        from("direct:setUserActionEventStartedTime")
            .routeId("setUserActionEventStartedTime")
            //.to("log:setUserActionEventStartedTime?level=INFO&showAll=true&multiline=true")
            .setProperty("userActionEvent", body())
            .setProperty("executionDetails",constant(""))
            .doTry()
                .process(exchange -> {
                    UserActionEvent event = exchange.getIn().getBody(UserActionEvent.class);
                    anvoiceProcedures.setUserActionEventStartedTime(event);
                })
            .doCatch(Exception.class)
                .log("Exception executing anvoiceProcedures.setUserActionEventStartedTime: ${exception.message}")
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Exception executing anvoiceProcedures.setUserActionEventStartedTime: ${exception.message}\"}],"))
            .end();

        //consumes properties UserActionEvent userActionEvent and String executionDetails
        from("direct:saveEventExecutionDetails")
            .routeId("saveEventExecutionDetails")
            //.to("log:saveEventExecutionDetails?level=INFO&showAll=true&multiline=true")
            .doTry()
                .process(exchange -> {
                    UserActionEvent event = exchange.getProperty("userActionEvent", UserActionEvent.class);
                    String executionDetails = exchange.getProperty("executionDetails", String.class);
                    anvoiceProcedures.saveEventExecutionDetails(event, executionDetails);
                })
            .doCatch(Exception.class)
                .to("log:saveEventExecutionDetails?level=INFO&showAll=true&multiline=true")
                .log("Exception executing anvoiceProcedures.saveEventExecutionDetails: ${exception.message}")
            .end();

        //consumes EventValidateAndIssueElectricityInvoice
        from("direct:EventValidateAndIssueElectricityInvoice")
            .routeId("EventValidateAndIssueElectricityInvoice")
            //.to("log:EventValidateAndIssueElectricityInvoice?level=INFO&showAll=true&multiline=true")
            .to("direct:setUserActionEventStartedTime")
            .process(exchange -> {
                EventValidateAndIssueElectricityInvoice event = exchange.getIn().getBody(EventValidateAndIssueElectricityInvoice.class);
                exchange.getIn().setBody(event.getElectricityInvoice());
            })
            .to("direct:validateAndIssueElectricityInvoice")
            .to("direct:saveEventExecutionDetails");

        //consumes ElectricityInvoice
        from("direct:validateAndIssueElectricityInvoice")
            .routeId("validateAndIssueElectricityInvoice")
            //.to("log:validateAndIssueElectricityInvoice?level=INFO&showAll=true&multiline=true")
            .doTry()
                .process(exchange -> {
                    exchange.getIn().setBody(anvoiceProcedures.validateAndIssueElectricityInvoice(exchange.getIn().getBody(ElectricityInvoice.class)));
                })
                .marshal().json(JsonLibrary.Jackson)
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}${body},"))
            .doCatch(Exception.class)
                .log("Exception executing anvoiceProcedures.validateAndIssueElectricityInvoice: ${exception.message}")
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Exception executing anvoiceProcedures.validateAndIssueElectricityInvoice: ${exception.message}\"}],"))
            .end();

        //consumes EventGenerateElectricityInvoicePdf
        from("direct:EventGenerateElectricityInvoicePdf")
            .routeId("EventGenerateElectricityInvoicePdf")
            //.to("log:EventGenerateElectricityInvoicePdf?level=INFO&showAll=true&multiline=true")
            .to("direct:setUserActionEventStartedTime")
            .process(exchange -> {
                EventGenerateElectricityInvoicePdf event = exchange.getIn().getBody(EventGenerateElectricityInvoicePdf.class);
                exchange.getIn().setBody(event.getElectricityInvoice());
            })
            .to("direct:generateElectricityInvoicePdf")
            .to("direct:saveEventExecutionDetails");

        //consumes ElectricityInvoice
        from("direct:generateElectricityInvoicePdf")
            .routeId("generateElectricityInvoicePdf")
            //.to("log:generateElectricityInvoicePdf?level=INFO&showAll=true&multiline=true")
            .doTry()
                .process(exchange -> {
                    exchange.getIn().setBody(anvoiceProcedures.generateElectricityInvoicePdf(exchange.getIn().getBody(ElectricityInvoice.class)));
                })
                .marshal().json(JsonLibrary.Jackson)
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}${body},"))
            .doCatch(Exception.class)
                .log("Exception executing anvoiceProcedures.generateElectricityInvoicePdf: ${exception.message}")
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\": \"Exception executing anvoiceProcedures.generateElectricityInvoicePdf: ${exception.message}\"}],"))
            .end();

        //consumes EventFetchCrmDataForInvoice
        from("direct:EventFetchCrmDataForInvoice")
            .routeId("EventFetchCrmDataForInvoice")
            //.to("log:EventFetchCrmDataForInvoice?level=INFO&showAll=true&multiline=true")
            .to("direct:setUserActionEventStartedTime")
            .process(exchange -> {
                EventFetchCrmDataForInvoice event = exchange.getIn().getBody(EventFetchCrmDataForInvoice.class);
                exchange.getIn().setBody(event.getElectricityInvoice());
            })
            .to("direct:fetchCrmDataForInvoice");

        //consumes body ElectricityInvoice and property executionDetails
		from("direct:fetchCrmDataForInvoice")
            .routeId("fetchCrmDataForInvoice")
            //get the EIK for this invoice
            //TODO change to REST?
            .process(exchange -> {
                ElectricityInvoice myObject = exchange.getIn().getBody(ElectricityInvoice.class);
                PowerPlant powerPlant = myObject.getPowerPlant();
                if(powerPlant == null) {
                    //System.out.println("MissingPowerPlant");
                    exchange.setProperty("executionDetails", exchange.getProperty("executionDetails")+"[{\"error\":\"MissingPowerPlant\",\"id\":\""+myObject.getId()+"\",\"class\":\""+ElectricityInvoice.class.getSimpleName()+"\",\"field\":\"powerPlant\",\"value\":null}],");
                    return;
                }
                LegalPerson owner = powerPlant.getOwner();
                if(owner == null) {
                    //System.out.println("MissingLegalPerson");
                    exchange.setProperty("executionDetails", exchange.getProperty("executionDetails")+"[{\"error\":\"MissingLegalPerson\",\"id\":\""+powerPlant.getId()+"\",\"class\":\""+PowerPlant.class.getSimpleName()+"\",\"field\":\"powerPlant\",\"value\":null}],");
                    return;
                }
                exchange.setProperty("legalPersonId", owner.getId());
                String eik = owner.getEik();
                exchange.setProperty("eik",eik);
                if(eik == null || eik.isEmpty()) {
                    //System.out.println("MissingLegalPersonIdentifier");
                    exchange.setProperty("executionDetails", exchange.getProperty("executionDetails")+"[{\"error\":\"MissingLegalPersonIdentifier\",\"id\":\""+owner.getId()+"\",\"class\":\""+LegalPerson.class.getSimpleName()+"\",\"field\":\"eik\",\"value\":null}],");
                    return;
                }
            })
            .choice()
                .when(simple("${exchangeProperty.eik} == null || ${exchangeProperty.eik} == '' "))
                    .log("No EIK for this LegalPerson") //when error, no next step
                .otherwise()
                    .log(LoggingLevel.DEBUG, "EIK found")
                    .to("direct:checkForExistingBankAccount") //next step
            .end();
            
        //consumes body N/A and properties executionDetails,legalPersonId
		from("direct:checkForExistingBankAccount")
        .routeId("checkForExistingBankAccount")
            //check for existing bank account
			.removeHeaders("*") //prevent header leak
			.setHeader(Exchange.HTTP_METHOD, constant("GET"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
            .convertBodyTo(String.class) //otherwise I get error "No body available of type: java.io.InputStream"
			.recipientList(simple("https4://"+CRM_HOST+"/api/reports/builder/1?from=BankAccount&select=BankAccount&BankAccount.bankAccountOwner.id=${exchangeProperty.legalPersonId}&page=0&size=1&sort=BankAccount.id%2Cdesc")) //dynamic URI
            .unmarshal().json(JsonLibrary.Jackson)
            //.to("log:fetchCrmDataForInvoiceAfterAccountCheck?level=INFO&showAll=true&multiline=true")
            .choice()
                .when().jsonpath("$._embedded.hashMaps[0].BankAccount.iban", true)
                    .setProperty("bankAccount",jsonpath("$._embedded.hashMaps[0].BankAccount.iban"))
                    .setProperty("bankAccountLast10",simple("${exchangeProperty.bankAccount.substring(12)}"))
                    .to("direct:getSapIdByEik") //next step
                .otherwise()
                    .log("MissingBankAccount") //when error, no next step
                    .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"MissingBankAccount\",\"id\":\"${exchangeProperty.legalPersonId}\",\"class\":\"LegalPerson\",\"field\":\"bankAccounts\",\"value\":null}],"))
            .end();

        //consumes body N/A and properties executionDetails,eik
        from("direct:getSapIdByEik")
            .routeId("getSapIdByEik")
            //call the CRM to get the SAP ID by the Eik
			.removeHeaders("*") //prevent header leak
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            //TODO credentials from app props
            .setHeader("Authorization", constant("Basic " + new String(Base64.getEncoder().encode(("sapsi_tsq:/<pu6L#bpF+}").getBytes())))) 
            .setBody(simple("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:sap-com:document:sap:rfc:functions\">" + //
                                "<soap:Header/>" + //
                                "   <soap:Body>" + //
                                "      <urn:ZWS_CREDITOR_EIK_SAPID>" + //
                                "         <I_BUKRS>3000</I_BUKRS>" + //
                                "         <I_STCD1>${exchangeProperty.eik}</I_STCD1>" + //
                                "      </urn:ZWS_CREDITOR_EIK_SAPID>" + //
                                "   </soap:Body>" + //
                                "</soap:Envelope>"))
            //.to("log:fetchCrmDataForInvoiceBeforeSend?level=INFO&showAll=true&multiline=true")
            .doTry()
			    .to("http4://EPRO-SAP-TSQ.energo-pro.bg:8051/sap/bc/srt/rfc/sap/zws_creditor_eik_sapid/100/zws_creditor_eik_sapid/zws_creditor_eik_sapid_binding")
                .setProperty("sapId",xpath("//ET_SAPID", String.class))
                .choice()
                    .when(simple("${exchangeProperty.sapId} == null || ${exchangeProperty.sapId} == '' "))
                        .log("No SAP ID for this EIK") //when error, no next step
                        .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"No SAP ID for this EIK: ${exchangeProperty.eik}\"}],"))
                    .otherwise()
                        .log(LoggingLevel.DEBUG, "SAP ID found")
                        .to("direct:getCreditorDetailsBySapId") //next step
                .endChoice()
            .endDoTry()
            .doCatch(Exception.class)
                .log("Exception while fetching SAP ID: ${exception.message}")
                .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Exception while fetching SAP ID for EIK ${exchangeProperty.eik}: ${exception.message}\"}],"))
            .end();
            
        //consumes body N/A and properties executionDetails,sapId
        from("direct:getCreditorDetailsBySapId")
            .routeId("getCreditorDetailsBySapId")
			.removeHeaders("*") //prevent header leak
            .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            .setHeader(Exchange.CONTENT_TYPE, constant("text/xml"))
            //TODO credentials from app props
            .setHeader("Authorization", constant("Basic " + new String(Base64.getEncoder().encode(("sapsi_tsq:/<pu6L#bpF+}").getBytes())))) 
            //now get the CRM details for this SAP ID
            .setBody(simple("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:sap-com:document:sap:rfc:functions\">\r\n" + //
                                "<soap:Header/>\r\n" + //
                                "   <soap:Body>\r\n" + //
                                "      <urn:ZWS_CREDITOR_GETDETAIL>\r\n" + //
                                "         <!--Optional:-->\r\n" + //
                                "         <COMPANYCODE>3000</COMPANYCODE>\r\n" + //
                                "         <CREDITORID>${exchangeProperty.sapId}</CREDITORID>\r\n" + //
                                "         <!--Optional:-->\r\n" + //
                                "         <CREDITOR_BANK_DETAIL>\r\n" + //
                                "         </CREDITOR_BANK_DETAIL>\r\n" + //
                                "      </urn:ZWS_CREDITOR_GETDETAIL>\r\n" + //
                                "   </soap:Body>\r\n" + //
                                "</soap:Envelope>"))
            //.to("log:fetchCrmDataForInvoiceBeforeDetails?level=INFO&showAll=true&multiline=true")
            .to("http4://EPRO-SAP-TSQ.energo-pro.bg:8051/sap/bc/srt/rfc/sap/zws_creditor_getdetail/100/zws_creditor_getdetail/zws_creditor_getdetail_binding")
            .convertBodyTo(String.class)
            //.to("log:fetchCrmDataForInvoiceAfterDetails?level=INFO&showAll=true&multiline=true")
            .choice()
                .when().xpath("boolean(//CREDITOR_BANK_DETAIL/item[BANK_ACCT=${exchangeProperty.bankAccountLast10}])")
                    .log("Bank account match found")
                    .to("direct:updateLegalPersonWithSapCreditorDetails") //next step
                .otherwise()
                    .log("Bank account missing in SAP") //when error, no next step
                    .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Bank account number ${exchangeProperty.bankAccountLast10} cannot be found in SAP for EIK ${exchangeProperty.eik}\"}],"))
            .end();
            
        //consumes body XML with creditor details from SAP and properties executionDetails,legalPersonId
        from("direct:updateLegalPersonWithSapCreditorDetails")
            .routeId("updateLegalPersonWithSapCreditorDetails")
            .setProperty("sapName",xpath("//CREDITOR_GENERAL_DETAIL/NAME", String.class))
            .setProperty("sapCountry",xpath("//CREDITOR_GENERAL_DETAIL/COUNTRY", String.class))
            .setProperty("sapCity",xpath("//CREDITOR_GENERAL_DETAIL/CITY", String.class))
            .setProperty("sapStreet",xpath("//CREDITOR_GENERAL_DETAIL/STREET", String.class))
            .setProperty("sapPostCode",xpath("//CREDITOR_GENERAL_DETAIL/POSTL_CODE", String.class))
            // .setProperty("sapBic",xpath("//CREDITOR_BANK_DETAIL/item[PARTNER_BK='4']/BANK_KEY", String.class))
            // .setProperty("sapBankAccount",xpath("//CREDITOR_BANK_DETAIL/item[PARTNER_BK='4']/BANK_ACCT", String.class))
            //.to("log:fetchCrmDataForInvoiceAfterDetails?level=INFO&showAll=true&multiline=true")
			// .removeHeaders("*") //prevent header leak
            // //get the legalPerson of the creditor
			// .setHeader(Exchange.HTTP_METHOD, constant("GET"))
			// .setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			// .process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			// .recipientList(simple("https4://"+CRM_HOST+"/api/reports/builder/1?from=LegalPerson&select=LegalPerson&LegalPerson.eik=${exchangeProperty.eik}&page=0&size=1&sort=LegalPerson.id%2Cdesc"))
            // .setProperty("legalPersonId",jsonpath("$._embedded.hashMaps[0].LegalPerson.id"))
            // //.to("log:fetchCrmDataForInvoiceFetchedLegalPerson?level=INFO&showAll=true&multiline=true")
            //update legalPerson data
			.removeHeaders("*") //prevent header leak
			.setHeader(Exchange.HTTP_METHOD, constant("PATCH"))
			.setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
			.process(AuthorizationRouteBuilder.setAuthToken(companyCode))
			.setBody(simple(
                    "{\"sapNumber\":\"${exchangeProperty.sapId}\","
                        +"\"name\":\"${exchangeProperty.sapName}\","
                        +"\"country\":\"${exchangeProperty.sapCountry}\","
                        +"\"city\":\"${exchangeProperty.sapCity}\","
                        +"\"street\":\"${exchangeProperty.sapStreet}\","
                        +"\"postCode\":\"${exchangeProperty.sapPostCode}\"}"
                )) 
            //string must be converted to UTF-8
            .process(exchange -> {
                // Get the current body
                String originalBody = exchange.getIn().getBody(String.class);
                // Encode the body as UTF-8
                byte[] utf8EncodedBytes = originalBody.getBytes(StandardCharsets.UTF_8);
                // Convert the byte array back to a UTF-8 encoded string
                String utf8EncodedString = new String(utf8EncodedBytes, StandardCharsets.UTF_8);
                // Set the new body
                exchange.getIn().setBody(utf8EncodedString);
            })
            //.to("log:fetchCrmDataForInvoiceBeforePatchLegalPerson?level=INFO&showAll=true&multiline=true")
			.recipientList(simple("https4://"+CRM_HOST+"/api/legalPersons/${exchangeProperty.legalPersonId}")) //dynamic URI
            //.to("log:fetchCrmDataForInvoiceAfterPatchLegalPerson?level=INFO&showAll=true&multiline=true")
            // .choice() //use .endChoice() if needed
            //     .when(simple("${exchangeProperty.sapBankAccount} == null || ${exchangeProperty.sapBankAccount} == '' || ${exchangeProperty.sapBic} == null || ${exchangeProperty.sapBic} == ''"))
            //         //TODO error reporting
            //         //.to("log:fetchCrmDataForInvoice_missingBankAccount?level=INFO&showAll=true&multiline=true")
            //         .setProperty("executionDetails", simple("${exchangeProperty.executionDetails}[{\"error\":\"Missing bank account\"}],"))
            //     .when().jsonpath("$._embedded.hashMaps[0].BankAccount.id", true)
            //         //TODO update bank account
            //         .setProperty("bankAccountId",jsonpath("$._embedded.hashMaps[0].BankAccount.id"))
            //         .removeHeaders("*") //prevent header leak
            //         .setHeader(Exchange.HTTP_METHOD, constant("PATCH"))
            //         .setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
            //         .process(AuthorizationRouteBuilder.setAuthToken(companyCode))
            //         .setBody(simple(
            //                 "{\"iban\":\"${exchangeProperty.sapBankAccount}\","
            //                     +"\"bic\":\"${exchangeProperty.sapBic}\"}"
            //             )) 
            //         //string must be converted to UTF-8
            //         .process(exchange -> {
            //             // Get the current body
            //             String originalBody = exchange.getIn().getBody(String.class);
            //             // Encode the body as UTF-8
            //             byte[] utf8EncodedBytes = originalBody.getBytes(StandardCharsets.UTF_8);
            //             // Convert the byte array back to a UTF-8 encoded string
            //             String utf8EncodedString = new String(utf8EncodedBytes, StandardCharsets.UTF_8);
            //             // Set the new body
            //             exchange.getIn().setBody(utf8EncodedString);
            //         })
            //         .recipientList(simple("https4://"+CRM_HOST+"/api/bankAccounts/${exchangeProperty.bankAccountId}")) //dynamic URI
            //         //.to("log:fetchCrmDataForInvoiceAfterAccountPatch?level=INFO&showAll=true&multiline=true")
            //         .endChoice()
            //     .otherwise()
            //         //TODO create bank account
            //         .removeHeaders("*") //prevent header leak
            //         .setHeader(Exchange.HTTP_METHOD, constant("POST"))
            //         .setHeader(Exchange.CONTENT_TYPE, constant("application/json;charset=UTF-8"))
            //         .process(AuthorizationRouteBuilder.setAuthToken(companyCode))
            //         .setBody(simple(
            //                 "{\"iban\":\"${exchangeProperty.sapBankAccount}\","
            //                     +"\"bic\":\"${exchangeProperty.sapBic}\","
            //                     +"\"bankAccountOwner\":\"https://"+CRM_HOST+"/api/legalPersons/${exchangeProperty.legalPersonId}\"}"
            //             )) 
            //         //string must be converted to UTF-8
            //         .process(exchange -> {
            //             // Get the current body
            //             String originalBody = exchange.getIn().getBody(String.class);
            //             // Encode the body as UTF-8
            //             byte[] utf8EncodedBytes = originalBody.getBytes(StandardCharsets.UTF_8);
            //             // Convert the byte array back to a UTF-8 encoded string
            //             String utf8EncodedString = new String(utf8EncodedBytes, StandardCharsets.UTF_8);
            //             // Set the new body
            //             exchange.getIn().setBody(utf8EncodedString);
            //         })
            //         .to("https4://"+CRM_HOST+"/api/bankAccounts") 
            //         //.to("log:fetchCrmDataForInvoiceAfterAccountCreate?level=INFO&showAll=true&multiline=true")
            // .end()
            //.to("log:fetchCrmDataForInvoiceEnd?level=INFO&showAll=true&multiline=true")
            //.to("direct:saveEventExecutionDetails")
            ;
    }
}
