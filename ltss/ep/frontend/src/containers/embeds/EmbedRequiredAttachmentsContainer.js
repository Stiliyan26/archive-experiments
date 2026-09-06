import React from 'react';
import url from 'url'
import { Accordion, Card, ButtonGroup, Button, ListGroup, ListGroupItem } from 'react-bootstrap';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { withTranslation } from 'react-i18next';

import history from './../../scripts/history'
import { ContextAwareToggle } from './../../scripts/util'
import { isAuthenticated } from './../../components/pages/login/Login.js'

import { fetchRESTFollow, dispatchCleanRESTData, resetRESTCallLimit } from './../../actions/taskActions';
import { resolveObjectPath, reduceCountingPromises } from './../../scripts/dataUtils';

class EmbedRequiredAttachmentsContainer extends React.Component {
	constructor(...args) {
		super(...args);
		this.state = {
			open: this.props.expanded !== undefined ? this.props.expanded : false,
		};
	}

	refreshData() {
		this.props.actions.dispatchCleanRESTData(this.props.componentPath);
	}

	retrieveData() {
		//get the param used for the data in the redux state
		const curr_href = this.props.data && this.props.data._links ? url.parse(this.props.data._links.self.href,true).query.task : this.props.href;
		if(this.props.href && this.props.componentPath
				&& (!this.props.data || this.props.href != curr_href)) {
			this.props.actions.fetchRESTFollow(
				{
					url: API_URL+"/taskRequiredAttachments/search/findByTask",
					params: {
						task: this.props.href
					}
				},
				this.props.componentPath,
				data => (data),
				'EmbedRequiredAttachmentsContainer.retrieveData',
				{attachment:
					{
						node: "_embedded.taskRequiredAttachments"
					}
				}
			);
		}
	}

	componentDidMount(){
		if(!isAuthenticated(this.props.auth)){
			history.push('/login')
		} else {
			this.retrieveData();
		}
	}

	componentDidUpdate(prevProps, prevState) {
		this.retrieveData();
	}

	render() {
		let listItems = <FontAwesomeIcon icon="spinner" size="2x" spin/>;
		let okCount = <FontAwesomeIcon icon="spinner" spin/>;
		let totalCount = <FontAwesomeIcon icon="spinner" spin/>;
		if(this.props.items instanceof Array) {
			listItems = this.props.items.map((attachment) =>
				<ListGroupItem key={attachment.id.toString()}>
					<Button variant="outline-dark"><FontAwesomeIcon icon="trash-alt"/></Button>&nbsp;
					{attachment.description} ({attachment.type}):&nbsp;
					{attachment.attachment ? attachment.attachment.name : this.props.t("--none--")}&nbsp;
					{attachment.attachment ?
						<ButtonGroup>
							<Button variant="outline-dark"><FontAwesomeIcon icon="download"/></Button>
							<Button variant="outline-dark"><FontAwesomeIcon icon="unlink"/></Button>
						</ButtonGroup>
						: <Button variant="outline-dark"><FontAwesomeIcon icon="paperclip"/></Button>
					}
				</ListGroupItem>
			);
			okCount = this.props.items.reduce((sum,value) => sum + (value.attachment?1:0), 0);
			totalCount = this.props.items.length;
		}
		return (
			<Accordion activeKey={this.state.open ? "0" : ""}>
				<Card>
					<Card.Header>
						<ButtonGroup>
							<Button variant="outline-dark" onClick={() => this.setState({ open: !this.state.open })}>
								<FontAwesomeIcon icon="check-square"/>&nbsp;
								{this.props.t("RequiredAttachments._className_plural")} ({okCount}/{totalCount})&nbsp;
								<FontAwesomeIcon icon="caret-square-down"/>
							</Button>
							<Button variant="outline-dark"><FontAwesomeIcon icon="plus"/></Button>
							<Button variant="outline-dark" title={this.props.t("ReactTable.Refresh")} onClick={(e) => {e.stopPropagation(); resetRESTCallLimit(); this.refreshData();}}><FontAwesomeIcon icon="sync"/></Button>
						</ButtonGroup>
					</Card.Header>
					<Accordion.Collapse eventKey="0"><Card.Body>
						<ListGroup fill>{listItems}</ListGroup>
					</Card.Body></Accordion.Collapse>
				</Card>
			</Accordion>
		);
	}
}

//redux mapping of props
function mapStateToProps(state,ownProps) {
	let data = resolveObjectPath(ownProps.componentPath,state.rest); //rest because of fetchRES
	return {
		auth: state.auth,
		data: data,
		items: data && data._embedded ? data._embedded.taskRequiredAttachments : undefined,
		componentPath: ownProps.componentPath,
		href: ownProps.href
	};
}

//redux mapping of actions
function mapDispatchToProps(dispatch) {
	return {
		actions: bindActionCreators(Object.assign({}, { fetchRESTFollow, dispatchCleanRESTData }), dispatch)
	};
}

//export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(EmbedRequiredAttachmentsContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
