import React from 'react'; //needed for Cell to work, otherwise gives error "ReferenceError: React is not defined"
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import axios from "axios";
import { X_AUTH_TOKEN } from "../selfie/constants/selfieConstants";
import { dataTypes, fluidSizes } from "../selfie/constants/santaDataTypes";
import FieldMultiSelectOrEditContainer from '../fields/FieldMultiSelectOrEditContainer';
import { Link } from 'react-router-dom';

const selfieEntityDefinitions = {
	agreementTypes: {
		className: "AgreementType",
		displayAttr: "text",
		pageURL: "/agreementTypes",
		columns: [
			{
				accessor: "id",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "code",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			},
			{
				accessor: "description",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			},
			{
				accessor: "text",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			},
			{
				accessor: "isValid",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
		]
	},
	agreementTypeMappings: {
		className: "AgreementTypeMapping",
		displayAttr: "description",
		pageURL: "/agreementTypeMappings",
		columns: [
			{
				Header: "Agreement Types",
				accessor: "agreementTypes",
				filterable: false,
				sortable: false,
				fluidSize: fluidSizes.THREE,
				Cell: (props) => <FieldMultiSelectOrEditContainer
					componentPath={props.original._componentPath + '.' + props.column._fieldRelPath} //existing path in redux store where we put data
					href={props.original && props.original._links && props.original._links.agreementTypes ? props.original._links.agreementTypes.href : undefined}
					listType="agreementTypes"
					listAttr="text"
					editable={props.original._editable}
				/>,
			},
			{
				accessor: "crmCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "description",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	loiMeasurementUnits: {
		className: "LoiMeasurementUnit",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	loiDocumentTypes: {
		className: "LoiDocumentType",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	documentRanges: {
		className: "DocumentRange",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "rangeFrom",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "rangeTo",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "current",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER,
				Cell: (props) => props.original.current + 1
			},
			{
				accessor: "isActive",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
		]
	},
	electricityInvoices: {
		className: "ElectricityInvoice",
		displayAttr: "reportingPointOwn",
		pageURL: "/electricityInvoices",
		columns: [
			{
				accessor: "invoiceNumber",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "taxEventDate",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "loiDocumentType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiDocumentTypes",
			},
			{
				accessor: "reportingPointOwn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
				isLink: true,
				URL: "/electricityInvoices"
			},
			{
				accessor: "agreementType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementTypes"
			},
			{
				accessor: "periodFrom",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "periodTo",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "totalQuantity",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "loiMeasurementUnit",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiMeasurementUnits",
			},
			{
				accessor: "priceInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "priceInEuros",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "vatInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "vatInEuros",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "totalSumInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "totalSumInEuros",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "pdfDocument",
				fluidSize: fluidSizes.THREE,
				width: 180,
				dataType: dataTypes.STRING,
				Cell: (props) =>
					props.original.hasDbFile ? (
						<FontAwesomeIcon
							size="4x"
							icon="file-pdf"
							style={{ color: "#0F65AA", cursor: "pointer" }}
							onClick={() => {
								axios({
									method: "GET",
									url: props.original._links.dbFile.href,
									headers: { Authorization: sessionStorage[X_AUTH_TOKEN] }
								})
									.then(response => {
										let data = response.data;

										let objbuilder = '';
										objbuilder += ('<object width="100%" height="100%" data="data:application/pdf;base64,');
										objbuilder += (data.content);
										objbuilder += ('" type="application/pdf" class="internal">');
										objbuilder += ('<embed src="data:application/pdf;base64,');
										objbuilder += (data.content);
										objbuilder += ('" type="application/pdf"  />');
										objbuilder += ('</object>');

										let win = window.open("#", "_blank");
										let title = "Electricity Invoice";

										win.document.write('<html><title>' + title + '</title><body style="margin-top: 0px; margin-left: 0px; margin-right: 0px; margin-bottom: 0px;">');
										win.document.write(objbuilder);
										win.document.write('</body></html>');

										jQuery(win.document);
									});
							}}
						/>
					) : (
						<span style={{ cursor: "not-allowed" }}>
							No File
						</span>
					)
			},
			{
				accessor: "isValid",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
			{
				accessor: "hasDbFile",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
			{
				accessor: "invoiceCorrection",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
				Cell: (props) => {
					// console.log("selfieEntityDefinitions-props: ", props)

					return <Link to={"/electricityInvoices/" + props.original.id}>
						<FontAwesomeIcon
							size="4x"
							icon="file-signature"
							style={{ color: "#0F65AA", cursor: "pointer" }}
						/>
					</Link>
				}
			},

		]
	},
	importValues: {
		className: "ImportValue",
		displayAttr: "reportingPointOwn",
		columns: [
			{
				accessor: "reportingPointOwn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
				isLink: true,
				URL: "/importValues"
			},
			{
				accessor: "loiDocumentType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiDocumentTypes",
			},
			{
				accessor: "agreementType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementTypes",
			},
			{
				accessor: "priceInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "periodFrom",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "periodTo",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "isValid",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
			{
				accessor: "electricityInvoice",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "electricityInvoices",
			},
		]
	},
	importQuantities: {
		className: "ImportQuantity",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "reportingPointOwn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
				isLink: true,
				URL: "/importQuantities"
			},
			{
				accessor: "agreementType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementTypes",
			},
			{
				accessor: "totalQuantity",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "loiMeasurementUnit",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiMeasurementUnits",
			},
			{
				accessor: "periodFrom",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "periodTo",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "loiDocumentType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiDocumentTypes",
			},
			{
				accessor: "isValid",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
			{
				accessor: "electricityInvoice",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "electricityInvoices",
			},
		]
	},
	importValueAndQuantities: {
		className: "ImportValueAndQuantity",
		displayAttr: "reportingPointOwn",
		columns: [
			{
				accessor: "reportingPointOwn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
				isLink: true,
				URL: "/importValueAndQuantities"
			},
			{
				accessor: "loiDocumentType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiDocumentTypes",
			},
			{
				accessor: "agreementType",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementTypes",
			},
			{
				accessor: "loiMeasurementUnit",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiMeasurementUnits",
			},
			{
				accessor: "totalSumInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "periodFrom",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "periodTo",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE
			},
			{
				accessor: "totalQuantity",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "priceInLevs",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "isValid",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN
			},
			{
				accessor: "electricityInvoice",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "electricityInvoices",
			},
		]
	},
	Contacts: {
		className: "Contact",
		displayAttr: "name",
		columns: [
			{
				accessor: "name",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "description",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "phone",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "fax",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "email",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			},
			{
				accessor: "webSite",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	accountingPeriods: {
		className: "AccountingPeriod",
		displayAttr: "month",
		columns: [
			{
				accessor: "month",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "code",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "isActive",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN,
			},
		]
	},

	//Add Agreements
	loiReasonForTerminations: {
		className: "LoiReasonForTermination",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	loiAgreementStatuses: {
		className: "LoiAgreementStatus",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	loiTypeOFServices: {
		className: "LoiTypeOFService",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	loiStatusCodes: {
		className: "LoiStatusCode",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER
			},
			{
				accessor: "listOptionItemName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING
			}
		]
	},
	agreementSelfInvoicings: {
		className: "AgreementSelfInvoicing", 
		displayAttr: "id",
		columns: [
			{
				accessor: "id",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER,
				isLink: true,
				URL: "/agreementSelfInvoicings" 
			},
			{
				accessor: "agreementSelfInvoicingId",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementSelfInvoicings",
			},
			{
				accessor: "createdOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "agreementStartDate",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "agreementEndDate",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "isVatIncluded",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN,
			},
			{
				accessor: "vatNumber",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "vatRegistrationDate",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "isClientResponseAccept",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN,
			},
			{
				accessor: "customer",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "customerRepresentativeFirst",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "customerRepresentativeSecond",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "egn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "ownName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "reasonForTermination",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiReasonForTerminations",
			},
			{
				accessor: "terminatedOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "terminationDate",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "signedOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "typeOFService",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiTypeOFServices",
			},
			{
				accessor: "modifiedOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "isStateCodeStatusActive",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN,
			},
			{
				accessor: "agreementStatus",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiAgreementStatuses",
			},
			{
				accessor: "powerPlant",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "powerPlants",
			},
			{
				accessor: "agreementSelfInvoicingIdCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
		]
	},
	selfInvoicingLines: {
		className: "SelfInvoicingLine",
		displayAttr: "id",
		columns: [
			{
				accessor: "id",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.NUMBER,
			},
			{
				accessor: "createdOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "activatedOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "deActivatedOn",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.DATE,
			},
			{
				accessor: "bic",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "iban",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "agreementSelfInvoicing",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "agreementSelfInvoicings",
			},
			{
				accessor: "invoiceEmail",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "invoiceEmailSec",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "ownName",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
			{
				accessor: "isActiveStateCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.BOOLEAN,
			},
			{
				accessor: "statusCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.ENTITY,
				entityType: "loiStatusCodes",
			},
			{
				accessor: "selfInvoicingLineIdCode",
				fluidSize: fluidSizes.THREE,
				dataType: dataTypes.STRING,
			},
		]
	},
	
};


export default selfieEntityDefinitions;