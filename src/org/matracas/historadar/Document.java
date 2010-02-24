///////////////////////////////////////////////////////////////////////////
//
//   Copyright 2010 Alberto González Palomo
//   Author: Alberto González Palomo - http://matracas.org/
//
//   This file is part of Java Feedback Toolkit, abbreviated as JFT.
//
//   JFT is free software; you can redistribute it and/or modify
//   it under the terms of the GNU General Public License as published by
//   the Free Software Foundation; either version 3 of the License, or
//   (at your option) any later version.
//
//   JFT is distributed in the hope that it will be useful,
//   but WITHOUT ANY WARRANTY; without even the implied warranty of
//   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//   GNU General Public License for more details.
//
//   You should have received a copy of the GNU General Public License
//   along with JFT; if not, see <http://www.gnu.org/licenses/>.
//
/////////////////////////////////////////////////////////////////////////////

package org.matracas.historadar;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Document
{
    private String plainText;
    
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
        
    public String getPlainText()
    {
        return plainText;
    }
        
    public void setPlainText(String text)
    {
        plainText = text;
    }
    
    public static class Collection
    {
        private Map<String, Document> documents;
    
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
        
        public Iterator<Document> getDocumentIterator()
        {
            return documents.values().iterator();
        }
        
        public Document getDocument(String identifier)
        {
            return documents.get(identifier);
        }
        
    }
    
}
    
