package edu.nrao.dss.client;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import edu.nrao.dss.client.util.TimeUtils;

public class ScheduleControl extends FormPanel {
	
	private Schedule schedule;
	private LabelField scheduleAverage, currentAverage, unscheduledTime;
	private boolean schedulePressed;
	int dataSize;
	private NumberFormat scoreFormat = NumberFormat.getFormat("0.00");
	
	public FactorsDlg factorsDlg;
	
	private Button scheduleButton;
	private Button emailButton;
	private Button publishButton;
	private Button deletePendingBtn;
	private Button factorsButton;
	
	public ScheduleControl(Schedule sched) {
		schedule = sched;
		initLayout();
		initListeners();
	}
	
	public void setScheduleSummary(List<BaseModelData> data) {
		if (!schedulePressed) {
			dataSize = data.size();
		}
		if (data.size() <= 0) {
			return;
		}

		// Note: time computation done in minutes
		long msecPerMinute = 60*1000;
		double total_scheduled = 0.0;
		long total_empty = 0;
		double total_score = 0.0;
		
		BaseModelData init = data.get(0);
		Date t = TimeUtils.toDate(init);
		long   end      = t.getTime()/msecPerMinute;
		double dur      = 0.0; //init.get("duration");
		long   duration = 0; //Math.round(60.*dur);
		long   start    = 0;
		double score    = 0.0;
		
		for (BaseModelData datum : data) {
			t = TimeUtils.toDate(datum);
			start = t.getTime()/msecPerMinute;
			Object value = datum.get("duration");
			// This is needed because newly entered values from the user are of type
			// String, but only become Double upon being returned from the server
			if (value.getClass() == String.class) {
				dur = Double.valueOf(value.toString());
			} else {
				dur = datum.get("duration");
			}
			duration = Math.round(60.*dur);
			score = datum.get("cscore");
			total_scheduled += duration;
			total_score += duration*score;
			total_empty += start - end;
			end = start + duration;
		}
		double currentAverageValue = total_score/total_scheduled;
		currentAverage.setValue(scoreFormat.format(currentAverageValue));
		if (schedulePressed && dataSize != data.size()) {
			scheduleAverage.setValue(scoreFormat.format(currentAverageValue));
			schedulePressed = false;
			dataSize = data.size();
		}
		unscheduledTime.setValue(TimeUtils.min2sex((int)total_empty));
	}
	
	private void initLayout() {
		setHeading("Schedule Control");
		setWidth("100%");
		
		String col1Width = "200px";
		String col2Width = "400px";
		String labelFontSize = "11";
		
		TableLayout tb = new TableLayout(2);
		tb.setWidth("100%");
		tb.setBorder(0);
		setLayout(tb);

		final FormPanel left = new FormPanel();
		left.setHeaderVisible(false);
		left.setBorders(false);
		
		// Auto schedules the current calendar
		scheduleButton = new Button("Schedule");
		schedulePressed = false;
		scheduleButton.setToolTip("Generate a schedule for free periods over the specified calendar range");
		left.add(scheduleButton);
		
		emailButton = new Button("Email");
		emailButton.setToolTip("Emails a schedule to staff and observers starting now and covering the next two days");
		left.add(emailButton);
		
		// publishes all periods currently displayed (state moved from pending to scheduled)
		publishButton = new Button("Publish");
		publishButton.setToolTip("Publishes all the currently visible Periods: state is moved from Pending (P) to Scheduled (S) and become visible to Observer.");
		left.add(publishButton);
		
		// deletes all pending periods currently displayed (state moved from pending to deleted)
		deletePendingBtn = new Button("Delete Pending");
		deletePendingBtn.setToolTip("Deletes all the currently visible Periods in the Pending (P) state.");
		left.add(deletePendingBtn);		
		
		// Factors
		factorsButton = new Button("Factors");
		factorsButton.setToolTip("Provides access to individual score factors for selected session and time range");
		factorsDlg = new FactorsDlg(schedule);
		factorsDlg.hide();
		left.add(factorsButton);

		// add the left hand side w/ all the buttons
		TableData tdLeft = new TableData();
		tdLeft.setVerticalAlign(VerticalAlignment.TOP);
		// TODO: why must I do this, just to get the two forms to share space?
		tdLeft.setColspan(1);
		tdLeft.setWidth(col1Width);
		
        add(left, tdLeft);

        // now for the right hand side w/ all the status fields
        // make the font a little smaller to save a little space
		final FormPanel right = new FormPanel();
		right.setHeaderVisible(false);
		right.setBorders(false);
        
		scheduleAverage = new LabelField();
		scheduleAverage.setToolTip("Average score of displayed periods resulting from last press of the 'Schedule' button");
		scheduleAverage.setFieldLabel("Schedule Average Score");
		scheduleAverage.setLabelStyle("font-size : " + labelFontSize);
		right.add(scheduleAverage);
		
		currentAverage = new LabelField();
		currentAverage.setToolTip("Current average score of displayed periods");
		currentAverage.setFieldLabel("Current Average Score");
		currentAverage.setLabelStyle("font-size : " + labelFontSize);
		right.add(currentAverage);
		
		unscheduledTime = new LabelField();
		unscheduledTime.setToolTip("Total unscheduled time among displayed periods");
		unscheduledTime.setFieldLabel("Unscheduled Time");
		unscheduledTime.setLabelStyle("font-size : " + labelFontSize);
		right.add(unscheduledTime);
		
		// add the right hand side w/ all the status fields
		TableData tdRight = new TableData();
		tdRight.setVerticalAlign(VerticalAlignment.TOP);
		// TODO: why must I do this, just to get the two forms to share space?
		tdRight.setColspan(1);
		tdRight.setWidth(col2Width);
		
        add(right, tdRight);
		
	}
	
