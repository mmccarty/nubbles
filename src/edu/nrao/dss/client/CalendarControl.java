package edu.nrao.dss.client;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;

public class CalendarControl extends ContentPanel { //FormPanel {
	
	private Schedule schedule;
	
	// scoring sessions
	private ScoresComboBox scoresComboBox;
	
	public CalendarControl(Schedule sched) {
		schedule = sched;
		initLayout();
	}
	
	@SuppressWarnings("unchecked")
	public void showSessionScores(String session) {
		scoresComboBox.setSimpleValue(session);
		scoresComboBox.getSessionScores(session);
	}
	
	private void initLayout() {
		setHeading("Calendar Control");
		setBorders(true);
		setWidth("100%");
		
	    String leftWidth = "300px";
	    String rightWidth = "300px";
	    String bottomWidth = "100%";

	    /* Table layout for making this a 2x2 format, instead of a single column */
		TableLayout tb = new TableLayout(2);
		//tb.setWidth("50%");
		tb.setBorder(0);
		setLayout(tb);

		TableData tdLeft = new TableData();
		tdLeft.setVerticalAlign(VerticalAlignment.TOP);
		// TODO: why must I do this, just to get the two forms to share space?
		tdLeft.setColspan(1);
		tdLeft.setWidth(leftWidth);
		
		TableData tdRight = new TableData();
		tdRight.setVerticalAlign(VerticalAlignment.TOP);
		// TODO: why must I do this, just to get the two forms to share space?
		tdRight.setColspan(1);
		tdRight.setWidth(rightWidth);

		
		FormPanel left = new FormPanel();
		left.setHeaderVisible(false);
		left.setBodyBorder(false);
		
		FormPanel right = new FormPanel();
		right.setHeaderVisible(false);		
		/* end of 2x2 formatting stuff */
		
		// Date - when this changes, change the start of the calendar view
	    final DateField dt = new DateField();
	    dt.setValue(schedule.startCalendarDay);
	    dt.setFieldLabel("Start Date");
		dt.setToolTip("Set the schedule and display start day");
	    dt.addListener(Events.Valid, new Listener<BaseEvent>() {
	    	public void handleEvent(BaseEvent be) {
	            schedule.startCalendarDay = dt.getValue();
	            schedule.startVacancyDate = schedule.startCalendarDay;
	            schedule.vacancyControl.vacancyDate.setValue(schedule.startVacancyDate);
	            schedule.updateCalendar();
	    	}
	    });
	    right.add(dt);
	    //add(dt);

		// Days - when this changes, change the length of the calendar view
		final SimpleComboBox<Integer> days;
		days = new SimpleComboBox<Integer>();
		days.setForceSelection(true);
		days.add(1);
		days.add(2);
		days.add(3);
		days.add(4);
		days.add(5);
		days.add(6);
		days.add(7);
		days.setToolTip("Set the schedule and display duration");

		days.setFieldLabel("Days");
		days.setEditable(false);
		days.setSimpleValue(schedule.numCalendarDays);
		days.setTriggerAction(TriggerAction.ALL);
	    days.addListener(Events.Valid, new Listener<BaseEvent>() {
	    	public void handleEvent(BaseEvent be) {
	    		schedule.numCalendarDays = days.getSimpleValue(); 
	            schedule.updateCalendar();
	    	}
	    });
		//add(days);
	    right.add(days);
	    
	    add(right, tdRight);
		
		// Timezone - controls the reference for all the date/times in the tab
		final SimpleComboBox<String> tz;
		tz = new SimpleComboBox<String>();
		tz.setForceSelection(true);
		tz.setTriggerAction(TriggerAction.ALL);
		tz.add("UTC");
		tz.add("ET");
		tz.setToolTip("Set the timezone for all dates/times");

		tz.setFieldLabel("TZ");
		tz.setEditable(false);
		tz.setSimpleValue(schedule.timezone);
	    tz.addListener(Events.Valid, new Listener<BaseEvent>() {
	    	public void handleEvent(BaseEvent be) {
	    		//schedule.timezone = tz.getSimpleValue();
	    		schedule.setTimezone(tz.getSimpleValue());
	    		schedule.baseUrl = "/periods/" + schedule.timezone;
	        	schedule.scheduleExplorer.pe.setRootURL(schedule.baseUrl);
	            schedule.updateCalendar();
	    	}
	    });
		//add(tz);
	    left.add(tz);
		
		// Scores
		scoresComboBox = new ScoresComboBox(schedule);
		scoresComboBox.setFieldLabel("Scores");
        //add(scoresComboBox);
        left.add(scoresComboBox);
        add(left, tdLeft);
        
        schedule.scores = new Scores(scoresComboBox, new ScoresForCalendar(schedule));
        
	}
}
