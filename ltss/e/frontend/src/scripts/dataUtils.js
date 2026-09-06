import React from 'react';
import i18n from 'i18next';
import lodashIsEmpty from 'lodash/isEmpty'

export function resolveObjectPath(path, obj) {
	if(!path || !obj) return obj; //if there is no path, return the same object
	return path.split('.').reduce(function(prev, curr) {
		return curr == '' ? prev : (prev ? prev[curr] : undefined)
	}, obj || self)
}

export function resolveObjectParentPath(path, obj) {
	if(!path || !obj) return {};
	return path.split('.').reduce(function(prev, curr) {
		return curr == '' ? prev : (prev.value ? {parentValue: prev.value, value: prev.value[curr]} : {})
	}, {parentValue: obj || self, value: obj || self})
}

export function deleteObjectPath(path, obj) {
	if(!path || !obj) return undefined;
	const pathArray = path.split('.');
	let curr = obj || self;
	for(var i = 0; i<pathArray.length-1; i++) {
		if(pathArray[i] == '') {
			throw({path, obj, value});
		} else {
			if(!curr[pathArray[i]]) {
				return false;
			}
			curr = curr[pathArray[i]];
		}
	}
	if(curr instanceof Array) {
		curr.splice(pathArray[pathArray.length-1], 1);
	} else {
		delete curr[pathArray[pathArray.length-1]];
	}
	return true;
}

//alternatively use selectors https://medium.com/@MattiaManzati/how-to-reuse-redux-components-8acd5b4d376a
export function buildObjectPathAndAssign(path, obj, value) {
	if(!path || !obj) return undefined;
	const pathArray = path.split('.');
	//console.log('pathArray',pathArray);
	let curr = obj || self;
	//console.log('curr',curr);
	for(var i = 0; i<pathArray.length-1; i++) {
		if(pathArray[i] == '') {
			throw({path, obj, value});
		} else {
			if(!curr[pathArray[i]]) {
				//console.log('Create',pathArray[i]);
				curr[pathArray[i]] = {};
			}
			//console.log('Go to',pathArray[i]);
			curr = curr[pathArray[i]];
		}
	}
	//console.log('Set',pathArray[pathArray.length-1]);
	curr[pathArray[pathArray.length-1]] = value;
	return curr[pathArray[pathArray.length-1]];
}

export function reduceCountingPromises(reducedArray, aggregatingFn) {
	return reducedArray.reduce((total,curr) => (
			!curr ? total
			: ( curr instanceof Promise ? {aggregate: total.aggregate, promises: total.promises+1}
				: {aggregate: aggregatingFn(total,curr), promises: total.promises}
			)
		), {aggregate: 0, promises: 0});
}


export function getNomenclatureByCriteria(nomenclatureKey, criteria, rest, fetchRESTFollow, dispatchEditRESTData) {
	if(rest && rest[nomenclatureKey]) {
		const nomenclatures = rest[nomenclatureKey];
		let found = undefined;
		if(nomenclatures instanceof Array) {
			found = nomenclatures.find(criteria);
			if(found) {
				return Promise.resolve(found);
			}
		}
	}
	//if cache not timed out
	if(rest && rest[nomenclatureKey] && ((new Date()) - rest[nomenclatureKey]._lastFetch) < 2500 ) {
		return Promise.resolve(undefined);
	}
	console.log("Nomenclature not found in cache, refetching",nomenclatureKey);
	let fetchPromiseWrapper = {};
	//if not found load and try again
	fetchRESTFollow(
		{
			url: `${API_URL}/${nomenclatureKey}?page=0&size=1000`,
		},
		nomenclatureKey,
		response => {
			let result = response.data._embedded[nomenclatureKey];
			result._lastFetch = new Date();
			return result;
		},
		'getNomenclatureByCriteria',
		{},
		fetchPromiseWrapper
	);
	return fetchPromiseWrapper.promise
		.then((mergeObj) => {
			let response = mergeObj[0].value;
			//console.log("getNomenclatureByCriteria response",response);
			if(response instanceof Error) {
				//TODO this seems to be moved here without fix
				dispatchEditRESTData(this.props.componentPath+".specific.errors."+nomenclatureKey,{error: response.toString(),response: response});
			} else {
				if(response instanceof Array) {
					return response.find(criteria);
				}
			}
			throw new Error("Error.NomenclatureNotFound");
		});
}

export function getNomenclatureByCode(nomenclatureKey, code, rest, fetchRESTFollow, dispatchEditRESTData) {
	return getNomenclatureByCriteria(nomenclatureKey, (elem) => (elem.code == code), rest, fetchRESTFollow, dispatchEditRESTData);
}

export function getLoiByCode(nomenclatureKey, code, rest, fetchRESTFollow, dispatchEditRESTData) {
	return getNomenclatureByCriteria(nomenclatureKey, (elem) => (elem.listOptionItemCode == code), rest, fetchRESTFollow, dispatchEditRESTData);
}

