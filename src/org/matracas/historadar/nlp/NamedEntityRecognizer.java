/**
 * 
 */
package org.matracas.historadar.nlp;
import java.sql.Time;
import java.text.DateFormat;
import java.util.*;
import org.matracas.historadar.Document;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Souhail
 *
 */
public class NamedEntityRecognizer {

	/** Returns a string containing the attendance list.
	 * @param document
	 * @return
	 */
	public String getAttendancePart(Document document){
		String attendanceList = "";
		
		Pattern pattern = Pattern.compile("present ?:(.*?)(\f|[1\\]] ?\\.)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		Matcher matcher = pattern.matcher(document.getPlainText());
		
        if (matcher.find()){
	        attendanceList = matcher.group(1);
        }
        
        return attendanceList;
    }
	
	public ArrayList<String> getAttendanceNames(String attendancePart){
		ArrayList<String> attendanceNames = new ArrayList<String>();
		Pattern pattern = Pattern.compile("([A-Z][ .]{1,2}\\. ?(?:[A-Z]{1,2}\\.)?)\\s+([A-Z]+[ -]?[A-Z]*)(\\*?)([,.])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		Matcher matcher = pattern.matcher(attendancePart);
		
        while (matcher.find()){
	        attendanceNames.add(matcher.group(0));
        }
        
        return attendanceNames;
	}
	
	public ArrayList<String> GetAllAttendanceParts(Document.Collection documentCollection){
		ArrayList<String> allAttendanceParts = new ArrayList<String>();
		
		return allAttendanceParts;
	}

	
}
	    





