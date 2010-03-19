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

import java.util.Collections;
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
public class OpenNlpNER extends NER
{
 

    public OpenNlpNER(Document.Collection collection)
    {
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


        GISModel m;
        DataInputStream personFile, locationFile, sentenceFile, tokenFile;
        try {
            /*create input streams for the files containing the required models*/
            personFile   = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/person.bin.gz")));
            locationFile   = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/location.bin.gz")));
            sentenceFile = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/EnglishSD.bin.gz")));
            tokenFile    = new DataInputStream(new GZIPInputStream(getClass().getResourceAsStream("/lib/opennlp/models/EnglishTok.bin.gz")));

            /*get the models and create with them the objects necessary to process the text*/
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


            /*split the text into sentences*/
            String[] sentences = detector.sentDetect(plainText);

            /*For each sentence, first tokenize it, then get the person names from it.
              @position: required to keep track of where in the text we are*/
            int position = 0;
            for (String sent:sentences) {

                //tokenize and find names
                String[] tokens = tokenizer.tokenize(sent);
                Span[] spans = personFinder.find(tokens);

                //now create a segment for each name found and add it to the list
                for (Span s:spans) {
                    Segment segment = createSegment(s,tokens,plainText,position);
                    segment.put("pattern-name", "person");
                    segments.add(segment);
                }

                position = position + sent.length();
            }


            /*Now do the same thing as above again, only for locations. (Yes, we tokenize everything
              twice. It's not pretty, but at least it works.)*/
            position = 0;
            for (String sent:sentences) {

                //tokenize and find names
                String[] tokens = tokenizer.tokenize(sent);
                Span[] spans = locationFinder.find(tokens);

                //now create a segment for each name found and add it to the list
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


        /*for some reason, View seems not to be able to handle unsorted segment lists, so we sort
         it before returning it*/
        Collections.sort(segments);
        return segments;
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
        StringBuffer NE = new StringBuffer();
        for (int i=span.getStart();i<span.getEnd();i++) {
            NE.append(tokens[i]);
        }

        /*try moving the segment around in the text, until it matches the NE*/
        for (start=guessedSegment.getBegin()+10;start>guessedSegment.getBegin()-10;start--) {
            for (end=start;end<start+NE.length()+10;end++) {

                /*get the text in the estimated segment (without whitespaces)*/
                String segmentText = plainText.substring(start, end);
                int length = segmentText.length(); //remember the length before whitespaces are removed
                segmentText = segmentText.replaceAll(" ", "");

                /*compare text from the segment to NE. If they match, find end and return segment*/
                if (segmentText.equals(NE.toString())) {
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