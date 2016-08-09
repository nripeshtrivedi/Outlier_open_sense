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

import weka.core.Instance;


public interface DataObject {

    
    // *****************************************************************************************************************
    // methods
    // *****************************************************************************************************************

    /**
     * Compares two DataObjects in respect to their attribute-values
     * @param dataObject The DataObject, that is compared with this.dataObject
     * @return Returns true, if the DataObjects correspond in each value, else returns false
     */
    boolean equals(DataObject dataObject);

    /**
     * Calculates the distance between dataObject and this.dataObject
     * @param dataObject The DataObject, that is used for distance-calculation with this.dataObject
     * @return double-value The distance between dataObject and this.dataObject
     */
    double distance(DataObject dataObject);
    double distance_1(DataObject dataObject);

    /**
     * Returns the original instance
     * @return originalInstance
     */
    Instance getInstance();

    /**
     * Returns the key for this DataObject
     * @return key
     */
    String getKey();

    /**
     * Sets the key for this DataObject
     * @param key The key is represented as string
     */
    void setKey(String key);
}
