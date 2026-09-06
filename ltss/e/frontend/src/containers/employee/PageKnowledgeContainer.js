import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import classNames from 'classnames'

import history from '../../scripts/history'
import { isAuthenticated } from '../../components/pages/login/Login.js'

import Header from '../../components/generic/Header'
import Select from '../OverflowSelect'

import {getEntityDefinition} from '../nomenclatures/entityDefinitions.js'
import { resetRESTCallLimit, fetchRESTFollow } from '../../actions/taskActions';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer'

//Page: can be used as a landing page
//Container: redux container class
class PageKnowledgeContainer extends React.Component {

	constructor(props){
		super(props);
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		}else{
			this.fetchDictionaries()
		}
	}

	fetchDictionaries(){
		this.props.actions.fetchRESTFollow(
			{
				url: API_URL+"/reports/builder/1?from=Dictionary&select=Dictionary&page=0&size=50000"
			},
			'dictionary',
			response => ( response.data ),
			'PageKnowledgeContainer.fetchDictionaries',
		);
	}
	render() {
		const dictionariesOptions = [];
		const { dictionaries } = this.props;
		let selectedDictionaryId = this.props.match.params.selectedDictionaryId
		if(dictionaries && dictionaries._embedded && dictionaries._embedded.hashMaps){
			let dictionariesList = dictionaries._embedded.hashMaps.map((dictionary) => dictionary.Dictionary)
			for(let dictionary of dictionariesList) {
				if(dictionary.name){
					dictionariesOptions.push({label: dictionary.name, value: dictionary});
				}
			}
		}
		let selectedDictionaryOption = dictionariesOptions.find((dictOption) => {return dictOption.value.id == selectedDictionaryId})

		const dictionaryDefinition = getEntityDefinition('dictionaryTerms');
		const dictionaryClassificationPolicyDefinition = getEntityDefinition('dictionaryClassificationPolicies');
		const tableClasses = classNames({
			'clients-table align-center-table -highlight': true,
			'hidden': !dictionaryDefinition
		})
		return (
			<div className="nomenclatures-wrapper">
				<Header text={this.props.t("Knowledge.title")} class='page-header text-align-center no-margin' />
				<div className='page-body'>
					<div className="col-sm-12 form-group">
						<Select
							className="col-sm-12 no-padding"
							name="form-field-name"
							options={dictionariesOptions}
							onChange={(e) => {
								resetRESTCallLimit();
								history.push('/knowledge/'+e.value.id);
							}}
							value={selectedDictionaryOption}
							placeholder={this.props.t("Knowledge.SelectDictionary")}
						/>
					</div>
					<div className='col-sm-12'>
						<EmbedRetrieveEntityListContainer
							key={selectedDictionaryId}
							title={dictionaryDefinition.label}
							icon={dictionaryDefinition.icon}
							columns={dictionaryDefinition.columns}
							componentPath={`dictionaryTerms.${selectedDictionaryId}`}
							retrieveType={"dictionaryTerms"}
							defaultFilter={[{id: 'dictionary.id', value: selectedDictionaryId}]}
							expanded={true}
							asTable={true}
							onAddData={(newItem) => newItem.dictionary = `${API_URL}/dictionaries/${selectedDictionaryId}`}
						/>
						<br></br>
						<EmbedRetrieveEntityListContainer
							title={dictionaryClassificationPolicyDefinition.label}
							icon={dictionaryClassificationPolicyDefinition.icon}
							columns={dictionaryClassificationPolicyDefinition.columns}
							componentPath={`dictionaryClassificationPolicies`}
							retrieveType={"dictionaryClassificationPolicies"}
							expanded={false}
							asTable={true}
							// onAddData={(newItem) => newItem.dictionary = `${API_URL}/dictionaries/${selectedDictionaryId}`}
						/>
					</div>
				</div>
			</div>
			)
	}
}

//redux mapping
function mapStateToProps(state,ownProps) {
	return {
		auth: state.auth,
		dictionaries: state.rest.dictionary,
		dictionaryTerms: state.rest.dictionaryTerms
	};
}

//redux mapping
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageKnowledgeContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
