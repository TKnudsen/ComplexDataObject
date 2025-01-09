package com.github.TKnudsen.ComplexDataObject.model.io.parsers.objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * In line with the solution to parse many dates programatically.
 * https://stackoverflow.com/questions/3389348/parse-any-date-in-java
 * 
 * Focus on European date notation. The US standard MM/dd/YYYY is not supported.
 * 
 * To reduce complexity the parser replaces "\\", "_", "/", and "." by "-";
 * 
 * Older versions of the parser defined a series of date formats. This is
 * applied if new patterns do not match
 * 
 */
public class DateParser implements IObjectParser<Date> {

	private static LinkedHashMap<String, String> DATA_FORMAT_PATTERNS = dateFormatPatterns();

	private static LinkedHashMap<String, String> dateFormatPatterns() {

		LinkedHashMap<String, String> dateFormats = new LinkedHashMap<String, String>();

		dateFormats.put("^\\d{8}$", "yyyyMMdd");
		dateFormats.put("^\\d{1,2}-\\d{1,2}-\\d{4}$", "dd-MM-yyyy");
		dateFormats.put("^\\d{1,2}\\\\d{1,2}\\\\d{4}$", "dd\\MM\\yyyy");
		dateFormats.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		dateFormats.put("^\\d{4}-\\d{1,2}$", "yyyy-MM");
		dateFormats.put("^\\d{4}-\\d{1,2}-\\d{1,2}$", "yyyy-MM-dd");
		dateFormats.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$", "dd MMM yyyy");
		dateFormats.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$", "dd MMMM yyyy");
		dateFormats.put("^\\d{12}$", "yyyyMMddHHmm");
		dateFormats.put("^\\d{8}\\s\\d{4}$", "yyyyMMdd HHmm");
		dateFormats.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}$", "dd-MM-yyyy HH:mm");
		dateFormats.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy-MM-dd HH:mm");
		dateFormats.put("^\\d{4}/\\d{1,2}/\\d{1,2}\\s\\d{1,2}:\\d{2}$", "yyyy/MM/dd HH:mm");
		dateFormats.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMM yyyy HH:mm");
		dateFormats.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$", "dd MMMM yyyy HH:mm");
		dateFormats.put("^\\d{14}$", "yyyyMMddHHmmss");
		dateFormats.put("^\\d{8}\\s\\d{6}$", "yyyyMMdd HHmmss");
		dateFormats.put("^\\d{8}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyyMMdd HH:mm:ss");
		dateFormats.put("^\\d{1,2}-\\d{1,2}-\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd-MM-yyyy HH:mm:ss");
		dateFormats.put("^\\d{4}-\\d{1,2}-\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy-MM-dd HH:mm:ss");
		dateFormats.put("^\\d{4}:\\d{1,2}:\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$", "yyyy:MM:dd HH:mm:ss");
		dateFormats.put("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMM yyyy HH:mm:ss");
		dateFormats.put("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$", "dd MMMM yyyy HH:mm:ss");
		dateFormats.put("^\\d{1,2}-\\d{4}$", "MM-yyyy");

