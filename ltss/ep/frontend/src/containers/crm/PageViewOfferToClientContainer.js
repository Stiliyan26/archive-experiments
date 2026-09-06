import React from 'react';
import Modal from 'react-responsive-modal';
import Select from 'react-select'

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { ButtonGroup, Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants';
import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import EmbedOfferToClientContainer from '../embeds/EmbedOfferToClientContainer'
import EmbedChangeHistory from '../embeds/EmbedChangeHistory'

import { resetRESTCallLimit } from '../../actions/taskActions';
import { getEntityDefinition } from '../nomenclatures/entityDefinitions.js'
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//View: presents details of object
//Container: redux container class
class PageViewOfferToClientContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			isModalSelectArticlesOpen: false,
			selectedArticleType: {},
			needUpdate: 0,
		};
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
			} else if(this.props.id && this.props.id == parseInt(this.props.match.params.offerToClients_id,10)) {
				const offerLinesDef = getEntityDefinition("offerLines",{offerToClient: {show: false}});
				const importedOrdersDef = getEntityDefinition("importedOrders",{offer: {show: false}, compId: {show: false}, updateCountAsBigInt: {show: false}});
				const articleProductsDef = getEntityDefinition("articleProducts",{});
				const articleServicesDef = getEntityDefinition("articleServices",{});
				const currenciesDef = getEntityDefinition("currencies",{});
				let selectedArticles = Array.from(this.props._articleProducts && this.props._articleProducts.selectedRows && this.props._articleProducts.selectedRows.map ? this.props._articleProducts.selectedRows.map : [])
						.concat(Array.from(this.props._articleServices && this.props._articleServices.selectedRows && this.props._articleServices.selectedRows.map ? this.props._articleServices.selectedRows.map : []))
						.concat(Array.from(this.props._currencies && this.props._currencies.selectedRows && this.props._currencies.selectedRows.map ? this.props._currencies.selectedRows.map : []));
				body = <div className='page-body'>
						<div key="actionButtons" style={{display: 'inline-block'}}>
							<Button onClick={(e) => {
									e.stopPropagation();
									this.setState({ isModalSelectArticlesOpen: true });
								}}>
								<FontAwesomeIcon icon="magic"/>&nbsp;
								{this.props.t("OfferToClient.AddArticle")}
							</Button>
							<Modal open={this.state.isModalSelectArticlesOpen} onClose={() => {this.setState({ isModalSelectArticlesOpen: false });}}>
								<ButtonGroup>
									<Button className="col-sm-12" variant={(selectedArticles.length > 0 ? "success" : "warning")} disabled={(selectedArticles.length > 0)}
										onClick={selectedArticles.length > 0 ? () => {
											resetRESTCallLimit();
											selectedArticles.forEach((elem) => {
												this.offerLinesContainer.addData({
													article: elem[1],
													_links: {article: {href: elem[1]._links.self.href}},
												});
											});
											this.setState({ isModalSelectArticlesOpen: false });
										} : undefined}
									>
										<FontAwesomeIcon icon="plus"/>
										&nbsp;{this.props.t("OfferToClient.AddRowsForNArticles", {rows: selectedArticles.length})}
									</Button>
								</ButtonGroup>
								<label className="contracts-add-form-label">{this.props.t("OfferToClient.ArticleType")}</label>
								<Select
									value={this.state.selectedArticleType}
									options={[{value: 1, label: this.props.t("ArticleProduct._className_plural")},{value: 2, label: this.props.t("ArticleService._className_plural")},{value: 3, label: this.props.t("Currency._className_plural")}]}
									onChange={(e) => {
										this.setState({ selectedArticleType: e });
									}}
									placeholder={this.props.t("Choose...")}
								/>
								{this.state.selectedArticleType.value == 1 ?
									<EmbedRetrieveEntityListContainer
										title={articleProductsDef.label}
										icon={articleProductsDef.icon}
										columns={articleProductsDef.columns}
										retrieveType = "articleProducts"
										componentPath = {this.props.componentPath + "._articleProducts"} //existing path in redux store where we put data
										hasRowSelecting = {true}
										expanded={true}
										asTable={true}
										creatable={false}
										editable={false}
										isMultiSelect = {true}
										afterAddSelectedRowsF={(selectedRows) => {this.setState({needUpdate: this.state.needUpdate + 1 });}}
									/> : ''}
								{this.state.selectedArticleType.value == 2 ?
									<EmbedRetrieveEntityListContainer
										title={articleServicesDef.label}
										icon={articleServicesDef.icon}
										columns={articleServicesDef.columns}
										retrieveType = "articleServices"
										componentPath = {this.props.componentPath + "._articleServices"} //existing path in redux store where we put data
										hasRowSelecting = {true}
										expanded={true}
										asTable={true}
										creatable={false}
										editable={false}
										isMultiSelect = {true}
										afterAddSelectedRowsF={(selectedRows) => {this.setState({needUpdate: this.state.needUpdate + 1 });}}
									/> : ''}
								{this.state.selectedArticleType.value == 3 ?
									<EmbedRetrieveEntityListContainer
										title={currenciesDef.label}
										icon={currenciesDef.icon}
										columns={currenciesDef.columns}
										retrieveType = "currencies"
										componentPath = {this.props.componentPath + "._currencies"} //existing path in redux store where we put data
										hasRowSelecting = {true}
										expanded={true}
										asTable={true}
										creatable={false}
										editable={false}
										isMultiSelect = {true}
										afterAddSelectedRowsF={(selectedRows) => {this.setState({needUpdate: this.state.needUpdate + 1 });}}
									/> : ''}
							</Modal>
						</div>
						<EmbedRetrieveEntityListContainer
							ref={instance => {
								if(instance) {
									this.offerLinesContainer = instance.getWrappedInstance();
								} else {
									this.offerLinesContainer = undefined;
								}
							}}
							title={offerLinesDef.label}
							icon={offerLinesDef.icon}
							columns={offerLinesDef.columns}
							componentPath={this.props.componentPath + ".offerLines"}
							retrieveType="offerLines"
							parentHref={this.props.href}
							parentAttr="offerToClient"
							parentData={this.props.data}
							expanded={true}
							asTable={true}
						/>
						<EmbedRetrieveEntityListContainer
							title={importedOrdersDef.label}
							icon={importedOrdersDef.icon}
							columns={importedOrdersDef.columns}
							componentPath={this.props.componentPath + ".importedOrders"}
							retrieveType="importedOrders"
							parentHref={this.props.href}
							parentAttr="offer"
							parentData={this.props.data}
							expanded={true}
							asTable={true}
							creatable={false}
							editable={false}
						/>
						<EmbedChangeHistory entity_id={this.props.id} componentPath={this.props.componentPath + ".changelog"} retrieveType="offerToClients"/>
					</div>;
			}
		}
		return (
			<div className="page-body-wrapper">
				<Header text={this.props.t("OfferToClient._className")} class='page-header text-align-center no-margin' />
				<EmbedOfferToClientContainer
						retrieve_id={this.props.match.params.offerToClients_id}
						componentPath={this.props.componentPath + ".offerToClient"}
						onChange={(data) => {
							if(data.id) {
								history.push('/offerToClients/'+data.id);
							} else {
								history.push('/offerToClients/add');
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
	let componentPath =  "offerToClientView";
	const data = state.rest[componentPath] ? state.rest[componentPath].offerToClient : undefined;
	return {
		auth: state.auth,
		componentPath: componentPath,
		data: data,
		href: data && data._links && data._links.self ? data._links.self.href : undefined,
		id: data && data.id ? data.id : undefined,
		_articleProducts: state.rest[componentPath] ? state.rest[componentPath]._articleProducts : undefined,
		_articleServices: state.rest[componentPath] ? state.rest[componentPath]._articleServices : undefined,
		_currencies: state.rest[componentPath] ? state.rest[componentPath]._currencies : undefined,
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { }), dispatch)
	};
}

const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewOfferToClientContainer);
const WithRouterComponent = withRouter(ConnectComponent);
//export class wrapped in redux and in router
export default withTranslation()(WithRouterComponent);
