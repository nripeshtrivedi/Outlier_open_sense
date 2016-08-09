/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    Copyright (C) 2006
 *    & Motaz K. Saad (motaz.saad@gmail.com) Islamic University - Gaza - Palestine
 */

/**
 *
 * @author [motaz.saad] Motaz K. Saad (motaz.saad@gmail.com)
 */

package motaz;
import motaz.util.Database;
import motaz.util.DataObject;
import weka.core.*;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.filters.unsupervised.attribute.Normalize; //[0,1]
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.instance.RemoveWithValues;
import outlier_detection.PostgreSQLlocal;
import motaz.util.ds.PriorityQueue;
import motaz.util.ds.PriorityQueueElement;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.ArrayList;


import  java.util.*;
import  java.io.*;
import  weka.core.*;
import  weka.filters.Filter;




public class CODB  {
    
    /**
     * Specifies the number of the Nearest Neighbors
     */
    private int K = 7;
    
    /**
     * Specifies the top N COF 
     */
    private double topN;
    
    /**
     * Specifies the Alpha Coeffeciant 
     */
    private double alpha = 1000;
    
    /**
     * Specifies the Beta Coeffeciant 
     */
    private double beta = 0.1;
    
    /**
     * Specifies the Justifiction Option 
     */
    private boolean Justification = false;
    
    /**
     * Holds the 

-type that is used
     * (default = motaz.util.EuclidianDataObject)
     */
    private String database_distanceType = "motaz.util.EuclidianDataObject";
    
    /**
     * Replaces all missing values for nominal and numeric attributes in 
     * a dataset with the modes and means from the training data.
     */
    private ReplaceMissingValues replaceMissingValues_Filter;
    
    /**
     * Normalizing numeric attributes to [0.0, 1.0]
     */
    private Normalize normalize;
    
    /**
     * Holds the type of the used database
     * (default = motaz.util.SequentialDatabase)
     */
    private String database_Type = "motaz.util.SequentialDatabase";

    /**
     * The database that is used
     */
    private Database database;
    
    /**
     * The database that is used
     */
    private Database db;
    
    /**
     * Specify the ReplaceMissingValues option
     */
    private boolean ReplaceMissingValues = false;
    
    /**
     * Specify the RemoveWithValues option
     */
    private boolean RemoveWithValues = false;
    
    private boolean ls = false;
    
    private boolean print = false;
    
    public static double pk_list[];
    public static double top_list[];
    Instances newData;
    
    /**
     * Holds the time-value (seconds) for the duration of the class outlier 
     * detection process
     */
    private double elapsedTime;

    // *****************************************************************************************************************
    // constructors
    // *****************************************************************************************************************  
    // *****************************************************************************************************************

    // *****************************************************************************************************************
    // methods
    
    
    public String GO(String[] options) throws Exception {
        
    Instances train = null;
    String attributeRangeString;
    Range attributesToOutput = null;
    StringBuffer text = new StringBuffer();
    int theClass = -1; // No class attribute assigned to instances
    
	train = PostgreSQLlocal.readdata();  
    theClass = train.numAttributes()-1;
    train.setClassIndex(theClass);
        
    
    
    System.out.println("succesfully read data");
    
    Utils.checkForRemainingOptions(options);
 
    buildCODB(train);
   
  
    text.append(toString());
    
    return  text.toString();
    }
    
    // *****************************************************************************************************************

