var MongoClient = require('mongodb').MongoClient;
var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;
var dateFormat = require('dateformat');

FlowDataProvider = function () { };

FlowDataProvider.prototype.find = function (callback) {
    console.log('vao');
    MongoClient.connect('mongodb://103.22.221.55:27017/smartxdb', function (err, db) {
        var collection = db.collection("flowlevel-data");
        collection.find({protocol: 'icmp'}).sort({timestamp: -1}).limit(1).toArray(function (err, flows) {
            callback(null, flows);
        });
    });
};
exports.FlowDataProvider = FlowDataProvider;
