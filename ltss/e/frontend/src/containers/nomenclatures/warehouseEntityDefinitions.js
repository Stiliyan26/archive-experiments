import React from 'react'; //needed for Cell to work, otherwise gives error "ReferenceError: React is not defined"
import moment from 'moment'
import { getLabelWithConstraints } from './entityDefinitions'

const warehouseEntityDefinitions = {
	//WAREHOUSE
	cStocks: {
		className: "CStock",
		pageURL: "/cStocks",
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
		lookupColumns: ["godId.nameBg","godId.code"],
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/cStocks",
				width: 50,
			},
			{
				accessor: "godId",
				fluidSize: 5,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
			},
			{
				accessor: "stockDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "ddlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveryDetails"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "meeId",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "availableQuantity",
				fluidSize: 2,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={props.original.initialQuantity - props.original.reserveQuantity - props.original.blockedQuantity - props.original.consignmentQuantity} disabled={true}/>
			},
			{
				accessor: "initialQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "quantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "reserveQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "blockedQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "costQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "cost",
				fluidSize: 2,
				dataType: "UNIT"
			},
			{
				accessor: "ddlCost",
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
				accessor: "consignmentQuantity",
				fluidSize: 2,
				dataType: "UNIT"
			},
		]
	},
	loiOrderStatuses: {
		className: "LoiOrderStatus",
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
	cOrders: {
		className: "COrder",
		displayAttr: "orderNum",
		pageURL: "/orders",
		columns: [
			{
				accessor: "orderNum",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/orders"
			},
			{
				accessor: "dateOrr",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "timeLimit",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "parId",
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
				accessor: "status",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiOrderStatuses"
			},
			{
				accessor: "total",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "finishDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: 'createdBy',
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "secUsers",
				isReadOnly: true,
			}
		]
	},
	cOrderDetails: {
		className: "COrderDetail",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id,
				(item.godId && item.godId.nameBg ? item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg && item.meeId ? item.quantityConfirm + item.meeId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.nameBg + " x"+item.priceConfirm : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.nameBg + " x"+item.priceConfirm+"лв." : ""),
				(item.godId && item.godId.nameBg ? item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg && item.meeId ? item.quantityConfirm + item.meeId.code + " " + item.godId.code + " " + item.godId.nameBg : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.code + " " + item.godId.nameBg + " x"+item.priceConfirm : ""),
				(item.godId && item.godId.nameBg ? item.quantityConfirm + "x " + item.godId.code + " " + item.godId.nameBg + " x"+item.priceConfirm+"лв." : "")
			],constraints);}),
		columns: [
			{
				accessor: "orrId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cOrders"
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				width: 240
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "price",
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
				accessor: "priceConfirm",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "quantityConfirm",
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
				accessor: "ddlQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cancelledQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "remainingQuantity",
				fluidSize: 2,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={props.original.quantityConfirm - props.original.ddlQuantity - props.original.cancelledQuantity} disabled={true}/>
			},
			{
				accessor: "rqyQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "stkQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "plnQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "deliveryDetails",
				show: false,
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveryDetails",
				mappedBy: "orlId",
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
	cReserveQuantities: {
		className: "CReserveQuantity",
		displayAttr: "id",
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				width: 240
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "initialQuantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ofrId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cOffers"
			},
			{
				accessor: "odlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cOfferDetails"
			},
			{
				accessor: "stkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks"
			},
			{
				accessor: "partner",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "term",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "retId",
				fluidSize: 3,
				dataType: "UNIT"
			},
		]
	},
	cDeliveryGoodMaps: {
		className: "CDeliveryGoodMap",
		displayAttr: "deyCode",
		pageURL: "/deliveryGoodMap",
		columns: [
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners",
				optionFilter: [{where: {
						op: "notEqual",
						operands: ["partnerType.listOptionItemCode", {literal: 1}],
					},
				}],
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				doCalcOnChange: true,
			},
			{
				accessor: "deyCode",
				fluidSize: 3,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/deliveryGoodMap"
			},
		]
	},
	loiCostMethods: {
		className: "LoiCostMethod",
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
	cDeliveries: {
		className: "CDelivery",
		displayAttr: "deyNumber",
		pageURL: "/cDeliveries",
		columns: [
			{
				accessor: "deyDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "deyNumber",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/cDeliveries"
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "tdtId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cTypeDocuments"
			},
			{
				accessor: "groundsNumber",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "groundsDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "costMethod",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiCostMethods"
			},
			{
				accessor: "paymentType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPaymentTypes"
			},
			{
				accessor: "datePayment",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "exchangeRate",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vat",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "sum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "oblSum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "danOsnova",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "endSum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "vatSum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "vatTo",
				fluidSize: 3,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={Math.round(100*100*props.original.vatSum/(100-props.original.discount))/100} disabled={true}/>
			},
			{
				accessor: "total",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "remarks",
				fluidSize: 6,
				dataType: "TEXTAREA"
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
				dataType: "TEXT"
			},
			{
				accessor: "dlyId",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "typeDoc",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiTypeDocs"
			},
			{
				accessor: "posted",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "cost",
				fluidSize: 3,
				dataType: "UNIT"
			},
		]
	},
	cDeliveryDetails: {
		className: "CDeliveryDetail",
		displayAttr: "serialNumber",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				"\u{1F194}"+item.id,
				(item.godId ? "\u{1F194}"+item.id+" "+item.godId.nameBg : ""),
				(item.godId ? item.deyId.deyNumber+" "+item.godId.nameBg : ""),
				item.serialNumber]
			,constraints);}),
		columns: [
			{
				accessor: "stocks",
				show: false,
				isReadOnly: true,
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks",
				mappedBy: "ddlId",
			},
			{
				accessor: "orlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cOrderDetails"
			},
			{
				accessor: "deyId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveries"
			},
			{
				accessor: "seeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cServices"
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
				width: 240
			},
			{
				accessor: "batch",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "serialNumber",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "expiry",
				fluidSize: 3,
				dataType: "DATE"
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
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "vat",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "costBase",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "cost",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "valueAll",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "valDisc",
				fluidSize: 3,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={props.original.valueAll*(100-props.original.discount)/100} disabled={true}/>
			},
			{
				accessor: "vatAll",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "total",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},

			{
				accessor: "valuePrice",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "ddlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveryDetails"
			},
			{
				accessor: "stkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks"
			},
			{
				accessor: "varId",
				fluidSize: 3,
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
	cBlocks: {
		className: "CBlock",
		displayAttr: "documentNumber",
		columns: [
			{
				accessor: "typeDoc",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "documentNumber",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "remark",
				fluidSize: 6,
				dataType: "TEXTAREA",
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
		]
	},
	cBlockedQuantities: {
		className: "CBlockedQuantity",
		displayAttr: "id",
		columns: [
			{
				accessor: "stkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cStocks"
			},
			{
				accessor: "blkId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cBlocks"
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "effectiveDate",
				fluidSize: 3,
				dataType: "DATE"
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	cSales: {
		className: "CSale",
		displayAttr: "documentNumber",
		pageURL: "/cSales",
		columns: [
			{
				accessor: "saleDate",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "documentNumber",
				fluidSize: 3,
				dataType: "UNIT",
				isLink: true,
				pageURL: "/cSales"
			},
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "outId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "paymentType",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiPaymentTypes"
			},
			{
				accessor: "datePayment",
				fluidSize: 3,
				dataType: "DATE",
			},
			{
				accessor: "idtCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "fCtInvDealTypes"
			},
			{
				accessor: "vat",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "sum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "oblSum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "danOsnova",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "endSum",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "vatSum",
				fluidSize: 3,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={props.original.vatto*(100-props.original.discount)/100} disabled={true}/>
			},
			{
				accessor: "total",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "exchangeRate",
				fluidSize: 3,
				dataType: "UNIT",
				isReadOnly: true,
			},
			{
				accessor: "remark",
				fluidSize: 6,
				dataType: "TEXTAREA",
			},

			{
				accessor: "placeDeals",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "tdtId",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "typeDoc",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "loiTypeDocs"
			},
			{
				accessor: "saeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cSales"
			},
			{
				accessor: "vatto",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "oldTypeDoc",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "ofrId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cOffers"
			},
			{
				accessor: "posted",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "advanceUsed",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "cost",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "totalPayed",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "payed",
				fluidSize: 3,
				dataType: "TEXT",
			},
		]
	},
	cSaleDetails: {
		className: "CSaleDetail",
		displayAttr: "id",
		columns: [
			{
				accessor: "stkId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cStocks",
				expandColumns: ["godId"],
				width: 240,
				doCalcOnChange: true
			},
			{
				accessor: "seeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cServices"
			},
			{
				accessor: "batch",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "serialNumber",
				fluidSize: 3,
				dataType: "TEXT",
			},
			{
				accessor: "meeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			},
			{
				accessor: "quantity",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "price",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "totalWhtVat",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "valDisc",
				fluidSize: 3,
				dataType: "UNIT",
				isCalculated: true,
				filterable: false,
				sortable: false,
				Cell: (props) => <input className="form-control" type="text" value={Math.round(100* props.original.totalWhtVat*(100-props.original.discount)/100 )/100} disabled={true}/>
			},
			{
				accessor: "vat",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "totalVat",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "total",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},

			{
				accessor: "priceVat",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "rateExchange",
				fluidSize: 3,
				dataType: "UNIT",
			},
			{
				accessor: "sdlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cSaleDetails"
			},
			{
				accessor: "saeId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cSales"
			},
			{
				accessor: "odlId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cOfferDetails"
			},
			{
				accessor: "cost",
				fluidSize: 3,
				dataType: "UNIT",
			},
		]
	},
	cDeliveryOffers: {
		className: "CDeliveryOffer",
		displayAttr: "offerNo",
		columns: [
			{
				accessor: "parId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cCcPartners"
			},
			{
				accessor: "offerNo",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "dateOffer",
				fluidSize: 3,
				dataType: "DATE"
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
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "remarks",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "outCode",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCcOrganizationUnits"
			}
		]
	},
	cDeliveryOfferItems: {
		className: "CDeliveryOfferItem",
		displayAttr: "name",
		columns: [
			{
				accessor: "dorId",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cDeliveryOffers"
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
				accessor: "price",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "currency",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cCtCurrencies"
			},
			{
				accessor: "discount",
				fluidSize: 3,
				dataType: "UNIT"
			},
			{
				accessor: "godId",
				fluidSize: 3,
				dataType: "FILTERED_ENTITY",
				entityType: "cGoodses",
			},
			{
				accessor: "status",
				fluidSize: 3,
				dataType: "TEXT"
			},
			{
				accessor: "measure",
				fluidSize: 3,
				dataType: "ENTITY",
				entityType: "cMeasures"
			}
		]
	},
	vendorInvoices: {
		className: "VendorInvoice",
		displayAttr: "invoiceNum",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
			(item.invoiceNum ? item.invoiceNum : "-"),
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD"),
			(item.invoiceNum ? item.invoiceNum : "-") + "/" + moment(item.invoiceDate).format("YYYYMMDD") + (item.invoiceCounterParty ? ("/" + item.invoiceCounterParty.name) : "")], constraints);}),
		icon: "file-invoice",
		pageURL: "/vendorInvoices",
		columns: [
					{
						accessor: 'id',
						fluidSize: 1,
						dataType: "UNIT",
						width: 50,
						isLink: true,
						pageURL: "/vendorInvoices",
						isReadOnly: true,
					}, {
						accessor: "invoiceDate",
						fluidSize: 3,
						dataType: "DATE",
					},{
						accessor: "invoiceNum",
						fluidSize: 2,
						dataType: "UNIT",
						isLink: true,
						pageURL: "/vendorInvoices",
					},{
						accessor: "invoiceCounterParty",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "cCcPartners",
					}, {
						accessor: "invoiceCurrency",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "cCtCurrencies",
					},
					{
						accessor: "invoiceExchangeRate",
						fluidSize: 2,
						dataType: "UNIT"
					},
					{
						accessor: "invoiceCode",
						fluidSize: 2,
						dataType: "TEXT",
					},{
						accessor: "invoiceDescription",
						fluidSize: 4,
						dataType: "TEXT",
					}, {
						accessor: "taxBaseAmount",
						fluidSize: 2,
						dataType: "UNIT",
						isReadOnly: true,
					}, {
						accessor: "taxAmount",
						fluidSize: 2,
						dataType: "UNIT",
						isReadOnly: true,
					}, {
						accessor: "totalAmount",
						fluidSize: 2,
						dataType: "UNIT",
						isReadOnly: true,
					}, {
						accessor: "isExported",
						fluidSize: 2,
						dataType: "BOOLEAN",
					}, {
						accessor: "invoiceRows",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "vendorInvoiceRows",
						mappedBy: "invoice",
					},
				]
	},
	vendorInvoiceRows: {
		className: "VendorInvoiceRow",
		displayAttr: "id",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
				item.id,
				(item.invoice ? item.id+"/"+item.invoice.invoiceNum : ""),
				(item.goods && item.invoice ? item.invoice.invoiceNum+" "+item.goods.nameBg : ""),
			], constraints);}),
		columns: [
					{
						accessor: "invoice",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "vendorInvoices",
					}, {
						accessor: "goods",
						fluidSize: 3,
						dataType: "FILTERED_ENTITY",
						entityType: "cGoodses",
					}, {
						accessor: "invoiceRowDescription",
						fluidSize: 3,
						dataType: "TEXT",
					}, {
						accessor: "quantity",
						fluidSize: 1,
						dataType: "UNIT",
					}, {
						accessor: "priceRate",
						fluidSize: 2,
						dataType: "UNIT",
					},
					{
						Header: "Разпределения",
						accessor: "allocationProxies",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "allocationProxies",
						mappedBy: "allocationOrigin",
					},
				]
	},
}

export default warehouseEntityDefinitions
