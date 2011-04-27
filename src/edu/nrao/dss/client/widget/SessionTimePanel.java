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
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import edu.nrao.dss.client.util.JSONCallbackAdapter;
import edu.nrao.dss.client.util.JSONRequest;
import edu.nrao.dss.client.widget.form.DSSTimeValidator;
import edu.nrao.dss.client.TimeAccounting;

public class SessionTimePanel extends ContentPanel {
	
	public SimpleComboBox<String> sessions = new SimpleComboBox<String>();	
	public TextField<String> sessionName = new TextField<String>();
	public NumberField sessionGrade = new NumberField();
	public NumberField sessionTime = new NumberField();
//	public SimpleComboBox<String> periods = new SimpleComboBox<String>();
    public SessionTimeAccountPanel sessionTimeAccounting = new SessionTimeAccountPanel();
//	public HashMap<String, Integer> periodInfo = new HashMap<String, Integer>();
	Button saveSess = new Button("Save Session Time Accounting");
	
	private String pcode;
	private JSONObject json1;
	
	private ContentPanel parent;
	
	public SessionTimePanel() {
		initLayout();
		initListeners();
	}
	
	private void initLayout() {
		setLayout(new RowLayout(Orientation.VERTICAL)); //FitLayout());
		setBorders(false);
		setVisible(true);
		setHeaderVisible(false);
	
		// this table is used to place two forms side by side
		LayoutContainer sessionTable = new LayoutContainer();
		TableLayout tbSess = new TableLayout(2);
		tbSess.setWidth("100%");
		tbSess.setBorder(0);
		sessionTable.setLayout(tbSess);
		sessionTable.setBorders(true);
	
		TableData tdSess = new TableData();
		tdSess.setVerticalAlign(VerticalAlignment.TOP);
		
		// Question: why must I do this, just to get the two forms to share space?
		tdSess.setColspan(1);
		tdSess.setWidth("400px");
		
		final FormPanel sessionForm = new FormPanel();
		sessionForm.setHeading("Session");
		sessionForm.setBorders(false);
		sessionForm.setBodyBorder(false);
	
		sessions.setFieldLabel("Sessions");
	    sessionForm.add(sessions);
	    
	    // TODO: get rid of.
		// what's the current session?
		sessionName.setValue("");
		sessionName.setReadOnly(true);
		// Note: this doesn't work
		sessionName.setStyleAttribute("color", "grey");
		sessionName.setFieldLabel("Session Name");
		sessionName.setVisible(false);
		sessionForm.add(sessionName);
	
		sessionTable.add(sessionForm, tdSess);
		
		final FormPanel sessionForm2 = new FormPanel();
		sessionForm2.setHeading("Allotment");
		sessionForm2.setBorders(false);
		
		sessionGrade.setReadOnly(true);
		// Note: this doesn't work
		sessionGrade.setStyleAttribute("color", "grey");
		sessionGrade.setFieldLabel("Grade");
		sessionForm2.add(sessionGrade);
		
		sessionTime.setFieldLabel("Alloted (Hrs)");
		sessionTime.setFormat(NumberFormat.getFormat("#0.00"));
		sessionTime.setValidator(new DSSTimeValidator()); 	
		sessionForm2.add(sessionTime);
		
		sessionForm2.add(saveSess);
		
		sessionTable.add(sessionForm2, tdSess);	
		
	    add(sessionTable, new RowData(1, -1, new Margins(4)));
	    sessionTimeAccounting.setHeading("Session Time Accounting");
	    sessionTimeAccounting.collapse();
		add(sessionTimeAccounting, new RowData(1, -1, new Margins(4)));		
	}
	