    /**
     * prepair the data:remove instances with missing value or replace missing values (if specified)
     * perform normalization for numeric attributes
     * @param instances The instances that need to be detected for class outliers
     * @throws java.lang.Exception If clustering was not successful
     */
    public void buildCODB(Instances instances) throws Exception {
                
        if (instances.checkForStringAttributes()) {
            throw new Exception("Can't handle string attributes!");
        }
        
       
        NumericToNominal convert= new NumericToNominal();
        String[] options= new String[2];
        options[0]="-R";
        options[1]="3";  //range of variables to make nominal

        convert.setOptions(options);
        convert.setInputFormat(instances);

        newData=Filter.useFilter(instances, convert);
        newData.setClassIndex(3);
        Instances filteredInstances = newData;
        System.out.println(newData.attribute(2).isNominal());
       
        database = databaseForName(getDatabase_Type(), filteredInstances);
        for (int i = 0; i < database.getInstances().numInstances(); i++)
        {
        	
        }
        for (int i = 0; i < database.getInstances().numInstances(); i++) {
            DataObject dataObject = dataObjectForName(getDatabase_distanceType(),
                    database.getInstances().instance(i),
                    Integer.toString(i),
                    database);
           
            database.insert(dataObject);
           
            
            
        }
        pk_list=new double[database.size()];
        //Get the max row 
       
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        String query="select max(pk) as ind from geo_osfpm.geo_osfpm_outlier";
        preparedStatement =  PostgreSQLlocal.PostgreSQLlocal().prepareStatement(query);
        rs = preparedStatement.executeQuery();
        rs.next();
        int pk=rs.getInt("ind");
        System.out.println(database.size());
        for(int i=0;i<database.size();i++)
        {
        	DataObject dataObject = dataObjectForName(getDatabase_distanceType(),
                    database.getInstances().instance(i),
                    Integer.toString(i),
                    database);
        	pk_list[i]=pk+i+1;;
        	
        }
        setTopN((database.size()/(5)));
        database.setMinMaxValues();
        System.out.println("Inserted Values");
    }

    
    
    
    /**
     * Returns an enumeration of all the available options..
     *
     * @return Enumeration An enumeration of all available options.
     */
  /*  public Enumeration listOptions() {
        Vector vector = new Vector();

        vector.addElement(
                new Option("\tepsilon (default = 1.5)",
                        "E",
                        1,
                        "-E <double>"));
        vector.addElement(
                new Option("\tTop N COF (default = 10)",
                        "n",
                        1,
                        "-n <double>"));
        vector.addElement(
                new Option("\tK (default = 7)",
                        "m",
                        1,
                        "-m <int>"));
        vector.addElement(
                new Option("\tindex (database) used for DBScan (default = weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase)",
                        "I",
                        1,
                        "-I <String>"));
        vector.addElement(
                new Option("\tdistance-type (default = weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject)",
                        "D",
                        1,
                        "-D <String>"));
        return vector.elements();
    }*/
    
    /**
     * Returns an enumeration of all the available options..
     *
     * @return Enumeration An enumeration of all available options.
     */
  /*  public String help() {
        StringBuffer strBuf = new StringBuffer();
        
        strBuf.append("CODB Algorithm\nInputs:\n");
        strBuf.append(
                "Data set file name (required) -t <String>\n");
        strBuf.append(
                "Top N COF (default = 10) -n <double>\n");
        strBuf.append(
                "k nearest neighbors (default = 7) -k <int>\n");
        strBuf.append(
                "Alpha coeffecient (default = 100) -a <double>\n");
        strBuf.append(
                "Beta coeffecient (default = 0.1) -b <double>\n");
        strBuf.append(
                "distance-type (default = motaz.util.EuclidianDataObject) -D <String>\n");
        strBuf.append(
                "Replace Missing Vaules (default = false) -r \n");
        strBuf.append(
                "Remove Missing Vaules (default = false) -m \n");
        return strBuf.toString();
    }*/
    
    

    /**
     * Sets the OptionHandler's options using the given list. All options
     * will be set (or reset) during this call (i.e. incremental setting
     * of options is not possible).
     *
     * @param options The list of options as an array of strings
     * @exception java.lang.Exception If an option is not supported
     */
    public void setOptions() throws Exception {
        
   
            setK(this.K);
        

        
            setDatabase_Type(this.database_Type);
        

        
            setDatabase_distanceType(this.database_distanceType);
        
        
   
            setTopN(this.topN);
        
        
        
            setAlpha(this.alpha);
        
        
        
            setBeta(this.beta);
        
        
        setJustification(this.Justification);
        
        setRemoveWithValues(this.RemoveWithValues);
        
        setReplaceMissingValues(this.ReplaceMissingValues);
        
        setLs(this.ls);
        
        setPrint(this.print);
        
    }

