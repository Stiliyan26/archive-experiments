import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

import { ReactTableDefaults } from 'react-table-v6'

const trans = i18n.t;

let englishTrans = require('./static/locales/en/translation.json');
let bulgarianTrans = require('./static/locales/bg/translation.json');

i18n
	// detect user language
	// learn more: https://github.com/i18next/i18next-browser-languageDetector
	.use(LanguageDetector)
	// pass the i18n instance to react-i18next.
	.use(initReactI18next)
	// init i18next
	// for all options read: https://www.i18next.com/overview/configuration-options
	.init({
		debug: true,
		fallbackLng: 'en',
		interpolation: {
			escapeValue: false, // not needed for react as it escapes by default
		},
		resources: {
				"en": {
					"translation": englishTrans
				},
				"bg": {
					"translation": bulgarianTrans
			}
		}
	}, () => {
		// Altering the default props of react-table
		Object.assign(ReactTableDefaults, {
			defaultPageSize: 20,
			filterable: true,
			defaultFilterMethod: (filter, row, column) => {
					//console.log(filter, row, column);
					// const id = filter.pivotId || filter.id
					// return row[id] !== undefined ? String(row[id]).toLowerCase().indexOf(filter.value.toLowerCase()) != -1 : true
					const id = filter.pivotId || filter.id;
					return row[id] ? String(row[id]).toLowerCase().includes(filter.value.toLowerCase()) 
						: (row._subRows.find((subRow) => String(subRow[id]).toLowerCase().includes(filter.value.toLowerCase()) ) != -1 : false);
			},
			column: {
				...ReactTableDefaults.column,
				headerClassName: 'context-menu-blocker',
			},
			previousText: i18n.t("ReactTable.previousText"),
			nextText: i18n.t("ReactTable.nextText"),
			loadingText: i18n.t("ReactTable.loadingText"),
			noDataText: i18n.t("ReactTable.noDataText"),
			pageText: i18n.t("ReactTable.pageText"),
			ofText: i18n.t("ReactTable.ofText"),
			rowsText: i18n.t("ReactTable.rowsText"),
		})
	});

export default i18n;