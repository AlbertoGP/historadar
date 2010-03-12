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
import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.Element;

/**
 * Source text document.
 */
public class Document
{
    protected String plainText;
    protected String identifier;
    
    /**
     * Constructs an empty document.
     *
     * The content can be set later with {@link #setPlainText(String)}
     *
     * @param identifier string identifier for the document
     */
    public Document(String identifier)
    {
        this.identifier = identifier;
    }
    
    /**
     * Constructs a new document from a file.
     *
     * At the moment only plain text files encoded as UTF-8 are supported.
     *
     * @param file the file from which to read the document
     * @param identifier string identifier for the document
     */
    public Document(File file, String identifier)
        throws java.io.FileNotFoundException
    {
        this(identifier);
        if (null == this.identifier) this.identifier = file.getName();
        
        BufferedReader reader = null;
        try {
            plainText = "";
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                plainText += new String(line.getBytes(), "UTF-8") + "\n";
            }
        }
        catch (java.io.IOException e) {
            System.err.println(e);
        }
        
        try {
            if (reader != null) reader.close();
        }
        catch (java.io.IOException e) {
            System.err.println(e);
        }
    }
    
    /**
     * Get the document's identifier as a string.
     *
     * @return string identifier
     */
    public String getIdentifier()
    {
        return identifier;
    }
    
    /**
     * Get the plain text version of the document as a string.
     *
     * @return plain text content as a string
     */
    public String getPlainText()
    {
        return plainText;
    }
    
    /**
     * Get the plain text version of the document as a string.
     *
     * @return plain text content as a string
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
     * Collection of string entities indexed by class.
     */
    public static class Metadata extends Hashtable<String, Metadata.Values>
    {
        public static class Values extends Vector<String>
        {
        }
        
        /**
         * Add an entity of the given class to the collection.
         *
         * @param entryClass class for the entity
         * @param value the entity as a string
         * @return the collection so that we can chain calls to this function
         *         like entries.add("class1","value1").add("class2","value2)...
         */
        public Metadata add(String entryClass, String value)
        {
            Values values = get(entryClass);
            if (null == values) {
                put(entryClass, values = new Values());
            }
            values.add(value);
            
            return this;
        }
        
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
     * A list of document segments.
     */
    public static class SegmentList extends Vector<Segment>
    {
        public SegmentList()
        {
        }
        
        public SegmentList(String[] tokens, String text)
        {
            int begin, end;
            for (int i = 0; i < tokens.length; ++i) {
                begin = text.indexOf(tokens[i]);
                if (begin >= 0) {
                    end = begin + tokens[i].length();
                    add(new Segment(begin, end));
                    text = text.substring(end);
                }
            }
        }
    }
    
    /**
     * A table of patterns.
     */
    public static class PatternTable extends HashMap<String, Pattern>
    {
        public static final int CASE_INSENSITIVE = Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE;
        
        public void put(String name, String pattern)
        {
            put(name, pattern, false);
        }
        
        public void put(String name, String pattern, boolean caseInsensitive)
        {
            if (caseInsensitive) {
                put(name, Pattern.compile(pattern, CASE_INSENSITIVE));
            }
            else {
                put(name, Pattern.compile(pattern));
            }
        }
        
        public void putWords(String name, Vector<String> words)
        {
            String pattern = "\\b(";
            for (String word : words) {
                pattern += "|" + word;
            }
            pattern += ")\\b";
            
            put(name, pattern);
        }
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
     *         Document document = new Document("example-document");
     *         document.setPlainText("The rain in Spain falls mainly on the plain. The cats in France smoke fragance.");
     *         Document.PatternTable patterns = new Document.PatternTable();
     *         patterns.put("sentence",    Pattern.compile("[^.:!?]+[.:!?]"));
     *         patterns.put("country",     Pattern.compile("\\b(Spain|France)\\b"));
     *         patterns.put("verb",        Pattern.compile("\\b(falls?|smokes?)\\b"));
     *         patterns.put("preposition", Pattern.compile("\\b(in|on|at)\\b"));
     *         patterns.put("article",     Pattern.compile("\\b(the|an|a)\\b", Document.PatternTable.CASE_INSENSITIVE));
     *         patterns.put("animal",      Pattern.compile("\\b(dogs?|cats?)\\b"));
     *         patterns.put("noun",        Pattern.compile("\\b(dogs?|cats?|Spain|France|plain|fragance|rain)\\b"));
     *         Document.SegmentList segments = document.segment(patterns);
     *         Iterator&lt;Document.Segment&gt; i = segments.iterator();
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
    public SegmentList segment(Map<String, Pattern> patterns)
    {
        SegmentList segments = new SegmentList();
        
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
     * Encode the document as an XML document with the segments tagged.
     *
     * The root element is "document" in the namespace "",
     * each segment is encoded as an element called "segment"
     * with its properties as XML attributes.
     * Overlapping segments are split so that the result is a tree.
     *
     * @param segments the segments that will be marked as XML elements
     * @return XML document
     */
    public org.w3c.dom.Document getXML(SegmentList segments)
    {
        return getXML(segments, new XMLVocabulary());
    }
    
    /**
     * Encode the document as an XML document with the segments tagged,
     * using the given XML vocabulary.
     *
     * The root element is "document" in the namespace "",
     * each segment is encoded as an element called "segment"
     * with its properties as XML attributes.
     * Overlapping segments are split so that the result is a tree.
     *
     * @param segments the segments that will be marked as XML elements
     * @param format the XML vocabulary (format) to produce
     * @return XML document
     */
    public org.w3c.dom.Document getXML(SegmentList segments, XMLVocabulary format)
    {
        org.w3c.dom.Document xmlDocument = format.createDocument();
        Element root = format.getContentElement();
        
        if (plainText != null) {
            java.util.Stack<Segment> stack = new java.util.Stack<Segment>();
            Element parentElement = root;
            Segment parentSegment = null;
            int currentCharacter = 0;
            Iterator<Document.Segment> i = segments.iterator();
            while (i.hasNext()) {
                Segment segment = i.next();
                if (stack.empty()) parentSegment = null;
                else               parentSegment = stack.peek();
                
                if (null == parentSegment) {
                    // ... [segment]
                    currentCharacter = format.appendText(parentElement, plainText, currentCharacter, segment.getBegin());
                    
                    parentElement = format.appendElement(parentElement, segment);
                    stack.push(segment);
                }
                else if (segment.getBegin() >= parentSegment.getEnd()) {
                    // [parentSegment] ... [segment]
                    while (parentSegment != null
                           && segment.getBegin() >= parentSegment.getEnd()) {
                        currentCharacter = format.appendText(parentElement, plainText, currentCharacter, parentSegment.getEnd());
                        
                        if (parentElement.getParentNode() instanceof Element) {
                            parentElement = (Element) parentElement.getParentNode();
                            stack.pop();// == parentSegment
                            if (stack.empty()) parentSegment = null;
                            else               parentSegment = stack.peek();
                        }
                        else {
                            System.err.println("Error: parent element can not be the document");
                        }
                    }
                    
                    currentCharacter = format.appendText(parentElement, plainText, currentCharacter, segment.getBegin());
                    
                    parentElement = format.appendElement(parentElement, segment);
                    stack.push(segment);
                }
                else {
                    // [parentSegment...[segment]...]
                    // Here we could deal with overlaps, but we ignore that for now.
                    // We assume this means that the current segment is contained in the parent.
                    currentCharacter = format.appendText(parentElement, plainText, currentCharacter, segment.getBegin());
                    
                    parentElement = format.appendElement(parentElement, segment);
                    stack.push(segment);
                }
            }
            
            if (! stack.empty()) parentSegment = stack.peek();
            while (parentSegment != null) {
                currentCharacter = format.appendText(parentElement, plainText, currentCharacter, parentSegment.getEnd());
                
                if (parentElement.getParentNode() instanceof Element) {
                    parentElement = (Element) parentElement.getParentNode();
                    stack.pop();// == parentSegment
                    if (stack.empty()) parentSegment = null;
                    else               parentSegment = stack.peek();
                }
                else {
                    System.err.println("Error: parent element can not be the document");
                }
            }
            
            // Text after the last segment:
            currentCharacter = format.appendText(root, plainText, currentCharacter, plainText.length());
        }
        
        return xmlDocument;
    }
    
    public XMLVocabulary getXMLVocabulary(String name)
    {
        if ("html".equalsIgnoreCase(name) || "xhtml".equalsIgnoreCase(name)) {
            System.err.println("html");
            return new XMLVocabularyHTML();
        }
        else {
            System.err.println("xml");
            return new XMLVocabulary();
        }
    }
    
    public static class XMLVocabulary
    {
        protected org.w3c.dom.bootstrap.DOMImplementationRegistry registry;
        protected DOMImplementation dom;
        protected DOMImplementationLS loadSave;
        protected org.w3c.dom.ls.LSSerializer serializer;
        protected org.w3c.dom.Document document;
        protected Element content;
        
        public XMLVocabulary()
        {
            try {
                registry = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance();
            }
            catch (java.lang.ClassNotFoundException e) {
                System.err.println("Error: no XML DOM implementation available:\n" + e);
                return;
            }
            catch (java.lang.InstantiationException e) {
                System.err.println("Error: XML DOM instantiation exception:\n" + e);
                return;
            }
            catch (java.lang.IllegalAccessException e) {
                System.err.println("Error: XML DOM illegal access exception:\n" + e);
                return;
            }
            
            dom = registry.getDOMImplementation("XML 3.0");
            loadSave = (DOMImplementationLS) registry.getDOMImplementation("LS");
            serializer = loadSave.createLSSerializer();
        }
        
        public org.w3c.dom.Document createDocument()
        {
            document = dom.createDocument("http://matracas.org/ns/historadar", "document", null);
            content = document.getDocumentElement();
            
            return document;
        }
        
        public Element getContentElement()
        {
            return content;
        }
        
        public int appendText(Element element, String text, int begin, int end)
        {
            if (begin >= 0 && begin < end && end <= text.length()) {
                appendText(element, text.substring(begin, end));
                return end;
            }
            else {
                return begin;
            }
        }
        
        public void appendText(Element element, String text)
        {
            element.appendChild(element.getOwnerDocument().createTextNode(text.replace("", "\n------------------------------------------------------\n")));
        }
        
        public Element appendElement(Element element, Segment segment)
        {
            String tagName = segment.get("pattern-name");
            if (null == tagName) tagName = "segment";
            Element tag = element.getOwnerDocument().createElement(tagName);
            element.appendChild(tag);
            
            return tag;
        }
        
        public String serialize(org.w3c.dom.Node node)
        {
            return serializer.writeToString(node);
        }
        
    }
    
    public static class XMLVocabularyHTML extends XMLVocabulary
    {
        public XMLVocabularyHTML()
        {
            super();
            serializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        }
        
        public org.w3c.dom.Document createDocument()
        {
            document = dom.createDocument("http://www.w3.org/1999/xhtml", "html", null);
            content = document.createElement("body");
            document.getDocumentElement().appendChild(content);
            
            return document;
        }
        
        public Element appendElement(Element element, Segment segment)
        {
            String tagName = segment.get("pattern-name");
            if (null == tagName) tagName = "segment";
            Element tag = element.getOwnerDocument().createElement("span");
            tag.setAttribute("class", tagName);
            tag.setAttribute("title", tagName);
            element.appendChild(tag);
            
            return tag;
        }
        
        public String serialize(org.w3c.dom.Node node)
        {
            return serializer.writeToString(node).replace("\n", "<br/>");
        }
        
    }
    
    /**
     * Encode the document as an XML string with the segments tagged.
     *
     * It is the serialized version of the document from {@link #getXML(SegmentList)}
     *
     * @param segments the segments that will be marked as XML elements
     * @return serialized XML document
     */
    public String getXMLString(SegmentList segments)
    {
        return getXMLString(segments, new XMLVocabulary());
    }
    
    /**
     * Encode the document as an XML string with the segments tagged,
     * using the given XML vocabulary.
     *
     * It is the serialized version of the document from {@link #getXML(SegmentList)}
     *
     * @param segments the segments that will be marked as XML elements
     * @param format the XML vocabulary (format) to produce
     * @return serialized XML document
     */
    public String getXMLString(SegmentList segments, XMLVocabulary format)
    {
        return format.serialize(getXML(segments, format));
    }
    
    /**
     * Document collection. Most analysis techniques work better when applying them to collections of documents than to isolated texts.
     */
    public static class Collection implements java.lang.Iterable<Document>
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
                System.err.println("Loading file " + i + " of " + files.length + ": " + files[i].getName());
                document = null;
                try {
                    if (files[i].getName().endsWith(".txt")) { 
                        document = new Document(files[i], null);
                    }
                }
                catch (java.io.FileNotFoundException e) {
                    System.err.println("File not found: " + files[i].getName());
                }
                
                if (document != null) {
                    identifier = files[i].getName();
                    documents.put(identifier, document);
                }
            }
        }
        
        public int size()
        {
            return documents.size();
        }
        
        /**
         * Get an iterator for all documents in the collection.
         *
         * @return iterator for the documents
         */
        public Iterator<Document> iterator()
        {
            return documents.values().iterator();
        }
        
        /**
         * Get an iterator for all identifiers of the documents in the collection.
         *
         * @return iterator for the identifiers
         */
        public Iterator<String> identifierIterator()
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
            if (null == identifier) {
                if (documents.isEmpty()) return null;
                else                     return iterator().next();
            }
            
            return documents.get(identifier);
        }
        
    }
    
}
    
