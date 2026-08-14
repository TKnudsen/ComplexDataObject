package com.github.TKnudsen.ComplexDataObject.data.time;

import java.util.Date;

import com.github.TKnudsen.ComplexDataObject.data.interfaces.ISelfDescription;

/**
 * <p>
 * Temporal information about an event (with no temporal duration).
 * </p>
 * 
 * @version 1.02
 * @since 2016
 */
public class TimeEvent implements ISelfDescription {

	/**
	 * 
	 */
	private final String name;

	/**
	 * 
	 */
	private final String description;

	/**
	 * 
	 */
	private final Date date;

	public TimeEvent(String name, Date date, String description) {
		super();
		this.name = name;
		this.description = description;
		this.date = date;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public Date getDate() {
		return date;
	}

}