	private HashMap<String, Object> getTimeRange() {
		HashMap<String, Object> keys = new HashMap<String, Object>();
		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(schedule.startCalendarDay) + " 00:00:00";

		keys.put("start", startStr);
		keys.put("duration", schedule.numCalendarDays);
		keys.put("tz", schedule.timezone);
		return keys;
	}
	
	private void initListeners() {
		
		scheduleButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				schedulePressed = true;
				HashMap<String, Object> keys = getTimeRange();
				String startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(schedule.startCalendarDay) + " 00:00:00";
	    		Integer numScheduleDays = schedule.numCalendarDays < 2 ? 1 : (schedule.numCalendarDays -1); 
				String msg = "Scheduling from " + startStr + " (" + schedule.timezone + ")" + " until " + numScheduleDays.toString() + " days later at 8:00 (ET).";
				final MessageBox box = MessageBox.wait("Calling Scheduling Algorithm", msg, "Be Patient ...");
				JSONRequest.post("/runscheduler", keys,
						new JSONCallbackAdapter() {
							public void onSuccess(JSONObject json) {
								schedule.updateCalendar();
								box.close();
							}
						});
			}
		});
		
		emailButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				String msg = "Generating scheduling email for observations over the next two days";
				final MessageBox box = MessageBox.wait("Getting Email Text", msg, "Be Patient ...");
				
				// Must set keys here somehow to transmit proper time range.  What is the time range?
				HashMap<String, Object> keys = getTimeRange();
				
				JSONRequest.get("/schedule/email", keys,
						new JSONCallbackAdapter() {
							public void onSuccess(JSONObject json) {
								String addr[] = new String[3];
								String subject[] = new String[3];
								String body[] = new String[3];
								String address_key[] = {"observer_address", "deleted_address", "staff_address"};
								String subject_key[] = {"observer_subject", "deleted_subject", "staff_subject"};
								String body_key[] = {"observer_body", "deleted_body", "staff_body"};
								                   
								for (int j = 0; j < 3; ++j)
								{
									JSONArray emails = json.get(address_key[j]).isArray();
									//String addr = "";
									addr[j] = "";
									
									for (int i = 0; i < emails.size(); ++i)
									{
										addr[j] += emails.get(i).isString().stringValue() + ", ";
									}
	
									addr[j] = addr[j].substring(0, addr[j].length() - 2); // Get rid of last comma.
									subject[j] = json.get(subject_key[j]).isString().stringValue();
									body[j] = json.get(body_key[j]).isString().stringValue();
								}
								
								EmailDialogBox dlg = new EmailDialogBox(addr, subject, body);
								dlg.show();
								box.close();
							}
							
							public void onError(String error, JSONObject json)
							{
								box.close();
								super.onError(error, json);
							}
						});
			}
		});
		
		publishButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				// make the JSON request for the periods so we can make appointments
				// we need the same url in a different format
				//final MessageBox box = MessageBox.confirm("Publish Pending Periods", "r u sure?", l);
				HashMap<String, Object> keys = getTimeRange();
				JSONRequest.post("/periods/publish", keys,
						new JSONCallbackAdapter() {
							public void onSuccess(JSONObject json) {
								schedule.updateCalendar();
							}
						});
			}
		});

		
		deletePendingBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				// make the JSON request for the periods so we can make appointments
				// we need the same url in a different format
				HashMap<String, Object> keys = getTimeRange();
				//final MessageBox box = MessageBox.confirm("Publish Pending Periods", "r u sure?", l);
				JSONRequest.post("/periods/delete_pending", keys,
						new JSONCallbackAdapter() {
							public void onSuccess(JSONObject json) {
								schedule.updateCalendar();
							}
						});
			}
		});
		
		factorsButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				BaseModelData selectedItem = schedule.scheduleExplorer.pe.grid
				        .getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					HashMap<String, Object> periodValues =
						new HashMap<String, Object>(selectedItem.getProperties());
					factorsDlg.initValues(periodValues);
				} else {
					factorsDlg.clearFormFields();
				}
				
				factorsDlg.show();
			}
		});		
	}
}
