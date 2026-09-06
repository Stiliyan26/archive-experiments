import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom'
import { withTranslation } from 'react-i18next';

import * as Constants from '../../static/constants.js';
import history from '../../scripts/history.js';
import { isAuthenticated } from '../../components/pages/login/Login.js';

import NewHeader from '../../components/generic/NewHeader.js';
import EmbedRetrieveEntityListContainer from '../nomenclatures/EmbedRetrieveEntityListContainer.js';
import EmbedEntityContainer from '../embeds/EmbedEntityContainer.js';
import EmbedChangeHistory from '../embeds/EmbedChangeHistory.js'
import { resolveObjectPath } from "../../scripts/dataUtils.js";

import { getEntityDefinition, getExpandedColumns } from '../nomenclatures/entityDefinitions.js';
import { LOGIN } from './constants/selfiePaths.js';


// Page: can be used as a landing page 
// Table: presents table of the objects
// Container: redux container class
class PageViewAgreementSelfInvoicingContainer extends React.Component {
    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }
    }

    render() {
        let body = "";

        if (this.props.data) {

            if (this.props.data[Constants.PATH_FOR_LOADING] instanceof Promise) {
                body = <FontAwesomeIcon icon="spinner" size="2x" spin />;

            } else if (this.props.id && this.props.id == parseInt(this.props.match.params.entity_id, 10)) {
                const selfInvoicingLinesDef = getEntityDefinition("selfInvoicingLines", {});

                body = <div className="page-body">
                    <EmbedRetrieveEntityListContainer
                        title={selfInvoicingLinesDef.label}
                        icon={selfInvoicingLinesDef.icon}
                        columns={selfInvoicingLinesDef.columns}
                        componentPath="agreementSelfInvoicings.selfInvoicingLinesDef"
                        retrieveType="selfInvoicingLines"
                        parentHref={this.props.href}
                        parentAttr="agreementSelfInvoicing"
                        parentData={this.props.data}
                        asTable={true}
                    />

                    <EmbedChangeHistory
                        entity_id={this.props.id}
                        componentPath={"powerPlantView.agreementSelfInvoicings.changelog"}
                        retrieveType={this.props.entityName}
                    />
                </div>;
            }
        }

        return (
            <div className="page-body-wrapper">
                <NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} />
                <div className='page-body'>
                    <EmbedEntityContainer
                        componentPath={this.props.componentPath + "." + this.props.entityDef.className}
                        retrieve_id={this.props.match.params.entity_id}
                        onBeforeChange={(data) => {
                            if (!this.props.entityDef.pageURL) { console.error("pageURL is missing for entity " + this.props.entityDef.className); }
                            if (data.id) {
                                history.push(this.props.entityDef.pageURL + '/' + data.id);
                            } else {
                                history.push(this.props.entityDef.pageURL + '/add');
                            }
                        }}
                        entityName={"agreementSelfInvoicings"}
                        headerText={this.props.t("AgreementSelfInvoicing._className_plural")}
                        creatable={false}
                        expanded={true}
                    />

                    {body}

                </div>
            </div>
        );
    }
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
    const entityDef = getEntityDefinition("agreementSelfInvoicings", {});
    const componentPath = "PageTable" + entityDef.className;
    const title = ownProps.t("AgreementSelfInvoicing._className_plural");
    let data = resolveObjectPath(componentPath + "." + "AgreementSelfInvoicing" + "._embedded." + "agreementSelfInvoicings" + ".0", state.rest);

    return {
        auth: state.auth,
        componentPath: componentPath,
        entityName: "agreementSelfInvoicings",
        entityDef: entityDef,
        expand: ownProps.expand ? ownProps.expand : [],
        title: title,
        icon: entityDef.icon ? entityDef.icon : "list",
        hasRowSelecting: ownProps.hasRowSelecting,
        selectedRowsColumns: ownProps.selectedRowsColumns,
        data: data,
        href: data && data._links && data._links.self ? data._links.self.href : undefined,
        id: data && data.id ? data.id : undefined,
    };
}

// redux mapping of actions
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

// export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageViewAgreementSelfInvoicingContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
