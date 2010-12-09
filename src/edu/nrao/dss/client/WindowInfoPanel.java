package edu.nrao.dss.client;

import java.util.Date;
import java.util.HashMap;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
// comment
import com.google.gwt.json.client.JSONString;

// This class maps directly to a single window object on the server side.  It replaces 
// what a single row in the window explorer used to cover, before multiple periods and date 
// ranges were introduced.

// TODO: highlight unsaved changes - see TimeAccounting for one way to do this.
// TODO: pretty it up.

public class WindowInfoPanel extends PeriodGroupInfoPanel {
	
	
	public WindowInfoPanel(JSONObject winJson, String url,
			String groupPeriodType) {
		super(winJson, url, groupPeriodType);
	}

	// attributes exclusive to windows
	private String errorMsgs;
	private Date start;
	private String startStr;
	private int numDays;
	private Date end;
	private String endStr;
	private Double total_time;
	private Double time_billed;
	private Double time_remaining;
	
	// window UI widgets
	private LabelField errors;
	private LabelField dt;
	private LabelField end_dt;
	private LabelField days;
	private NumberField total;
	// TODO: maybe these read-only fields should just be labels too?
	private NumberField billed;
	private NumberField remaining;
	private CheckBox cmp; 

	private WindowRangeExplorer wre;
	private WindowedPeriodExplorer wpe;
	
	protected void translateJson(JSONObject winJson) {
		
		handle = winJson.get("handle").isString().stringValue();
		
		id = (int) winJson.get("id").isNumber().doubleValue();
		
		// concat the multiple warnings (if any) into a single string
		errorMsgs = "";
		JSONArray errJson = winJson.get("errors").isArray();
		for (int i = 0; i < errJson.size(); i++) {
			if (errorMsgs.length() > 0) {
				errorMsgs += ";";
			}
		    errorMsgs += errJson.get(i).isString().stringValue();	
		}
		
		// newly created windows have no ranges, so these date fields will be null
        if (winJson.get("start").isString() == null) {
        	startStr = "?";
        	start = null;
        } else {
    		startStr = winJson.get("start").isString().stringValue();
    		start = DateTimeFormat.getFormat("yyyy-MM-dd").parse(startStr);   
    		startStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(start);
        }
        if (winJson.get("end").isString() == null) {
        	endStr = "?";
        	end = null;
        } else {
    		endStr = winJson.get("end").isString().stringValue();
    		end = DateTimeFormat.getFormat("yyyy-MM-dd").parse(endStr); 
    		endStr = DateTimeFormat.getFormat("yyyy-MM-dd").format(end);   		
        }
        
        if (winJson.get("duration").isNumber() != null) {
		    numDays = (int) winJson.get("duration").isNumber().doubleValue();
        } else {
        	numDays = 0;
        }
		
		total_time = winJson.get("total_time").isNumber().doubleValue();
		time_billed = winJson.get("time_billed").isNumber().doubleValue();
		time_remaining = winJson.get("time_remaining").isNumber().doubleValue();
		
		complete = winJson.get("complete").isBoolean().booleanValue();
		
		String cmpStr = (complete == true) ? "Complete" : "Not Complete";
		
		String warnings = (errorMsgs.length() == 0) ? "" : "WARNINGS";
		
		// the header is a summary: [date range] time, complete (id) [Warning]
		// TODO: tell if it's non-contigious!
		header = "Window [" + startStr + " - " + endStr + "] " + Double.toString(time_remaining) + " Hrs Left; "+ cmpStr + "; (" + Integer.toString(id) + "): " + warnings;
	}
	
	private Date jsonToDate(JSONObject json, String key) {
		JSONString jsonDt = json.get(key).isString();
		if (jsonDt != null) {
			return  DateTimeFormat.getFormat("yyyy-MM-dd").parse(jsonDt.stringValue());
		} else {
			return null;
		}
	}
	
