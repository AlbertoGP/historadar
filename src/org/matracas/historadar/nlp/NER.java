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
 * Named entities extracted from a document.
 *
 */
public class NER
{
    private static final String NAMESPACE = "http://matracas.org/ns/historadar/";
    public static final String location     = NAMESPACE + "location";
    public static final String person       = NAMESPACE + "person";
    public static final String organization = NAMESPACE + "organization";
    
    public NER(Document.Collection collection)
    {
        // TODO: linguistic analysis through the collection
    }
    
    /**
     * Get the entities from a given document,
     * based on the linguistic analysis of the document collection.
     *
     * @param document the document where we look for entities
     */
    public EntityTypes getEntities(Document document)
    {
        EntityTypes entities = new EntityTypes();
        String plainText = document.getPlainText();
        
        // TODO: extract entities form plain text
        // These are some dumb examples until this is implemented:
        entities.add(person, "not here nor there");
        entities.add(person, "Big Kahuna");
        entities.add(person, "His Excellence Mister Foolserrand");
        entities.add(person, "Totem Master");
        entities.add(organization, "Council of Notable People");
        
        return entities;
    }
    
    public class Entities extends Vector<String>
    {
    };
    
    /**
     * Collection of string entities indexed by type.
     */
    public class EntityTypes extends Hashtable<String, Entities>
    {
        /**
         * Add an entity of the given class to the collection.
         *
         * @param type the type of the entity
         * @param entity the entity as a string
         * @return the collection so that we can chain calls to this function
         *         like collection.add("class1","entity1").add("class2","entity2)...
         */
        public EntityTypes add(String type, String entity)
        {
            Entities entities = get(type);
            if (null == entities) {
                put(type, entities = new Entities());
            }
            entities.add(entity);
                
            return this;
        }
            
    }
}
