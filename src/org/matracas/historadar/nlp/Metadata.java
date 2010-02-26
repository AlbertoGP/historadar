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
    public static final String TITLE = DC_NAMESPACE + "title";
    public static final String DATE  = DC_NAMESPACE + "date";
    
    public Metadata(Document.Collection collection)
    {
    }
    
    public Entries getMetadata(Document document)
    {
        Entries entries = new Entries();
        String plainText = document.getPlainText();
        
        // TODO: extract metadata entries form plain text
        // e.g. entries.date("1949-03-18");
        entries.title("NO TITLE FOUND YET");
        entries.date("XXXX-XX-XX");
        
        return entries;
    }
    
    public class Entries extends Hashtable<String, String>
    {
        public Entries title(String title)
        {
            put(TITLE, title);
            
            return this;
        }
        public String title()
        {
            return get(TITLE);
        }
        
        public Entries date(String date)
        {
            put(DATE, date);
            
            return this;
        }
        public String date()
        {
            return get(DATE);
        }
    };
    
}
