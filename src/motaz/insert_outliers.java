package motaz;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import motaz.util.DataObject;
import motaz.util.Database;
import motaz.util.ds.PriorityQueueElement;
import weka.core.Instances;
import outlier_detection.PostgreSQLlocal;
import outlier_detection.closest_point;

public class insert_outliers {

	static int index;
	static double vals[];
	static Connection connection;
	static double pk;
	static double longitude;
	static double latitude;
	static double ldsa;
	static double timed;
	static double station;
	static double outlier;
	
	public static void Insert_outliers(double pk_list[],List top, Database database) throws SQLException
	{
		{
			outlier=0.0;
			System.out.println(top.size());
			System.out.println(database.size());
			for(int i = 0 ; i< database.size(); i++)
			{
				DataObject COFdataObject = (DataObject) database.getDataObject(Integer.toString(i));
	            for(int j=0;j<top.size();j++)
	            {
	            	 PriorityQueueElement pqe = ( PriorityQueueElement ) top.get(j);
	                 DataObject dataObj = (DataObject) pqe.getObject();
	                 if(dataObj.equals(COFdataObject))
	                 {
	                	 outlier=1.0;
	                	 break;
	                 }
	            }
	            pk=pk_list[i];
	            latitude=COFdataObject.getInstance().value(0);
	        	longitude=COFdataObject.getInstance().value(1);
	        	station=Integer.parseInt(COFdataObject.getInstance().stringValue(database.getInstances().attribute("station")));
	        	ldsa=COFdataObject.getInstance().value(3);
	        	if(connection==null)
	        	{
	        	 connection=PostgreSQLlocal.PostgreSQLlocal();
	        	}
	        	
	        	
	        	PreparedStatement ps_sql = connection.prepareStatement("INSERT INTO geo_osfpm.geo_osfpm_outlier (pk, latitude, longitude, ldsa,station, outlier) VALUES (?, ? ,?, ?, ?, ?) ;");
	        	ps_sql.setDouble(1,pk);
	      	    ps_sql.setDouble(2,latitude);
	      	    ps_sql.setDouble(3,longitude);
	      	    ps_sql.setDouble(4,ldsa);
	      	    ps_sql.setObject(5, station);
	      	    ps_sql.setDouble(6, outlier);
	      	    ps_sql.executeUpdate();
	      	    ps_sql.close();
	      	    outlier=0.0;
	}

}
	}

}
