// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

public class DcatCardinality {

    private Integer min;
    private Integer max;
	private final Condition condition; // Makes the min value conditional, most properties have no condition for cardinality 

    private static final String LETTER_N = "n";
    private static final String DOUBLE_DOTS = "..";
    public static final Integer MAX = Integer.MAX_VALUE; // The value used for max when the specification states no upper bound

	public record Condition(String property, String value) { }
	
	public DcatCardinality(Integer min, Integer max, Condition condition) {
		this.min = min;
		this.max = max;
		this.condition = condition;
	}

	/**
	 * Convenience method for determining if cardinality is 1 or more

	 * @return boolean
	 */
	public boolean isOneOrMore() {
		return min != null && min >= 1;
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
		if (MAX.equals(max)) {
			return min + DOUBLE_DOTS + LETTER_N;
		}
		if (min.equals(max)) {
			return min.toString();
		}
		return min + DOUBLE_DOTS + max;
	}

    public Integer getMin() {
        return min;
    }

    public Integer getMax() {
        return max;
    }

	public Condition getCondition() {
		return condition;
	}
}