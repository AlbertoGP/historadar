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

package org.matracas.historadar.nlp.ner;

import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;
import org.matracas.historadar.Document;
import org.matracas.historadar.nlp.NER;

/**
 * Named entities extracted from a document.
 *
 */
public class SimpleRegexp extends NER
{
    protected Document.PatternTable patterns;
    
    public SimpleRegexp(Document.Collection collection)
    {
//        // TODO: linguistic analysis through the collection
//        // These are some dumb examples until this is implemented:
//        entities.add(person, "the Prime Minister");
//        entities.add(person, "the King of Greece");
//        entities.add(person, "Earl Curzon of Kedleston");
//        entities.add(location, "Germany");
//        entities.add(location, "Greece");
//
//        // Now build the patterns to match them:
//        patterns = new Document.PatternTable();
//        for (Map.Entry<String, Entities> type : entities.entrySet()) {
//            for (String entity : type.getValue()) {
//                patterns.put(type.getKey(), entity);
//            }
//        }
                 patterns.put("country",   
                 "\\b(Spain|France|Germany|USA|Greece|Italy|Russia|Portugal|Switzerland|Austria|Belgium|Netherlands|Denmark|Sweden|Morocco" +
                 "|Romania|the Balkans|Turkey|England|Bulgaria)\\b"
                 );
         patterns.put("person",    "\\b(the Prime Minister|Lord Curzon|Vice-Admiral|Commander-in-Chief|Under-Secratary of State for Foreign Affairs" +
                 "|the Sherif|Chief of the Imperial General Staff|Sir William Robertson|General Sarrail|Mr. Chamberlain|Lord Harding|Sir G. Buchanan|McMahon)\\b"
                 );
         patterns.put("city",    
                 "\\b(Rome|Paris|Larissa|Medina|Salonica|Petrograd|Mecca|Athens)\\b"
                 );
    }
    
    /**
     * Get the entities from a given document,
     * based on the linguistic analysis of the document collection.
     *
     * @param document the document where we look for entities
     */
    public Document.SegmentList getEntities(Document document)
    {
        Document.SegmentList segments = new Document.SegmentList();
        String plainText = document.getPlainText();
        
        // TODO: extract entities form plain text
        segments = document.segment(patterns);
        
        return segments;
    }
    
}
