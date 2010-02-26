///////////////////////////////////////////////////////////////////////////
//
//   Copyright 2010 Alberto González Palomo
//   Author: Alberto González Palomo - http://matracas.org/
//
//   This file is part of HistoRadar, the History Radar.
//
//   HistoRadar is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//
//   HistoRadar is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with HistoRadar; if not, see <http://www.gnu.org/licenses/>.
//
/////////////////////////////////////////////////////////////////////////////

package org.matracas.historadar.nlp;

import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;
import org.matracas.historadar.Document;

/**
 * Metadata extracted from a document.
 *
 * The metadata entries have the meanings defined by the
 * Dublin Core® Metadata Initiative, described in
 * <a href="http://dublincore.org/documents/dces/">http://dublincore.org/documents/dces/</a>
 */
public class Metadata
{
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    public static final String contributor = DC_NAMESPACE + "contributor";
    public static final String coverage    = DC_NAMESPACE + "coverage";
    public static final String creator     = DC_NAMESPACE + "creator";
    public static final String date        = DC_NAMESPACE + "date";
    public static final String description = DC_NAMESPACE + "description";
    public static final String format      = DC_NAMESPACE + "format";
    public static final String identifier  = DC_NAMESPACE + "identifier";
    public static final String language    = DC_NAMESPACE + "language";
    public static final String publisher   = DC_NAMESPACE + "publisher";
    public static final String relation    = DC_NAMESPACE + "relation";
    public static final String rights      = DC_NAMESPACE + "rights";
    public static final String source      = DC_NAMESPACE + "source";
    public static final String subject     = DC_NAMESPACE + "subject";
    public static final String title       = DC_NAMESPACE + "title";
    public static final String type        = DC_NAMESPACE + "type";
    
    public Metadata(Document.Collection collection)
    {
        // TODO: linguistic analysis of the collection, if necessary
    }
    
    /**
     * Get the metadata entries from the given document,
     * based on the linguistic analysis of the collection.
     *
     * @param document from which to extract the metadata
     * @return entries indexed by type
     */
    public Entries getMetadata(Document document)
    {
        Entries entries = new Entries();
        String plainText = document.getPlainText();
        
        // TODO: extract metadata entries form plain text
        // e.g. entries.date("1949-03-18");
        entries.add(title, "NO TITLE FOUND YET");
        entries.add(date, "XXXX-XX-01");
        entries.add(date, "XXXX-XX-02");
        entries.add(date, "XXXX-XX-03");
        
        return entries;
    }
    
    public class Values extends Vector<String>
    {
    };
    
    /**
     * Collection of string entities indexed by class.
     */
    public class Entries extends Hashtable<String, Values>
    {
        /**
         * Add an entity of the given class to the collection.
         *
         * @param entryClass class for the entity
         * @param value the entity as a string
         * @return the collection so that we can chain calls to this function
         *         like entries.add("class1","value1").add("class2","value2)...
         */
        public Entries add(String entryClass, String value)
        {
            Values values = get(entryClass);
            if (null == values) {
                put(entryClass, values = new Values());
            }
            values.add(value);
                
            return this;
        }
            
    }
}
