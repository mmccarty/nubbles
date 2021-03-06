// Copyright (C) 2011 Associated Universities, Inc. Washington DC, USA.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
// 
// Correspondence concerning GBT software should be addressed as follows:
//       GBT Operations
//       National Radio Astronomy Observatory
//       P. O. Box 2
//       Green Bank, WV 24944-0002 USA

package edu.nrao.dss.client.widget;

import java.util.HashMap;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import edu.nrao.dss.client.Period;
import edu.nrao.dss.client.TimeAccounting;
import edu.nrao.dss.client.util.JSONCallbackAdapter;
import edu.nrao.dss.client.util.JSONRequest;

public class PeriodSummaryPanel extends ContentPanel {
	
	private Period period;
	private TextField<String> label = new TextField<String>();
	private TextField<String> start = new TextField<String>();
	private TextField<String> dur = new TextField<String>();
	private NumberField hscore = new NumberField();
	private NumberField cscore = new NumberField();
	private CheckBox backup = new CheckBox();
	private CheckBox moc_ack = new CheckBox();
	private TextField<String> state = new TextField<String>();
	private PeriodTimeAccountPanel ta = new PeriodTimeAccountPanel();
	private Button save = new Button();

	public SimpleComboBox<String> periods = new SimpleComboBox<String>();
	public HashMap<String, Integer> periodInfo = new HashMap<String, Integer>();
	
    public PeriodSummaryPanel(Period p) {
    	period = p;
    	initLayout();
    	initListeners();
    }
    
    private void initLayout() {
    	
    	setHeading("Period Summary Panel");
    	
    	setLayout(new RowLayout(Orientation.VERTICAL));
    	
    	LayoutContainer lc = new LayoutContainer();
    	TableLayout tl = new TableLayout(2);
    	tl.setWidth("100%");
		TableData td = new TableData();
		td.setVerticalAlign(VerticalAlignment.TOP);    	
    	lc.setLayout(tl);
    	
    	FormPanel periodForm = new FormPanel();
    	periodForm.setHeading("Period Form");
        periodForm.setHeaderVisible(false);
        periodForm.setBorders(false);
        periodForm.setBodyBorder(false);
        
		periods.setTriggerAction(TriggerAction.ALL);
		periods.setFieldLabel("Period");
	
        
    	// field per attribute, roughly:
        setReadOnly("Name", label);
    	setReadOnly("Start", start);
    	setReadOnly("Duration (Hrs)", dur);

    	periodForm.add(periods);
    	periodForm.add(label);
    	periodForm.add(start);
    	periodForm.add(dur);
    	
    	save.setText("Save Period Changes");
    	
    	periodForm.add(save);
    	
    	lc.add(periodForm, td);
    	
    	FormPanel periodForm2 = new FormPanel();
    	periodForm2.setHeading("Period Form2");
    	periodForm2.setHeaderVisible(false);
    	periodForm2.setBorders(false);
    	periodForm2.setBodyBorder(false);
    	
    	// score
    	hscore.setFieldLabel("Historical Score");
    	hscore.setReadOnly(true);
    	cscore.setFieldLabel("Current Score");
    	cscore.setReadOnly(true);
    	// NOTE: this doesn't work
    	hscore.setStyleAttribute("color", "grey");
    	periodForm2.add(hscore);
    	cscore.setStyleAttribute("color", "grey");

    	periodForm2.add(cscore);

    	setReadOnly("State", state);
    	periodForm2.add(state);
    	
    	// backup 
    	backup.setFieldLabel("Backup");
    	backup.setReadOnly(true);
    	// Note: this doesn't work
    	backup.setStyleAttribute("color", "grey");
    	periodForm2.add(backup);
    	
    	moc_ack.setFieldLabel("MOC Ack.");
    	periodForm2.add(moc_ack);
    	
        
    	lc.add(periodForm2, td);
    	
    	add(lc);

    	// Time Accounting get's its own form
    	ta.setHeading("Period Time Accounting");
    	ta.collapse();
    	add(ta);
    	
    	// initialize the forms!
    	setValues(period);
    	ta.setPeriod(period);
    }
    
    private void initListeners() {
		periods.addListener(Events.Valid, new Listener<BaseEvent>() {
		  	public void handleEvent(BaseEvent be) {
		  			GWT.log("period Events.Valid");
		  			// a period has been picked! display it!
	       			updatePeriod();
		  		}
			});    	
    	moc_ack.addListener(Events.OnClick, new Listener<BaseEvent>() {
    		public void handleEvent(BaseEvent be) {
    			//GWT.log("Updating period's moc_ack field", null);
    			String url = "/scheduler/period/" + Integer.toString(period.getId()) + "/toggle_moc";
    			HashMap<String, Object> keys = new HashMap<String, Object>();
    			JSONRequest.post(url, keys,
    					new JSONCallbackAdapter() {
    						public void onSuccess(JSONObject json) {
    						}
    					});		
    		}
    	});    	
    	save.addListener(Events.OnClick, new Listener<BaseEvent>() {
    		public void handleEvent(BaseEvent be) {
    			// The time accounting panel has the only editable widgets
    			// so we leave it up to it.
    			ta.sendUpdates();
    		}
    	});   	
    }
    
    public void setSaveButtonVisible(boolean visible) {
    	save.setVisible(visible);
    }
    
    public void hidePeriodPicker() {
    	periods.setVisible(false);
    }
    
