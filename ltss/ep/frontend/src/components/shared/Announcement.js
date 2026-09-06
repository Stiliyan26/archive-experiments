import React from 'react';
import classNames from 'classnames'
import { Modal, Button } from 'react-bootstrap';

export default class Announcement extends React.Component {
	constructor(props){
		super(props);
		this.state= { };
	}
	render() {
		let loaderClasses = classNames({
			"hidden": !this.props.visible,
			"announcement-wrapper": true,
			"static-modal": true,
			// "error": this.props.error,
			// "success": !this.props.error,
			"text-align-middle": true
		})

		return (
			<div className={loaderClasses} >
				<Modal.Dialog>
					<Modal.Header className="text-align-center">
						<Modal.Title>{this.props.title}</Modal.Title>
					</Modal.Header>

					<Modal.Body>
						{this.props.message}
					</Modal.Body>

					<Modal.Footer>
						<Button onClick={ this.props.refuseCallback }>{this.props.refuseLabel}</Button>
						<Button variant="primary" onClick={ this.props.acceptCallback }> {this.props.acceptLabel} </Button>
					</Modal.Footer>

				</Modal.Dialog>
			</div>
		);
	}
}
