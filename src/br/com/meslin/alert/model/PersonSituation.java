/**
 * 
 */
package br.com.meslin.alert.model;

import java.util.HashSet;

/**
 * @author meslin
 *
 */
public class PersonSituation extends MobileObject {
	/** this is new person? */
	private boolean isNewPerson;
	/** the new groups of this person */
	private HashSet<Integer> difGroups;

	/**
	 * Constructor
	 * @param mobileObject
	 * @param isNewPerson
	 * @param difGroups
	 */
	public PersonSituation(MobileObject mobileObject, Boolean isNewPerson, HashSet<Integer> difGroups) {
		super(mobileObject);
		this.isNewPerson = isNewPerson;
		this.difGroups = difGroups;
	}

	/**
	 * @return the difGroups
	 */
	public HashSet<Integer> getDifGroups() {
		return difGroups;
	}

	/**
	 * @param difGroups the difGroups to set
	 */
	public void setDifGroups(HashSet<Integer> difGroups) {
		this.difGroups = difGroups;
	}

	/**
	 * @return the isNewPerson
	 */
	public boolean isNewPerson() {
		return isNewPerson;
	}

	/**
	 * @param isNewPerson the isNewPerson to set
	 */
	public void setNewPerson(boolean isNewPerson) {
		this.isNewPerson = isNewPerson;
	}
}
