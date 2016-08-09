package outlier_detection;
import java.sql.*; 
import java.util.*; 
import java.lang.*; 
import org.postgis.*;
import org.postgresql.util.PGobject;

import weka.core.Instances;
import weka.experiment.InstanceQuery;

public class PostgreSQLlocal {
	static Connection c=null;
	static Connection conn_gsn=null;
	static PreparedStatement updateemp; 
	static int station=0;
    static double ldsa=0.0;
    static double latitude=0.0;
    static double longitude=0.0;
    static double vals[];
   public static Connection PostgreSQLlocal() {
      try {
         Class.forName("org.postgresql.Driver");
         c = DriverManager
            .getConnection("jdbc:postgresql://localhost:5432/postgres",
            "postgres", "aaa");
      } catch (Exception e) {
         e.printStackTrace();
         System.err.println(e.getClass().getName()+": "+e.getMessage());
         System.exit(0);
      }
      System.out.println("Opened database successfully");
      return c;
   }
   public static Connection PostgreSQLhost() {
	      try {
	         Class.forName("org.postgresql.Driver");
	         conn_gsn = DriverManager.getConnection(
	                 "", "",
	                 "");
	      } catch (Exception e) {
	         e.printStackTrace();
	         System.err.println(e.getClass().getName()+": "+e.getMessage());
	         System.exit(0);
	      }
	      System.out.println("Opened database successfully");
	      return conn_gsn;
	   }
  
   public static Instances readdata() throws Exception{
	
	   InstanceQuery query = new InstanceQuery();
	   query.setUsername("postgres");
	   query.setPassword("aaa");
	 //  query.setQuery("select DISTINCT ON (ldsa) latitude, longitude, station, ldsa from geo_osfpm.geo_osfpm_tl where ldsa<=2000 and station=41 or station=43 or station=55");
	   query.setQuery("select DISTINCT ON (ldsa) latitude, longitude, station, ldsa from geo_osfpm.geo_osfpm_tl where ldsa<=2000 and station=45 LIMIT 3800");
	   // You can declare that your data set is sparse
	   // query.setSparseData(true);
	   Instances data_1 = query.retrieveInstances();
	   query.setQuery("select DISTINCT ON (ldsa) latitude, longitude, station, ldsa from geo_osfpm.geo_osfpm_tl where ldsa<=2000 and station=54 LIMIT 3800");
	   Instances data_2=query.retrieveInstances();
	   Instances data = new Instances(data_1, 7676);
	   for(int i=0;i<data_1.numInstances();i++)
	   {
		   data.add(data_1.get(i));
		   
	   }
	   for(int j=0;j<data_2.numInstances();j++)
	   {
		   data.add(data_2.get(j));
		   
	   }
   	    return data;
	   
   }
   
   }