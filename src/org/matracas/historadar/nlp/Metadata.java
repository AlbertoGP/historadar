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
    public Metadata(Document.Collection collection)
    {
    }
    
    public Entries getMetadata(Document document)
    {
        Entries entries = new Entries();
        String plainText = document.getPlainText();
        
        // TODO: extract metadata entries form plain text
        // e.g. entries.dcDate("1949-03-18");
        
        return entries;
    }
    
    public class Entries extends Hashtable<String, String>
    {
        private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
        
        public Entries title(String title)
        {
            put(DC_NAMESPACE + "title", title);
            
            return this;
        }
        
        public Entries date(String date)
        {
            put(DC_NAMESPACE + "date", date);
            
            return this;
        }
    };
    
}
