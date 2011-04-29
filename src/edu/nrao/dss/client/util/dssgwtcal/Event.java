package edu.nrao.dss.client.util.dssgwtcal;

import java.util.ArrayList;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

// an event represents an 'appointment' in the real world, and can map to one or more appointments.
// appointments map to blocks of time on our calendar.

public class Event {
	public int id;
	public String title;
	public String description;
	public Date start;
	public Date end;
	public Date start_day;
	public Date end_day;
	public boolean selected;
	private ArrayList<Appointment> appointments = new ArrayList<Appointment>();
	private long msInHour = 60 * 60 * 1000;
	private long msInDay = 24 * 60 * 60 * 1000;
	private String type;
	private String session_type;
	private String state;
	private DateRange[] dstFiveHourOffsets;

	private static final DateTimeFormat DATE_FORMAT = DateTimeFormat
			.getFormat("yyyy-MM-dd HH:mm:ss");

	public class DateRange {
		public Date start;
		public Date end;

		public DateRange(Date start, Date end) {
			this.start = start;
			this.end = end;
		}

		public DateRange(String start, String end) {
			this.start = DATE_FORMAT.parse(start);
			this.end = DATE_FORMAT.parse(end);
		}

		public boolean dateInRange(Date dt) {
			return ((dt.before(this.end)) && (dt.after(this.start)));
		}
	}

	public Event(int id, String title, String description, Date start,
			Date start_day, Date end, Date end_day, String type,
			String session_type, String state) {
		this.dstFiveHourOffsets = this.getDSTFiveHourOffsets();
		this.id = id;
		this.title = title;
		this.description = description;
		this.start = start;
		this.start_day = start_day;
		this.end = getSafeEndDate(end); // don't end at midnight, but 1 min.
										// before
		this.end_day = end_day;
		this.type = type;
		this.session_type = session_type;
		this.state = state;
		createAppointments();
	}

	// TODO: how to get this to work forever?
	// TODO: shouldn't all this related time shit live in it's own library?
	// Specify the date ranges where the offset between ET & UT is 5 hours, not 4.
	// Dates according to:
	// http://www.usno.navy.mil/USNO/astronomical-applications/astronomical-information-center/daylight-time
	// 2010 March 14 November 7
	// 2011 March 13 November 6
	// 2012 March 11 November 4
	// 2013 March 10 November 3
	// 2014 March 9 November 2
	// 2015 March 8 November 1
	private DateRange[] getDSTFiveHourOffsets() {
		DateRange[] drs = {
				new DateRange("2009-11-01 00:00:00", "2010-03-14 00:00:00"),
				new DateRange("2010-11-07 00:00:00", "2011-03-13 00:00:00"),
				new DateRange("2011-11-06 00:00:00", "2012-03-11 00:00:00"),
				new DateRange("2012-11-04 00:00:00", "2013-03-10 00:00:00"),
				new DateRange("2013-11-03 00:00:00", "2014-03-09 00:00:00"),
				new DateRange("2014-11-02 00:00:00", "2015-03-08 00:00:00") };
		return drs;
	}

	// TODO: Total fucking kluge: when we calculate the next day using
	// Date.getTime() + milliseconds
	// the Date class will take DST into account, so watch for these offsets
	private Date[] getDSTPositiveOffsets() {
		Date[] dts = { DATE_FORMAT.parse("2010-03-15 01:00:00"),
				DATE_FORMAT.parse("2011-03-14 01:00:00"),
				DATE_FORMAT.parse("2012-03-12 01:00:00"),
				DATE_FORMAT.parse("2013-03-11 01:00:00"),
				DATE_FORMAT.parse("2014-03-10 01:00:00"),
				DATE_FORMAT.parse("2015-03-09 01:00:00") };
		return dts;
	}

	private Date[] getDSTNegativeOffsets() {
		Date[] dts = { DATE_FORMAT.parse("2009-11-01 23:00:00"),
				DATE_FORMAT.parse("2010-11-07 23:00:00"),
				DATE_FORMAT.parse("2011-11-06 23:00:00"),
				DATE_FORMAT.parse("2012-11-04 23:00:00"),
				DATE_FORMAT.parse("2013-11-03 23:00:00"),
				DATE_FORMAT.parse("2014-11-02 23:00:00") };
		return dts;
	}

	// Figure out wether DST applies for this date or not
	private long getGmtOffsetMs(Date dt) {
		long offset = 4;
		for (int i = 0; i < dstFiveHourOffsets.length; i++) {
			if (dstFiveHourOffsets[i].dateInRange(dt)) {
				offset = 5;
			}
		}

		return offset * 60 * 60 * 1000; // milliseconds
	}

