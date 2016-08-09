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
 *    Copyright (C) 2004
 *    & Matthias Schubert (schubert@dbs.ifi.lmu.de)
 *    & Zhanna Melnikova-Albrecht (melnikov@cip.ifi.lmu.de)
 *    & Rainer Holzmann (holzmann@cip.ifi.lmu.de)
 */

package motaz.util;

import motaz.util.Database;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Utils;

import java.io.Serializable;


 
public class EuclidianDataObject implements DataObject {

    /**
     * Holds the original instance
     */
    private Instance instance;

    /**
     * Holds the (unique) key that is associated with this DataObject
     */
    private String key;

    /**
     * Holds the database, that is the keeper of this DataObject
     */
    private Database database;

    // *****************************************************************************************************************
    // constructors
    // *****************************************************************************************************************

    /**
     * Constructs a new DataObject. The original instance is kept as instance-variable
     * @param originalInstance the original instance
     */
    public EuclidianDataObject(Instance originalInstance, String key, Database database) {
        this.database = database;
        this.key = key;
        instance = originalInstance;
    }

    // *****************************************************************************************************************
    // methods
    // *****************************************************************************************************************

    /**
     * Compares two DataObjects in respect to their attribute-values
     * @param dataObject The DataObject, that is compared with this.dataObject
     * @return Returns true, if the DataObjects correspond in each value, else returns false
     */
    public boolean equals(DataObject dataObject) {
    	double latitude1;
    	double longitude1;
    	double station1;
    	double ldsa1;
    	double latitude2;
    	double longitude2;
    	double station2;
    	double ldsa2;
        if (this == dataObject) return true;
        if (!(dataObject instanceof EuclidianDataObject)) return false;

        final EuclidianDataObject euclidianDataObject = (EuclidianDataObject) dataObject;

        if (getInstance().equalHeaders(euclidianDataObject.getInstance())) {
            
            latitude1=euclidianDataObject.getInstance().value(0);
        	longitude1=euclidianDataObject.getInstance().value(1);
        	station1=Integer.parseInt(euclidianDataObject.getInstance().stringValue(database.getInstances().attribute("station")));
        	ldsa1=euclidianDataObject.getInstance().value(3);
        	latitude2=getInstance().value(0);
        	longitude2=getInstance().value(1);
        	station2=Integer.parseInt(getInstance().stringValue(database.getInstances().attribute("station")));
        	ldsa2=this.getInstance().value(3);
        	if((latitude1==latitude2)&&(longitude1==longitude2)&&(station1==station2)&&(ldsa1==ldsa2))
        		return true;
        	else return false;
        }
		return false;
        }
      
    

    /**
     * Calculates the euclidian-distance between dataObject and this.dataObject excluding the class label
     * @param dataObject The DataObject, that is used for distance-calculation with this.dataObject
     * @return double-value The euclidian-distance between dataObject and this.dataObject
     *                      NaN, if the computation could not be performed
     */
    public double distance(DataObject dataObject) {
        double dist = 0.0;

        if (!(dataObject instanceof EuclidianDataObject)) return Double.NaN;

        if (getInstance().equalHeaders(dataObject.getInstance())) {
            for (int i = 0; i < getInstance().numAttributes() -1; i++) {
            	if(i==3)
            	{
                double cDistance = computeDistance(getInstance().index(i),
                        getInstance().valueSparse(i),
                        dataObject.getInstance().valueSparse(i));
                dist += 2*Math.pow(cDistance, 2.0);
            }
            	else
            	{
            		double cDistance = computeDistance(getInstance().index(i),
                            getInstance().valueSparse(i),
                            dataObject.getInstance().valueSparse(i));
                    dist += Math.pow(cDistance, 2.0);	
            	}
            return Math.sqrt(dist);
        }
        }
        return Double.NaN;
    }
    public double distance_1(DataObject dataObject) {
        double dist = 0.0;

        if (!(dataObject instanceof EuclidianDataObject)) return Double.NaN;

        if (getInstance().equalHeaders(dataObject.getInstance())) {
            for (int i = 0; i < getInstance().numAttributes() -1; i++) {
            	
                double cDistance = computeDistance(getInstance().index(i),
                        getInstance().valueSparse(i),
                        dataObject.getInstance().valueSparse(i));
                dist += Math.pow(cDistance, 2.0);
          
            return Math.sqrt(dist);
        }
        }
        return Double.NaN;
    }
    
    /**
     * Performs euclidian-distance-calculation between two given values
     * @param index of the attribute within the DataObject's instance
     * @param v value_1
     * @param v1 value_2
     * @return double norm-distance between value_1 and value_2
     */
    private double computeDistance(int index, double v, double v1) {
        switch (getInstance().attribute(index).type()) {
            case Attribute.NOMINAL:
                return (((int) v != (int) v1)) ? 1 : 0;

            case Attribute.NUMERIC:
                
                    return norm(v, index) - norm(v1, index);

            default:
                return 0;
        }
    }

    /**
     * Normalizes a given value of a numeric attribute.
     *
     * @param x the value to be normalized
     * @param i the attribute's index
     */
    private double norm(double x, int i) {
        if (Double.isNaN(database.getAttributeMinValues()[i])
                || Utils.eq(database.getAttributeMaxValues()[i], database.getAttributeMinValues()[i])) {
            return 0;
        } else {
            return (x - database.getAttributeMinValues()[i]) /
                    (database.getAttributeMaxValues()[i] - database.getAttributeMinValues()[i]);
        }
    }

    /**
     * Returns the original instance
     * @return originalInstance
     */
    public Instance getInstance() {
        return instance;
    }

    /**
     * Returns the key for this DataObject
     * @return key
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key for this DataObject
     * @param key The key is represented as string
     */
    public void setKey(String key) {
        this.key = key;
    }

    
    public String toString() {
        return instance.toString();
    }

    // *****************************************************************************************************************
    // inner classes
    // *****************************************************************************************************************

}
