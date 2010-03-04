/**
 * 
 */
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
        
        
//	        pattern = pattern.compile("([[A-Z]{1,2}\. ?(?:[A-Z]{1,2}\.)?)\s+([A-Z]+[ -]?[A-Z]*)(\*?)([,.])", Pattern.CASE_SENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
//	        matcher = pattern.matcher(plainName);
//        }
//	        if(matcher.find()){
//	            attendanceListString = matcher.group(1);
//		        InitialString = matcher.group(2);
//		        nameString = matcher.group(3);
//		        asteriskString = matcher.group(4);
//		        ponctuationString = matcher.group(5);
//		        
//		return attendanceList;
	}
}