	private long getGmtOffsetMs(long day) {
		Date dt = new Date(day * msInDay);
		return getGmtOffsetMs(dt);
	}

	private long getDayOffset(Date dt) {
		return (dt.getTime() - getGmtOffsetMs(dt)) % msInDay;
	}

	// avoid wrap-around when a time block ends on midnight
	private Date getSafeEndDate(Date end) {
		if (getDayOffset(end) == 0) {
			return new Date(end.getTime() - (60 * 1000)); // loose a minute!
		} else {
			return end;
		}
	}

	// Date -> GMT day number
	private long getDay(Date dt) {
		long time = dt.getTime();
		return (time - getGmtOffsetMs(dt)) / msInDay;
	}

	// Date -> GMT day number (taking more care w/ UTC offset)
	private long getDay(Date dt, Date offsetDt) {
		long time = dt.getTime();
		return (time - getGmtOffsetMs(offsetDt)) / msInDay;
	}

	// GMT day number -> last few seconds of that day as Date
	private Date getEndDayDate(long day) {
		// the next day is from day + i, so get that, then subtract a few
		// seconds
		long gmtDay = (msInDay * (day + 1)) + getGmtOffsetMs(day + 1);
		long msOffset = 60 * 1000; // 1 min.
		return new Date(gmtDay - msOffset);

	}

	private int getDaySpan() {
		long dayStart = getDay(start);
		long dayEnd = getDay(end, start);
		return (int) (dayEnd - dayStart);

	}

	private Date getNextDay(Date day, int days) {
		// here's what you do if you wouldn't have to worry about DST
		Date nextDay = new Date(day.getTime() + (days * msInDay));
		// but you do have to worry about; viva la Kluge.
		for (Date dt : getDSTPositiveOffsets()) {
			if (nextDay.equals(dt)) {
				nextDay = new Date(nextDay.getTime() - msInHour);
			}
		}
		for (Date dt : getDSTNegativeOffsets()) {
			if (nextDay.equals(dt)) {
				nextDay = new Date(nextDay.getTime() + msInHour);
			}
		}
		return nextDay;
	}

	private void createAppointments() {
		// map this single event to one or more appointments according to
		// whether or not the event spans more then one day.
		int daySpan = getDaySpan();
		Date apptStart;
		Date apptEnd;
		for (int i = 0; i <= daySpan; i++) {
			// first?
			if (i == 0) {
				apptStart = new Date(start.getTime());
			} else {
				// all the next appointments start at the start of the day
				apptStart = getNextDay(start_day, i);
			}
			// last?
			if (i == daySpan) {
				apptEnd = new Date(end.getTime());
			} else {
				// all continuing appointments start at the end of the day
				// apptEnd = getEndDayDate(dayStart + i);
				Date nextDay = new Date(end_day.getTime() + (i * msInDay));
				long msOffset = 60 * 1000; // 1 min.
				apptEnd = new Date(nextDay.getTime() - msOffset);
			}

			// our Event becomes one or more of their Appointments
			Appointment appt = new Appointment();
			appt.setEventId(id);
			appt.setStart(apptStart);
			appt.setEnd(apptEnd);

			// TODO: format tittle and description better
			appt.setTitle(title); // title + " : " + Integer.toString(i));
			String desc = description;
			if (daySpan > 0) {
				desc = desc + " (Day " + Integer.toString(i + 1) + ")";
			}
			appt.setDescription(desc);
            appt.addStyleName(getStyleName());
			appointments.add(appt);
		}

	}

	// TODO: need to improve the way we indicate period attributes
	public String getStyleName() {
		String style = "";
		if (type != "not windowed!") {
			if (type == "default period") {
				style = "gwt-appointment-green";
			} else {
				style = "gwt-appointment-yellow";
			}
		} else {
			if (session_type.contains("O")) {
				// Open Session
				style = "gwt-appointment-blue";
			} else if (session_type.contains("E")) {
				// Elective Session
				style = "gwt-appointment-darkpurple";
			} else {
				// Fixed Session
				style = "gwt-appointment-red";
			}
		}
		// Pending state wins out over everything else
		if (state.contains("P")) {
			style = "gwt-appointment-orange";
		}	
		return style;
	}
	
	public ArrayList<Appointment> getAppointments() {
		return appointments;
	}
	
	public String getType() {
		return type;
	}
	
	public String getSessionType() {
		return session_type;
	}
	
	public String getState() {
		return state;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setSessionType(String session_type) {
		this.session_type = session_type;
	}
	
	public void setState(String state) {
		this.state = state;
	}
}
