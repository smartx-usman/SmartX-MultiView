var MongoClient = require('mongodb').MongoClient;
var Db = require('mongodb').Db;
var Connection = require('mongodb').Connection;
var Server = require('mongodb').Server;
var BSON = require('mongodb').BSON;
var ObjectID = require('mongodb').ObjectID;
var dateFormat  = require('dateformat');

UserProvider = function() {};

UserProvider.prototype.find = function(callback) 
{
    MongoClient.connect('mongodb://127.0.0.1:27017/smartxdb', function(err, db)
    {
        var collection = db.collection("configuration-multiview-users");
        collection.find().toArray(function(err, users){
		callback(null,users);
	});
    });
};
exports.UserProvider = UserProvider;
