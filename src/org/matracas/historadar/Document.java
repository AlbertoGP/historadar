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

package org.matracas.historadar;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Source text document.
 */
public class Document
{
    private String plainText;
    
    /**
     * Constructs an empty document.
     *
     * The content can be set later with {@link #setPlainText(String)}
     */
    public Document()
    {
    }
    
    /**
     * Constructs a new document from a file.
     *
     * At the moment only plain text files encoded as UTF-8 are supported.
     */
    public Document(File file)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        plainText = "";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            plainText += new String(line.getBytes(), "UTF-8") + "\n";
        }
        reader.close();
    }
    
    /**
     * Get the plain text version of the document as a string.
     *
     * @return string plain text content as a string
     */
    public String getPlainText()
    {
        return plainText;
    }
    
    /**
     * Get the plain text version of the document as a string.
     *
     * @return string plain text content as a string
     */
    public String getPlainText(Segment segment)
    {
        return plainText.substring(segment.begin, segment.end);
    }
    
    /**
     * Set the content of the document from a plain text version.
     *
     * @param text plain text content as a string
     */
    public void setPlainText(String text)
    {
        plainText = text;
    }
    
    /**
     * Attributed document segment. This can be a word, a paragraph, etc.
     * <br/>
     * The segment's attributes can be handled using the {@link Map} interface,
     * for instance {@link Map#put(Object, Object)} and {@link Map#get(Object)}
     */
    public static class Segment extends HashMap<String, String>
        implements java.lang.Comparable<Segment>
    {
        protected int begin, end;
        
        public Segment(int begin, int end) {
            this.begin = begin;
            this.end   = end;
        }
        
        public int getBegin() { return begin; }
        public int getEnd()   { return end;   }
        
        public int compareTo(Segment other)
        {
            if      (begin < other.begin) return -1;
            else if (begin > other.begin) return  1;
            else if (end   > other.end  ) return -1;
            else if (end   < other.end  ) return  1;
            else return 0;
        }
    }
    
    public class SegmentIterator
    {
        Matcher matcher;
        int start, end;
        
        public SegmentIterator(Pattern pattern)
        {
            matcher = pattern.matcher(plainText);
            findNext();
        }
        
        public boolean hasNext()
        {
            return start != -1;
        }
        
        public Segment next()
        {
            Segment next = new Segment(start, end);
            findNext();
            
            return next;
        }
        
        protected void findNext()
        {
            if (matcher.find()) {
                start = matcher.start();
                end   = matcher.end();
            }
            else {
                start = -1;
                end   = -1;
            }
        }
    }
    
    public SegmentIterator iterator(Pattern pattern)
    {
        return new SegmentIterator(pattern);
    }
    
    /**
     * Segment the document with the given named patterns.
     *
     * @param patterns the patterns that define the segments.
     *        It is a map from name strings to patterns.
     *        The segments produced by each pattern have the property
     *        "pattern-name" set to the corresponding name string.
     * @return segments that match the pattern, ordered by
     *         enclosing segment first
     *
     *
     * <hr/>
     * <b>Example: a toy sentence splitter and Part-Of-Speech/Named-Entity tagger</b>
     * <pre>
     * import java.util.Map;
     * import java.util.HashMap;
     * import java.util.regex.Pattern;
     * import java.util.Vector;
     * import java.util.Iterator;
     * import org.matracas.historadar.Document;
     * 
     * public class POS
     * {
     *     public static void main(String args[])
     *     {
     *         Document document = new Document();
     *         document.setPlainText("The rain in Spain falls mainly on the plain. The cats in France smoke fragance.");
     *         Map<String, Pattern> patterns = new HashMap<String, Pattern>();
     *         int I = Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
     *         patterns.put("sentence",    Pattern.compile("[^.:!?]+[.:!?]"));
     *         patterns.put("country",     Pattern.compile("\\b(Spain|France)\\b"));
     *         patterns.put("verb",        Pattern.compile("\\b(falls?|smokes?)\\b"));
     *         patterns.put("preposition", Pattern.compile("\\b(in|on|at)\\b"));
     *         patterns.put("article",     Pattern.compile("\\b(the|an|a)\\b", I));
     *         patterns.put("animal",      Pattern.compile("\\b(dogs?|cats?)\\b"));
     *         patterns.put("noun",        Pattern.compile("\\b(dogs?|cats?|Spain|France|plain|fragance|rain)\\b"));
     *         Vector<Document.Segment> segments = document.segment(patterns);
     *         Iterator<Document.Segment> i = segments.iterator();
     *         while (i.hasNext()) {
     *             Document.Segment segment = i.next();
     *             System.err.println("Segment [" + segment.getBegin() + ", " + segment.getEnd() + "] "
     *                                + segment.get("pattern-name") + ": "
     *                                + document.getPlainText(segment));
     *         }
     *     }
     * }
     * </pre>
     *
     * This produces the following output:
     * <pre>
     * Segment [0, 44] sentence: The rain in Spain falls mainly on the plain.
     * Segment [0, 3] article: The
     * Segment [4, 8] noun: rain
     * Segment [9, 11] preposition: in
     * Segment [12, 17] noun: Spain
     * Segment [12, 17] country: Spain
     * Segment [18, 23] verb: falls
     * Segment [31, 33] preposition: on
     * Segment [34, 37] article: the
     * Segment [38, 43] noun: plain
     * Segment [44, 79] sentence:  The cats in France smoke fragance.
     * Segment [45, 48] article: The
     * Segment [49, 53] noun: cats
     * Segment [49, 53] animal: cats
     * Segment [54, 56] preposition: in
     * Segment [57, 63] noun: France
     * Segment [57, 63] country: France
     * Segment [64, 69] verb: smoke
     * Segment [70, 78] noun: fragance
     * </pre>
     */
    public Vector<Segment> segment(Map<String, Pattern> patterns)
    {
        Vector<Segment> segments = new Vector<Segment>();
        
        for (Map.Entry<String, Pattern> entry : patterns.entrySet()) {
            SegmentIterator i = new SegmentIterator(entry.getValue());
            while (i.hasNext()) {
                Segment segment = i.next();
                segment.put("pattern-name", entry.getKey());
                segments.add(segment);
            }
        }
        
        java.util.Collections.sort(segments);
        
        return segments;
    }
    
    /**
     * Encode the document as an XML string with the segments tagged.
     *
     * <p><b>TODO: not implemented yet</b></p>
     * The root element is "document",
     * each segment is encoded as an element called "segment"
     * with its properties as XML attributes.
     * Overlapping segments are split so that the result is a tree.
     *
     * @param segments the segments that will be marked as XML elements
     * @return serialized XML document
     */
    public String getXmlString(Vector<Segment> segments)
    {
        return "<document>" + plainText + "</document>";
    }
    
    /**
     * Document collection. Most analysis techniques work better when applying them to collections of documents than to isolated texts.
     */
    public static class Collection
    {
        private Map<String, Document> documents;
        
        /**
         * Constructs a collection from the files found in a directory.
         *
         * Since the {@link Document} class only supports plain text files
         * for now, the only files that are loaded are those whose name
         * ends with the extension "<tt>.txt</tt>".
         */
        public Collection(File directory)
        {
            documents = new HashMap<String, Document>();
            
            Document document;
            String identifier;
            File[] files = directory.listFiles();
            for (int i = 0; i < files.length; ++i) {
                document = null;
                try {
                    if (files[i].getName().endsWith(".txt")) { 
                        document = new Document(files[i]);
                    }
                }
                catch (java.io.FileNotFoundException e) {
                    System.err.println("File not found: " + files[i].getName());
                }
                catch (java.io.IOException e) {
                    System.err.println("IO error: " + e);
                }
                
                if (document != null) {
                    identifier = files[i].getName();
                    documents.put(identifier, document);
                }
            }
        }
        
        /**
         * Get an iterator for all documents in the collection.
         *
         * @return iterator for the documents
         */
        public Iterator<Document> getDocumentIterator()
        {
            return documents.values().iterator();
        }
        
        /**
         * Get an iterator for all identifiers of the documents in the collection.
         *
         * @return iterator for the identifiers
         */
        public Iterator<String> getDocumentIdentifierIterator()
        {
            return documents.keySet().iterator();
        }
        
        /**
         * Get a document from the collection from its identifier string.
         *
         * @param identifier string
         * @return document object if found in the collection,
         *         or <code>null</code> otherwise
         */
        public Document getDocument(String identifier)
        {
            return documents.get(identifier);
        }
        
    }
    
}
    
