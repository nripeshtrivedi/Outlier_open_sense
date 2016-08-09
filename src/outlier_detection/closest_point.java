package outlier_detection;

import java.sql.*; 
import java.util.*; 
import java.lang.*; 
import org.postgis.*;
import org.postgresql.util.PGobject; 

public class closest_point { 
	
	static double latitude;
	static double longitude;
	static double pk;
	static Connection conn=null;
	static int first=0;
	static double station=0;
    static StringBuffer sBuffer;
    static double ldsa;
    static long timed=0L;
public static void closest_point_insert(double val[]) { 


  try { 
    /* 
    * Load the JDBC driver and establish a connection. 
    */
     if(conn==null)
     {
    conn =  PostgreSQLlocal.PostgreSQLlocal();
     }
     
    /* 
    * Add the geometry types to the connection. Note that you 
    * must cast the connection to the pgsql-specific connection 
    * implementation before calling the addDataType() method. 
    */
    pk=val[0];
    latitude=val[4];
    longitude=val[5];
    station=val[2];
    ldsa=val[3];
    timed=(long) val[1];
    PGobject geom=new PGobject(); 
    ((org.postgresql.PGConnection)conn).addDataType("geometry",(Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
    ((org.postgresql.PGConnection)conn).addDataType("box3d",(Class<? extends PGobject>) Class.forName("org.postgis.PGbox3d"));
    /* 
    * Create a statement and execute a select query. 
    */ 
    String insert="POINT("+longitude+" "+latitude+")";
    String select_all="Select geo_osfpm.ST_AsText(tl.geom) as text from geo_osfpm.tl";
    PreparedStatement ps_select_all = conn.prepareStatement(select_all);
    ResultSet r_select_all=ps_select_all.executeQuery();

    
    	PreparedStatement ps_close_point = conn.prepareStatement("SELECT geo_osfpm.ST_AsText(geo_osfpm.ST_ClosestPoint(geo_osfpm.ST_GeomFromText(geo_osfpm.ST_AsText(tl.geom)),geo_osfpm.ST_GeomFromText(?))) from geo_osfpm.tl;");
    
    	ps_close_point.setString(1, insert);
    	ResultSet r_close_point=ps_close_point.executeQuery();
    	r_close_point.next();
    	
    	sBuffer= new StringBuffer(r_close_point.getString(1));
    System.out.println("Point Inserted");
    String sql = "INSERT INTO geo_osfpm.geo_osfpm_tl (pk, latitude, longitude, station,ldsa, timed) VALUES (?, geo_osfpm.ST_Y(geo_osfpm.ST_GeomFromText(?)) ,geo_osfpm.ST_X(geo_osfpm.ST_GeomFromText(?)), ?, ?, ?); ";
    PreparedStatement ps_sql = conn.prepareStatement(sql);
    ps_sql.setDouble(1,pk);
    ps_sql.setString(2,sBuffer.toString());
    ps_sql.setString(3,sBuffer.toString());
    ps_sql.setObject(4,station);
    ps_sql.setDouble(5,ldsa);
    ps_sql.setLong(6,timed);
    ps_sql.executeUpdate();
    ps_sql.close(); 
    sBuffer=null;
   
  //} 
  }
catch( Exception e ) { 
  e.printStackTrace(); 
  } 
} 
}