	private void initListeners() {
		sessions.addListener(Events.Valid, new Listener<BaseEvent>() {
		  	public void handleEvent(BaseEvent be) {
                // a session has been picked - get it's info from the server
		  		getSession();
		   	}
		});		
		sessionTime.addListener(Events.Blur, new Listener<BaseEvent>() {
			@Override
			public void handleEvent(BaseEvent be) {
				// make sure changes to the allotted time get marked red
				String color = (sessionTime.getValue().doubleValue() == sessionTime.getOriginalValue().doubleValue()) ? "black" : "red";
				sessionTime.el().firstChild().setStyleAttribute("color", color);
			}
		});	
		saveSess.addListener(Events.OnClick, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				// the user wants to save the changes they made to the session's time
				sendSessionAllotment();
			}
		});		
	}

	public void setNewSessions(String pcode) {
		this.pcode = pcode;
		clearAll();
		updateSessionOptions(pcode);
	}
	
	private void clearAll() {
		sessions.clearSelections();
		sessions.removeAll();
		sessionName.clear();
		sessionGrade.clear();
		sessionTime.clear();
		sessionTimeAccounting.clearAll();
	}
	
	// populate the session picker with session names for the given project code
	public void updateSessionOptions(final String pcode) {
		JSONRequest.get("/scheduler/sessions/options"
			      , new HashMap<String, Object>() {{
			    	  put("mode", "session_names");
			    	  put("pcode", pcode);
			        }}
			      , new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json) {
				// get ready to populate the sessions codes list
				sessions.clearSelections();
				sessions.removeAll();
				JSONArray names = json.get("session names").isArray();
				for (int i = 0; i < names.size(); ++i){
					String name = names.get(i).toString().replace('"', ' ').trim();
					sessions.add(name);
				}
			}
		});    
	}
	
	// a session has been selected - populate it's panel 
	protected void getSession() {
		// don't bother unless there's a valid name been selected
		if ((sessions.getSimpleValue() == null ) | (sessions.getSimpleValue() == "")) {
			return;
		}
		JSONRequest.get("/scheduler/projects/time_accounting/" + pcode
			      , new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json) {
                // populate this panel	            
				populateSessTimeAccounting(json, sessions.getSimpleValue());
				// how does this choice affect other panels?
				if (parent != null) {
				    ((TimeAccounting) parent).sessionSelected(sessions.getSimpleValue());
				}
			}
		});    					
	}	
	
	//given the JSON which has all the time accounting info in it, update the current session
	public void populateSessTimeAccounting(JSONObject json, String sessName) {
		// find the section of the json that has our session in it:
	    JSONArray names = json.get("sessions").isArray();
	    for (int i = 0; i < names.size(); ++i) {
	    	JSONObject session = names.get(i).isObject();
	    	String name = session.get("name").isString().stringValue();
	    	if (name.equals(sessName)) {
	    		// got it!
	    		GWT.log("matched session name " + sessName, null);
	    		sessionName.setValue(sessName);
	    		sessionGrade.setValue(session.get("grade").isNumber().doubleValue());
	    		// time field
	    		double time = session.get("total_time").isNumber().doubleValue();
	    		sessionTime.setValue(time); 
	    		sessionTime.setOriginalValue(time);
	    		sessionTime.el().firstChild().setStyleAttribute("color", "black");
	    		sessionTimeAccounting.setValues(session);
	    	}	
	    }	
	}

	private void sendSessionAllotment() {
	
		String url = "/scheduler/sessions/time_accounting/" + sessionName.getValue();
		HashMap <String, Object> keys = new HashMap<String, Object>();
	
		keys.put("total_time", sessionTime.getValue().doubleValue());
		keys.put("description", sessionTimeAccounting.getDescription());
		
		JSONRequest.post(url, keys, new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json) {
                // when a change is successfully made to a session's time accounting,
				// that affects it's Project's time accounting, so we may have to update
				// from the top down
				if (parent != null) {
					// leave it up to the parent to populate this panel
				    ((TimeAccounting) parent).setTimeAccountingFromJSON(json);
				} else {
					// if no parent, it's up to us to populate the panel
				    populateSessTimeAccounting(json, sessions.getSimpleValue());    	
				}
			}
		});
	}	
	
	public void setParent(ContentPanel parent) {
		this.parent = parent;
	}
}	