    /**
     * Gets the current option settings for the OptionHandler.
     *
     * @return String[] The list of current option settings as an array of strings
     */
   /* public String[] getOptions() {
        String[] options = new String[17];
        int current = 0;

        options[current++] = "-k";
        options[current++] = "" + getK();
        options[current++] = "-I";
        options[current++] = "" + getDatabase_Type();
        options[current++] = "-D";
        options[current++] = "" + getDatabase_distanceType();
        options[current++] = "-n";
        options[current++] = "" + getTopN();
        options[current++] = "-a";
        options[current++] = "" + getAlpha();
        options[current++] = "-b";
        options[current++] = "" + getBeta();
        if (getJustification()) 
            options[current++] = "-j";
        else
            options[current++] = "";
        options[current++] = "-m";
        options[current++] = "" + getRemoveWithValues();
        options[current++] = "-r";
        options[current++] = "" + getReplaceMissingValues();
        options[current++] = "-p";
        options[current++] = "" + getPrint();


        return options;
    }*/

    /**
     * Returns a new Class-Instance of the specified database
     * @param database_Type String of the specified database
     * @param instances Instances that were delivered from WEKA
     * @return Database New constructed Database
     */
    public Database databaseForName(String database_Type, Instances instances) {
        Object o = null;

        Constructor co = null;
        try {
            co = (Class.forName(database_Type)).getConstructor(new Class[]{Instances.class});
            o = co.newInstance(new Object[]{instances});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
        	
  
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return (Database) o;
    }

    /**
     * Returns a new Class-Instance of the specified database
     * @param database_distanceType String of the specified distance-type
     * @param instance The original instance that needs to hold by this DataObject
     * @param key Key for this DataObject
     * @param database Link to the database
     * @return DataObject New constructed DataObject
     */
    

    public List Pcl (Instances insts) {
       
        int tc = insts.attributeStats(insts.numAttributes()-2).totalCount;
      
        int []nomC = insts.attributeStats(insts.numAttributes()-2).nominalCounts; // For Station
       
        double freq = 0.0;
        List l = new ArrayList();
        for (int i = 0 ; i < nomC.length ; i++) {
            freq = nomC[i] / (double) tc;
           
            l.add(freq);
        }
     
        return l;
    }

    public DataObject dataObjectForName(String database_distanceType, Instance instance, String key, Database database) {
        Object o = null;

        Constructor co = null;
        try {
            co = (Class.forName(database_distanceType)).
                    getConstructor(new Class[]{Instance.class, String.class, Database.class});
            o = co.newInstance(new Object[]{instance, key, database});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return (DataObject) o;
    }

    /**
     * Sets a new value for K
     * @param Integer K
     */
    public void setK(int K) {
        this.K = K;
    }
    
    /**
     * Sets a new value for ls
     * @param Boolean ls
     */
    public void setLs(boolean ls) {
        this.ls = ls;
    }
    
    /**
     * Sets a new value for print
     * @param Boolean print
     */
    public void setPrint(boolean print) {
        this.print = print;
    }
    
    /**
     * Sets a new value for RemoveWithValues
     * @param boolean RemoveWithValues
     */
    public void setRemoveWithValues(boolean RemoveWithValues) {
        this.RemoveWithValues = RemoveWithValues;
    }
    
    /**
     * Sets a new value for ReplaceMissingValues
     * @param boolean ReplaceMissingValues
     */
    public void setReplaceMissingValues(boolean ReplaceMissingValues) {
        this.ReplaceMissingValues = ReplaceMissingValues;
    }
    
    
    public void setTopN(double topN) {
        this.topN = topN;
    }
    
    
    public void setAlpha(double alpha2) {
        this.alpha = alpha2;
    }
    
    public void setBeta(double beta) {
        this.beta = beta;
    }
    
    
   public void setJustification(boolean Justification) {
        this.Justification = Justification;
    }

    /**
     * Returns the value of K
     * @return int K
     */
    public int getK() {
        return K;
    }
    
    /**
     * Returns the value of ls
     * @return boolean ls
     */
    public boolean getLs() {
        return ls;
    }
    
    /**
     * Returns the value of RemoveWithValues
     * @return boolean RemoveWithValues
     */
    public boolean getRemoveWithValues() {
        return RemoveWithValues;
    }
    
    /**
     * Returns the value of ReplaceMissingValues
     * @return boolean ReplaceMissingValues
     */
    public boolean getReplaceMissingValues() {
        return ReplaceMissingValues;
    }

    /**
     * Returns the distance-type
     * @return String Distance-type
     */
    public String getDatabase_distanceType() {
        return database_distanceType;
    }

    /**
     * Returns the type of the used index (database)
     * @return String Index-type
     */
    public String getDatabase_Type() {
        return database_Type;
    }
    
    public double getTopN() {
        return topN;
    }
    
    public double getAlpha() {
        return alpha;
    }
    
    public double getBeta() {
        return beta;
    }
    
    public boolean getJustification() {
        return Justification;
    }
    
    public boolean getPrint() {
        return print;
    }
    
    

    /**
     * Sets a new distance-type
     * @param database_distanceType The new distance-type
     */
    public void setDatabase_distanceType(String database_distanceType) {
        this.database_distanceType = database_distanceType;
    }

    /**
     * Sets a new database-type
     * @param database_Type The new database-type
     */
    public void setDatabase_Type(String database_Type) {
        this.database_Type = database_Type;
    }

    /**
     * Returns the tip text for this property
     * @return tip text for this property suitable for
     * displaying in the explorer/experimenter gui
     */
    public String epsilonTipText() {
        return "radius of the epsilon-range-queries";
    }

    /**
     * Returns the tip text for this property
     * @return tip text for this property suitable for
     * displaying in the explorer/experimenter gui
     */
    public String minPointsTipText() {
        return "minimun number of DataObjects required in an epsilon-range-query";
    }

    /**
     * Returns the tip text for this property
     * @return tip text for this property suitable for
     * displaying in the explorer/experimenter gui
     */
    public String database_TypeTipText() {
        return "used database";
    }

    /**
     * Returns the tip text for this property
     * @return tip text for this property suitable for
     * displaying in the explorer/experimenter gui
     */
    public String database_distanceTypeTipText() {
        return "used distance-type";
    }

    /**
     * Returns a string describing this DataMining-Algorithm
     * @return String Information for the gui-explorer
     */
    public String globalInfo() {
        return " Ester M., Kriegel H.-P., Sander J., Xu X.: A Density-Based Algorithm for Discovering"+
        " Clusters in Large Spatial Databases with Noise, Proc. 2nd Int. Conf. on Knowledge Discovery "+
        "and Data Mining (KDD'96), Portland, OR, 1996, pp. 226-231.";
    }
    
    /*---------------------------------------------------------------------------------*/
    /*---------------------------------------------------------------------------------*/
   /* public List Pcl (Instances insts) {
       
        int tc = insts.attributeStats(insts.numAttributes()-1).totalCount;
        int []nomC = insts.attributeStats(insts.numAttributes()-1).nominalCounts;
        double freq = 0.0;
        List l = new ArrayList();

            freq = nomC[i] / (double) tc;
            l.add(freq);
        }
        
        return l;
    }*/
    
    
    
    public Instances dataObjectsClassLableSubset (int clIndx) {
        Instances insts = database.getInstances();
        insts.delete();
        for (int i = 0; i < database.size(); i++) {
            DataObject dataObject = (DataObject) database.getDataObject(Integer.toString(i));
            int attIndex = dataObject.getInstance().numAttributes()-1;
            if ( (int) dataObject.getInstance().value(attIndex) == clIndx)
                insts.add(dataObject.getInstance());
        }       
        return insts;
    }
    
    public Instances dataObjectsKnnSubset (DataObject dataObject) {
        Instances insts = database.getInstances();
        insts.delete();
        List knn = new ArrayList();
        
        knn = myknn(getK(), dataObject);
        
        for (int i = 0; i < knn.size(); i++) {
            PriorityQueueElement pqe = ( PriorityQueueElement ) knn.get(i);
            DataObject knndataObject = (DataObject) pqe.getObject();
            insts.add(knndataObject.getInstance());
        }
        return insts;
    }
    
        
    public double Deviation(DataObject dataObject) {
        
        double dist = 0.0;
        for (int i = 0; i < database.size(); i++) {
            DataObject dataObj = (DataObject) database.getDataObject(Integer.toString(i));
            //int attIndex = dataObject.getInstance().numAttributes()-2;
            //int clIndx = (int) dataObj.getInstance().value(attIndex);
           
            if ( dataObject.getInstance().stringValue(database.getInstances().attribute("station")) == dataObj.getInstance().stringValue(database.getInstances().attribute("station")))
                dist += dataObject.distance(dataObj);        
        }
        
        return dist;
    }
    
    public double KDist(DataObject dataObject) {
        
        double dist = 0.0;
        
        List avgKnn = myknn( getK(),  dataObject);
        for (int i = 0; i < avgKnn.size(); i++) {
            
            PriorityQueueElement pqe = ( PriorityQueueElement ) avgKnn.get(i);
            DataObject dataObj = (DataObject) pqe.getObject();
                
             dist += dataObj.distance(dataObject);
        }
        
        return dist;
    }
    
   /* public double myCOF_RankTest(DataObject dataObject) {
        Double PclKnn, Sim;
        Integer indx;
        
        indx = (int) dataObject.getInstance().value(dataObject.getInstance().numAttributes()-1);
              
        List knnFreq = Pcl(dataObjectsKnnSubset(dataObject));
        PclKnn = (Double) knnFreq.get(indx);
        
        
        Sim = Deviation(dataObject);
        
        //double myCOF_Rank = PclC * Sim / PclD;
        double myCOF_Rank = PclKnn;
        
        return myCOF_Rank;
    }*/
    
    public double COF_Rank(DataObject dataObject) {
        Double PclKnn, Deviation, KDist;
        Integer indx;
        
        indx = (int) dataObject.getInstance().value(dataObject.getInstance().numAttributes()-2);
             
        List knnFreq = Pcl(dataObjectsKnnSubset(dataObject));
        PclKnn = (Double) knnFreq.get(0) * getK();
        
        Deviation = Deviation(dataObject);
       
        
        KDist = KDist(dataObject);
        
        //double myCOF_Rank = PclC * Sim / PclD;
        double myCOF_Rank =  PclKnn + alpha / Deviation+beta * KDist;
       
        return myCOF_Rank;
    }
    
    public List COF_RankFactors(DataObject dataObject) {
        Double PclKnn, Deviation, KDist;
        Integer indx;
        List COF_Factors = new ArrayList();
        
        indx = (int) dataObject.getInstance().value(dataObject.getInstance().numAttributes()-2);
             
       List knnFreq = Pcl(dataObjectsKnnSubset(dataObject));
      
     
       PclKnn = (Double) knnFreq.get(0) * getK();
        
        
        Deviation = Deviation(dataObject);
        
        KDist = KDist(dataObject);
        
       COF_Factors.add(PclKnn);
        COF_Factors.add(Deviation);
        COF_Factors.add(KDist);
        
        
        //double myCOF_Rank = PclC * Sim / PclD;
        //double myCOF_Rank = PclKnn + alpha / Deviation + beta * KDist;
        
        return COF_Factors;
    }
    
 /*   public List myCOF_RankAllTest() {
        
        long time_1 = System.currentTimeMillis();
        
        PriorityQueue priorityQueue = new PriorityQueue();
        List topCOF = new ArrayList();
        for (int i = 0; i < database.size(); i++) {
            DataObject dataObject = (DataObject) database.getDataObject(Integer.toString(i));
                double COF = myCOF_RankTest(dataObject);
                if ( priorityQueue.size() < getTopN() ) {
                    priorityQueue.add(COF, dataObject); 
               }
                else {
                    if (COF < priorityQueue.getPriority(0)) {
                        priorityQueue.next(); //removes the highest COF
                        priorityQueue.add(COF, dataObject);
                    }
                }
        }
        
        while (priorityQueue.hasNext()) {
            topCOF.add(0, priorityQueue.next());
        }
        
        long time_2 = System.currentTimeMillis();
        elapsedTime = (double) (time_2 - time_1) / 1000.0;
        
        return topCOF;
    }
    */
    public List COF_RankAll() {
        
        long time_1 = System.currentTimeMillis();
        
        PriorityQueue priorityQueue = new PriorityQueue();
        List topCOF = new ArrayList();
        for (int i = 0; i < database.size(); i++) {
        	
            DataObject dataObject = (DataObject) database.getDataObject(Integer.toString(i));
            
                double COF = COF_Rank(dataObject);
              
              
                if ( priorityQueue.size() < getTopN() ) {
                    priorityQueue.add(COF, dataObject); 
                }
                else {
                    if (COF < priorityQueue.getPriority(0)) {
                        priorityQueue.next(); //removes the highest COF
                        priorityQueue.add(COF, dataObject);
                    }
                }
        }
        
        while (priorityQueue.hasNext()) {
            topCOF.add(0, priorityQueue.next());
        }
        top_list=new double[topCOF.size()];
        
        for(int i=0;i<topCOF.size();i++)
        {
        	 PriorityQueueElement pqe = ( PriorityQueueElement ) topCOF.get(i);
             DataObject dataObj = (DataObject) pqe.getObject();
        	 top_list[i]=dataObj.getInstance().value(0);
        }
        
        long time_2 = System.currentTimeMillis();
        elapsedTime = (double) (time_2 - time_1) / 1000.0;
        
        return topCOF;
    }
    

    
    /**
     * Emits the k next-neighbours and performs an epsilon-range-query at the parallel.
     * The returned list contains with all k next-neighbours;
     * @param k number of next neighbours     
     * @param dataObject the start object
     * @return list with the k-next neighbours (PriorityQueueElements)
     */
    public List myknn(int k, DataObject dataObject) {
        
        List nextNeighbours_List = new ArrayList();
        
        PriorityQueue priorityQueue = new PriorityQueue();
        
        for (int i = 0; i < database.size(); i++) {
            DataObject next_dataObject = (DataObject) database.getDataObject(Integer.toString(i));
            double dist = next_dataObject.distance_1(dataObject);
             if (priorityQueue.size() < k) {
                priorityQueue.add((dist), next_dataObject);
                
            } else {
                if ((dist) < priorityQueue.getPriority(0)) {
                    priorityQueue.next(); //removes the highest distance
                    priorityQueue.add((dist), next_dataObject);
                }
            }
        }

        while (priorityQueue.hasNext()) {
            nextNeighbours_List.add(0, priorityQueue.next());
        }
        
        return nextNeighbours_List; 
    }
    
    /**
     * Performs an epsilon range query for this dataObject
     * @param epsilon Specifies the range for the query
     * @param queryDataObject The dataObject that is used as query-object for epsilon range query
     * @return List with all the DataObjects that are within the specified range
     */
    public List myEpsilonRangeQuery(double epsilon, DataObject queryDataObject) {
        ArrayList epsilonRange_List = new ArrayList();
        
        for (int i = 0; i < database.size(); i++) {
            DataObject dataObject = (DataObject) database.getDataObject(Integer.toString(i));
            double distance = queryDataObject.distance(dataObject);
            if (distance < epsilon) {
                epsilonRange_List.add(dataObject);
            }
        }

        return epsilonRange_List;
    }

    public StringBuffer report () {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat(".#####");
        //decimalFormat.format(
        for (int i = 0; i < database.size(); i++) {
            DataObject dataObject = (DataObject) database.getDataObject(Integer.toString(i));
            
            stringBuffer.append("(" + Utils.doubleToString(Double.parseDouble(dataObject.getKey()),
                    (Integer.toString(database.size()).length()), 0) + ".) "
                    + Utils.padRight(dataObject.toString(), 69) + "\n");
            
            List knn = myknn(getK(),  dataObject);
            
                        
                      
            stringBuffer.append("=== The "+ getK() + " Nearst Neighbors of the Instance #"+ dataObject.getKey() + " ====\n");
            for (int j = 0; j < knn.size(); j++)
            {
                PriorityQueueElement pqe = ( PriorityQueueElement ) knn.get(j);
                DataObject knndataObject = (DataObject) pqe.getObject();
                stringBuffer.append(knndataObject.getKey() + "\t\t" + knndataObject.toString() + "\t\tDistance: " + decimalFormat.format(pqe.getPriority())  +"\n");

            }
                     
            stringBuffer.append("======================================================================================\n");    
        }
        return stringBuffer;
    }
    
    public StringBuffer topCOFInfo (List topCOF) {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat(".#####");
        DecimalFormat decimalFormat2 = new DecimalFormat(".##");
        
        stringBuffer.append("\n=== Top " + getTopN() + " COF ===\n");
        for (int i = 0 ; i< topCOF.size(); i++) {
            PriorityQueueElement pqe = ( PriorityQueueElement ) topCOF.get(i);
            DataObject COFdataObject = (DataObject) pqe.getObject();
            List COFdetials = COF_RankFactors(COFdataObject);
            stringBuffer.append((i+1) + ". (" + COFdataObject.getKey() + ".)  " + COFdataObject.toString() + "  COF: " + decimalFormat.format(pqe.getPriority())  +"\n");
            stringBuffer.append("PCL = " + COFdetials.get(0) + ", Deviation = " + decimalFormat2.format(COFdetials.get(1)) + ", KDist = " + decimalFormat2.format(COFdetials.get(2)) + "\n");
            stringBuffer.append("------------------------------------------\n");
        }
        
        stringBuffer.append("==========================================\n\n");
        stringBuffer.append("=== Top " + getTopN() + " COF Detailed Informations ===\n\n");
        
   /*     for (int i = 0; i < topCOF.size(); i++) {
            PriorityQueueElement pqe = ( PriorityQueueElement ) topCOF.get(i);
            DataObject dataObject = (DataObject) pqe.getObject();
            
            stringBuffer.append((i + 1) + ". (" + Utils.doubleToString(Double.parseDouble(dataObject.getKey()),
                    (Integer.toString(database.size()).length()), 0) + ".) "
                    + dataObject.toString() + "\n");
            
            List knn = myknn(getK(),  dataObject);
            
                        
                      
           /* stringBuffer.append("=== The " + getK() + " Nearest Neighbors of the Instance #"+ dataObject.getKey() + " ====\n");
            for (int j = 0; j < knn.size(); j++) {
                pqe = ( PriorityQueueElement ) knn.get(j);
                DataObject knndataObject = (DataObject) pqe.getObject();
                stringBuffer.append(knndataObject.

ey() + "  " + knndataObject.toString() + "  Distance: " + decimalFormat.format(pqe.getPriority())  +"\n");

            }
            
            
            stringBuffer.append("-----------------------------------------------------------------------\n");
            stringBuffer.append("-----------------------------------------------------------------------\n");
            
        }
        if (getPrint()) {
            
            for (int i = 0; i < topCOF.size(); i++) {
                
                PriorityQueueElement pqe = ( PriorityQueueElement ) topCOF.get(i);
                DataObject dataObject = (DataObject) pqe.getObject();

                //stringBuffer.append( Integer.parseInt(dataObject.getKey()) + "," + dataObject.toString() + "\n");

                List knn = myknn(getK(),  dataObject);



                stringBuffer.append(dataObject.getKey() + "\n");
                for (int j = 0; j < knn.size(); j++) {
                    pqe = ( PriorityQueueElement ) knn.get(j);
                    DataObject knndataObject = (DataObject) pqe.getObject();
                    stringBuffer.append(knndataObject.getKey() + " " + knndataObject.toString() + " " + decimalFormat.format(pqe.getPriority())  +"\n");

                }
            }
        }*/
        
        return stringBuffer;
    }
    
        
    public StringBuffer topCOFInsNoInfo(List topCOF) {
        StringBuffer stringBuffer = new StringBuffer();
        DecimalFormat decimalFormat = new DecimalFormat(".#####");
        DecimalFormat decimalFormat2 = new DecimalFormat(".##");
        List ls = new ArrayList();
        
        stringBuffer.append("\n=== Top " + getTopN() + " COF Instances Number===\n");
        for (int i = 0 ; i< topCOF.size(); i++) {
            PriorityQueueElement pqe = ( PriorityQueueElement ) topCOF.get(i);
            DataObject COFdataObject = (DataObject) pqe.getObject();
            //stringBuffer.append(COFdataObject.getKey()+ "\n");
            ls.add(Integer.parseInt(COFdataObject.getKey()));
            
        }
        
        Collections.sort(ls);
        for (int i = 0; i < ls.size(); i++) {
            stringBuffer.append(ls.get(i) + "\n");
        }
        stringBuffer.append("==========================================\n\n");
        return stringBuffer;
    }
    
    /*---------------------------------------------------------------------------------*/
    /*---------------------------------------------------------------------------------*/

    public String toString() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Class Outlier Mining: Distance-Based Approach Algorithm results\n" +
                "========================================================================================\n");
        stringBuffer.append("Date: " + new java.util.Date() + "\n\n");
        stringBuffer.append("DataObjects size: (Number of Instances): " + database.size() + "\n");
        stringBuffer.append("Number of attributes: " + database.getInstances().numAttributes() + "\n");
        //stringBuffer.append("Epsilon: " + getEpsilon() + "; K: " + getK() + "\n");
        stringBuffer.append("K: " + getK() + "\n");
        stringBuffer.append("Top N COF: " + getTopN() + "\n");
        stringBuffer.append("Index: " + getDatabase_Type() + "\n");
        stringBuffer.append("Distance-type: " + getDatabase_distanceType() + "\n");
        stringBuffer.append("Alpha: " + getAlpha() + "\n");
        stringBuffer.append("Beta: " + getBeta() + "\n");
        stringBuffer.append("Remove Instance With Missing Values: " + getRemoveWithValues() + "\n");
        stringBuffer.append("Replace Missing Values: " + getReplaceMissingValues() + "\n");
        stringBuffer.append("# of Classes: " + database.getInstances().numClasses() + "\n");
        stringBuffer.append(database.getInstances().attribute(database.getInstances().classIndex()).toString() + "\n");
        stringBuffer.append(database.getInstances().attributeStats(database.getInstances().classIndex()).toString());
        
        
        
        DecimalFormat decimalFormat = new DecimalFormat(".##");
               
        List top;
        System.out.println("Ranking objects");
        top = COF_RankAll();
        try {
			insert_outliers.Insert_outliers(pk_list, top, database);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        stringBuffer.append(topCOFInfo(top));
        
        stringBuffer.append("Elapsed time: " + decimalFormat.format(elapsedTime) + "\n\n");
        
      /*  if (getJustification()) {
            stringBuffer.append(getK() + " Nearst Neighbors for all data set\n");
            stringBuffer.append(report());
            
        }*/
        
        
        
        /*====================================================================*/
        if (getLs()) {
            stringBuffer.append(topCOFInsNoInfo(top));
        }
        
        return stringBuffer.toString() + "\n";
    }

    /**
     * Main Method for testing DBScan
     * @param args Valid parameters are: 'E' epsilon (default = 1.2); 'M' minPoints (default = 6);
     *                                   'I' index-type (default = weka.clusterers.forOPTICSAndDBScan.Databases.SequentialDatabase);
     *                                   'D' distance-type (default = weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclidianDataObject);
     */
  /*  public static void main(String[] args) {
        try {
            
            CODB codb = new CODB();
            System.out.println(codb.GO(args));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    // *****************************************************************************************************************
    // inner classes
    // *****************************************************************************************************************

}