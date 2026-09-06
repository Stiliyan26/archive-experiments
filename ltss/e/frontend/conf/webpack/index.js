'use strict';

const dev = require('./Dev');
const dist = require('./Dist');
const test = require('./Test');
const staging = require('./Staging');
const demo = require('./Demo');
const testBgMashini = require('./TestBgMashini');
const distBgMashini = require('./DistBgMashini');
const distNepal = require('./DistNepal');
const testSelfie = require('./TestSelfie');
const testAnvoice = require('./TestAnvoice');

module.exports = {
  dev,
  dist,
  test,
  staging,
  demo,
  testBgMashini,
  distBgMashini,
  distNepal,
  testSelfie,
  testAnvoice
};
