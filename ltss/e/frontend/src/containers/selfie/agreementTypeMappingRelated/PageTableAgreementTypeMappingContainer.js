import React from 'react';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { withRouter } from 'react-router-dom';
import { Button } from 'react-bootstrap';
import { withTranslation } from 'react-i18next';
import axios from "axios";

import history from '../../../scripts/history';
import { isAuthenticated } from '../../../components/pages/login/Login.js';

import NewHeader from '../../../components/generic/NewHeader';
import EmbedRetrieveEntityListContainer from '../../nomenclatures/EmbedRetrieveEntityListContainer';

import { getEntityDefinition, getExpandedColumns } from '../../nomenclatures/entityDefinitions.js';
import { resolveObjectPath } from '../../../scripts/dataUtils';
import { patchRESTMultiData } from '../../../actions/taskActions';
import NepalDBFileContainer from '../../nepal/NepalDBFileContainer.js';
import { dispatchEditRESTData } from "../../../actions/taskActions.js";

import { selfieEntities } from "../constants/selfieEntities.js";
import { LOGIN } from "../constants/selfiePaths.js";


// Page: can be used as a landing page
// Table: presents table of the objects
// Container: redux container class
class PageTableAgreementTypeMappingContainer extends React.Component {
    componentDidMount() {
        if (!isAuthenticated(this.props.auth)) {
            history.push(LOGIN);
        }
    }

    render() {
        let columns = getEntityDefinition(this.props.entityName, {}).columns;

        return (
            <div className="page-body-wrapper">
                <NewHeader text={this.props.title} auth={this.props.auth.userAuthenticated} />
                
                <div className='page-body'>
                    <EmbedRetrieveEntityListContainer
                        retrieveType={this.props.entityName}
                        componentPath={this.props.componentPath} //existing path in redux store where we put data
                        columns={columns}
                        title={this.props.t(`${selfieEntities.AgreementTypeMapping.className}._className_plural`)}
                        icon={this.props.icon}
                        expanded={true}
                        asTable={true}
                        editable={true}
                        selectedRowsColumns={this.props.selectedRowsColumns}
                    />
                </div>
            </div>
        );
    }
}

// redux mapping of props
function mapStateToProps(state, ownProps) {
    const entityDef = getEntityDefinition(selfieEntities.AgreementTypeMapping.pluralCamelCase, undefined);
    const componentPath = "PageTable" + entityDef.className;
    const title = ownProps.t(`${selfieEntities.AgreementTypeMapping.className}._className_plural`);

    return {
        auth: state.auth,
        componentPath: componentPath,
        entityName: selfieEntities.AgreementTypeMapping.pluralCamelCase,
        entityDef: entityDef,
        expand: ownProps.expand ? ownProps.expand : [],
        title: title,
        icon: entityDef.icon ? entityDef.icon : "list",
        hasRowSelecting: ownProps.hasRowSelecting,
        selectedRowsColumns: ownProps.selectedRowsColumns,
    };
}

// redux mapping of actions
function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, { patchRESTMultiData, dispatchEditRESTData }), dispatch)
    };
}

// export class wrapped in redux and in router
const ConnectComponent = connect(mapStateToProps, mapDispatchToProps)(PageTableAgreementTypeMappingContainer);
const WithRouterComponent = withRouter(ConnectComponent);
export default withTranslation()(WithRouterComponent);
