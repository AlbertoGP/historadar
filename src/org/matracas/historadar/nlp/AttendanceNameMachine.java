/**
 * 
 */
package org.matracas.historadar.nlp;
import java.sql.Time;
import java.text.DateFormat;
import java.util.*;
import org.matracas.historadar.Document;

import com.sun.org.apache.bcel.internal.classfile.JavaClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Souhail
 *
 */
public class AttendanceNameMachine {
	ArrayList<String> titles;
	ArrayList<String> positions;
	ArrayList<String> firstNames;
	ArrayList<String> lastNames;
	
	/** The constructor processes all documents for the attendance part and
	 * tries to get a list of names out of it which can be accessed through the member methods.
	 */
	public AttendanceNameMachine(Document.Collection documentCollection) {
		ArrayList<String> allAttendanceBlocksArray = getAllAttendanceBlocks(documentCollection);
		String allAttendanceBlocks = new String();
		for (String attendanceBlock : allAttendanceBlocksArray) {
			allAttendanceBlocks += ", " + attendanceBlock;
		}
		
		titles = getTitles(allAttendanceBlocks);
		positions = getPositions(allAttendanceBlocks);
		firstNames = getFirstNames(allAttendanceBlocks);
		lastNames = getLastNames(allAttendanceBlocks);
		
	}
	
	/** Returns a string containing the attendance list.
	 * @param document
	 * @return
	 */
	private String getAttendanceBlock(Document document){
		String attendanceBlock = "";
		
		Pattern pattern = Pattern.compile("present ?:(.*?)(\f|[1\\]] ?\\.)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		Matcher matcher = pattern.matcher(document.getPlainText());
		
        if (matcher.find()){
	        attendanceBlock = matcher.group(1);
        }
        
        return attendanceBlock;
    }
	
	/**
	 * @param documentCollection
	 * @return
	 */
	private ArrayList<String> getAllAttendanceBlocks(Document.Collection documentCollection){
		ArrayList<String> allAttendanceBlocks = new ArrayList<String>();
		
		Iterator<Document> documentIterator = documentCollection.getDocumentIterator();
		
		while (documentIterator.hasNext()) {
			allAttendanceBlocks.add(getAttendanceBlock(documentIterator.next()));
		}
		
		return allAttendanceBlocks;
	}
	
	public ArrayList<AttendanceName> getAttendanceNames(Document document){
		ArrayList<AttendanceName> namesList = new ArrayList<AttendanceName>();
		
		
		return namesList;
	}
	
	/**
	 * @param attendanceBlock
	 * @return
	 */
	public ArrayList<String> getAttendanceNames(String attendanceBlock){
		ArrayList<String> attendanceNames = new ArrayList<String>();
		Pattern pattern = Pattern.compile("([A-Z][ .]{1,2}\\. ?(?:[A-Z]{1,2}\\.)?)\\s+([A-Z]+[ -]?[A-Z]*)(\\*?)([,.])", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.UNICODE_CASE | Pattern.CANON_EQ);
		Matcher matcher = pattern.matcher(attendanceBlock);
		
        while (matcher.find()){
	        attendanceNames.add(matcher.group(0));
        }
        
        return attendanceNames;
	}
	
	/**
	 * @param attendanceBlocks
	 * @return
	 */
	@Deprecated
	public ArrayList<String> getAttendanceNames(ArrayList<String> attendanceBlocks){
		String oneLargeBlock = new String();
		
		for (String attendanceBlock : attendanceBlocks) {
			oneLargeBlock += attendanceBlock;
		}
		
		return getAttendanceNames(oneLargeBlock);
	}
	
	private ArrayList<String> getTitles(String allAttendanceBlocks){
		return firstNames;
		
	}
	
	private ArrayList<String> getPositions(String allAttendanceBlocks){
		return firstNames;
		
	}
	
	private ArrayList<String> getFirstNames(String allAttendanceBlocks){
		return firstNames;
		
	}

	private ArrayList<String> getLastNames(String allAttendanceBlocks){
		return firstNames;
		
	}
}
	    





