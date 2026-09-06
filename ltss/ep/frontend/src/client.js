//import 'jquery'
import React from 'react';
import ReactDOM from 'react-dom';
import { AppContainer } from 'react-hot-loader';
import { Provider } from 'react-redux';
import { PersistGate } from 'redux-persist/lib/integration/react'
import axios from 'axios'

import configureStore from './stores';
import Routes from './routes'
//import moment from 'moment'

import './styles/prosidebar.scss'
import './styles/styles.css'
import './styles/navigation.css'
import './styles/footer.css'
import './styles/loader.css'
import './styles/datepicker.css'
import './styles/rc-switch.css'
import 'react-contexify/dist/ReactContexify.min.css'
import 'react-table/react-table.css'
//import 'react-datepicker/dist/react-datepicker.css';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'react-responsive-modal/styles.css';
import './i18n';

import { library } from '@fortawesome/fontawesome-svg-core'
import { 
	faAddressCard, faArrowAltCircleDown, faAsterisk, faBackward, faBan, faBars, faBook, faCalculator, faCaretSquareDown, faCaretSquareUp, 
	faCheck, faCheckSquare, faClock, faCode, faCog, 
	faComments, faCopy, faCube, faDownload, faEdit, faExchangeAlt, 
	faExclamationCircle, faEye, faFastForward, faFileExcel, faFileInvoice, faFolder, 
	faForward, faGasPump, faGraduationCap, faHandshake, faHistory, faLanguage, faLevelDownAlt, faLevelUpAlt, faList, faLock, 
	faMagic, faMap, faMoneyBill, faPaperclip, faPaste, 
	faPlus, faPoll, faPrint, faReceipt, faRoad, 
	faSave, faSearch, faSortAmountDownAlt, faSortAmountUp, faSpinner, faStar, faSync, 
	faTable, faTachometerAlt, faTasks, faThumbsDown, faThumbsUp, faTimes, faTrashAlt, faTruck, faTruckLoading, faUnlink, faUpload, faUser, faUserTie,
	faFileImport, faFilePdf, faFileUpload, faFileSignature, faMailBulk, faUserCheck, faIndustry
} from '@fortawesome/free-solid-svg-icons'


global.jQuery = require('jquery');
global.$ = jQuery;
//require('bootstrap-loader');

//font awesome
library.add(
	faAddressCard, faArrowAltCircleDown, faAsterisk, faBackward, faBan, faBars, faBook, faCalculator, faCaretSquareDown, faCaretSquareUp, 
	faCheck, faCheckSquare, faClock, faCode, faCog, 
	faComments, faCopy, faCube, faDownload, faEdit, faExchangeAlt, 
	faExclamationCircle, faEye, faFastForward, faFileExcel, faFileInvoice, faFolder, 
	faForward, faGasPump, faGraduationCap, faHandshake, faHistory, faLanguage, faLevelDownAlt, faLevelUpAlt, faList, faLock, 
	faMagic, faMap, faMoneyBill, faPaperclip, faPaste, 
	faPlus, faPoll, faPrint, faReceipt, faRoad, 
	faSave, faSearch, faSortAmountDownAlt, faSortAmountUp, faSpinner, faStar, faSync, 
	faTable, faTachometerAlt, faTasks, faThumbsDown, faThumbsUp, faTimes, faTrashAlt, faTruck, faTruckLoading, faUnlink, faUpload, faUser, faUserTie, 
	faFileImport, faFilePdf, faFileUpload, faFileSignature, faMailBulk, faUserCheck, faIndustry);

const store = configureStore({});
// App properties are stored in the build.properties file in the root dir
var appProperties = require('appProperties')
//moment.locale('bg')

// Attaching the csrf token to every call
// axios.defaults.baseURL = 'https://example.com';
axios.defaults.headers.common['X-CSRF'] = sessionStorage.getItem('csrf_token');

ReactDOM.render(

	<AppContainer>
		<Provider store={store.store} >
			<PersistGate loading={null} persistor={store.persistor}>
				<Routes/>
			</PersistGate>
		</Provider>
	</AppContainer>,
	document.getElementById('app')
);
