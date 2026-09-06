import React from 'react'; //needed for Cell to work, otherwise gives error "ReferenceError: React is not defined"
import XLSX from 'xlsx'
import { getLabelWithConstraints } from './entityDefinitions'
import FieldMultiSelectOrEditContainer from '../fields/FieldMultiSelectOrEditContainer'
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome"


const nepalEntityDefinitions = {
	schedules: {
		className: "Schedule",
		displayAttr: "messageIdentification",
		pageURL: "/schedules",
		columns: [
			{
				accessor: "messageIdentification",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				URL: "/schedules"
			},
			{
				accessor: "messageVersion",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "messageType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "processType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "scheduleClassificationType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "senderIdentificationV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "senderIdentificationCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "senderRole",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "receiverIdentificationV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "receiverIdentificationCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "receiverRole",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "messageDateTime",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "scheduleTimeInterval",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "scheduleTimeSeries",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "scheduleTimeSeries",
				mappedBy: "schedules",
				show: false
			},
			{
				accessor: "scheduleTimeStart",
				fluidSize: 3,
				dataType: "DATETIME"
			},
			{
				accessor: "scheduleTimeEnd",
				fluidSize: 3,
				dataType: "DATETIME"
			},
			{
				accessor: "isPPS",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "isSent",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "sendMailMessage",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "sendMailMessages"
			},
			{
				accessor: "dbFile",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "dBFiles",
			},
		]
	},
	scheduleTimeSeries: {
		className: "ScheduleTimeSeries",
		displayAttr: "id",
		columns: [
			{
				accessor: "id",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				URL: "/scheduleTimeSeries"
			},
			{
				accessor: "schedule",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "schedules"
			},
			{
				accessor: "sendersTimeSeriesIdentification",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "sendersTimeSeriesVersion",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "businessType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "product",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "objectAggregation",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "inAreaV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "inAreaCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outAreaV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outAreaCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "inPartyV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "inPartyCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outPartyV",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outPartyCodingScheme",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "measurementUnit",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "timeInterval",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "resolution",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "intervals",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "intervals",
				mappedBy: "scheduleTimeSeries",
				show: false
			}
		]
	},
	intervals: {
		className: "Interval",
		displayAttr: "pos",
		columns: [
			{
				accessor: "pos",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				URL: "/intervals"
			},
			{
				accessor: "qty",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "scheduleTimeSeries",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "scheduleTimeSeries"
				
			}
		]
	},
	loiMaxLoadMWSeasons: {
		className: "LoiMaxLoadMWSeason",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiMaxLoadWeathers: {
		className: "LoiMaxLoadWeather",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiTypeOfPowerPlants: {
		className: "LoiTypeOfPowerPlant",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiGrids: {
		className: "LoiGrid",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	powerPlantProfiles: {
		className: "PowerPlantProfile",
		displayAttr: "name",
		pageURL: "/powerPlantProfiles",
		columns: [
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				URL: "/powerPlantProfiles"
			},
			{
				accessor: "loiMaxLoadMWWeather",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiMaxLoadWeathers"
			},
			{
				accessor: "loiMaxLoadMWSeason",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiMaxLoadMWSeasons"
			},
			{
				accessor: "quarterOfHours",
				show: false,
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "quarterOfHours",
				mappedBy: "powerPlantProfile"
			},
			{
				accessor: "powerPlants",
				show: false,
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "powerPlants",
				mappedBy: "powerPlantProfile"
			}
		]
	},
	loiContractStatuses: {
		className: "LoiContractStatus",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiContractQuantities: {
		className: "LoiContractQuantity",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiContractPrices: {
		className: "LoiContractPrice",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiContractFees: {
		className: "LoiContractFee",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiProtocolCountPerMonths: {
		className: "LoiProtocolCountPerMonth",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiProtocolLineCounts: {
		className: "LoiProtocolLineCount",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	powerPlants: {
		className: "PowerPlant",
		displayAttr: "name",
		pageURL: "/powerPlants",
		columns: [
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				URL: "/powerPlants"
			},
			{
				accessor: "traderEic",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "producerEic",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "address",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "contactPerson",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "accessPoint",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "installedPowerMw",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "contractId",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "contractDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "annex",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "term",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "identificationNumber",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "distributionNetwork",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "value",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "valueSec",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "minPriceMWh",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiTypeOfPowerPlants"
			},
			{
				accessor: "grid",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiGrids"
			},
			{
				accessor: "powerPlantProfile",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "powerPlantProfiles"
			},
			{
				accessor: "owner",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "legalPersons"
			},
			{
				accessor: "contractStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractStatuses"
			},
			{
				accessor: "loiContractQuantity",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractQuantities"
			},
			{
				accessor: "loiContractPrice",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractPrices"
			},
			{
				accessor: "loiContractFee",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractFees"
			},
			{
				accessor: "loiProtocolCountPerMonth",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiProtocolCountPerMonths"
			},
			{
				accessor: "loiProtocolLineCount",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiProtocolLineCounts"
			}, 
			{
				accessor: "agreementType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "agreementTypes",
			},
			// {
			// 	accessor: "agreementsSelfInvoicing",
			// 	show: true,
			// 	fluidSize: 2,
			// 	dataType: "ENTITY",
			// 	entityType: "agreementSelfInvoicings",
			// 	mappedBy: "powerPlant"
			// }
			// {
			// 	accessor: "iban",
			// 	fluidSize: 3,
			// 	dataType: "TEXT",
			// }, 
			// {
			// 	accessor: "rangeForInvoicesFrom",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }, 
			// {
			// 	accessor: "rangeForInvoicesTo",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }, 
			// {
			// 	accessor: "rangeForCreditNotesFrom",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }, 
			// {
			// 	accessor: "rangeForCreditNotesTo",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }, 
			// {
			// 	accessor: "rangeForDebitNotesFrom",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }, 
			// {
			// 	accessor: "rangeForDebitNotesTo",
			// 	fluidSize: 3,
			// 	dataType: "UNIT",
			// }
		]
	},
	quarterOfHours: {
		className: "QuarterOfHour",
		displayAttr: "quarterOfHour",
		columns: [
			{
				accessor: "quarterOfHour",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				URL: "/quarterOfHours"
			},
			{
				accessor: "value",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "powerPlantProfile",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "powerPlantProfiles"
			}
		]
	},
	dBFiles: {
		className: "DBFile",
		label: "Файлове",
		displayAttr: "name",
		aclRestrictable: true,
		columns: [
			{
				Header: "Наименование",
				accessor: 'name',
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Тип",
				accessor: 'contentType',
				fluidSize: 2,
				dataType: "TEXT",
			}, {
				Header: "Хештаг етикети",
				accessor: 'hashTags',
				sortable: false,
				fluidSize: 2,
				Cell: (props) => <FieldMultiSelectOrEditContainer
					componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
					href={props.original && props.original._links && props.original._links.hashTags ? props.original._links.hashTags.href : undefined}
					listType="hashTags"
					listAttr="text"
					editable={props.original._editable}
				/>,
				filterToParam: (temp,curr) => {
					//TODO filter with select from list of hashtags
					//TODO put proper filter column name (_fieldPath?)
					temp["DBFile."+curr.id + ".text"] = curr.value;
					return temp;
				}
			}
		]
	},
	ibexPrices: {
		className: "IbexPrice",
		displayAttr: "quarterOfHour",
		columns: [
			{
				accessor: "localDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "hour",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "priceEUR",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "priceBGN",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "volume",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	ibexEnergyDeals: {
		className: "IbexEnergyDeal",
		displayAttr: "tradeId",
		columns: [
			{
				accessor: "id",
				fluidSize: 3,
				dataType: "UNIT",
				show: false,
				isLink: true,
				URL: "/ibexEnergyDeals"
			},
			{
				accessor: "updatedAt",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "tradeId",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				URL: "/ibexEnergyDeals"
			},
			{
				accessor: "tradeTime",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "state",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "eventSequenceNo",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "revisionNo",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "mediumDisplayName",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "companyTrade",
				fluidSize: 3,
				dataType: "BOOLEAN",
			},
			{
				accessor: "isDistributedToSchedules",
				fluidSize: 3,
				dataType: "BOOLEAN",
			},
		]
	},
	ibexEnergyDealLegs: {
		className: "IbexEnergyDealLeg",
		columns: [
			{
				accessor: "contractId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "side",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "unitPrice",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "deliveryAreaId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "aggressor",
				fluidSize: 3,
				dataType: "BOOLEAN",
			},
			{
				accessor: "portfolioId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "orderId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "userId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "deliveryStart",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "deliveryEnd",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "orderType",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "text",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "clientOrderId",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "ibexEnergyDeal",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "ibexEnergyDeals"
			},
		]
	},
	powerPlantProducedSchedules: {
		className: "PowerPlantProducedSchedule",
		columns: [
			{
				accessor: "identification",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "startTS",
				fluidSize: 3,
				dataType: "TIMESTAMP",
			},
			{
				accessor: "endTS",
				fluidSize: 3,
				dataType: "TIMESTAMP",
			},
			{
				accessor: "quantityKwh",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	powerPlantMeterReadings: {
		className: "PowerPlantMeterReading",
		columns: [
			{
				accessor: "identification",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "startTS",
				fluidSize: 3,
				dataType: "TIMESTAMP",
			},
			{
				accessor: "endTS",
				fluidSize: 3,
				dataType: "TIMESTAMP",
			},
			{
				accessor: "quantityKwh",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	loiProtocolStatuses: {
		className: "LoiProtocolStatus",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	powerPlantProtocols: {
		className: "PowerPlantProtocol",
		columns: [
			{
				accessor: "dateFrom",
				fluidSize: 3,
				width: 180,
				dataType: "DATE",
			},
			{
				accessor: "dateTo",
				fluidSize: 3,
				width: 180,
				dataType: "DATE",
			},
			{
				accessor: "owner",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "legalPersons"
			},
			{
				accessor: "powerPlant",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "powerPlants"
			},
			{
				accessor: "loiContractQuantity",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "loiContractQuantities"
			},
			{
				accessor: "loiContractPrice",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "loiContractPrices"
			},
			{
				accessor: "loiProtocolLineCount",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "loiProtocolLineCounts"
			},
			{
				accessor: "loiProtocolStatus",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "loiProtocolStatuses"
			},
			{
				accessor: "sendMailMessage",
				fluidSize: 3,
				width: 180,
				dataType: "ENTITY",
				entityType: "sendMailMessages"
			},
			{
				accessor: "pdfFile",
				fluidSize: 3,
				width: 180,
				dataType: "TEXT",
				Cell: (props) => <FontAwesomeIcon size="4x" icon="file-pdf" style={{color: "#0F65AA", cursor: "pointer" }} onClick={() => {
					var objbuilder = '';
					objbuilder += ('<object width="100%" height="100%" data="data:application/pdf;base64,');
					objbuilder += (props.original.pdfFile);
					objbuilder += ('" type="application/pdf" class="internal">');
					objbuilder += ('<embed src="data:application/pdf;base64,');
					objbuilder += (props.original.pdfFile);
					objbuilder += ('" type="application/pdf"  />');
					objbuilder += ('</object>');
					
					var win = window.open("#","_blank");
					var title = "my tab title";
					win.document.write('<html><title>'+ title +'</title><body style="margin-top: 0px; margin-left: 0px; margin-right: 0px; margin-bottom: 0px;">');
					win.document.write(objbuilder);
					win.document.write('</body></html>');
					jQuery(win.document);
				}
			} />,
			},
			{
				accessor: "xlsxFile",
				fluidSize: 3,
				width: 180,
				dataType: "TEXT",
				Cell: (props) => <FontAwesomeIcon size="4x" icon="file-excel" style={{color: "#0F65AA", cursor: "pointer" }} onClick={() => {
					var workbook = XLSX.read(props.original.xlsxFile, {type: "base64"});
					let itn = props["original"]["powerPlant"]["identificationNumber"]
					// let fromDate = props["row"]["dateFrom"]
					// let toDate = props["row"]["dateTo"]
					XLSX.writeFile(workbook, itn + ".xlsx");
				}} />,
			},
		]
	},
	energyBalancingContracts: {
		className: "EnergyBalancingContract",
		displayAttr: "name",
		pageURL: "/energyBalancingContracts",
		columns: [
			{
				accessor: "contractId",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "contractDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "annex",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "term",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "identificationNumber",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "distributionNetwork",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "condition",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				URL: "/energyBalancingContracts"
			},
			{
				accessor: "traderEic",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "producerEic",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "address",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "contactPerson",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "accessPoint",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "installedPowerMw",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "value",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "valueSec",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "minPriceMWh",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiTypeOfPowerPlants"
			},
			{
				accessor: "grid",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiGrids"
			},
			{
				accessor: "powerPlantProfile",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "powerPlantProfiles"
			},
			{
				accessor: "owner",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "legalPersons"
			},
			{
				accessor: "contractStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractStatuses"
			},
			{
				accessor: "loiContractPrice",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractPrices"
			},
			{
				accessor: "loiContractFee",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiContractFees"
			}
		]
	},
	loiNotificationTypes: {
		className: "LoiNotificationType",
		displayAttr: "listOptionItemName",
		columns: [
			{
				accessor: "listOptionItemCode",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "listOptionItemName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	notifications: {
		className: "Notification",
		columns: [
			{
				accessor: "message",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "messageDateTime",
				fluidSize: 3,
				dataType: "DATETIME"
			},
			{
				accessor: "expirationDate",
				fluidSize: 3,
				dataType: "DATETIME"
			},
			{
				accessor: "isActive",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "userName",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "roleName",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "identificationFirst",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "identificationSecond",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "identificationThird",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "identificationFourth",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "loiNotificationType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiNotificationTypes"
			}
		]
	},
}

export default nepalEntityDefinitions