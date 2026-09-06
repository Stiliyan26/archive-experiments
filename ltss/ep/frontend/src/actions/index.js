
import * as authActions from './auth.js'
import * as modalActions from './modal.js'

const actions = Object.assign({}, authActions, modalActions );
module.exports = actions;