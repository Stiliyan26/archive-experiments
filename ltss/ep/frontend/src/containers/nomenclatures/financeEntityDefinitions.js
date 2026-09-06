import React from 'react'; //needed for Cell to work, otherwise gives error "ReferenceError: React is not defined"
import { getLabelWithConstraints } from './entityDefinitions'

const financeEntityDefinitions = {
	//COMMON
	loiGoodsTypes: {
		className: "LoiGoodsType",
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
	cCcGoodsTypes: {
		className: "CCcGoodsType",
		displayAttr: "name",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "goodType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiGoodsTypes"
			},
			{
				accessor: "activeFrom",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "activeTo",
				fluidSize: 3,
				dataType: "DATE",
			}
		]
	},
	cGoodMarks: {
		className: "CGoodMark",
		displayAttr: "markName",
		columns: [
			{
				accessor: "markCode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "markName",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "description",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "defaultVendor",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "lastNumber",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	cGoodses: {
		className: "CGoods",
		displayAttr: "nameBg",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id,
				item.nameBg,
				(item.code ? item.code + " " + item.nameBg : ""),
				(item.manufacturerCode ? item.manufacturerCode + " " + item.nameBg : "")]
			,constraints);}),
		lookupColumns: ["nameBg","nameEng","code","manufacturerCode"],
		pageURL: "/cGoods",
		columns: [
			{
				accessor: "nameBg",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/cGoods",
			},
			{
				accessor: "nameEng",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "gteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes"
			},
			{
				accessor: "goodType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiGoodsTypes"
			},
			{
				accessor: "goodMark",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cGoodMarks"
			},
			{
				accessor: "defaultVendor",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "barcode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "manufacturerCode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "bundleQuantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "minQuantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "maxQuantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "weight",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "volume",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "reserveQuantities",
				show: false,
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cReserveQuantities",
				mappedBy: "godId",
			},
			{
				accessor: "description",
				fluidSize: 3,
				dataType: "TEXT",
			},
		]
	},
	cCtBanks: {
		className: "CCtBank",
		displayAttr: "name",
		pageURL: "/cCtBanks",
		columns: [
					{
						accessor: "name",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtBanks",
					},
					{
						accessor: "nameEn",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtBanks",
					},
					{
						accessor: "bic",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtBanks",
					},
					{
						accessor: "activeFrom",
						fluidSize: 3,
						dataType: "DATE",
					},
					{
						accessor: "activeTo",
						fluidSize: 3,
						dataType: "DATE",
					},
					{
						accessor: "format",
						fluidSize: 3,
						dataType: "TEXT",
					}
				]
	},
	loiCtBankAccountBatTypes: {
		className: "LoiCtBankAccountBatType",
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
	cCtBankAccounts: {
		className: "CCtBankAccount",
		displayAttr: "name",
		pageURL: "/cCtBankAccounts",
		columns: [
					{
						accessor: "bank",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "cCtBanks"
					},
					{
						accessor: "iban",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtBankAccounts",
					},
					{
						accessor: "name",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtBankAccounts",
					},
					{
						accessor: "currency",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "cCtCurrencies"
					},
					{
						accessor: "activeFrom",
						fluidSize: 3,
						dataType: "DATE",
					},
					{
						accessor: "activeTo",
						fluidSize: 3,
						dataType: "DATE",
					},
					{
						accessor: "batType",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "loiCtBankAccountBatTypes"
					},
					{
						accessor: "code",
						fluidSize: 3,
						dataType: "TEXT",
					},
					{
						accessor: "outCode",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "cCcOrganizationUnits"
					}
				]
	},
	cCtCurrencies: {
		className: "CCtCurrency",
		displayAttr: "name",
		pageURL: "/cCtCurrencies",
		columns: [
					{
						accessor: "code",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtCurrencies",
					},
					{
						accessor: "name",
						fluidSize: 3,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/cCtCurrencies",
					},
					{
						accessor: "isDefault",
						fluidSize: 3,
						dataType: "BOOLEAN",
					},
					{
						accessor: "unitEn",
						fluidSize: 3,
						dataType: "TEXT",
					},
					{
						accessor: "fractionEn",
						fluidSize: 3,
						dataType: "TEXT",
					},
					{
						accessor: "activeFrom",
						fluidSize: 3,
						dataType: "DATE",
					},
					{
						accessor: "activeTo",
						fluidSize: 3,
						dataType: "DATE",
					}
				]
	},
	cCcOrganizationUnits: {
		className: "CCcOrganizationUnit",
		displayAttr: "name",
		pageURL: "/organizationUnit",
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/organizationUnit"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "abbreviation",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "bulstat",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "address",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "mainOutCode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "outId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "organizationUnitType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "sebraCode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "vatNumber",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "ddsRegDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "ddsCloseRegDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "activeFrom",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "activeTo",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "nameEn",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "addressEn",
				fluidSize: 3,
				dataType: "TEXT",
			}
		]
	},
	cCfgDocumentCounters: {
		className: "CCfgDocumentCounter",
		displayAttr: "name",
		columns: [
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "description",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "initialValue",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "nextValue",
				fluidSize: 3,
				dataType: "UNIT",
			}
		]
	},
	cCfgDocumentPatterns: {
		className: "CCfgDocumentPattern",
		displayAttr: "documentCode",
		columns:[
			{
				accessor: "cfgDcr",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCfgDocumentCounters"
			},
			{
				accessor: "documentCode",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "pattern",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "complementarySymbols",
				fluidSize: 3,
				dataType: "TEXT",
			}
		]
	},
	cPmtCurrencyRates: {
		className: "CPmtCurrencyRate",
		displayAttr: "outCode",
		pageURL: "/cPmtCurrencyRates",
		columns: [
			{
				accessor: "id",
				fluidSize: 1,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/cPmtCurrencyRates",
				isReadOnly: true,
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "cuyCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "dateFrom",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "dateTo",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "unitOfCuy",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "inMainCuy",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "perMainCuy",
				fluidSize: 3,
				dataType: "UNIT"
			},
			//not for users but internal field - if "Y" then ignores rules
			//{
			//	accessor: "triggerIns",
			//	fluidSize: 3,
			//	dataType: "TEXT"
			//}
		]
	},
	loiCTransitionSides: {
		className: "LoiCTransitionSide",
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
	loiCTransitionTypes:{
		className: "LoiCTransitionType",
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
	fCtTransitionTypes: {
		className: "FCtTransitionType",
		displayAttr: "code",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
					"\u{1F194}"+item.id,
					(item.code ? item.code : ""),
					(item.name ? item.name : ""),
					(item.code && item.name ? item.code+" - "+item.name : "")
				]
			,constraints);}),
		lookupColumns: ["code","name"],
		pageURL: "/fCtTransitionTypes",
		columns:[
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtTransitionTypes",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtTransitionTypes",
			},
			{
				accessor: "tteType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiCTransitionTypes"
			},
			{
				accessor: "tteSide",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiCTransitionSides"
			},
			{
				accessor: "fCtBatchTteTypes",
				show: false,
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fCtBatchTteTypes",
				mappedBy: "tteId",
			},
		]
	},
	loiPartnerTypes: {
		className: "LoiPartnerType",
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
	cCtPartnerGroups: {
		className: "CCtPartnerGroup",
		displayAttr: "name",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFrom",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeTo",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			}
		]
	},
	loiLegalStatuses: {
		className: "LoiLegalStatus",
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
	cCcPartners: {
		className: "CCcPartner",
		displayAttr: "name",
		pageURL: "/partners",
		lookupColumns: ["name","bulstat"],
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id,
				item.name,
				(item.bulstat ? item.bulstat + " " + item.name : "")]
			,constraints);}),
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/partners"
			},
			{
				accessor: "accountMgrUser",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "secUsers"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "address",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "partnerType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPartnerTypes"
			},
			{
				accessor: "partnerGroup",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtPartnerGroups"
			},
			{
				accessor: "legalStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiLegalStatuses"
			},
			{
				accessor: "mol",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "bulstat",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "egn",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "vatNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			// {
			// 	accessor: "oldCode",
			// 	fluidSize: 3,
			// 	dataType: "UNIT"
			// },
			{
				accessor: "foreignNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFrom",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeTo",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "tel",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "fax",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "email",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "defaultVendorGoods",
				show: false,
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				mappedBy: "defaultVendor",
			},
		]
	},
	loiTypeDocs: {
		className: "LoiTypeDoc",
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
	cTypeDocuments: {
		className: "CTypeDocument",
		displayAttr: "name",
		columns: [
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "shortName",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	cMeasures: {
		className: "CMeasure",
		displayAttr: "name",
		columns: [
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "cofficient",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			}
		]
	},
	loiServiceTypes: {
		className: "LoiServiceType",
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
	cServices: {
		className: "CService",
		displayAttr: "name",
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "description",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "serviceType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiServiceTypes"
			}
		]
	},
	loiOfferStatuses: {
		className: "LoiOfferStatus",
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
	cOffers: {
		className: "COffer",
		displayAttr: "numberOfr",
		pageURL: "/offers",
		columns: [
			{
				accessor: "numberOfr",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				isReadOnly: true,
				pageURL: "/offers"
			},
			{
				accessor: "startDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "endDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "partner",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "outId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiOfferStatuses"
			},
			{
				accessor: "discount",
				fluidSize: 2,
				dataType: "UNIT",
				isReadOnly: true
			},
			{
				accessor: "vat",
				fluidSize: 2,
				dataType: "UNIT",
				isReadOnly: true
			},
			{
				accessor: "total",
				fluidSize: 2,
				dataType: "UNIT",
				isReadOnly: true
			},
			{
				accessor: "totalWithDisc",
				fluidSize: 2,
				dataType: "UNIT",
				isReadOnly: true
			},
			{
				accessor: "advanceTotal",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "sumVat",
				fluidSize: 2,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={Math.round(props.original.totalWithDisc*props.original.vat)/100} disabled={true}/>
			},
			{
				accessor: "totalWithVat",
				fluidSize: 2,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={Math.round(props.original.totalWithDisc*(100+props.original.vat))/100} disabled={true}/>
			},
			{
				accessor: "proformNumber",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "proformDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "currency",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "remark",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "person",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	cOfferDetails: {
		className: "COfferDetail",
		displayAttr: "id",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id,
				(item.godId && item.godId.nameBg ? item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg && item.meeId ? item.quantity + item.meeId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.nameBg + " x"+item.price1 : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.nameBg + " x"+item.price1+"лв." : ""),
				(item.godId && item.godId.nameBg ? item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg && item.meeId ? item.quantity + item.meeId.code + " " + item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.code + " " + item.godId.nameBg + " x"+item.price1 : ""),
				(item.godId && item.godId.nameBg ? item.quantity + "x " + item.godId.code + " " + item.godId.nameBg + " x"+item.price1+"лв." : "")
			],constraints);}),
		columns: [
			{
				accessor: "godId",
				fluidSize: 4,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				width: 240,
				doCalcOnChange: true,
			},
			{
				accessor: "seeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cServices"
			},
			{
				accessor: "ofrId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cOffers"
			},
			{
				accessor: "discount",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "price1",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "price2",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "price3",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "price4",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "price5",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "quantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "total",
				fluidSize: 2,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={Math.round(props.original.quantity*props.original.price1*100)/100} disabled={true}/>
			},
			{
				accessor: "sdlQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "allocationProxies",
				show: false,
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "allocationProxies",
				mappedBy: "allocationOrigin",
			},
		]
	},
	cOfferStatuses: {
		className: "COfferStatus",
		displayAttr: "id",
		columns: [
			{
				accessor: "ofrId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cOffers"
			},
			{
				accessor: "oldStatus",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "loiOfferStatuses"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "loiOfferStatuses"
			},
			{
				accessor: "remark",
				fluidSize: 3,
				dataType: "TEXT"
			},
		]
	},
	cPriceLists: {
		className: "CPriceList",
		displayAttr: "id",
		pageURL: "/cPriceList",
		columns: [
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
			},
			{
				accessor: "price1",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "currency",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "startDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "endDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "basePrice",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "doiId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveryOfferItems"
			},
			{
				accessor: "stkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "price2",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "price3",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "price4",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "price5",
				fluidSize: 3,
				dataType: "UNIT"
			},
		]
	},
	cRequests: {
		className: "CRequest",
		displayAttr: "status",
		columns: [
			{
				accessor: "dateOdy",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "term",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "partner",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "retId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cRequests"
			},
			{
				accessor: "numberOdy",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/requests"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "remark",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			}
		]
	},
	cRequestDetails: {
		className: "CRequestDetail",
		displayAttr: "godId",
		columns: [
			{
				accessor: "retId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cRequests"
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "price",
				fluidSize: 3,
				dataType: "UNIT"
			}
		]
	},

	//FINANCE
	loiBreTransitionResults: {
		className: "LoiBreTransitionResult",
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
	fBreTransitions: {
		className: "FBreTransition",
		displayAttr: "id",
		pageURL: "/fBreTransitions",
		columns: [
			{
				accessor: "bahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "refNo",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fBreTransitions",
			},
			{
				accessor: "refDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "result",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBreTransitionResults",
			},
			{
				accessor: "tteCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "amountTotal",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "dependenceType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchTypeRuleDependenceTypes"
			},
			{
				accessor: "dependenceCode",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s"
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s"
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts"
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes"
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources"
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms"
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions"
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks"
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "postDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "module",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "amountOutstanding",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "amountDo",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "amountVat",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cost",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cuyCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "cuyRate",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "dueDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "ideCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtInvDealTypes"
			},
			{
				accessor: "addRefNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "addRefDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "advance",
				fluidSize: 3,
				dataType: "UNIT"
			}
		]
	},
	fCcContracts: {
		className: "FCcContract",
		displayAttr: "id",
		pageURL: "/fCcContracts",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "contractNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "contractStartDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "contractEndDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "contractDate",
				fluidSize: 3,
				dataType: "DATE"
			}
		]
	},
	fCcEbks: {
		className: "FCcEbk",
		pageURL:"/fCcEbks",
		displayAttr: "name",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "ebkGroup",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "ebkIdUp",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fCcEbks"
			},
			{
				accessor: "serbraCode",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "source",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "formula",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCcFinsources: {
		className: "FCcFinsource",
		pageURL:"/fCcFinsources",
		displayAttr: "name",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCcFunctions: {
		className: "FCcFunction",
		pageURL:"/fCcFunctions",
		displayAttr: "code",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "source",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "funIdUp",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fCcFunctions"
			},
			{
				accessor: "fType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCcPrograms: {
		className: "FCcProgram",
		pageURL:"/fCcPrograms",
		displayAttr: "code",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "prmIdUp",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fCcPrograms"
			},
			{
				accessor: "prmType",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "prmGroup",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "sortCode",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCcReserve1s: {
		className: "FCcReserve1",
		pageURL:"/fCcReserve1s",
		displayAttr: "code",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "re1IdUp",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fCcReserve1s"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type1",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type2",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCcReserve2s: {
		className: "FCcReserve2",
		pageURL:"/fCcReserve2s",
		displayAttr: "code",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type1",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type2",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiBatchCalculationTypes: {
		className: "LoiBatchCalculationType",
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
	fCtBatchTypes: {
		className: "FCtBatchType",
		pageURL:"/fCtBatchTypes",
		displayAttr: "name",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
					"\u{1F194}"+item.id,
					(item.code ? item.code : ""),
					(item.name ? item.name : ""),
					(item.code && item.name ? item.code+" - "+item.name : "")
				]
			,constraints);}),
		lookupColumns: ["code","name"],
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtBatchTypes",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtBatchTypes",
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "isDds",
				fluidSize: 3,
				dataType: "BOOLEAN",
			},
			{
				accessor: "typeNo",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchCalculationTypes"
			},
			{
				accessor: "side",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchJteDefaultSides"
			}
		]
	},
	loiInvInvoicePaymentTypes: {
		className: "LoiInvInvoicePaymentType",
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
	loiInvInvoiceStatuses: {
		className: "LoiInvInvoiceStatus",
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
	fInvInvoices: {
		className: "FInvInvoice",
		displayAttr: "id",
		columns: [
			{
				accessor: "invNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "invDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "datePayment",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "dateAccount",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "ineId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fInvInvoices",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "withoutVatReason",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "percentVat",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "taxBase",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vatAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "totalAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "paymentType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiInvInvoicePaymentTypes"
			},
			{
				accessor: "batId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtBankAccounts",
			},
			{
				accessor: "reason",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "notes",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "compilerName",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "salePeriod",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "purchasePeriod",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "invType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchJteDefaultSides",
			},
			{
				accessor: "cuyCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "cuyRate",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiInvInvoiceStatuses"
			},
			{
				accessor: "vodBahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "bahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "processStatus",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cuyUnit",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "tteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes"
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "ideId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtInvDealTypes"
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks"
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions"
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms"
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources"
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes"
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts"
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s"
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s"
			}
		]
	},
	loiJournalTypeCalculationTypes: {
		className: "LoiJournalTypeCalculationType",
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
	fJournalTypes: {
		className: "FJournalType",
		pageURL:"/fJournalTypes",
		displayAttr: "name",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
					"\u{1F194}"+item.id,
					(item.code ? item.code : ""),
					(item.name ? item.name : ""),
					(item.code && item.name ? item.code+" - "+item.name : "")
				]
			,constraints);}),
		lookupColumns: ["code","name"],
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fJournalTypes",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fJournalTypes",
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiJournalTypeCalculationTypes"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			}
		]
	},
	fDiscounts: {
		className: "FDiscount",
		displayAttr: "discount",
		columns: [
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "god",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "pgpId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtPartnerGroups"
			},
			{
				accessor: "gteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			}
		]
	},
	loiTypeOfFinancialAccounts: {
		className: "LoiTypeOfFinancialAccount",
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
	loiAccountActivityTypes: {
		className: "LoiAccountActivityType",
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
	fChartAccounts: {
		className: "FChartAccount",
		displayAttr: "name",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
					"\u{1F194}"+item.id,
					(item.code ? item.code : ""),
					(item.name ? item.name : ""),
					(item.code && item.name ? item.code+" - "+item.name : "")
				]
			,constraints);}),
		lookupColumns: ["code","name"],
		pageURL: "/chartAccounts",
		columns:[
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/chartAccounts"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "coaIdUp",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			},
			{
				accessor: "isSynthetic",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiTypeOfFinancialAccounts"
			},
			{
				accessor: "isBalanced",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiAccountActivityTypes"
			},
			{
				accessor: "dateOpened",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "dateClosed",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "isForeignCurrency",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "ebkRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "funRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "prmRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "fieRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "parRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "gteRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "cotRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "re1Required",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "re2Required",
				fluidSize: 3,
				dataType: "BOOLEAN"
			},
			{
				accessor: "outRequired",
				fluidSize: 3,
				dataType: "BOOLEAN"
			}
		]
	},
	loiBatchJteDefaultSides: {
		className: "LoiBatchJteDefaultSide",
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
	fCtBatchJteDefaults: {
		className: "FCtBatchJteDefault",
		displayAttr: "id",
		columns: [
			{
				accessor: "bteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtBatchTypes",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "jteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fJournalTypes"
			},
			{
				accessor: "side",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchJteDefaultSides"
			}
		]
	},
	loiBatchTteLinkStatuses: {
		className: "LoiBatchTteLinkStatus",
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
	fCtBatchTteLinks: {
		className: "FCtBatchTteLink",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "tteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes"
			},
			{
				accessor: "bteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtBatchTypes",
			},
			{
				accessor: "ideId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtInvDealTypes",
			},
			{
				accessor: "orderNum",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchTteLinkStatuses"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			}
		]
	},
	fCtBatchTteTypes: {
		className: "FCtBatchTteType",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "tteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes"
			},
			{
				accessor: "bteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtBatchTypes",
			}
		]
	},
	loiBatchTypeRuleAmountTypes: {
		className: "LoiBatchTypeRuleAmountType",
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
	loiBatchTypeRuleDependenceTypes: {
		className: "LoiBatchTypeRuleDependenceType",
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
	fCtBatchTypeRules: {
		className: "FCtBatchTypeRule",
		displayAttr: "id",
		pageURL: "/fCtBatchTypeRules",
		columns: [
			{
				accessor: "rueId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtRules",
			},
			{
				accessor: "tteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes",
			},
			{
				accessor: "jteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fJournalTypes"
			},
			{
				accessor: "amountType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchTypeRuleAmountTypes"
			},
			{
				accessor: "coaIdDt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			},
			{
				accessor: "coaIdCt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			},
			{
				accessor: "orderNum",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/fCtBatchTypeRules",
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "dependenceType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchTypeRuleDependenceTypes",
			},
			{
				accessor: "dependenceIdFake",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "dependenceId",
				fluidSize: 3,
				dataType: "UNIT"
			}
		]
	},
	fCtContractTypes: {
		className: "FCtContractType",
		displayAttr: "id",
		pageURL: "/fCtContractTypes",
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "kind",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCtGoods: {
		className: "FCtGood",
		displayAttr: "code",
		pageURL: "/fCtGoods",
		columns: [
			{
				accessor: "gteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes",
			},
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtGoods",
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "activeFromDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "activeToDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "price",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "measure",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "priceAdditional",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "oldId",
				fluidSize: 3,
				dataType: "TEXT"
			},
			
			{
				accessor: "outMask",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	loiRepresentativeTypes: {
		className: "LoiRepresentativeType",
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
	fCtRepresentatives: {
		className: "FCtRepresentative",
		displayAttr: "name",
		pageURL: "/fCtRepresentatives",
		columns: [
			{
				accessor: "ideNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtRepresentatives",
			},
			{
				accessor: "nameEn",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiRepresentativeTypes",
			},
			{
				accessor: "position",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "country",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "zipCode",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "city",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "address",
				fluidSize: 3,
				dataType: "TEXTAREA"
			},
			{
				accessor: "phone",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "mobile1",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "mobile2",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "email",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "notes",
				fluidSize: 3,
				dataType: "TEXTAREA"
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			}
		]
	},
	fCtRules: {
		className: "FCtRule",
		displayAttr: "code",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
					"\u{1F194}"+item.id,
					(item.code ? item.code : ""),
					(item.name ? item.name : ""),
					(item.code && item.name ? item.code+" - "+item.name : "")
				]
			,constraints);}),
		lookupColumns: ["code","name"],
		pageURL: "/fCtRules",
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtRules",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fCtRules",
			},
			{
				accessor: "expression",
				fluidSize: 3,
				dataType: "TEXTAREA"
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXTAREA"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			}
		]
	},
	loiCtStornoTypeTypes: {
		className: "LoiCtStornoTypeType",
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
	fCtStornoTypes: {
		className: "FCtStornoType",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "type",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiCtStornoTypeTypes",
			},
			{
				accessor: "isDefault",
				fluidSize: 3,
				dataType: "BOOLEAN",
			}
		]
	},
	fHpCcBalances: {
		className: "FHpCcBalance",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "ccEbkCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccEbkDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFunCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFunDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccPrmCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccPrmDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFieCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFieDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccParCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccParDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccGteCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccGteDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccCotCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccCotDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccOutCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccOutDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe1CtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe1DtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe2CtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe2DtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "period",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "coaId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts",
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks",
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions",
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources",
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms",
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes",
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts",
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s",
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s",
			},
		]
	},
	loiInvDdsFileStatuses: {
		className: "LoiInvDdsFileStatus",
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
	fInvDdsFiles: {
		className: "FInvDdsFile",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "period",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "reeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtRepresentatives",
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiInvDdsFileStatuses",
			},
			{
				accessor: "vatPayIn",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vatRestore1",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vatRestore3",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vatRestore4",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "salesFile",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "purchaseFile",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "viesFile",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "deklarationFile",
				fluidSize: 3,
				dataType: "TEXT"
			}
		]
	},
	fCtJteDefaults: {
		className: "FCtJteDefault",
		displayAttr: "name",
		columns: [
			{
				accessor: "jte",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fJournalTypes"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "coaDt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			},
			{
				accessor: "coaCt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			}
		]
	},
	loiPtJournalStatuses: {
		className: "LoiPtJournalStatus",
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
	loiPtJournalCcStatuses: {
		className: "LoiPtJournalCcStatus",
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
	fPtBatches: {
		className: "FPtBatch",
		displayAttr: "refNo",
		pageURL: "/fPtBatches",
		columns: [
			{
				accessor: "refNo",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/fPtBatches"
			},
			{
				accessor: "refDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "bteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtBatchTypes",
			},
			{
				accessor: "ideId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtInvDealTypes",
				doCalcOnChange: true,
			},
			{
				accessor: "corrRefNo",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "salePeriod",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "purchasePeriod",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "cuyCode",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cCtCurrencies",
				doCalcOnChange: true,
			},
			{
				accessor: "cuyUnit",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "cuyRate",
				fluidSize: 2,
				dataType: "UNIT",
				doCalcOnChange: true,
			},
			{
				accessor: "amount2Currency",
				fluidSize: 2,
				dataType: "UNIT",
				doCalcOnChange: true,
			},
			{
				accessor: "amount3Currency",
				fluidSize: 2,
				dataType: "UNIT",
				doCalcOnChange: true,
			},
			{
				accessor: "amountCurrency",
				fluidSize: 2,
				dataType: "UNIT",
				doCalcOnChange: true,
			},
			{
				accessor: "amountOutstandingCurrency",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "amount2",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "amount3",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "amount",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "amountOutstanding",
				fluidSize: 2,
				dataType: "UNIT",
			},
			{
				accessor: "postDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "dueDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "descr",
				fluidSize: 12,
				dataType: "TEXTAREA",
			},
			{
				accessor: "module",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "dfeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fInvDdsFiles",
			},
			{
				accessor: "dfeId2",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fInvDdsFiles",
			},
		]
	},
	loiPtBatchLinkStatuses: {
		className: "LoiPtBatchLinkStatus",
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
	loiLinkedFlags: {
		className: "LoiLinkedFlag",
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
	fPtBatchLinks: {
		className: "FPtBatchLink",
		displayAttr: "id",
		columns: [
			{
				accessor: "bahId1",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches",
			},
			{
				accessor: "bahId2",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches",
			},
			{
				accessor: "amount",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "amountCurrency",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "cuyCode",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "cuyRate",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "cuyUnit",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "status",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "loiPtBatchLinkStatuses"
			},
			{
				accessor: "flag1",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "loiLinkedFlags"
			},
			{
				accessor: "flag2",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "loiLinkedFlags"
			}
		]
	},
	loiPtBatchCcDetailStatuses: {
		className: "LoiPtBatchCcDetailStatus",
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
	fPtBatchCcDetails: {
		className: "FPtBatchCcDetail",
		displayAttr: "id",
		columns: [
			{
				accessor: "tteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtTransitionTypes"
			},
			{
				accessor: "amountDo",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "amountVat",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "amountTotal",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtBatchCcDetailStatuses"
			},
			{
				accessor: "result",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions"
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes"
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks"
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources"
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms"
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts"
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s"
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s"
			},
			{
				accessor: "cuyCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "postDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "dependenceType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchTypeRuleDependenceTypes"
			},
			{
				accessor: "dependenceId",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "bahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
		]
	},
	fPtCcBalances: {
		className: "FPtCcBalance",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "ccEbkCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccEbkDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFunCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFunDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccPrmCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccPrmDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFieCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccFieDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccParCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccParDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccGteCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccGteDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccCotCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccCotDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccOutCtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccOutDtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe1CtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe1DtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe2CtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ccRe2DtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "period",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "coaId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts",
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks",
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions",
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources",
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms",
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes",
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts",
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s",
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s",
			},
		]
	},
	loiPtClosingAccountPeriodMonths: {
		className: "LoiPtClosingAccountPeriodMonth",
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
	loiPtClosingAccountStatuses: {
		className: "LoiPtClosingAccountStatus",
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
	fPtClosingAccounts: {
		className: "FPtClosingAccount",
		displayAttr: "id",
		columns: [
			{
				accessor: "periodMonth",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtClosingAccountPeriodMonths"
			},
			{
				accessor: "periodYear",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "coaId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			},
			{
				accessor: "bahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtClosingAccountStatuses"
			},
		]
	},
	fPtCoaBalances: {
		className: "FPtCoaBalance",
		displayAttr: "id",
		columns: [
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "ctAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "dtAmount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "period",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "dateUpdated",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "coaId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts"
			}
		]
	},
	fPtJournals: {
		className: "FPtJournal",
		displayAttr: "journalNo",
		pageURL: "/fPtJournals",
		columns: [
			{
				accessor: "bahId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fPtBatches"
			},
			{
				accessor: "journalNo",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/fPtJournals",
			},
			{
				accessor: "postDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "jteId",
				fluidSize: 6,
				dataType: "ENTITY",
				entityType: "fJournalTypes",
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtJournalStatuses"
			},
			{
				accessor: "stornoJolId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fPtJournals",
			},
			{
				accessor: "descr",
				fluidSize: 6,
				dataType: "TEXT"
			},
			{
				accessor: "cuyCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies",
			},
			{
				accessor: "cuyUnit",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cuyRate",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "ccStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtJournalCcStatuses",
			},
		]
	},
	loiPtPostingCcStatuses: {
		className: "LoiPtPostingCcStatus",
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
	loiPtPostingCoaStatuses: {
		className: "LoiPtPostingCoaStatus",
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
	fPtPostings: {
		className: "FPtPosting",
		displayAttr: "id",
		columns: [
			{
				accessor: "jolId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fPtJournals"
			},
			{
				accessor: "coaIdDt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts",
			},
			{
				accessor: "coaIdCt",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "fChartAccounts",
			},
			{
				accessor: "amountCurrency",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "amount",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "descr",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "ccEbkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcEbks",
			},
			{
				accessor: "ccPrmId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcPrograms",
			},
			{
				accessor: "ccFunId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFunctions",
			},
			{
				accessor: "ccParId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcPartners",
			},
			{
				accessor: "ccGteId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcGoodsTypes",
			},
			{
				accessor: "ccFieId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcFinsources",
			},
			{
				accessor: "ccCotId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcContracts",
			},
			{
				accessor: "ccOutId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits",
			},
			{
				accessor: "ccRe1Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve1s",
			},
			{
				accessor: "ccRe2Id",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCcReserve2s",
			},
			{
				accessor: "postDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "coaStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtPostingCoaStatuses"
			},
			{
				accessor: "ccStatus",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPtPostingCcStatuses"
			},
		]
	},
	fCtInvDealTypes: {
		className: "FCtInvDealType",
		displayAttr: "code",
		columns: [
			{
				accessor: "code",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "name",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "descr",
				fluidSize: 6,
				dataType: "TEXTAREA",
			},
			{
				accessor: "side",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiBatchJteDefaultSides",
			},
			{
				accessor: "percentVat",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT",
			},
		]
	},
}




export default financeEntityDefinitions
