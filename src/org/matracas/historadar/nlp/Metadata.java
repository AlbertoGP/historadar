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

import java.sql.Time;
import java.text.DateFormat;
import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;
import org.matracas.historadar.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metadata extracted from a document.
 *
 * The metadata entries have the meanings defined by the
 * Dublin Core® Metadata Initiative, described in
 * <a href="http://dublincore.org/documents/dces/">http://dublincore.org/documents/dces/</a>
 */
public class Metadata
{
    private static final String DC_NAMESPACE = "http://purl.org/dc/elements/1.1/";
    public static final String contributor = DC_NAMESPACE + "contributor";
    public static final String coverage    = DC_NAMESPACE + "coverage";
    public static final String creator     = DC_NAMESPACE + "creator";
    public static final String date        = DC_NAMESPACE + "date";
    public static final String description = DC_NAMESPACE + "description";
    public static final String format      = DC_NAMESPACE + "format";
    public static final String identifier  = DC_NAMESPACE + "identifier";
    public static final String language    = DC_NAMESPACE + "language";
    public static final String publisher   = DC_NAMESPACE + "publisher";
    public static final String relation    = DC_NAMESPACE + "relation";
    public static final String rights      = DC_NAMESPACE + "rights";
    public static final String source      = DC_NAMESPACE + "source";
    public static final String subject     = DC_NAMESPACE + "subject";
    public static final String title       = DC_NAMESPACE + "title";
    public static final String type        = DC_NAMESPACE + "type";
    
    public Metadata(Document.Collection collection)
    {
        // TODO: linguistic analysis of the collection, if necessary
    }
    
    /**
     * Get the metadata entries from the given document,
     * based on the linguistic analysis of the collection.
     *
     * For example, to print all the "date" entries:
     * <pre>
     * Metadata metadata = new Metadata(documents);
     * Metadata.Entries entries = metadata.getMetadata(document);
     * Metadata.Values values;
     * values = entries.get(Metadata.date);
     * if (values != null) {
     *     Iterator valueIterator = values.iterator();
     *     while (valueIterator.hasNext()) {
     *         System.err.println("Date: " + valueIterator.next());
     *     }
     * }
     * </pre>
     *
     * @param document from which to extract the metadata
     * @return entries indexed by type
     */
    public Entries getMetadata(Document document) throws Exception
    {
        Entries entries = new Entries();
        String plainText = document.getPlainText();
                
        String monthString = "";
        String dayString = "";
        String yearString = "";
        String hourString = ""; 
		String minuteString = "";
		String dayTimeString = ""; // "a" OR "p" (a.m. or p.m.)
		
		Integer month;
		Integer day;
		Integer year;
		Integer hour;   // > 12 if dayTimeString.contains("p")
		Integer minute; 
		
		try {
	        entries.add(title, "NO TITLE FOUND YET");
	        
	        Pattern pattern = Pattern.compile("held.+?on.?((monday|tuesday|wednesday|thursday|friday|saturday|sunday).+?)present", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ); //Thanks to http://www.regular-expressions.info/java.html
	        Matcher matcher = pattern.matcher(plainText);
	        if (matcher.find()){
		        String plainDate = matcher.group(1);
	        	// entries.add(date, plainDate);
		        
		        // Friday, October 11, 1918, at 4 p.m.
		        pattern = pattern.compile("\\S+\\s+(\\S+)\\s+(\\d+)\\D+(\\d+)\\D+(.+)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		        matcher = pattern.matcher(plainDate);
		        
		        if(matcher.find()){
			        monthString = matcher.group(1);
			        dayString = matcher.group(2);
			        yearString = matcher.group(3);
			        String timeString = matcher.group(4);
			        
			        // Split up times like "3-15 p.m.": "1130 a.m.", "4 p.m.", "12 noon"
		        	pattern = pattern.compile("(.*)(a|p|n).*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		        	matcher = pattern.matcher(timeString);
		        	if(matcher.find()){
				        String hourMinutePart = matcher.group(1);
		        		dayTimeString = matcher.group(2).toLowerCase(); // must be "a" or "p" for calculating the integered hour time later
				        pattern = pattern.compile("(\\d{1,2})\\D*?(\\d*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
				        matcher = pattern.matcher(hourMinutePart);
				        if(matcher.find()){
				        	hourString = matcher.group(1);
				        	if (matcher.groupCount() > 1 && !matcher.group(2).equals("")){
				        		minuteString = matcher.group(2);
				        	}else{
				        		minuteString = "00";
				        	}
				        }
		        	}
			        // entries.add(date, dayString + " " + monthString + " " + yearString + " at " + hourString + ":" + minuteString + " " + dayTimeString + ".m.");
			        
			        
			        /* Conversion of time values to Integers */
			        
			        if (monthString.equalsIgnoreCase("january")) {
						month = 1;
					} else if (monthString.equalsIgnoreCase("february")) {
						month = 2;
					} else if (monthString.equalsIgnoreCase("march")) {
						month = 3;
					} else if (monthString.equalsIgnoreCase("april")) {
						month = 4;
					} else if (monthString.equalsIgnoreCase("may")) {
						month = 5;
					} else if (monthString.equalsIgnoreCase("june")) {
						month = 6;
					} else if (monthString.equalsIgnoreCase("july")) {
						month = 7;
					} else if (monthString.equalsIgnoreCase("august")) {
						month = 8;
					} else if (monthString.equalsIgnoreCase("september")) {
						month = 9;
					} else if (monthString.equalsIgnoreCase("october")) {
						month = 10;
					} else if (monthString.equalsIgnoreCase("november")) {
						month = 11;
					} else if (monthString.equalsIgnoreCase("december")) {
						month = 12;
					} else {
						month = 0;
					}
			        
			        day = Integer.parseInt(dayString);
			        year = Integer.parseInt(yearString);
			        hour = Integer.parseInt(hourString);
			        minute = Integer.parseInt(minuteString);
			        
			        if (dayTimeString.equalsIgnoreCase("p")) {
						hour += 12;
					}
			        
			        /* Adding time as split meta data */
			        entries.add(date, year.toString() + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " + hour.toString() + ":" + String.format("%02d", minute));
		        }
	        }
		}
	    catch(Exception e){
	    	entries.add(date, e.getLocalizedMessage());
	    }
	    finally{
	    	return entries;
	    }
    }
    
    public class Values extends Vector<String>
    {
    };
    
    /**
     * Collection of string entities indexed by class.
     */
    public class Entries extends Hashtable<String, Values>
    {
        /**
         * Add an entity of the given class to the collection.
         *
         * @param entryClass class for the entity
         * @param value the entity as a string
         * @return the collection so that we can chain calls to this function
         *         like entries.add("class1","value1").add("class2","value2)...
         */
        public Entries add(String entryClass, String value)
        {
            Values values = get(entryClass);
            if (null == values) {
                put(entryClass, values = new Values());
            }
            values.add(value);
            
            return this;
        }
        
    }
}
