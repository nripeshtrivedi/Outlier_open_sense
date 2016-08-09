package outlier_detection;

import weka.core.Instances;
import weka.experiment.InstanceQuery;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;

import motaz.CODB;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.core.*;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import java.sql.Statement;



public class machine {
	static int pk=0;
	static long timed=0L;
	static double ldsa=0.0;
	static int station=0;
	static double latitude=0.0;
	static double longitude=0.0;
	static double vals[];
	static Instances data;
	@SuppressWarnings("rawtypes")
	static FastVector  atts;
	static int c=1;
	static int count=0;
	static String name;
	static long first_date=1420070400;
	static long second_date= 1420070400;
    static long month_c= 2629743;
    static long first_date_100;
    static long second_date_100;

	


	  public static Connection getDBConn() {
	        try {
	            Class.forName("org.postgresql.Driver");

	        } catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        }
	        Connection connection = null;

	        try {
	            connection = DriverManager.getConnection(
	                    Config.connString,
	                    Config.connUser,
	                    Config.connPassword);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return connection;
	    }
	 @SuppressWarnings({ "unchecked", "rawtypes" })// sampling 
	public  static void getDatasetDB(String tableName) throws Exception {
	        Statement st = null;
	        ResultSet rs = null;
	        PreparedStatement preparedStatement = null;
	        Connection connection = getDBConn();
	        int station_values[]={41,43,45, 47, 48, 49, 50, 51, 54, 55};
	        long buckets[]={1417392000,1430438400,1430438400,1433030400,1433116800,1440979200,1441065600,1446249600,1446336000,1448928000};
	        int bucket_count=0;
	        
	        for(int i=0;i<station_values.length; i++)
	        {
	        	bucket_count=0;
	        while((bucket_count<10))
	        {
	        	 System.out.println(bucket_count);
	        	
	        long date_1=buckets[bucket_count]*1000;
	        double date_2=buckets[bucket_count+1]*1000;
	        String query="SELECT DISTINCT ON (ldsa) pk, latitude, longitude, station, ldsa, timed FROM geo_osfpm WHERE station=? AND timed>= ? AND timed<=? AND Latitude is not null AND longitude is not null AND ldsa<=2000 ANd ldsa >=1 LIMIT 10000";
	        preparedStatement =  connection.prepareStatement(query);
	       preparedStatement.setInt(1,station_values[i]);
	       
	        preparedStatement.setDouble(2,date_1);
	        preparedStatement.setDouble(3,date_2);
	        rs = preparedStatement.executeQuery();
	        bucket_count=bucket_count+2;
	      
	         System.out.println(preparedStatement);
	        while (rs.next()) {
	        	pk = rs.getInt("pk");
	        	timed=rs.getLong("timed");
	        	station = rs.getInt("station");
	        	ldsa=rs.getDouble("ldsa");
	        	latitude=rs.getDouble("latitude");
	        	longitude=rs.getDouble("longitude");
	        	vals = new double[6];
	        	vals[0]=pk;
	        	vals[1]=timed;
	        	vals[2]=station;
	        	vals[3]=ldsa;
	        	vals[4]=latitude;
	        	vals[5]=longitude;
	        	
	        	closest_point.closest_point_insert(vals);
	        	
	        }
	       
	        }

	        }
	        
	 }

	 
	 
	 public static void main(String args[]) throws Exception
	 {
	/*   	getDBConn();
		    String tablename="geo_osfpm";
		    getDatasetDB(tablename);  
		//    PostgreSQLlocal.readdata(); */
	//CODB codb = new CODB();
    //System.out.println(codb.GO(args)); 
	//incremental_claasifier.build_save_classifier(); 
   incremental_claasifier.updateclassifier();
	 }
}

