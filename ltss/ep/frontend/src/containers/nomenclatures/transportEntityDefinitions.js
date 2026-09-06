import React from 'react';
import { Link } from 'react-router-dom'

import { resolveObjectPath, getMessageFromCode, extractColumnErrorMessage } from './../../scripts/dataUtils';
import { getLabelWithConstraints } from './entityDefinitions'

import FieldSelectOrEditContainer from '../fields/FieldSelectOrEditContainer'

const transportEntityDefinitions = {
	shippingContainerTypes: {
		className: "ShippingContainerType",
		label: "Тип контейнер",
		displayAttr: "code",
		columns: [
					{
						Header: "Код",
						accessor: "code",
						fluidSize: 2,
						dataType: "TEXT",
					},{
						Header: "Описание",
						accessor: "description",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	loiVehicleTypes: {
		className: "LoiVehicleType",
		label: "Опции Тип транспортно средство",
		displayAttr: "listOptionItemName",
		columns: [
					{
						Header: "Код",
						accessor: "listOptionItemCode",
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: "listOptionItemName",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	vehicles: {
		className: "Vehicle",
		label: "Транспортни ср-ва",
		displayAttr: "licensePlate",
		icon: "truck",
		pageURL: "/vehicles",
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
				width: 50,
				Cell: (props) => {
					//because of the ID column, which is common
					const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
					return <Link to={"/vehicles/"+parentId}>{props.value}</Link>;
				}
			},{
				Header: "Регистрационен номер",
				accessor: "licensePlate",
				fluidSize: 2,
				dataType: "TEXT",
				isLink: true,
				pageURL: "/vehicles",
				isRequired: true,
			},{
				Header: "Тип",
				accessor: "vehicleType",
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "loiVehicleTypes",
				isRequired: true,
			},{
				Header: "Наименование",
				accessor: 'assetName',
				fluidSize: 2,
				dataType: "TEXT",
				isRequired: true,
			},{
				Header: "Марка и модел",
				fluidSize: 3,
				accessor: "modelName",
				dataType: "TEXT",
			},{
				Header: "Изминати КМ при закупуване",
				accessor: "purchaseOdometer",
				fluidSize: 2,
				dataType: "UNIT",
			},{
				Header: "Височина на ремарке",
				accessor: "trailerHeight",
				fluidSize: 1,
				dataType: "UNIT",
			},{
				Header: "Евро стандарт",
				accessor: "euroStandard",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Дата на закупуване",
				accessor: "purchaseDate",
				fluidSize: 2,
				dataType: "DATE",
			},{
				Header: "Дата на производство",
				accessor: "manufactureDate",
				fluidSize: 2,
				dataType: "DATE",
			},{
				Header: "Собственост",
				accessor: "ownerName",
				fluidSize: 3,
				dataType: "TEXT",
			},{
				Header: "Кубатура и мощност",
				accessor: "enginePowerAndCc",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Вместимост на горивото",
				accessor: "tankLiters",
				fluidSize: 1,
				dataType: "TEXT",
			},{
				Header: "Гориво",
				accessor: "fuelType",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Номер на двигател",
				accessor: "engineNo",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Шаси",
				accessor: "chassisNo",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Масло",
				accessor: "oil",
				fluidSize: 2,
				dataType: "TEXT",
			},{
				Header: "Антифриз",
				accessor: "antifreeze",
				fluidSize: 2,
				dataType: "TEXT",
			}, {
				Header: "Прикачени документи към актива",
				accessor: 'assetAttachments',
				show: false,
				fluidSize: 2,
				dataType: "ENTITY",
				entityType: "assetAttachments",
				mappedBy: "asset",
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
			{
				Header: "Курсове на влекача",
				accessor: "transportsForTractors",
				show: false,
				fluidSize: 2,
				dataType: "FILTERED_ENTITY",
				entityType: "transports",
				mappedBy: "tractorUnit",
			},
		]
	},
	vehicleFuelReports: {
		className: "VehicleFuelReport",
		label: "Гориво",
		displayAttr: "odometer",
		icon: "gas-pump",
		pageURL: "/vehicleFuelReports",
		columns: [{
				Header: 'Код',
				accessor: 'id',
				fluidSize: 1,
				dataType: "UNIT",
				width: 50,
				Cell: (props) => {
					//because of the ID column, which is common
					const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
					return <Link to={"/vehicleFuelReports/"+parentId}>{props.value}</Link>;
				}
			}, {
				Header: "Километраж",
				accessor: "odometer",
				fluidSize: 2,
				dataType: "UNIT",
			},{
				Header: "Резервоар",
				accessor: "fuelInTank",
				fluidSize: 2,
				dataType: "UNIT",
			},{
				Header: "Дата",
				accessor: 'issueDate',
				fluidSize: 2,
				dataType: "DATE",
			},{
				Header: "Превозно ср-во",
				accessor: "vehicle",
				dataType: "ENTITY",
				entityType: "vehicles",
			},
		]
	},
	loiTransportTypes: {
		className: "LoiTransportType",
		label: "Опции Тип транспорт",
		displayAttr: "listOptionItemName",
		columns: [
					{
						Header: "Код",
						accessor: "listOptionItemCode",
						fluidSize: 2,
						dataType: "UNIT",
					},{
						Header: "Наименование",
						accessor: "listOptionItemName",
						fluidSize: 2,
						dataType: "TEXT",
					}
				]
	},
	transportOrders: {
		className: "TransportOrder",
		label: "Поръчка",
		icon: "handshake",
		pageURL: "/transportOrders",
		displayAttr: "id",
		columns: [
					{
						Header: 'Код',
						accessor: 'id',
						fluidSize: 1,
						dataType: "UNIT",
						width: 50,
						Cell: (props) => {
							//because of the ID column, which is common
							const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
							return <Link to={"/transportOrders/"+parentId}>{props.value}</Link>;
						}
					}, {
						Header: "Превозвач",
						accessor: "transporter",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "legalPersons",
						isRequired: true,
					},{
						Header: "Заявител",
						accessor: "orderOrderer",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "legalPersons",
						isRequired: true,
					},{
						Header: "Изпращач",
						accessor: "sender",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "legalPersons",
					},{
						Header: "Лице и телефон за контакт на изпращача",
						accessor: "senderContact",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Маршрут",
						accessor: 'orderRoute',
						fluidSize: 3,
						dataType: "TEXT",
						Cell: (props) => <FieldSelectOrEditContainer
							componentPath={props.original._componentPath+'.'+props.column._fieldRelPath} //existing path in redux store where we put data
							listReportEntity={"TransportOrder"}
							listReportAttr={"orderRoute"}
							editable={props.original._editable}
							onChange={(href) => {}}
						/>,
						isRequired: true,
					},{
						Header: "Дата",
						accessor: "orderDate",
						fluidSize: 3,
						dataType: "DATE",
					},{
						Header: "Получател",
						accessor: "receiver",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Лице и телефон за контакт на получателя",
						accessor: "receiverContact",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Товар",
						accessor: "cargo",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Описание на съдържанието",
						accessor: "orderContents",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Тегло бруто",
						accessor: "weight",
						fluidSize: 3,
						dataType: "UNIT",
					},{
						Header: "Референтен номер в склада на товарене",
						accessor: "loadingWarehouseRef",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Товарен пункт",
						accessor: "loadingPoint",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Дата и час на товарене",
						accessor: "orderLoadingDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Обмитяваща митница износ",
						accessor: "exportCustoms",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Митнически агент (име, телефон)",
						accessor: "exportCustomsAgent",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Разтоварен пункт",
						accessor: "unloadingPoint",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Дата и час на разтоварване",
						accessor: "orderUnloadingDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Обмитяваща митница внос",
						accessor: "importCustoms",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Митнически агент (име, телефон)",
						accessor: "importCustomsAgent",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Договорено навло",
						accessor: "orderPaymentAmount",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Валута",
						accessor: "orderPaymentCurrency",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "currencies",
					},{
						Header: "Платец на навлото",
						accessor: "payer",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Начин и срок на плащане",
						accessor: "paymentDetails",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Дата и място на връщане на празния контейнер",
						accessor: "emptiesReturnPoint",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Бележки",
						accessor: "notes",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Контейнери",
						accessor: "shippingContainers",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "transportOrderShippingContainers",
						mappedBy: "transportOrder",
					},{
						Header: "Статус на задачата",
						accessor: "status",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskStatuses",
						editableColumn: true,
//					}, {
//						Header: "Тип на задачата",
//						accessor: "type",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskTypes",
//						editableColumn: true,
//					}, {
//						Header: "Приоритет на задачата",
//						accessor: "priority",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskPriorities",
//						editableColumn: true,
					}, {
						Header: "Заглавие на задачата",
						accessor: 'title',
						show: false,
						fluidSize: 4,
						dataType: "TEXT",
						isLink: true,
						pageURL: "/tasks",
					}, {
						Header: "Описание на задачата",
						accessor: 'description',
						show: false,
						fluidSize: 12,
						dataType: "TEXTAREA",
					}, {
						Header: "Контрагент на задачата",
						accessor: "counterParty",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "legalPersons",
						editableColumn: true,
					}, {
						Header: "Изпълнител на задачата",
						accessor: "assigned",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUsers",
						editableColumn: true,
					}, {
						Header: "Краен срок на задачата",
						accessor: 'deadline',
						show: false,
						fluidSize: 2,
						dataType: "DATE",
						editableColumn: true,
					}, {
						Header: "Коментари на задачата",
						accessor: 'comments',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "comments",
						mappedBy: "task",
					}, {
						Header: "Прикачени документи",
						accessor: 'taskAttachments',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskAttachments",
						mappedBy: "task",
					}, {
						Header: "Надзадачи",
						accessor: "relationsToTask",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskRelations",
						mappedBy: "fromTask",
					}, {
						Header: "Подзадачи",
						accessor: "relationsFromTask",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskRelations",
						mappedBy: "toTask",
					}
				]
	},
	transportOrderShippingContainers: {
		className: "TransportOrderShippingContainer",
		label: "Контейнери на поръчката",
		columns: [
					{
						Header: "Поръчка",
						accessor: "transportOrder",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "transportOrders",
					},{
						Header: "Номер",
						accessor: "number",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Тип",
						accessor: "type",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "shippingContainerTypes",
					}
				]
	},
	transports: {
		className: "Transport",
		label: "Курс",
		icon: "road",
		pageURL: "/transports",
		displayAttr: "id",
		displayFn: ((item, constraints) => {return getLabelWithConstraints([
						"\u{1F194}"+String(item.id), 
						item.tractorUnit ? item.id + "/" + item.tractorUnit.licensePlate : "", 
						item.id + (item.tractorUnit ? ("/" + item.tractorUnit.licensePlate) : "") + "/" + item.route], constraints);}),
		columns: [
					{
						Header: 'Код',
						accessor: 'id',
						fluidSize: 1,
						dataType: "UNIT",
						width: 50,
						Cell: (props) => {
								//because of the ID column, which is common
								const parentId = resolveObjectPath(props.column._parentPath+".id",props.original);
								return <Link to={"/transports/"+parentId}>{props.value}</Link>;
							}
					},{
						Header: "Заявител",
						accessor: "orderer",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "legalPersons",
					},{
						Header: "Статус на курса",
						accessor: "status",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskStatuses",
						editableColumn: true,
					},{
						Header: "Влекач",
						accessor: "tractorUnit",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "vehicles",
						optionFilter: [{where: {
								op: "equal",
								operands: ["vehicleType.listOptionItemCode", {literal: 1}],
							},
						}],
						expandColumns: "vehicleType",
						isRequired: true,
					},{
						Header: "Ремарке",
						accessor: "trailer",
						fluidSize: 2,
						dataType: "FILTERED_ENTITY",
						entityType: "vehicles",
						optionFilter: [{where: {
								op: "equal",
								operands: ["vehicleType.listOptionItemCode", {literal: 2}],
							},
						}],
						expandColumns: "vehicleType",
						isRequired: true,
					},{
						Header: "Шофьор",
						accessor: "driver",
						fluidSize: 3,
						dataType: "FILTERED_ENTITY",
						entityType: "employees",
						isRequired: true,
					},{
						Header: "Платено",
						accessor: 'isPaid',
						fluidSize: 1,
						dataType: "BOOLEAN",
					},{
						Header: "Договорено навло",
						accessor: "paymentAmount",
						fluidSize: 2,
						dataType: "UNIT",
						isRequired: true,
					},{
						Header: "Валута",
						accessor: "paymentCurrency",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "currencies",
						isRequired: true,
					},{
						Header: "Разстояние (Км)",
						accessor: "distanceKm",
						fluidSize: 2,
						dataType: "UNIT",
						isRequired: true,
					},{
						Header: "Контейнер тип",
						accessor: "containerType",
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "shippingContainerTypes",
					},{
						Header: "Контейнер номер",
						accessor: "containerNumber",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Вид превоз",
						accessor: "transportType",
						fluidSize: 3,
						dataType: "ENTITY",
						entityType: "loiTransportTypes",
						isRequired: true,
					},{
						Header: "Маршрут",
						accessor: "route",
						fluidSize: 3,
						dataType: "TEXTAREA",
					},{
						Header: "Дата и час на товарене",
						accessor: "loadingDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Дата и час на разтоварване",
						accessor: "unloadingDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Изпратено на",
						accessor: "sendDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Получено на",
						accessor: "receiveDate",
						fluidSize: 3,
						dataType: "TIMESTAMP",
					},{
						Header: "Падеж",
						accessor: "paymentDate",
						fluidSize: 3,
						dataType: "DATE",
					},{
						Header: "Обратна разписка",
						accessor: "confirmation",
						fluidSize: 3,
						dataType: "TEXT",
					},{
						Header: "Описание на съдържанието",
						accessor: "contents",
						fluidSize: 6,
						dataType: "TEXTAREA",
//					}, {
//						Header: "Тип на задачата",
//						accessor: "type",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskTypes",
//						editableColumn: true,
//					}, {
//						Header: "Приоритет на задачата",
//						accessor: "priority",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "taskPriorities",
//						editableColumn: true,
//					}, {
//						Header: "Заглавие на задачата",
//						accessor: 'title',
//						show: false,
//						fluidSize: 4,
//						dataType: "TEXT",
//						isLink: true,
//						pageURL: "/tasks",
					}, {
						Header: "Описание на задачата",
						accessor: 'description',
						show: false,
						fluidSize: 12,
						dataType: "TEXTAREA",
					}, {
						Header: "Контрагент на задачата",
						accessor: "counterParty",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "legalPersons",
						editableColumn: true,
					}, {
						Header: "Изпълнител на задачата",
						accessor: "assigned",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "secUsers",
						editableColumn: true,
					}, {
						Header: "Краен срок на задачата",
						accessor: 'deadline',
						show: false,
						fluidSize: 2,
						dataType: "DATE",
						editableColumn: true,
					}, {
						Header: "Коментари на задачата",
						accessor: 'comments',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "comments",
						mappedBy: "task",
					}, {
						Header: "Прикачени документи",
						accessor: 'taskAttachments',
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskAttachments",
						mappedBy: "task",
					}, {
						Header: "Надзадачи",
						accessor: "relationsToTask",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskRelations",
						mappedBy: "fromTask",
					}, {
						Header: "Подзадачи",
						accessor: "relationsFromTask",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "taskRelations",
						mappedBy: "toTask",
					}, {
						Header: "Фактури",
						accessor: "invoiceRows",
						show: false,
						fluidSize: 2,
						dataType: "ENTITY",
						entityType: "invoiceRows",
						mappedBy: "transport",
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

export default transportEntityDefinitions