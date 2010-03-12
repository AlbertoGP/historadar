/**
 * 
 */
package org.matracas.historadar.nlp;

/**
 * @author Johannes
 *
 */
public class AttendanceName {
	public String title = new String();
	public String position = new String();
	public String firstName = new String();
	public String lastName = new String();

	public AttendanceName() {
		
	}
	
	public AttendanceName(String title, String position, String firstName, String lastName) {
		this.title = title;
		this.position = position;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	@Override
	public String toString(){
		return this.title + ", " + this.position + ", " + this.firstName + ", " + this.lastName;
	}
}
