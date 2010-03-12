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
import java.util.Collections;
import org.matracas.historadar.Document;

import org.matracas.historadar.Document.Segment;
import opennlp.tools.namefind.*;
import opennlp.tools.sentdetect.*;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.maxent.GISModel;
import opennlp.maxent.io.BinaryGISModelReader;
import opennlp.tools.util.Span;
import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * Named entities extracted from a document.
 *
 */
public class NER
{
    protected static final String NAMESPACE = "http://matracas.org/ns/historadar/";
    public static final String location     = NAMESPACE + "location";
    public static final String person       = NAMESPACE + "person";
    public static final String organization = NAMESPACE + "organization";
    
    protected EntityTypes entities;
    protected Document.PatternTable patterns;
    protected NER()
    {
    }
    
    public NER(Document.Collection collection)
    {
        // TODO: linguistic analysis through the collection
        entities = new EntityTypes();
        
        // Add some entities:
        // These are some dumb examples until this is implemented:
        //entities.add(person, "not here nor there");
        //entities.add(person, "Big Kahuna");
        //entities.add(person, "His Excellence Mister Foolserrand");
        //entities.add(person, "Totem Master");
        //entities.add(organization, "Council of Notable People");
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
        GISModel m;
        DataInputStream personFile, locationFile, sentenceFile, tokenFile;
        try {
            personFile   = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/person.bin.gz")));
            locationFile   = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/location.bin.gz")));
            sentenceFile = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/EnglishSD.bin.gz")));
            tokenFile    = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/EnglishTok.bin.gz")));
            
            BinaryGISModelReader reader = new BinaryGISModelReader(personFile);
            m = reader.getModel();
            NameFinderME personFinder = new NameFinderME(m);

            reader = new BinaryGISModelReader(locationFile);
            m = reader.getModel();
            NameFinderME locationFinder = new NameFinderME(m);

            reader = new BinaryGISModelReader(sentenceFile);
            m = reader.getModel();
            SentenceDetectorME detector = new SentenceDetectorME(m);
            
            reader = new BinaryGISModelReader(tokenFile);
            m = reader.getModel();
            TokenizerME tokenizer = new TokenizerME(m);
            
            String[] sentences = detector.sentDetect(plainText);
            

            int position = 0;
          
            for (String sent:sentences) {
                String[] tokens = tokenizer.tokenize(sent);
                Span[] spans = personFinder.find(tokens);

                for (Span s:spans) {
                    Segment segment = createSegment(s,tokens,plainText,position);
                    segment.put("pattern-name", "person");
                    segments.add(segment);
                }

                position = position + sent.length();
            }


            position = 0;

            for (String sent:sentences) {
                String[] tokens = tokenizer.tokenize(sent);
                Span[] spans = locationFinder.find(tokens);

                for (Span s:spans) {
                    Segment segment = createSegment(s,tokens,plainText,position);
                    segment.put("pattern-name", "location");
                    segments.add(segment);
                }

                position = position + sent.length();
            }
            

            
//            System.out.println("Nr of segments found: "+segments.size());


        } catch (Exception e) {
            System.err.println("exception got thrown in NER.getEntities: "+ e);
            return segments;
        }



        Collections.sort(segments);
        return segments;
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

    private Document.Segment createSegment(Span span,String[] tokens,String plainText,int position) {
        /* get a rough estimation of where the segment should start and end, using the
         * numbers of letters in the tokens */
        int start = position;
        int end = position;
        for(int i=0;i<span.getStart()-1;i++) {
            start = start + tokens[i].length()+1;
        }
        for(int i=0;i<span.getEnd();i++) {
            end = end + tokens[i].length()+1;
        }
        Document.Segment guessedSegment = new Document.Segment(start, end);

        /*get the NE from the list of tokens*/
        String NE = "";
        for (int i=span.getStart();i<span.getEnd();i++) {
            NE = NE + (tokens[i]);
        }

        /*try moving the segment around in the text, until it matches the NE*/
        for (start=guessedSegment.getBegin()+10;start>guessedSegment.getBegin()-10;start--) {
            for (end=start;end<start+NE.length()+10;end++) {

                /*get the text in the estimated segment (without whitespaces)*/
                String segmentText = plainText.substring(start, end);
                int length = segmentText.length(); //remember the length before whitespaces are removed
                segmentText = segmentText.replaceAll(" ", "");

                /*compare text from the segment to NE. If they match, find end and return segment*/               
                if (segmentText.equals(NE)) {
                    end = start + length;
//                    System.out.println("match: " + segmentText);
                    return new Document.Segment(start, end);
                }
            }
        }
        
        /* if we don't get an exact match, just return the guess*/

//        System.out.println("no match: " + NE);
        return guessedSegment;
    }
}
