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
    public Entities getEntities(Document document)
    {
        Entities entities = new Entities();
        String plainText = document.getPlainText();
        
        // TODO: extract entities form plain text
        // These are some dumb examples until this is implemented:
        entities.location("not here nor there");
        entities.person("Big Kahuna");
        entities.person("His Excellence Mister Foolserrand");
        entities.person("Totem Master");
        entities.organization("Council of Notable People");
        
        return entities;
    }
    
    /**
     * An entity extracted from a document, built from a string.
     */
    public static class Entity
    {
        protected String string;
        
        public Entity(String string)
        {
            this.string = string;
        }
        
        /**
         * Type of entity.
         */
        public enum Type
        {
            LOCATION, PERSON, ORGANIZATION;
        }
    }
    
    /**
     * Table of entities indexed by type.
     */
    public class Entities extends Hashtable<Entity.Type, Vector<Entity> >
    {
        public Entities location(String location)
        {
            Vector<Entity> entitiesOfType = get(Entity.Type.LOCATION);
            if (null == entitiesOfType) {
                put(Entity.Type.LOCATION, entitiesOfType = new Vector<Entity>());
            }
            entitiesOfType.add(new Entity(location));
            
            return this;
        }
        public Vector<Entity> location()
        {
            return get(Entity.Type.LOCATION);
        }
        
        public Entities person(String person)
        {
            Vector<Entity> entitiesOfType = get(Entity.Type.PERSON);
            if (null == entitiesOfType) {
                put(Entity.Type.PERSON, entitiesOfType = new Vector<Entity>());
            }
            entitiesOfType.add(new Entity(person));
            
            return this;
        }
        public Vector<Entity> person()
        {
            return get(Entity.Type.PERSON);
        }
        
        public Entities organization(String organization)
        {
            Vector<Entity> entitiesOfType = get(Entity.Type.ORGANIZATION);
            if (null == entitiesOfType) {
                put(Entity.Type.ORGANIZATION, entitiesOfType = new Vector<Entity>());
            }
            entitiesOfType.add(new Entity(organization));
            
            return this;
        }
        public Vector<Entity> organization()
        {
            return get(Entity.Type.ORGANIZATION);
        }
        
    };
    
}