	// if the window is incomplete, or there are warnings, make it red and bold.
	protected void updateHeading() {
		String color = "green";
		setHeading(header);
		if (complete == false || errorMsgs.length() > 0) {
			getHeader().setStyleAttribute("font-weight", "bold");
			color = "red";
		}
		getHeader().setStyleAttribute("color", color);
	}
	

	
	// class attributes -> widgets
	protected void loadPeriodGroup() {
		errors.setValue(errorMsgs);
		// hide this field if there are no warning about this window
		if (errorMsgs.length() == 0) {
			errors.setVisible(false);
		} else {
			errors.setVisible(true);
		}
	    dt.setValue(startStr);
	    days.setValue(numDays);
	    end_dt.setValue(endStr);
	    total.setValue(total_time);
	    billed.setValue(time_billed);
	    remaining.setValue(time_remaining);
	    cmp.setValue(complete);
	    updateHeading();
	}
	
	@Override
	protected void updateGroupPeriod(JSONObject json) {
    	JSONObject winJson = json.get("window").isObject();
    	translateJson(winJson);
    	loadPeriodGroup();
        wre.loadData();
    	wpe.loadData();		
	}
	
	// send off the current state of the window to the server
	// then reload the results
	protected void savePeriodGroup() {
		HashMap<String, Object> keys = new HashMap<String, Object>();
		keys.put("_method", "put");
		keys.put("total_time", total.getValue().doubleValue());
		keys.put("complete", cmp.getValue());
		keys.put("handle", handle);
	    JSONRequest.post("/" + url + "/" + Integer.toString(id), keys, new JSONCallbackAdapter() {
	            @Override
	            public void onSuccess(JSONObject json) {
	            	// get back from the server this window & display it again
	            	getPeriodGroup();
	            }
	    });		
	}
	
	// Sets up the fields exclusive to Windows
	// This is called from the parents initLayout()
	@Override
	protected void initFormFields(FormPanel fp) {

		// hide this field if there are no problems with the window
		errors = new LabelField();
		errors.setValue(errorMsgs);
		errors.setReadOnly(true);
		errors.setFieldLabel("Warnings");
        errors.setStyleAttribute("color", "red");		
		if (errorMsgs.length() == 0) {
			errors.setVisible(false);
			
		} else {
			errors.setVisible(true);
		}
		fp.add(errors);
		
	    dt = new LabelField();
	    dt.setValue(startStr);
	    dt.setFieldLabel("Start Date");
	    fp.add(dt);
	    
	    days = new LabelField(); 
	    days.setFieldLabel("Days");
	    days.setValue(numDays);
	    fp.add(days);
	    
	    end_dt = new LabelField(); 
	    end_dt.setValue(endStr);
	    end_dt.setFieldLabel("End Date");
	    fp.add(end_dt);
	    
	    total = new NumberField();
	    total.setValue(total_time);
	    total.setFieldLabel("Total Time (Hrs)");
	    fp.add(total);
	    
	    billed = new NumberField();
	    billed.setValue(time_billed);
	    billed.setFieldLabel("Billed Time (Hrs)");
	    billed.setReadOnly(true);
	    fp.add(billed);
	    
	    remaining = new NumberField();
	    remaining.setValue(time_remaining);
	    remaining.setFieldLabel("Time Remaining (Hrs)");
	    remaining.setReadOnly(true);
	    fp.add(remaining);
	    
	    cmp = new CheckBox();
	    cmp.setFieldLabel("Complete");
	    cmp.setValue(complete);
	    fp.add(cmp);		
		
	}

	// Window Panels need to use the WindowedPeriodExplorers
	// This is called from the parents initLayout()
	@Override
	protected void initGroupPeriodExplorer(FormPanel fp) {
	    //TODO: add the window range explorer here
        wre = new WindowRangeExplorer(id, handle);
        wre.registerObservers(this);
        fp.add(wre);
	    wpe = new WindowedPeriodExplorer(id, handle);
	    wpe.registerObservers(this);
	    fp.add(wpe);	
	    
	}
		
}
