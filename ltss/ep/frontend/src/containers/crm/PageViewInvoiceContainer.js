import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedInvoiceContainer from '../embeds/EmbedInvoiceContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'

import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import { dispatchEditRESTData } from '../../actions/taskActions';
import { resolveObjectPath } from '../../scripts/dataUtils';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewInvoiceContainer extends React.Component {

	onChildUpdate(componentPath,value) {
		//refresh the page data
		this.props.actions.dispatchEditRESTData(this.props.componentPath+"."+this.props.entityDef.className,{});
	}
	
	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}
	}
	
	render() {
		let body = '';
		if(this.props.data) {
			if(this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
				body = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.entity_id,10)) {
				const invoiceRowsDef = getEntityDefinition("invoiceRows",{invoice: {show: false}});
				body = <div className='page-body'>
						<EmbedRetrieveEntityListContainer
							title={invoiceRowsDef.label}
							icon={invoiceRowsDef.icon}
							expanded={true}
							columns={invoiceRowsDef.columns}
							componentPath={this.props.componentPath + ".invoiceRows"}
							retrieveType="invoiceRows"
							parentHref={this.props.href}
							parentAttr="invoice"
							parentData={this.props.data}
							onCommitChange={()=>{this.onChildUpdate();}}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath+".changelog"} retrieveType={this.props.entityName}/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.headerText} class='page-header text-align-center no-margin' />
				<EmbedInvoiceContainer 
					retrieve_id={this.props.match.params.entity_id} 
					componentPath={this.props.componentPath+"."+this.props.entityDef.className}
					onBeforeChange={(data) => {
						if(!this.props.entityDef.pageURL) {console.error("pageURL is missing for entity "+this.props.entityDef.className);}
						if(data.id) {
							history.push(this.props.entityDef.pageURL+'/'+data.id);
						} else {
							history.push(this.props.entityDef.pageURL+'/add');
						}}}
					expanded={true}
				/>
				{body}
			</div>
			);
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	const entityName = "invoices";
	const entityDef = getEntityDefinition(entityName,undefined);
	const componentPath = "invoiceView";
	let viewData = resolveObjectPath(componentPath,state.rest);
	const data = viewData ? viewData[entityDef.className] : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		entityName: entityName,
		entityDef: entityDef,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		//UI
		headerText: ownProps.t("Invoice._className"),
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { dispatchEditRESTData }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewInvoiceContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
