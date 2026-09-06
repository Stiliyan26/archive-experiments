import React, { useContext } from 'react';
import AccordionContext from "react-bootstrap/AccordionContext";
import { useAccordionToggle } from "react-bootstrap/AccordionToggle";
import { Button } from 'react-bootstrap';

var Util = {
	createCookie: (name,value,hours) => {
	    var expires = "";
	    if (hours) {
	        var date = new Date();
	        date.setTime(date.getTime() + (hours*60*60*1000));
	        expires = "; expires=" + date.toUTCString();
	    }
	    document.cookie = name + "=" + value + expires + "; path=/";
	},

	readCookie: (name) => {
	    var nameEQ = name + "=";
	    var ca = document.cookie.split(';');
	    for(var i=0;i < ca.length;i++) {
	        var c = ca[i];
	        while (c.charAt(0)==' ') c = c.substring(1,c.length);
	        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	    }
	    return null;
	},

	eraseCookie: (name) => {
	    Util.createCookie(name,"",-1);
	}
}

export default Util

export function ContextAwareToggle({ children, eventKey, callback }) {
	const currentEventKey = useContext(AccordionContext);

	const decoratedOnClick = useAccordionToggle(
		eventKey,
		() => callback && callback(eventKey),
	);

	const isCurrentEventKey = currentEventKey === eventKey;

	return (
		<Button variant="outline-dark" onClick={decoratedOnClick}>
			{children}
		</Button>
	);
}