    public void setNewPeriods(String pcode, String sessionName) {
    	clearAll();
        updatePeriodOptions(pcode, sessionName);
    }
    
    private void clearAll() {
    	label.clear();
    	start.clear();
    	dur.clear();
    	hscore.clear();
    	cscore.clear();
    	backup.clear();
    	moc_ack.clear();
    	state.clear();
    	ta.clearAll();     	
    }
    
    public void updatePeriodForm(int periodId) {
    	// Get this period from the server and populate the form.
        // Note that we are using UTC, regardless of the timezone that may be
    	// being used in this context.
    	JSONRequest.get("/scheduler/periods/UTC/" + Integer.toString(periodId)
    		      , new JSONCallbackAdapter() {
    		public void onSuccess(JSONObject json) {
    			loadPeriodJson(json);
    			getScore();
    		}
    	});    		
    	
    }

    // Get the score from the Antioch Server 
    private void getScore() {
    	if (period != null) {
	      String url = "/update_periods?pids=" + Integer.toString(this.period.getId());
	      JSONRequest.get(url, new JSONCallbackAdapter() {
	         public void onSuccess(JSONObject json) {
	                setScores(json);
	         }   
	      });      	
    	}    
    }
    
    // updates the fields that show the score
    private void setScores(JSONObject json) {
    	
	      JSONArray scores = new JSONArray();
	      scores = json.get("scores").isArray();
	      int id;
	      double cs, hs;
	      JSONObject jsonScore = new JSONObject();
	      for (int i = 0; i < scores.size(); i++) {
	         jsonScore = scores.get(i).isObject();
	         // we need the id to make sure we've got the right one
	         id = (int) jsonScore.get("pid").isNumber().doubleValue();
	         // we'll always be update the current score
	         cs = jsonScore.get("score").isNumber().doubleValue();
	         // we may not have to update the historical score        
	         if (id == this.period.getId()) {
	             cscore.setValue(cs);
	             // now, see if we need to update the historical score
	             if (jsonScore.get("hscore").isObject().containsKey("Just")) {
	               hs = jsonScore.get("hscore").isObject().get("Just").isNumber().doubleValue();
		           hscore.setValue(hs);
                 }
	         }   
	      }    	
    }
    
	// JSON period -> JAVA period
    public void loadPeriodJson(JSONObject json) {
     	Period period = Period.parseJSON(json.get("period").isObject());
     	setPeriod(period);
    }
    
	// a session has been selected, so now what are the periods that we can choose from?
	public void updatePeriodOptions(final String pcode, final String sessionName) {
	    //GWT.log("updatePeriodOptions", null);
		JSONRequest.get("/scheduler/sessions/options"
			      , new HashMap<String, Object>() {{
			    	  put("mode", "periods");
			    	  put("pcode", pcode);
			    	  put("session_name", sessionName);
			        }}
			      , new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json) {
				loadPeriodInfoJson(json);

			}
		});    	
	}
	
	private void loadPeriodInfoJson(JSONObject json) {
		// get ready to populate the sessions codes list
		periods.clearSelections();
		periods.removeAll();
		periodInfo.clear();
		//project_codes.clear();
		JSONArray ps = json.get("periods").isArray();
		JSONArray ids = json.get("period ids").isArray();
		for (int i = 0; i < ps.size(); ++i){
			// the periods drop down is populated w/ descriptions of the periods
			String p = ps.get(i).toString().replace('"', ' ').trim();
			String id = ids.get(i).toString().replace('"', ' ').trim();
			// the labels displayed need to be unique, so we add the period id at the end
			String label = p + " (" + id + ")";
			periods.add(label);
			// we need to save the mapping from 'description' to 'id'
			periodInfo.put(label, Integer.parseInt(id));
			
		}		
	}
	
	// a period has been selected - so update the period summary panel
	protected void updatePeriod() {
		//GWT.log("updatePeriod", null);
		// what's the period id for chosen period (displayed using time info)?
		String name = periods.getSimpleValue();
		int periodId = periodInfo.get(name);
		// get this period from the server and update this panel
		updatePeriodForm(periodId);
	}
	
    private void setReadOnly(String label, TextField<String> tf) {
    	tf.setFieldLabel(label);
    	tf.setReadOnly(true);
    	// Note: this doesn't work
    	tf.setStyleAttribute("color", "grey");
    }
    
	public void setParent(TimeAccounting p) {
		ta.setParent(p);
	}  
	
    public void setPeriod(Period period) {
    	this.period = period;
    	setValues(period);
    	ta.setPeriod(period);
    }
    
    private void setValues(Period period) {
        if (period != null) {
        	label.setValue(period.getHandle());
        	start.setValue(period.getStartString());
        	dur.setValue(period.getDurationString());
        	hscore.setValue(period.getHScore());
        	cscore.setValue(period.getCScore());
        	moc_ack.setValue(period.getMocAck());
        	backup.setValue(period.isBackup());
        	state.setValue(period.getState());
        	// new period means the score has to be redone
        	getScore();         	
        }
   	
    }
    
    public boolean hasChanged() {
    	return ta.hasChanged();
    }
    
    // for testing purposes - summary of the widget values
    public String[] getTestString() {
    	return new String[] {label.getValue(), start.getValue(), dur.getValue(), hscore.getValue().toString(), cscore.getValue().toString(), state.getValue()};
    }
}
