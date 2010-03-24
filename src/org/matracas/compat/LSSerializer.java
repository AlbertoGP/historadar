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

package org.matracas.compat;

/**
 * Compatibility implementation of LSSerializer for use with Java 1.5
 *
 */
public class LSSerializer implements org.w3c.dom.ls.LSSerializer
{
    protected org.w3c.dom.DOMConfiguration configuration;
    protected org.w3c.dom.ls.LSSerializerFilter filter;
    protected String newLine;
    
    public LSSerializer()
    {
        configuration = new DOMConfiguration();
        filter = null;
        newLine = "\n";
    }
    
    public class DOMConfiguration implements org.w3c.dom.DOMConfiguration
    {
        private java.util.Map<String, Object> configuration;
        
        public DOMConfiguration()
        {
            configuration = new java.util.Hashtable<String, Object>();
        }
        
        public boolean canSetParameter(String name, Object value)
        {
            return true;
        }
        
        public Object getParameter(String name)
        {
            return configuration.get(name);
        }
        
        public class DOMStringList implements org.w3c.dom.DOMStringList
        {
            java.util.Vector<String> strings;
            
            public DOMStringList()
            {
                strings = new java.util.Vector<String>();
            }
            
            public DOMStringList(java.util.Vector<String> strings)
            {
                this.strings = strings;
            }
            
            public DOMStringList(java.util.Set<String> strings)
            {
                this();
                this.strings.addAll(strings);
            }
            
            public boolean contains(String str)
            {
                for (String string : strings) {
                    if (string.equals(str)) return true;
                }
                
                return false;
            }
            
            public int getLength()
            {
                return strings.size();
            }
            
            public String item(int index)
            {
                return strings.get(index);
            }
        }
        
        public org.w3c.dom.DOMStringList getParameterNames()
        {
            return new DOMStringList(configuration.keySet());
        }
        
        public void setParameter(String name, Object value)
        {
            configuration.put(name, value);
        }
    }
    
    public org.w3c.dom.DOMConfiguration getDomConfig()
    {
        return configuration;
    }
    
    public org.w3c.dom.ls.LSSerializerFilter getFilter()
    {
        return filter;
    }
    
    public String getNewLine()
    {
        return newLine;
    }
    
    public void setFilter(org.w3c.dom.ls.LSSerializerFilter filter)
    {
        this.filter = filter;
    }
    
    public void setNewLine(String newLine)
    {
        this.newLine = newLine;
    }
    
    public boolean write(org.w3c.dom.Node node, org.w3c.dom.ls.LSOutput destination)
    {
        return false;
    }
    
    public String writeToString(org.w3c.dom.Node node)
    {
        java.io.StringWriter result = new java.io.StringWriter();
        javax.xml.transform.dom.DOMSource source = new javax.xml.transform.dom.DOMSource(node);
        javax.xml.transform.stream.StreamResult output = new javax.xml.transform.stream.StreamResult(result);
        try {
            javax.xml.transform.Transformer transformer = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
            Object outputXmlDeclaration = getDomConfig().getParameter("xml-declaration");
            if (outputXmlDeclaration != null
                && outputXmlDeclaration instanceof Boolean
                && (Boolean) outputXmlDeclaration == Boolean.FALSE) {
                transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "yes");
            }
            transformer.transform(source, output);
        }
        catch (Exception e) {
            return "<error>" + e.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;") + "</error>";
        }
        
        return result.toString();
    }
    
    public boolean writeToURI(org.w3c.dom.Node node, String uri)
    {
        return false;
    }
}
