package com.github.TKnudsen.ComplexDataObject.data.interval;

/**
 * <p>
 * ComplexDataObject
 * </p>
 * 
 * <p>
 * Stores a numerical interval defined by a start and an end value. The start
 * value must not be larger than the end value.
 * </p>
 * 
 * <p>
 * Copyright (c) 2016-2019
 * </p>
 * 
 * @author Juergen Bernard
 * @version 1.03
 */
public class NumberInterval {

	protected Number start;
	protected Number end;
	protected String name;

	protected NumberInterval() {

	}

	public NumberInterval(Number start, Number end) {
		this(start, end, "");
	}

	public NumberInterval(Number start, Number end, String name) {
		this.start = start;
		this.end = end;

		if (end.doubleValue() < start.doubleValue())
			throw new IllegalArgumentException(getName() + ": start value must not be larger than end value");

		this.name = name;
	}

	public boolean contains(Number value) {
		if (value.doubleValue() >= start.doubleValue() && value.doubleValue() <= end.doubleValue())
			return true;
		else
			return false;
	}

	public Number getDuration() {
		return Math.abs(getEnd().doubleValue() - getStart().doubleValue());
	}

	public Number getStart() {
		return start;
	}

	public void setStart(Number start) {
		this.start = start;
	}

	public Number getEnd() {
		return end;
	}

	public void setEnd(Number end) {
		this.end = end;
	}

	@Override
	public int hashCode() {
		int hash = 17;
		hash += (39 * hash + start.doubleValue());
		hash *= (39 * hash + end.doubleValue());
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		if (!(obj instanceof NumberInterval))
			return false;

		NumberInterval that = (NumberInterval) obj;
		return this.hashCode() == that.hashCode();
	}

	@Override
	public String toString() {
		return "Interval: [" + start + "-" + end + "]";
	}

	public String getName() {
		return name;
	}

}