export function extractColumnErrorMessage(itemError) {
	let errorMessage = "";
	if(itemError instanceof Error) {
		try {
			if(itemError.response && itemError.response.data) {
				if(itemError.response.data.message == "Forbidden") {
					return getMessageFromCode("Forbidden");
				}
				return JSON.parse(itemError.response.data.message.replace("\\\"","\""));
			} else {
				errorMessage = itemError.message;
			}
		}
		catch(error) {
			if(error instanceof SyntaxError) {
				errorMessage = itemError.response && itemError.response.data ? itemError.response.data.message : itemError.message;
			} else {
				console.error(error);
			}
		}
	}
	return errorMessage;
}

export function extractErrorMessage(itemError, entityDef, skipColumns) {
	let errorMessage = "";
	if(itemError instanceof Error ) {
		try {
			if(itemError.response && itemError.response.data) {
				if(itemError.response.data.message == "Forbidden") {
					return getMessageFromCode("Forbidden");
				}
				let error = JSON.parse(itemError.response.data.message.replace("\\\"","\""));
				errorMessage = error.reduce((acc,curr) => {
					if(curr.field) {
						let column = entityDef.columns.find((column) => {
								return column.accessor == curr.field
							});
						if(column && column.Header) {
							return skipColumns ? acc : (acc + "("+column.Header+") " + getMessageFromCode(curr.error, curr.value) + "; ");
						} else if(curr.error){
							return acc + getMessageFromCode(curr.error, curr.field, curr.value) + "; ";
						} else {
							return acc + JSON.stringify(curr);
						}
					} else if(!lodashIsEmpty(curr)) {
						return acc + JSON.stringify(curr);
					} else {
						return acc;
					}
				}, "")
			} else {
				errorMessage = itemError.message;
			}
		}
		catch(error) {
			if(error instanceof SyntaxError) {
				errorMessage = itemError.response && itemError.response.data ? itemError.response.data.message : itemError.message;
			} else {
				console.error(error);
			}
		}
	}
	return errorMessage;
}

export function getMessageFromCode(code, param1, param2, param3) {
	const message = i18n.t(["Error."+code,"Error.MissingErrorCode"]);
	const content = message.replace(/\{(.*)\}/g,(match,p1) => {
		let value = undefined;
		if(p1 == "param1") {
			value = param1;
		}
		if(p1 == "param2") {
			value = param2;
		}
		if(p1 == "param3") {
			value = param3;
		}
//		if(value === undefined) {
//			value = eval(p1); //TODO maybe too dangerous? put warning? when to warn?
//		}
		return value;
	});
	return content;
}

export function convertNumbersToBgnCurrency(number) {
	let num0 =
	[
		"нула", "един", "два", "три", "четири", "пет", "шест",
		"седем", "осем", "девет", "десет", "единадесет","дванадесет"
	]
	let num100 = [
		"","сто", "двеста",  "триста"
	]

	let div10 = (number - number % 10) / 10;
	let mod10 = number % 10;
	let	div100 = (number - (number % 100)) / 100;
	let mod100 = number % 100;
	let div1000 = (number - number % 1000) / 1000;
	let mod1000 = number % 1000;
	let div1000000 = (number - number % 1000000) / 1000000;
	let mod1000000 = number % 1000000;
	let div1000000000 = (number - number % 1000000000) / 1000000000;
	let mod1000000000 = number % 1000000000;

	if (number == 0) {
		return num0[number];
	}


	/* До двайсет */
	if (number > 0 && number < 20) {
		return (num0[number]) ? num0[number] : num0[mod10] +"надесет";
	}

	/* До сто */
	if (number > 19 && number < 100) {
		let tmp = num0[div10] + "десет";
		tmp = mod10 ? tmp + " и " + convertNumbersToBgnCurrency(mod10) : tmp;
		return tmp;
	}

	/* До хиляда */
	if (number > 99 && number < 1000) {
		let tmp = (num100[div100]) ? num100[div100] : num0[div100]+"стотин";
		if ((mod100 % 10 == 0 || mod100 < 20) && mod100 != 0) {
			tmp += " и";
		}
		if (mod100) {
			tmp += " "+convertNumbersToBgnCurrency(mod100);
		}
		return tmp;
	}

	/* До милион */
	if (number > 999 && number < 1000000) {
		/* Damn bulgarian @#$%@#$% два хиляди is wrong :) */
		let tmp = (div1000 == 1) ? "хиляда" :
				((div1000 == 2) ? "две хиляди" : convertNumbersToBgnCurrency(div1000)+" хиляди");
		if ((mod1000 % 10 == 0 || mod1000 < 20) && mod1000 != 0) {
			if (!((mod100 % 10 == 0 || mod100 < 20) && mod100 != 0)) {
				tmp += " и";
			}
		}
		if ((mod1000 % 10 == 0 || mod1000 < 20) && mod1000 != 0 && mod1000 < 100) {
			tmp += " и";
		}
		if (mod1000) {
			tmp += " " +convertNumbersToBgnCurrency(mod1000);
		}
		return tmp;
	}

	/* Над милион */
	if (number > 999999 && number < 1000000000) {
		let tmp = (div1000000 == 1) ? "един милион" : convertNumbersToBgnCurrency(div1000000)+" милиона";
		if ((mod1000000 % 10 == 0 || mod1000000 < 20) && mod1000000 != 0) {
			if (!((mod1000 % 10 == 0 || mod1000 < 20) && mod1000 != 0)) {
				if (!((mod100 % 10 == 0 || mod100 < 20) && mod100 != 0)) {
					tmp += " и";
				}
			}
		}
		tmp += ", ";
		if ((mod1000000 % 10 == 0 || mod1000000 < 20) && mod1000000 != 0 && mod1000000 < 1000) {
			if ((mod1000 % 10 == 0 || mod1000 < 20) && mod1000 != 0 && mod1000 < 100) {
				tmp += " и";
			}
		}
		if (mod1000000) {
			tmp += " "+ convertNumbersToBgnCurrency(mod1000000);
		}
		return tmp;
	}

	/* Над милиард */
	if (number > 99999999 && number <= 2000000000) {
		let tmp = (div1000000000 == 1) ? "един милиард" : "";
		tmp = (div1000000000 == 2) ? "два милиарда" : tmp;
		if (mod1000000000) {
			tmp += " "+convertNumbersToBgnCurrency(mod1000000000);
		}
		return tmp;
	}
	
	return "твърде голямо число";
}

