package com.gist.netcs.multiview;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.concurrent.TimeUnit;




import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;

public class SDNControllerStatusClass implements Runnable{
	private String devopscontroller;
	private String ThreadName="SDN Controller Config Thread";
	private String user, password;
	private Thread thread;
	private MongoClient mongoClient;
	private Document document;
	private MongoCollection<Document> collection1, collection2;
	private BatchPoints batchPoints;
	private static MongoDatabase db;
	private static InfluxDB influxDB;
	
	//public SDNControllerStatusClass(String dbHost, int dbPort, String dbName, String flowConfigMongoCollection, String flowConfigMongoCollectionRT, String devopscon, String User, String Password)
	public SDNControllerStatusClass(String dbHost, int dbPort, String dbName, String flowConfigMongoCollection, String flowConfigMongoCollectionRT, String devopscon, String User, String Password)
	{
		mongoClient = new MongoClient(dbHost, dbPort);
		db          = mongoClient.getDatabase(dbName);
		collection1 = db.getCollection(flowConfigMongoCollection);
		collection2 = db.getCollection(flowConfigMongoCollectionRT);
		
		// Connect to InfluxDB
        // influxDB = InfluxDBFactory.connect("http://103.22.221.55:8086", "root", "root");
        
        // Create a database
        // String InfluxdbName = "flow_config_visibility";
        // influxDB.createDatabase(InfluxdbName);
        // Create a 'batch' of example 'points'
        //batchPoints = BatchPoints
        //        .database(InfluxdbName)
        //        .tag("async", "true")
        //        .retentionPolicy("default")
        //        .consistency(InfluxDB.ConsistencyLevel.ALL)
       //         .tag("BatchTag", "BatchTagValue") // tag each point in the batch
        //        .build();
        
		devopscontroller          = devopscon;
		user 					  = User;
		password                  = Password;
	}
     
    {
		// TODO Auto-generated constructor stub
	}
	public void getFlowsDetails() 
    {
    	String baseURL   = "http://"+devopscontroller+":8080/controller/nb/v2/flowprogrammer";
    	String containerName = "default", actions, NodeID, BoxID; 
    	String [] id;
    	//Point point1;
    	Date timestamp = new Date();
    	System.out.println(devopscontroller);
    	collection2.deleteMany(new Document());
    
	    try {
	
	        // Create URL = base URL + container
	        URL url = new URL(baseURL + "/" + containerName);
	        System.out.println(url);
	
	        // Create authentication string and encode it to Base64
	        String authStr = user + ":" + password;
	        String encodedAuthStr = Base64.encodeBase64String(authStr.getBytes());
	
	        // Create Http connection
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	
	        // Set connection properties
	        connection.setRequestMethod("GET");
	        connection.setRequestProperty("Authorization", "Basic " + encodedAuthStr);
	        connection.setRequestProperty("Accept", "application/json");
	
	        // Get the response from connection's inputStream
	        InputStream content = (InputStream) connection.getInputStream();
	        BufferedReader in = new BufferedReader(new InputStreamReader(content));
	        String line = "";
	        
	        //JSONParser jsonParser = null;
	        line = in.readLine();
	        System.out.println(line);
	        JSONObject jsonObject = new JSONObject(line);
	        JSONArray jsonArray = jsonObject.getJSONArray("flowConfig");
	        for (int i=0 ; i<jsonArray.length(); i++)
	        {
	        	document = new Document();
	        	System.out.print("Node "+jsonArray.getJSONObject(i).get("node"));
	        	System.out.print(" Name "+jsonArray.getJSONObject(i).get("name"));
	        	System.out.print(" Install In Hw "+jsonArray.getJSONObject(i).get("installInHw"));
	        	System.out.print(" Ingress Port "+jsonArray.getJSONObject(i).get("ingressPort"));
	        	System.out.println(" Actions "+jsonArray.getJSONObject(i).get("actions"));
	        	//System.out.println(jsonArray.getJSONObject(i).get("id"));
	        	
	        	id      = jsonArray.getJSONObject(i).get("node").toString().split(",");
	        	NodeID  = id[0].substring(7, id[0].length()-1);
	        	actions = jsonArray.getJSONObject(i).get("actions").toString();
	        	
	        	if (NodeID.equals("33:33:33:33:33:33:33:11"))
	        		BoxID="SmartXBoxGIST";
	        	else if(NodeID.equals("33:33:33:33:33:33:33:31"))
	        		BoxID="SmartXBoxMYREN";
	        	else if(NodeID.equals("33:33:33:33:33:33:33:41"))
	        		BoxID="SmartXBoxID";
	        	else if(NodeID.equals("33:33:33:33:33:33:33:51"))
	        		BoxID="SmartXBoxPH";
	        	else if(NodeID.equals("33:33:33:33:33:33:33:61"))
	        		BoxID="SmartXBoxVN";
	        	else if(NodeID.equals("33:33:33:33:33:33:33:81"))
	        		BoxID="SmartXBoxPKS";
	        	else
	        		BoxID="";
	        	
	        	document.put("timestamp"    , timestamp);
	        	document.put("controllerIP" , devopscontroller);
	        	document.put("boxID"          , BoxID);
	        	document.put("node"         , NodeID);
	        	document.put("name"         , jsonArray.getJSONObject(i).get("name").toString());
	        	document.put("InstallInHw", jsonArray.getJSONObject(i).get("installInHw").toString());
	        	document.put("IngressPort" , jsonArray.getJSONObject(i).get("ingressPort").toString());
	        	document.put("Actions"      , actions.substring(2, actions.length()-2));
	        	
	        	collection1.insertOne(document);
	        	collection2.insertOne(document);
	        	
	        	//Insert to InfluxDB for Visualization
	        	//point1 = Point.measurement("FlowRules")
		        //        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
		        //       .addField("controllerIP", devopscontroller)
		        //        .addField("InstallInHw", jsonArray.getJSONObject(i).get("installInHw").toString())
		        //        .addField("name", jsonArray.getJSONObject(i).get("name").toString())
		        //        .tag("node", id[0].substring(7, id[0].length()-1)) // tag the individual point
		        //        .build();
	        	
	        	//batchPoints.point(point1);
		        
		        // Write them to InfluxDB
	        	//influxDB.write(arg0, arg1, arg2);
		        //influxDB.write(batchPoints);
	        }
	        
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
    }
    
    public void run() {
    	while (true)
		{
    		getFlowsDetails();
			try {
				//Sleep For 60 Minutes
				Thread.sleep(3600000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
    
    public void start() {
		System.out.println("Starting SDN Controller Flows Config Thread");
		if (thread==null){
			thread = new Thread(this, ThreadName);
			thread.start();
		}
	}
   
}

