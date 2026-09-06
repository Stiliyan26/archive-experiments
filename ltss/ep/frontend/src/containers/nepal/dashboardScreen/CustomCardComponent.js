import React from 'react';

import './CustomCardComponent.css'


import { Button, Card } from "react-bootstrap";
import { Link } from 'react-router-dom';

import { bindActionCreators } from 'redux';
import { connect } from 'react-redux';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';


const MAIN_CONTAINER = 'mainContainer'
const CUSTOM_CARD = 'customCard'
const FRONT_CARD = 'frontCard'
const BACK_CARD = 'backCard'
const CARD_HEADER_FRONT = 'cardHeaderFront'
const CARD_HEADER_BACK = 'cardHeaderBack'
const CARD_BODY_FRONT = 'cardBodyFront'
const CARD_BODY_FRONT_ICONS = 'cardBodyFrontIcons'
const CARD_BODY_FRONT_ICON = 'cardBodyFrontIcon'
const CARD_BODY_BACK_ICONS = 'cardBodyBackIcons'
const CARD_BODY_BACK_ICON = 'cardBodyBackIcon'
const FA_COLOR = 'faColor'
const CARD_BODY_BACK = 'cardBodyBack'
const CARD_FOOTER = 'cardFooter'

class CustomCardComponent extends React.Component {

    constructor(props) {
        super(props)
        this.state = {
            selectedText: "",
            selectedIcon: ""
        };
    }

    createGrid() {

        let gridColumnSize = ""

        for (let i = 0; i < this.props.data.length - 1; i++) {
            if (gridColumnSize == "") {
                gridColumnSize = "1fr"
            }
            gridColumnSize = gridColumnSize + " 1fr"
        }

        return gridColumnSize
    }

    staticBodyContent() {
        return <div style={{ marginTop: "30px", marginBottom: "30px" }}>
            <ol>
                {this.props.data.map((item, index) => <li key={index} className='listItem'>{item.shortText}</li>)}
            </ol>
        </div>
    }

    render() {
        return (

            <div className={MAIN_CONTAINER}>
                <div className={CUSTOM_CARD}>
                    <Card className={FRONT_CARD}>
                        <Card.Header className={CARD_HEADER_FRONT}>
                            {this.props.headerText}
                        </Card.Header>
                        <Card.Body className={CARD_BODY_FRONT}>
                            <div className={CARD_BODY_FRONT_ICONS} style={{ gridTemplateColumns: this.createGrid() }}>
                                {this.props.data.map((item, index) =>
                                    <div className={CARD_BODY_FRONT_ICON} key={index}>
                                        {
                                            <FontAwesomeIcon size="2x"
                                                icon={item.iconName}
                                            />
                                        }
                                    </div>)
                                }
                            </div>
                            {this.staticBodyContent()}
                        </Card.Body>
                    </Card>

                    <Card className={BACK_CARD}>
                        <Card.Header className={CARD_HEADER_BACK}>
                            {this.props.headerText}
                        </Card.Header>
                        <Card.Body className={CARD_BODY_BACK}>
                            <div className={CARD_BODY_BACK_ICONS} style={{ gridTemplateColumns: this.createGrid() }}>
                                {this.props.data.map((item, index) =>
                                    <div className={CARD_BODY_BACK_ICON} key={index}>
                                        {
                                            <FontAwesomeIcon
                                                size="2x"
                                                icon={item.iconName}
                                                className={FA_COLOR}
                                                onMouseEnter={() => this.setState({ selectedText: item.text, selectedIcon: item.iconName })}
                                                onMouseLeave={() => this.setState({ selectedText: "", selectedIcon: "" })}
                                            />
                                        }
                                    </div>)
                                }
                            </div>
                            <div style={{ marginTop: "30px", display: "grid", gridTemplateColumns: "1fr 2fr", overflow: "hidden" }}>
                                {this.state.selectedIcon == "" ? "" : <FontAwesomeIcon
                                    size="6x"
                                    icon={this.state.selectedIcon}
                                    className='selectedIcon'
                                />}
                                {this.state.selectedText == "" ? "" : <div className='selectedText'>{this.state.selectedText}</div>}
                                
                            </div>
                        </Card.Body>
                        <Card.Footer className={CARD_FOOTER}>
                            <Link to={this.props.buttonLink}>
                                <Button variant="outline-danger">
                                    {this.props.buttonText}
                                </Button>
                            </Link>
                        </Card.Footer>
                    </Card>
                </div>
            </div>
        )
    }
}

function mapStateToProps(state, ownProps) {
    return {};
}

function mapDispatchToProps(dispatch) {
    return {
        actions: bindActionCreators(Object.assign({}, {}), dispatch)
    };
}

export default connect(mapStateToProps, mapDispatchToProps)(CustomCardComponent);