export function convertNumbersToEnglish(s){
	//USA
	var th = ['','thousand','million', 'billion','trillion'];
	//UK
	//var th = ['','thousand','million', 'milliard','billion'];
	var dg = ['zero','one','two','three','four', 'five','six','seven','eight','nine'];
	var tn = ['ten','eleven','twelve','thirteen', 'fourteen','fifteen','sixteen', 'seventeen','eighteen','nineteen'];
	var tw = ['twenty','thirty','forty','fifty', 'sixty','seventy','eighty','ninety']; 
	
	s = (s||'').toString();
	s = s.replace(/[\, ]/g,'');
	if (s != parseFloat(s)) return 'not a number';
	var x = s.indexOf('.');
	if (x == -1) x = s.length;
	if (x > 15) return 'too big';
	var n = s.split('');
	var str = '';
	var sk = 0;
	for(var i=0; i < x; i++) {
		if((x-i)%3==2) {
			if(n[i] == '1') {
				str += tn[Number(n[i+1])] + ' ';
				i++;
				sk=1;
			} else if(n[i]!=0) {
				str += tw[n[i]-2] + ' ';
				sk=1;
			}
		} else if(n[i]!=0) {
			str += dg[n[i]] +' ';
			if((x-i)%3==0) str += 'hundred ';
			sk=1;
		}
		if((x-i)%3==1) {
			if(sk) str += th[(x-i-1)/3] + ' ';
			sk=0;
		}
	}
	if(x != s.length) {
		var y = s.length;
		str += 'point ';
		for(var i=x+1; i<y; i++) str += dg[n[i]] + ' ';
	}
	return str.replace(/\s+/g,' ');
}

export function getEntityFromURL(url) {
	let urlParts = url.split('/');
	return urlParts.length >=2 ? urlParts[urlParts.length-2] : urlParts[0];
}

export function getObjectDiff(obj1, obj2) {
	const diff = Object.keys(obj1).reduce((result, key) => {
		if (!obj2.hasOwnProperty(key)) {
			result[key] = obj1[key];
		} else if(obj1[key] == obj2[key]) {
			delete result[key];
		} else if(typeof (obj1[key]) == typeof ({}) && typeof (obj2[key]) == typeof ({})) {
			result[key] = getObjectDiff(obj1[key],obj2[key]);
		}
		return result;
	}, Object.assign({},obj2));

	return diff;
}

function fetchAll(startPage, fetchRESTFollow, restConfig,statePath,mapping,origin,follow_links = {}) {
	console.log('fetchAll',restConfig);
	restConfig.params.size = 1000;
	restConfig.params.page = startPage;
	let fetchPromiseWrapper = {};
	fetchRESTFollow(restConfig,statePath,mapping,origin,follow_links,fetchPromiseWrapper);
	if(fetchPromiseWrapper.promise) { //happens when call limit is hit on multi-row update
		fetchPromiseWrapper.promise.then((response) => {
			let totalPages = response && response[0] && response[0].value && response[0].value.page ? response[0].value.page.totalPages : 0;
			if(totalPages > startPage+1) {
				fetchAll(startPage+1, fetchRESTFollow, restConfig,statePath,mapping,origin,follow_links);
			}
		});
	};
}

export function fetchRESTAll(fetchRESTFollow, restConfig,statePath,mapping,origin,follow_links = {}) {
	fetchAll(0, fetchRESTFollow, restConfig,statePath,mapping,origin,follow_links);
}
