import React from 'react';

const watoEntityDefinitions = {
//	importedArticles: {
//		className: "ImportedArticle",
//		label: "Артикули импортирани от счетоводството",
//		displayAttr: "name",
//		columns: [
//					{
//						Header: "Артикул",
//						accessor: "article",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "articles",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Номер",
//						accessor: "number",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Тип артикул",
//						accessor: "articleType",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "predNom",
//						accessor: "predNom",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "nomNom",
//						accessor: 'nomNom',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Име",
//						accessor: 'name',
//						fluidSize: 2,
//						dataType: "TEXT",
//					},{
//						Header: "Идентификатор на мярка",
//						accessor: 'measureForeignId',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Мярка",
//						accessor: 'measure',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Мярка (съкр.)",
//						accessor: 'measureShort',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Продажна цена",
//						accessor: 'sellPrice',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Валута",
//						accessor: 'currency',
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрит",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedWarehouseStocks: {
//		className: "ImportedWarehouseStock",
//		label: "Складови наличности импортирани от счетоводството",
//		displayAttr: "totalInCompany",
//		columns: [
//					{
//						Header: "Артикул",
//						accessor: "article",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedArticles",
//					}, {
//						Header: "Идентификатор на артикула",
//						accessor: "foreignArticleId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на склада",
//						accessor: "warehouseId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Склад",
//						accessor: "warehouseName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Количество",
//						accessor: "quantity",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Запазено кол-во",
//						accessor: "quantityReserve",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Общо по складове",
//						accessor: "totalInCompany",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Общо запазено",
//						accessor: "reserveInCompany",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрит",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedLegalPersons: {
//		className: "ImportedLegalPerson",
//		label: "Контрагенти импортирани от счетоводството",
//		displayAttr: "companyName",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Контрагент",
//						accessor: "legalPerson",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "legalPersons",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "clName",
//						accessor: "clName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "БУЛСТАТ",
//						accessor: "bulstat",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Име",
//						accessor: "companyName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "ДДС номер",
//						accessor: "vatNumber",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "email",
//						accessor: "email",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Тел.",
//						accessor: "phoneNumber",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Доставчик",
//						accessor: 'supplier',
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Клиент",
//						accessor: 'client',
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Производител",
//						accessor: 'manufacturer',
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Подизпълнител",
//						accessor: 'subcontractor',
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "МОЛ",
//						accessor: "molName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Държава",
//						accessor: "regCountry",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Град",
//						accessor: "regCity",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Адрес",
//						accessor: "regAddress",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Държава за кореспонденция",
//						accessor: "currCountry",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Град за кореспонденция",
//						accessor: "currCity",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Адрес за кореспонденция",
//						accessor: "currAddress",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Код на компания",
//						accessor: 'compId',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрит",
//						accessor: 'foreignDeleted',
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedLegalPersonGroups: {
//		className: "ImportedLegalPersonGroup",
//		label: "Групи на контрагенти импортирани от счетоводството",
//		displayAttr: "companyName",
//		aclRestrictable: true,
//		columns: [
//					{
//						Header: "Контрагент",
//						accessor: "legalPerson",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "legalPersons",
//					}, {
//						Header: "Разпределен търговец",
//						accessor: "assignedSales",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "secUsers",
//					}, {
//						Header: "Направление",
//						accessor: "direction",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "directionCategories",
//					}, {
//						Header: "Район",
//						accessor: "area",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "areaCategories",
//					}, {
//						Header: "Вид дейност",
//						accessor: "business",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "businessCategories",
//					}, {
//						Header: "Група 5",
//						accessor: "generalCategory",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "generalCategories",
//					}, {
//						Header: "foreignId",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на контрагент",
//						accessor: "contragentID",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "mainGroupID",
//						accessor: "mainGroupID",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "groupID",
//						accessor: "groupID",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "mainGroupCode",
//						accessor: "mainGroupCode",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "mainGroupName",
//						accessor: "mainGroupName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "groupCode",
//						accessor: "groupCode",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "groupName",
//						accessor: "groupName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Код на компания",
//						accessor: 'compId',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedOrders: {
//		className: "ImportedOrder",
//		label: "Поръчки (импортирани)",
//		displayAttr: "docNum",
//		columns: [
//					{
//						Header: "Оферта",
//						accessor: "offer",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "offerToClients",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Номер",
//						accessor: "docNum",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Дата",
//						accessor: "docDate",
//						fluidSize: 2,
//						dataType: "DATE",
//					}, {
//						Header: "Сума",
//						accessor: 'total',
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Редове на поръчката",
//						accessor: "importedOrderRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrderRows",
//						mappedBy: "order",
//					}, {
//						Header: "Експ. листи на поръчката",
//						accessor: "importedExpeditionLists",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedExpeditionLists",
//						mappedBy: "order",
//					}, {
//						Header: "Фактури на поръчката",
//						accessor: "importedInvoices",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoices",
//						mappedBy: "order",
//					}, {
//						Header: "Фактурирана сума",
//						accessor: "sumInvoicedAmount",
//						isAggregate: true,
//						filterable: true,
//						sortable: false,
//						editable: false, //TODO make this work - the aggregates should not be editable
//						aggregation: {
//							op: "sum",
//							operands: ["importedInvoices.importedInvoicePayments.totalAmount"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Получена сума",
//						accessor: "sumInvoicePayments",
//						isAggregate: true,
//						filterable: true,
//						sortable: false,
//						aggregation: {
//							op: "sum",
//							operands: ["importedInvoices.importedInvoicePayments.totalPayed"]
//						},
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedOrderRows: {
//		className: "ImportedOrderRow",
//		label: "Редове на поръчки (импортирани)",
//		displayAttr: "foreignArticleId",
//		columns: [
//					{
//						Header: "Поръчка",
//						accessor: "order",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrders",
//					}, {
//						Header: "Импортиран артикул",
//						accessor: "article",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedArticles",
//					}, {
//						Header: "Ред на оферта",
//						accessor: "offerLine",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "offerLines",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на поръчката",
//						accessor: "foreignOrderId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на артикула",
//						accessor: "foreignArticleId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Редове на експ. листи",
//						accessor: "importedExpeditionListRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedExpeditionListRows",
//						mappedBy: "orderRow",
//					}, {
//						Header: "Редове на фактури",
//						accessor: "importedInvoiceRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoiceRows",
//						mappedBy: "orderRow",
//					}
//				]
//	},
//	importedExpeditionLists: {
//		className: "ImportedExpeditionList",
//		label: "Експедиционни листи (импортирани)",
//		displayAttr: "docNum",
//		columns: [
//					{
//						Header: "Поръчка",
//						accessor: "order",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrders",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на поръчка",
//						accessor: "foreignOrderId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Номер",
//						accessor: "docNum",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Дата",
//						accessor: "docDate",
//						fluidSize: 2,
//						dataType: "DATE",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Редове на експ. лист",
//						accessor: "importedExpeditionListRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedExpeditionListRows",
//						mappedBy: "expeditionList",
//					}
//				]
//	},
//	importedExpeditionListRows: {
//		className: "ImportedExpeditionListRow",
//		label: "Редове на експедиционни листи (импортирани)",
//		displayAttr: "foreignId",
//		columns: [
//					{
//						Header: "Експедиционен лист",
//						accessor: "expeditionList",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedExpeditionLists",
//					}, {
//						Header: "Ред на поръчка",
//						accessor: "orderRow",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrderRows",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на реда на поръчката",
//						accessor: "foreignOrderRowId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на експедиционния лист",
//						accessor: "foreignExpeditionListId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Кол-во",
//						accessor: "quantity",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedInvoices: {
//		className: "ImportedInvoice",
//		label: "Фактури (импортирани)",
//		displayAttr: "docNum",
//		columns: [
//					{
//						Header: "Поръчка",
//						accessor: "order",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrders",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на поръчка",
//						accessor: "foreignOrderId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Номер",
//						accessor: "docNum",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Дата",
//						accessor: "docDate",
//						fluidSize: 2,
//						dataType: "DATE",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Редове на фактурата",
//						accessor: "importedInvoiceRows",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoiceRows",
//						mappedBy: "invoice",
//					}, {
//						Header: "Плащания на фактурата",
//						accessor: "importedInvoicePayments",
//						show: false,
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoicePayments",
//						mappedBy: "invoice",
//					}
//				]
//	},
//	importedInvoiceRows: {
//		className: "ImportedInvoiceRow",
//		label: "Редове на фактури (импортирани)",
//		displayAttr: "foreignId",
//		columns: [
//					{
//						Header: "Фактура",
//						accessor: "invoice",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoices",
//					}, {
//						Header: "Ред на поръчка",
//						accessor: "orderRow",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedOrderRows",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на реда на поръчката",
//						accessor: "foreignOrderRowId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на фактурата",
//						accessor: "foreignInvoiceId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Кол-во",
//						accessor: "quantity",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Номер на редакция",
//						accessor: "updateCountAsBigInt",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	importedInvoicePayments: {
//		className: "ImportedInvoicePayment",
//		label: "Плащания по фактури (импортирани)",
//		displayAttr: "docNum",
//		columns: [
//					{
//						Header: "Фактура",
//						accessor: "invoice",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedInvoices",
//					}, {
//						Header: "Импортиран контрагент",
//						accessor: "contragent",
//						fluidSize: 2,
//						dataType: "ENTITY",
//						entityType: "importedLegalPersons",
//					}, {
//						Header: "Идентификатор",
//						accessor: "foreignId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Идентификатор на контрагента",
//						accessor: "foreignContragentId",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Номер",
//						accessor: "docNum",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Дата",
//						accessor: "docDate",
//						fluidSize: 2,
//						dataType: "DATE",
//					}, {
//						Header: "Компания",
//						accessor: "companyName",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Поръчка",
//						accessor: "orderNumber",
//						fluidSize: 2,
//						dataType: "TEXT",
//					}, {
//						Header: "Сума",
//						accessor: "totalAmount",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Сума без ДДС",
//						accessor: "totalNoVAT",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Платено",
//						accessor: "totalPayed",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}, {
//						Header: "Изтрита",
//						accessor: "foreignDeleted",
//						fluidSize: 2,
//						dataType: "BOOLEAN",
//					}, {
//						Header: "Код на компания",
//						accessor: "compId",
//						fluidSize: 2,
//						dataType: "UNIT",
//					}
//				]
//	},
//	wastes: {
//		className: 'Waste',
//		label: 'Отпадъци',
//		icon: "trash-alt",
//		pageURL: "/wastes",
//		columns: [{
//				Header: 'Дата на образуване',
//				accessor: 'generatedDate',
//				dataType: 'MONTH'
//			}, {
//				Header: 'Образувано количество (тон)',
//				accessor: 'generatedAmount',
//				dataType: 'TEXT'
//			},{
//				Header: 'Дата на предаване',
//				accessor: 'processedDate',
//				dataType: 'MONTH'
//			}, {
//				Header: 'Разходен център',
//				accessor: 'costCenter',
//				dataType: "ENTITY",
//				entityType: "wasteCostCenters",
//			}, {
//				Header: 'Предадено количесто (тон)',
//				accessor: 'processedAmount',
//				dataType: 'TEXT'
//			}, {
//				Header: 'Тип отпадъци',
//				accessor: 'wasteType',
//				dataType: 'ENTITY',
//				entityType: 'wasteTypes'
//			}
//		]
//	},
//	wasteCostCenters: {
//		className: 'WasteCostCenter',
//		label: 'Разходни центрове',
//		icon: "trash-alt",
//		// pageURL: "/wasteCostCenters",
//		displayAttr: "name",
//		columns: [{
//				Header: 'Наименование',
//				accessor: 'name',
//				dataType: 'TEXT',
//			}, {
//				Header: 'ЕИК',
//				accessor: 'eik',
//				dataType: 'TEXT'
//			}, {
//				Header: 'Основание за притеждание',
//				accessor: 'possessionReason',
//				dataType: 'TEXT'
//			}
//		]
//	},
//	wasteTypes: {
//		className: 'WasteType',
//		label: 'Типове отпадъци',
//		icon: "trash-alt",
//		displayAttr: 'name',
//		columns: [{
//				Header: 'Код',
//				accessor: 'wasteCode',
//				dataType: 'TEXT'
//			},{
//				Header: 'Наименование',
//				accessor: 'name',
//				dataType: 'TEXT'
//			},{
//				Header: 'Вид',
//				accessor: 'wasteKind',
//				dataType: 'ENTITY',
//				entityType: 'wasteKinds'
//			},{
//				Header: 'Произход',
//				accessor: 'origin',
//				dataType: 'TEXT'
//			},
//		]
//	},
//	wasteKinds: {
//		className: 'WasteKind',
//		label: 'Видове отпадъци',
//		icon: "trash-alt",
//		displayAttr: 'name',
//		columns: [{
//			Header: 'Наименование',
//			accessor: 'name',
//			dataType: 'TEXT'
//		}]
//	},
//	wasteAreas: {
//		className: 'WasteArea',
//		label: 'Площадки',
//		icon: "trash-alt",
//		displayAttr: 'name',
//		columns: [{
//				Header: 'Наименование',
//				accessor: 'name',
//				dataType: 'TEXT'
//			},{
//				Header: 'Адрес',
//				accessor: 'address',
//				dataType: 'TEXT'
//			},{
//				Header: 'Телефон',
//				accessor: 'phone',
//				dataType: 'TEXT'
//			},{
//				Header: 'Лице за контакт',
//				accessor: 'contactPerson',
//				dataType: 'TEXT'
//			},{
//				Header: 'Имейл',
//				accessor: 'email',
//				dataType: 'TEXT'
//			},
//		]
//	},
//	dictionaries: {
//		className: 'Dictionary',
//		label: 'Речници',
//		displayAttr: 'name',
//		columns: [{
//				Header: 'Наименование',
//				accessor: 'name',
//				dataType: 'TEXT'
//			}
//		]
//	},
//	dictionaryTerms: {
//		className: 'DictionaryTerm',
//		label: 'Термини',
//		displayAttr: 'name',
//		columns: [{
//				Header: "Име",
//				accessor: "name",
//				fluidSize: 2,
//				dataType: "TEXT",
//			},{
//				Header: 'Значение',
//				accessor: 'meaning',
//				dataType: 'TEXT',
//			},{
//				Header: 'Речник',
//				accessor: 'dictionary',
//				dataType: 'ENTITY',
//				entityType: 'dictionaries',
//				show: false
//			},
//		]
//	},
//	dictionaryClassificationPolicies: {
//		className: 'DictionaryClassificationPolicy',
//		label: 'Правила за класификация',
//		displayAttr: 'name',
//		icon: 'book',
//		columns: [{
//				Header: "Име",
//				accessor: "name",
//				fluidSize: 2,
//				dataType: "TEXT",
//			},{
//				Header: 'Съдържание',
//				accessor: 'content',
//				dataType: 'TEXT',
//			},{
//				Header: 'Речник',
//				accessor: 'dictionary',
//				dataType: 'ENTITY',
//				entityType: 'dictionaries'
//			},
//		]
//	},
}

export default watoEntityDefinitions
