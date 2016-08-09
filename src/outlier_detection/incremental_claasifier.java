package outlier_detection;


import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;
import weka.core.converters.DatabaseLoader;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.lazy.KStar;
import weka.classifiers.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import weka.classifiers.meta.*;
import weka.classifiers.trees.HoeffdingTree;
import weka.core.*;
import java.util.*;
import java.io.*;
import java.util.Date;
import java.sql.*;


import java.text.SimpleDateFormat;


	
	public class incremental_claasifier {
		 	Statement st = null;
	        static ResultSet rs = null;
	        static PreparedStatement preparedStatement = null;
	        static int check=1;
	        static boolean first=true;
            static int i=0;
            static double vals[];
            static int count=0;
            static boolean outlier;
            static int outlier_value;
            static Instances newData=null;
            static boolean file_exists;
            static StringBuffer sBuffer;
            
	  /**   
	   * Expects an ARFF file as first argument (class attribute is assumed
	   * to be the last attribute).
	   *
	   * @param args        the commandline arguments
	   * @throws Exception  if something goes wrong
	   */
            public static void build_save_classifier() throws Exception {
        	    // load data
        	    

        	    // train Kstar
        		//  HoeffdingTree h_tree=read_classifier();
        		  InstanceQuery query = new InstanceQuery();
        		   query.setUsername("postgres");
        		   query.setPassword("aaa");
        		   query.setQuery("SELECT latitude , longitude, ldsa, station, outlier from geo_osfpm.geo_osfpm_outlier where station=45 or station=54");
        		   
        		   // You can declare that your data set is sparse
        		   // query.setSparseData(true);
        		   Instances data = query.retrieveInstances();
        		
        		  
        		  HoeffdingTree h_tree=null;
        		  File f = new File("45_54/HoeffdingTree.model");
        		  Instances read_instances=null;
        		  NumericToNominal convert= new NumericToNominal();
        	        String[] options= new String[2];
        	        options[0]="-R";
        	        options[1]="4-5";  //range of variables to make nominal

        	        convert.setOptions(options);
        	        convert.setInputFormat(data);
        	        Instances newData=Filter.useFilter(data, convert);
        	       
          	        newData.setClassIndex(4);
          	       
          	      
          	     
          	      
        	       
        		  if(f.exists() && !f.isDirectory()) { 
        			  file_exists=true;
        			  if(file_exists)
        			  {
        			  h_tree=read_classifier();
        		  }
        		 }
        		
        		  for(int i=0;i<newData.numInstances();i++)
        		  {
        			  Instance current=newData.get(i);
        			  if((h_tree!=null))
        			  {
        	        	  h_tree.updateClassifier(current);  
        			  }
        			  else
        			  {
        				     h_tree=new HoeffdingTree();	
        	        		 h_tree.buildClassifier(newData);
        	        		 break;
        			  }
        		  }
             	       GenerateROC.area(h_tree, newData);
             	      ObjectOutputStream oos = new ObjectOutputStream(
                              new FileOutputStream("45_54/HoeffdingTree.model"));
                                 oos.writeObject(h_tree);
                                 oos.flush();
                                 oos.close();
        	      
        	        }

	        

	
	
	  public static HoeffdingTree read_classifier() throws Exception 
	  {
		  ObjectInputStream ois = new ObjectInputStream(
                  new FileInputStream("HoeffdingTree.model"));
		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
           ois.close();
           return h_tree;
	  }
	  public static void make_prediction(Instance instance, KStar k) throws Exception
 	  {  HoeffdingTree h_tree=read_classifier();
		 
	  }
	  public static void updateclassifier() throws Exception, IOException
	  {
		  int station=0;
		  double ldsa;
		  double latitude;
		  double longitude;
	     Instances new_insert_1;
		  double cls[];
		  String query="SELECT latitude, longitude, station, ldsa FROM geo_osfpm where ldsa<=2000 ANd ldsa >=1 LIMIT 2";
		  Connection conn_host = PostgreSQLlocal.PostgreSQLhost();
		  preparedStatement =  conn_host.prepareStatement(query);
		  rs = preparedStatement.executeQuery();
		  InstanceQuery query_1 = new InstanceQuery();
		 
		
      	  
      	 Attribute Attribute1 = new Attribute("latitude");
      	 Attribute Attribute2 = new Attribute("longitude");
      	
      	 FastVector fvWekaAttributes = new FastVector(4);
      	 fvWekaAttributes.addElement(Attribute1);
      	 fvWekaAttributes.addElement(Attribute2);

      	Instances data_1=new Instances("test",fvWekaAttributes,0);
       // Instances data_1 = query_1.retrieveInstances();
    	data_1.insertAttributeAt(new Attribute("ldsa"), data_1.numAttributes());
    
           
	 
		
		
	        
	
		Connection conn_local=PostgreSQLlocal.PostgreSQLlocal();
		
		while(rs.next()) 
		{
			 station = rs.getInt("station");
			List my_nominal_values = new ArrayList(2); 
	    	 if(station==41||station==47)
	    	 {
	    		 my_nominal_values.add("41");
	    		 my_nominal_values.add("47");
	    	 }
	    	 else if(station==43||station==55)
	    	 {
	    		 my_nominal_values.add("43"); 
	    		 my_nominal_values.add("55");
	    	 }
	    	 else if(station==45||station==54)
	    	 {
	    		 my_nominal_values.add("45"); 
	    		 my_nominal_values.add("54");
	    		 
	    	 }
	    	 else if(station==49||station==50)
	    	 {
	    		 my_nominal_values.add("49"); 
	    		 my_nominal_values.add("50"); 
	    	 }
	    	 else
	    	 {
	    		 my_nominal_values.add("48"); 
	    		 my_nominal_values.add("51"); 
	    	 }
	         
	      
	        	
	        	
	         
	        	data_1.insertAttributeAt(new Attribute("station", my_nominal_values), data_1.numAttributes());
	        	
	        	List outlier_values = new ArrayList(2);
	        	outlier_values.add("0.0"); 
	        	outlier_values.add("1.0"); 
	        	data_1.insertAttributeAt(new Attribute("outlier", outlier_values), data_1.numAttributes());
	            data_1.setClassIndex(4); 
	        	
	        	 ldsa = rs.getDouble("ldsa");
		        	latitude=rs.getDouble("latitude");
		        	longitude=rs.getDouble("longitude");
		        	String insert="POINT("+longitude+" "+latitude+")";
		        	PreparedStatement ps_close_point = conn_local.prepareStatement("SELECT geo_osfpm.ST_AsText(geo_osfpm.ST_ClosestPoint(geo_osfpm.ST_GeomFromText(geo_osfpm.ST_AsText(tl.geom)),geo_osfpm.ST_GeomFromText(?))) from geo_osfpm.tl;");
		            
		        	ps_close_point.setString(1, insert);
		        	ResultSet r_close_point=ps_close_point.executeQuery();
		        	r_close_point.next();
		        	
		        	sBuffer= new StringBuffer(r_close_point.getString(1));
		        	PreparedStatement ps_close_point_1 = conn_local.prepareStatement("SELECT geo_osfpm.ST_X(geo_osfpm.ST_GeomFromText(?)) as longitude, geo_osfpm.ST_Y(geo_osfpm.ST_GeomFromText(?)) as latitude;");
		        	ps_close_point_1.setString(1, sBuffer.toString());
		        	ps_close_point_1.setString(2, sBuffer.toString());
		        	ResultSet r_close_point_1=ps_close_point_1.executeQuery();
		        	r_close_point_1.next();
		        	double latitude_1=r_close_point_1.getDouble("latitude");
		        	double longitude_1=r_close_point_1.getDouble("longitude");
		        	Instance inst = new DenseInstance(5);
		        	inst.setDataset(data_1);
		        	inst.setValue(0, latitude_1);
		            inst.setValue(1, longitude_1); 
		        	 
		        	inst.setValue(2, ldsa);
		        	
		        	inst.setValue(3, Integer.toString(station));
		        
		        	inst.setClassMissing();
	        	 if(station==45||station==54)
	        	 {
	        		  ObjectInputStream ois = new ObjectInputStream(
	                          new FileInputStream("45_54/HoeffdingTree.model"));
	        		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
	        		  cls=h_tree.distributionForInstance(inst);
	        		  
	        		  System.out.println(cls[0]+"  "+cls[1]);
	        		  if(cls[0]>=0.5)
	  	        	{
	  	        		
	  	        		inst.setValue(4, "0.0");
	  	        	}
	  	        	else
	  	        	{
	  	        		inst.setValue(4, "1.0");
	  	        	}
	        		  h_tree.updateClassifier(inst);
	        		  ObjectOutputStream oos = new ObjectOutputStream(
                              new FileOutputStream("45_54/HoeffdingTree.model"));
                                 oos.writeObject(h_tree);
                                 oos.flush();
                                 oos.close();
	        	 }
	        	 else if(station==43||station==55)
	        	 {
	        		 ObjectInputStream ois = new ObjectInputStream(
	                          new FileInputStream("43_55/HoeffdingTree.model"));
	        		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
	        		  cls=h_tree.distributionForInstance(inst);
	        		  System.out.println(cls[0]+"  "+cls[1]);
	        		  if(cls[0]>=0.5)
		  	        	{
		  	        		
		  	        		inst.setValue(4, "0.0");
		  	        	}
		  	        	else
		  	        	{
		  	        		inst.setValue(4, "1.0");
		  	        	}
		        		  h_tree.updateClassifier(inst);
		        		  ObjectOutputStream oos = new ObjectOutputStream(
	                              new FileOutputStream("43_55/HoeffdingTree.model"));
	                                 oos.writeObject(h_tree);
	                                 oos.flush();
	                                 oos.close();
	        		  
	        	 }
	        	 else if(station==41||station==47)
	        	 {
	        		 ObjectInputStream ois = new ObjectInputStream(
	                          new FileInputStream("41_47/HoeffdingTree.model"));
	        		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
	        		  cls=h_tree.distributionForInstance(inst);
	        		  System.out.println(cls[0]+"  "+cls[1]);
	        		
	        		  if(cls[0]>=0.5)
		  	        	{
		  	        		
		  	        		inst.setValue(4, "0.0");
		  	        	}
		  	        	else
		  	        	{
		  	        		inst.setValue(4, "1.0");
		  	        	}
		        		  h_tree.updateClassifier(inst);
		        		  ObjectOutputStream oos = new ObjectOutputStream(
	                              new FileOutputStream("41_47/HoeffdingTree.model"));
	                                 oos.writeObject(h_tree);
	                                 oos.flush();
	                                 oos.close();
	        	 }
	        	 else if(station==48||station==51)
	        	 {
	        		 ObjectInputStream ois = new ObjectInputStream(
	                          new FileInputStream("48_51/HoeffdingTree.model"));
	        		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
	        		  cls=h_tree.distributionForInstance(inst);
	        		  System.out.println(cls[0]+"  "+cls[1]);
	        		 
	        		  if(cls[0]>=0.5)
		  	        	{
		  	        		
		  	        		inst.setValue(4, "0.0");
		  	        	}
		  	        	else
		  	        	{
		  	        		inst.setValue(4, "1.0");
		  	        	}
		        		  h_tree.updateClassifier(inst);
		        		  ObjectOutputStream oos = new ObjectOutputStream(
	                              new FileOutputStream("48_51/HoeffdingTree.model"));
	                                 oos.writeObject(h_tree);
	                                 oos.flush();
	                                 oos.close();
	        	 }
	        	 else
	        	 {
	        		 ObjectInputStream ois = new ObjectInputStream(
	                          new FileInputStream("49_50/HoeffdingTree.model"));
	        		  HoeffdingTree h_tree = (HoeffdingTree) ois.readObject();
	        		  cls=h_tree.distributionForInstance(inst);
	        		  System.out.println(cls[0]+"  "+cls[1]);
	        		 
	        		  if(cls[0]>=0.5)
		  	        	{
		  	        		
		  	        		inst.setValue(4, "0.0");
		  	        	}
		  	        	else
		  	        	{
		  	        		inst.setValue(4, "1.0");
		  	        	}
		        		  h_tree.updateClassifier(inst);
		        		  ObjectOutputStream oos = new ObjectOutputStream(
	                              new FileOutputStream("49_50/HoeffdingTree.model"));
	                                 oos.writeObject(h_tree);
	                                 oos.flush();
	                                 oos.close();
	        		 
	        	 }
	        	
	        	
	        	data_1.setClassIndex(2);
	        	data_1.deleteAttributeAt(4);
	        	data_1.deleteAttributeAt(3);
	        	
	        	
	        	
	        
		  }
		
	        	
	  	        
	        	
	  	        
	  }
	  
		  
	  
	  }
	