		return dateFormats;
	}

	private static LinkedHashMap<String, String> dateFormatExamples() {

		LinkedHashMap<String, String> dateFormats = new LinkedHashMap<String, String>();

		dateFormats.put("20201119", "yyyyMMdd");
		dateFormats.put("19-11-2020", "dd-MM-yyyy");
		dateFormats.put("19.11.2020", "dd.MM.yyyy");
		dateFormats.put("19_11_2020", "dd_MM_yyyy");
		dateFormats.put("19/11/2020", "dd/MM/yyyy");
		dateFormats.put("19\\11\\2020", "dd\\MM\\yyyy");
		dateFormats.put("2020-11-19", "yyyy-MM-dd");
		dateFormats.put("2020:11:19", "yyyy:MM:dd");
		dateFormats.put("2020-11", "yyyy-MM");
		dateFormats.put("2020/11/19", "yyyy/MM/dd");
		dateFormats.put("2020-11-19", "yyyy-MM-dd");
		dateFormats.put("19 Nov 2020", "dd MMM yyyy");
		dateFormats.put("19 November 2020", "dd MMMM yyyy");
		dateFormats.put("202011191304", "yyyyMMddHHmm");
		dateFormats.put("20201119 1304", "yyyyMMdd HHmm");
		dateFormats.put("19.11.2020 13:04:05", "dd.MM.yyyy HH:mm");
		dateFormats.put("19-11-2020 13:04:05", "dd-MM-yyyy HH:mm");
		dateFormats.put("2020-11-19 13:04", "yyyy-MM-dd HH:mm");
		dateFormats.put("2020/11/19 13:04", "yyyy/MM/dd HH:mm");
		dateFormats.put("19 Nov 2020 13:04", "dd MMM yyyy HH:mm");
		dateFormats.put("19 November 2020 13:04", "dd MMMM yyyy HH:mm");
		dateFormats.put("20201119130405", "yyyyMMddHHmmss");
		dateFormats.put("20201119 130405", "yyyyMMdd HHmmss");
		dateFormats.put("20201119 13:04:05", "yyyyMMdd HH:mm:ss");
		dateFormats.put("19-11-2020 13:04:05", "dd-MM-yyyy HH:mm:ss");
		dateFormats.put("2020-11-19 13:04:05", "yyyy-MM-dd HH:mm:ss");
		dateFormats.put("2020/11/19 13:04:05", "yyyy/MM/dd HH:mm:ss");
		dateFormats.put("19 Nov 2020 13:04:05", "dd MMM yyyy HH:mm:ss");
		dateFormats.put("19 November 2020 13:04:05", "dd MMMM yyyy HH:mm:ss");
		dateFormats.put("04.1986", "MM-yyyy");
		dateFormats.put("04-1986", "MM-yyyy");

		return dateFormats;
	}

	public static void main(String[] args) {
		DateParser dateParser = new DateParser();

		Map<String, String> dateFormatExamples = dateFormatExamples();
		for (String sample : dateFormatExamples.keySet()) {
			String pattern = dateFormatExamples.get(sample);

			Date apply = dateParser.apply(sample);

			System.out.println(dateParser.getClass().getSimpleName() + ": testing date " + sample + " of pattern "
					+ pattern + ", parsed result: " + apply);

			apply = dateParser.apply(sample);
		}
	}

	private static SimpleDateFormat zzz = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
	private static DateFormat ISO0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	private static DateFormat ISO1a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static DateFormat ISO1b = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
	private static DateFormat IS02 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	private static DateFormat IS03 = new SimpleDateFormat("yyyy-MM-dd HH");
	private static DateFormat IS04 = new SimpleDateFormat("yyyy-MM-dd");
	private static DateFormat IS05 = new SimpleDateFormat("yyyy-MM");
	private static DateFormat IS06 = new SimpleDateFormat("dd.MM.yyyy");
	private static DateFormat IS06b = new SimpleDateFormat("dd_MM_yyyy");
	private static DateFormat IS07 = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	private static DateFormat IS08 = new SimpleDateFormat("MM.dd.yyyy");
	private static DateFormat IS09a = new SimpleDateFormat("dd/MM/yyyy");
	private static DateFormat IS09b = new SimpleDateFormat("dd\\MM\\yyyy");
	private static DateFormat IS09c = new SimpleDateFormat("dd-MM-yyyy");

	public static String determineDateFormat(String dateString) {
		for (String regexp : DATA_FORMAT_PATTERNS.keySet()) {
			if (dateString.toLowerCase().matches(regexp)) {
				return DATA_FORMAT_PATTERNS.get(regexp);
			}
		}

		return null;
	}

	@Override
	public synchronized Date apply(Object object) {
		if (object == null)
			return null;
		if (object instanceof Date)
			return new Date(((Date) object).getTime());
		if (object instanceof Long)
			return new Date((long) object);

		Date date = null;

		String interpretableString = String.valueOf(object);
		String pruned = interpretableString.replace("T", " ");
		pruned = pruned.replace("\\", "-");
		pruned = pruned.replace("-", "-");
		pruned = pruned.replace("_", "-");
		pruned = pruned.replace("/", "-");
		pruned = pruned.replace(".", "-");
		pruned = pruned.trim();

		String dateFormatString = determineDateFormat(pruned);

		if (dateFormatString != null) {
			DateFormat dateFormat = new SimpleDateFormat(dateFormatString);
			try {
				synchronized (dateFormat) {
					date = dateFormat.parse(pruned);
				}
			} catch (ParseException pe0) {
			}
		} else {
			// alternative
			if (pruned.length() == 23)
				try {
					// 2007-01-01 00:00:00:000
					synchronized (ISO0) {
						date = ISO0.parse(pruned);
					}
				} catch (ParseException pe0) {
				}
			else if (pruned.length() == 19) {
				try {
					// 2007-01-01 00:00:00
					synchronized (ISO1a) {
						date = ISO1a.parse(pruned);
					}
				} catch (ParseException pe8) {
				}
				if (date == null)
					try {
						// 2007:01:01 00:00:00
						synchronized (ISO1b) {
							date = ISO1b.parse(pruned);
						}
					} catch (ParseException pe8) {
					}
			} else if (pruned.length() == 16)
				try {
					// 2007-01-01 00:00
					synchronized (IS02) {
						date = IS02.parse(pruned);
					}
				} catch (ParseException pe8) {
					try {
						// 2007.01.01 00:00
						date = IS07.parse(pruned);
					} catch (ParseException pe7) {
					}
				}
			else if (pruned.length() == 13)
				try {
					// 2007-01-01 00
					synchronized (IS03) {
						date = IS03.parse(pruned);
					}
				} catch (ParseException pe8) {
				}
			else if (pruned.length() == 10)
				try {
					// 2007-01-01
					synchronized (IS04) {
						date = IS04.parse(pruned);
					}
				} catch (ParseException pe8) {
					try {
						// 13.01.1969
						synchronized (IS06) {
							date = IS06.parse(pruned);
						}
					} catch (ParseException pe) {
						try {
							// 01_13_1969
							synchronized (IS06b) {
								date = IS06b.parse(pruned);
							}
						} catch (ParseException pe_) {
							try {
								// 01.13.1969
								synchronized (IS08) {
									date = IS08.parse(pruned);
								}
							} catch (ParseException pe__) {
								try {
									// 13/01/1969
									synchronized (IS09a) {
										date = IS09a.parse(pruned);
									}
								} catch (ParseException pe___) {
									try {
										// 13\01\1969
										synchronized (IS09b) {
											date = IS09b.parse(pruned);
										}
									} catch (ParseException pe____) {
										try {
											// 13-01-1969
											synchronized (IS09c) {
												date = IS09c.parse(pruned);
											}
										} catch (ParseException pe_____) {
										}
									}
								}
							}
						}
					}
				}
			else if (pruned.length() == 7)
				try {
					// 2007-01
					synchronized (IS05) {
						date = IS05.parse(pruned);
					}
				} catch (ParseException parseException) {
				}
			else {
				try {
					// Thu Apr 13 00:00:00 CEST 2017
					synchronized (zzz) {
						date = zzz.parse(String.valueOf(object));
					}
				} catch (ParseException parseException) {
				}
			}
		}

		if (date == null && object.toString().contains("+"))
			return apply(object.toString().subSequence(0, object.toString().indexOf("+")));
		else if (date == null && object.toString().contains("-"))
			return apply(object.toString().subSequence(0, object.toString().indexOf("-")));


		return date;
	}

	@Override
	public Class<Date> getOutputClassType() {
		return Date.class;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}

}
