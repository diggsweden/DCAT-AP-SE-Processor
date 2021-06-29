/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.rdf;

/**
 * Placeholder for cardinality values according to DCAT-AP-SE specification
 * 
 *  @see <a href="https://docs.dataportal.se/dcat/sv/">DCAT-AP-SE specification</a>
 *  
 * @author nacbr
 *
 */
public class Cardinality {
	
	private int min;
	private int max;
	
	private static final String LETTER_N = "n";
	private static final String DOUBLE_DOTS = "..";
	
	public Cardinality(int min, int max) {
		this.min = min;
		this.max = max;
	}
		
	public Cardinality(int min, String max) {
		this.min = min;
		
		if(max != null && max.equalsIgnoreCase(LETTER_N)) {
			//Symbolic max value to simplify comparison
			this.max = 65535;
		}
	}

	/**
	 * Gets the min value of this cardinality 
	 * 
	 * @return the min value
	 */
	public int getMin() {
		return min;
	}
	
	/**
	 * Gets the max value of this cardinality 
	 * 
	 * @return the max value
	 */
	public int getMax() {
		return max;
	}
	
	/**
	 * Convenience method for determining if cardinality is 1 

	 * @return boolean
	 */
	public boolean isOne() {
		return (min == max) && (min == 1);
	}

	/**
	 * Convenience method for determining if cardinality is 1 or more

	 * @return boolean
	 */
	public boolean isOneOrMore() {
		return (min == 1) && (max >= 1);
	}

	/**
	 * Convenience method for determining if cardinality is 0
	 * and upwards 
	 
	 * @return boolean
	 */
	public boolean isZeroOrMore() {
		return min == 0 && max >= 1;
	}
	
	/**
	 * Convenience method for determining if cardinality is 0
	 * or 1 
	 * 
	 * @return boolean
	 */
	public boolean isZeroOrOne() {
		return min == 0 && max == 1;
	}

	/**
	 * Convenience method for determining if a value is inside 
	 * the range of this cardinality
	 * @param value to be compared with this cardinality
	 * 
	 * @return boolean
	 */
	public boolean isInsideCardinality(int value) {
		return value >= min && value <= max;
	}
	
	/**
	 * Returns the min - max of this cardinality as either
	 * min..max or a number if min=max
	 * 
	 * @return String
	 */
	public String getInterval() {
		if(min == max) {
			return Integer.toString(min);
		}
		return Integer.toString(min) + DOUBLE_DOTS + ((max == 65535) ? LETTER_N : Integer.toString(max));
	}


}
