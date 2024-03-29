///////////////////////////////////////////////////////////////////////////
//
//   Copyright 2010 Alberto González Palomo
//   Author: Alberto González Palomo - http://matracas.org/
//   Copyright 2010 Johannes Braunias
//   Author: Johannes Braunias - https://historadar.googlecode.com/people/list
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
import org.matracas.historadar.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Metadata extracted from a document.
 *
 */
public class Metadata
{
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
     * Document.Metadata entries = metadata.getMetadata(document);
     * Document.Values values;
     * values = entries.get(metadata.date);
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
    public Document.Metadata getMetadata(Document document)
    {
        Document.Metadata entries = new Document.Metadata();
        String plainText = document.getPlainText();
                
        String monthString = "";
        String dayString = "";
        String dayOfWeekString = "";
        String yearString = "";
        String hourString = ""; 
		String minuteString = "";
		String dayTimeString = ""; // "a" OR "p" (a.m. or p.m.)
		
		Integer month = 0;
		Integer day = 0;
		Integer year = 0;
		Integer hour = 0;   // > 12 if dayTimeString.contains("p")
		Integer minute = 0;
		
		try {
	        entries.add(entries.title, "NO TITLE FOUND YET");
	        
	        Pattern pattern = Pattern.compile("held.{1,60}?((\\w+?day|\\w+?uary|march|april|may|june|july|august|\\w+?mber|\\w+?ober)\\b.+?(?:[.,] ?m[.,]?|oon))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ); //Thanks to http://www.regular-expressions.info/java.html
	        Matcher matcher = pattern.matcher(plainText);
	        if (matcher.find()){
	        	String plainDate = matcher.group(1);
	        	entries.add(entries.date, plainDate);
		        
	        	// Day of week
	        	pattern = pattern.compile("\\w+day", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		            dayOfWeekString = matcher.group(0);
		            // entries.add(entries.date, dayOfWeekString);
			    }
		        
		        // Month
		        pattern = pattern.compile("\\w+ry|\\w+ber|\\w+rch|\\w+ril|may|\\wune|\\w+ly|\\w+ust", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		            monthString = matcher.group(0);
		            // entries.add(entries.date, monthString);
				}
		        
		        // Year:
		        pattern = pattern.compile("(?:\\w+ry|\\w+ber|\\w+rch|\\w+ril|may|\\wune|\\w+ly|\\w+ust).+([12]\\d{3})", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		            yearString = matcher.group(1);
		            // entries.add(entries.date, yearString);
				}
		        
		        // Day of month: (digits before or after month)
		        pattern = pattern.compile("(\\d\\d?).{1,5}(?:\\w+ry|\\w+ber|\\w+rch|\\w+ril|may|\\wune|\\w+ly|\\w+ust)|(?:\\w+ry|\\w+ber|\\w+rch|\\w+ril|may|\\wune|\\w+ly|\\w+ust)\\s{1,2}(\\d\\d?)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		        	if (matcher.start(1) == matcher.end(1)){
		        		dayString = matcher.group(2);
		        	}
		        	else {
		        		dayString = matcher.group(1);
		        	}
		            // entries.add(entries.date, dayString);
				}
		        
		        // Hour and minute
		        pattern = pattern.compile("at\\s+(\\d{1,2})[- *o\\\\.]{0,4}(\\d*) ?(a|p|n)?", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		            hourString = matcher.group(1);
		            minuteString = matcher.group(2);
		            // entries.add(entries.date, hourString + ":" + minuteString);
				}
		        
		        // Time of day
		        pattern = pattern.compile("(a\\..*|p\\..*|noon.*)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE);
		        matcher = pattern.matcher(plainDate);
		        if(matcher.find()){
		            if (matcher.group(1).toLowerCase().startsWith("a")) {
						dayTimeString = "a.m.";
					} else if (matcher.group(1).toLowerCase().startsWith("p")) {
						dayTimeString = "p.m.";
					} else if (matcher.group(1).toLowerCase().startsWith("n")) {
						dayTimeString = "noon";
					} else {
						dayTimeString = "I have no idea how you can possibly get here.";
					}
		            // entries.add(entries.date, dayTimeString);
				}
		        
		        
		        /* Conversion of time values to Integers */
		        // Month:
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
		        
		        if (!dayString.equals("")) {day = Integer.parseInt(dayString);}
		        if (!yearString.equals("")) {year = Integer.parseInt(yearString);}
		        if (!hourString.equals("")) {hour = Integer.parseInt(hourString);}
		        if (!minuteString.equals("")) {minute = Integer.parseInt(minuteString);}
		        
		        if (dayTimeString.toLowerCase().startsWith("p")) {
					hour += 12;
				}
		        
		        /* Adding time as split meta data */
		        entries.add(entries.date, String.format("%4d-%02d-%02d %d:%02d (%s)", year, month, day, hour, minute, dayTimeString));
	        }
		}
	    catch(Exception e){
	    	entries.add(entries.date, e.getLocalizedMessage());
	    	System.out.println(e.getLocalizedMessage());
	    }
	    return entries;
    }
    
}
