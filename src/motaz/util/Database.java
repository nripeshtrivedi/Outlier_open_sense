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

import weka.clusterers.forOPTICSAndDBScan.DataObjects.EuclideanDataObject;
import weka.core.Instances;

import java.util.Iterator;
import java.util.List;


public interface Database {

    // *****************************************************************************************************************
    // methods
    // *****************************************************************************************************************

    /**
     * Select a dataObject from the database
     * @param key The key that is associated with the dataObject
     * @return dataObject
     */
    DataObject getDataObject(String key);

    /**
     * Returns the size of the database (the number of dataObjects in the database)
     * @return size
     */
    int size();

    /**
     * Returns an iterator over all the keys
     * @return iterator
     */
    Iterator keyIterator();

    /**
     * Returns an iterator over all the dataObjects in the database
     * @return iterator
     */
    Iterator dataObjectIterator();

    /**
     * Tests if the database contains the dataObject_Query
     * @param dataObject_Query The query-object
     * @return true if the database contains dataObject_Query, else false
     */
    boolean contains(DataObject dataObject_Query);

    /**
     * Inserts a new dataObject into the database
     * @param dataObject
     */
    void insert(DataObject dataObject);

    /**
     * Returns the original instances delivered from WEKA
     * @return instances
     */
    Instances getInstances();

    /**
     * Sets the minimum and maximum values for each attribute in different arrays
     * by walking through every DataObject of the database
     */
    void setMinMaxValues();

    /**
     * Returns the array of minimum-values for each attribute
     * @return attributeMinValues
     */
    double[] getAttributeMinValues();

    /**
     * Returns the array of maximum-values for each attribute
     * @return attributeMaxValues
     */
    double[] getAttributeMaxValues();

    
    
}
