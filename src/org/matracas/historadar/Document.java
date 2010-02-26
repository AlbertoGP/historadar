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
import java.util.Iterator;

/**
 * Source text document.
 */
public class Document
{
    private String plainText;
    
    /**
     * Constructs a new document from a file.
     *
     * At the moment only plain text files are supported.
     */
    public Document(File file)
        throws java.io.FileNotFoundException, java.io.IOException
    {
        plainText = "";
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            plainText += line + "\n";
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
     * Set the content of the document from a plain text version.
     *
     * @param text plain text content as a string
     */
    public void setPlainText(String text)
    {
        plainText = text;
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
                if (files[i].getName().endsWith(".txt")) try {
                        identifier = files[i].getName();
                        document = new Document(files[i]);
                        documents.put(identifier, document);
                    }
                    catch (java.io.FileNotFoundException e) {
                        System.err.println("File not found: " + files[i].getName());
                    }
                    catch (java.io.IOException e) {
                        System.err.println("IO error: " + e);
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
    
