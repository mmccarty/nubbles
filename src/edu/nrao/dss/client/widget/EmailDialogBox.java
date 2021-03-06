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

import java.util.Map;
import java.util.HashMap;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.TabItem;  
import com.extjs.gxt.ui.client.widget.TabPanel;  
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Element;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.Text;

import edu.nrao.dss.client.util.JSONCallbackAdapter;
import edu.nrao.dss.client.util.JSONRequest;


public class EmailDialogBox extends Dialog {
	public EmailDialogBox(String addrs[], String subj[], String bod[], int obsPeriodIds[], int changedPeriodIds[]) {
		super();

		// Basic Dlg settings
		setHeading("Email Schedule");
		addText("Review (and edit) the schedule e-mails before sending them to observers and staff.");
		setButtons(Dialog.OKCANCEL);
		GWT.log("EmailDialogBox", null);
		
		address = addrs;
		subject = subj;
		body = bod;

		for (int i = 0; i < 3; ++i)
		{
			tareas.put(tab_title[i], new HashMap<String, TextArea>());
		}
		
		this.changedPeriodIds = changedPeriodIds;
		this.obsPeriodIds = obsPeriodIds;
	}

	
	@Override
	protected void onRender(Element parent, int index) {
		super.onRender(parent, index);

		TabPanel email_tabs = new TabPanel();
		email_tabs.setAutoWidth(true);
		email_tabs.setAutoHeight(true);

		for (int i = 0; i < 3; ++i)
		{
			email_tabs.add(addTab(tab_title[i], tab_tool_tip[i], address[i], subject[i], body[i]));
		}
		
		add(email_tabs);
		setSize(950, 700);
		
		Button cancel = getButtonById(Dialog.CANCEL);
		cancel.addListener(Events.OnClick, new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
				hide();
			}
		});
		
		Button ok = getButtonById(Dialog.OK);
		ok.setText("Send");
		ok.addListener(Events.OnClick,new Listener<BaseEvent>() {
			public void handleEvent(BaseEvent be) {
	    		HashMap<String, Object> keys = new HashMap<String, Object>();
	    		
	    		for (int i = 0; i < 3; ++i)
	    		{
	    			String to_field, subject_field, body_field;
	    			Map<String, TextArea> ta = tareas.get(tab_title[i]);
	    			TextArea subjectText = ta.get("subject:");
	    			TextArea bodyText = ta.get("body:");
	    			TextArea addressText = ta.get("to:");
	    			to_field = addressText.getValue();
	    			subject_field = subjectText.getValue();
	    			body_field = bodyText.getValue();
	    			
	    			if (to_field == null)
	    			{
	    				continue;
	    			}
	    			
	    			subject_field = (subject_field == null) ? "" : subject_field;
	    			body_field = (body_field == null) ? "" : body_field;
		    		keys.put(subject_key[i], subject_field);
		    		keys.put(body_key[i], body_field);
		    		keys.put(address_key[i], to_field);
	    		}
	    		
	    		// Hack Hack Hack: we need to send over the list of changed period ids.
	    		// but we don't support arrays right now, so, make it into a string!
	    		String pids = "";
	    		for (int i = 0; i < obsPeriodIds.length; i++) {
	    			pids += i == 0 ? "" : ",";
	    			pids += Integer.toString(obsPeriodIds[i]);
	    		}
	    		keys.put("obs_periods", pids);
	    		pids = "";
	    		for (int i = 0; i < changedPeriodIds.length; i++) {
	    			pids += i == 0 ? "" : ",";
	    			pids += Integer.toString(changedPeriodIds[i]);
	    		}
	    		keys.put("changed_periods", pids);
	    		
	    		final MessageBox box = MessageBox.wait("Sending Email", "Sending scheduling e-mails to observers and staff.", "Be Patient ...");
				JSONRequest.post("/scheduler/schedule/email", keys,
						new JSONCallbackAdapter() {
							@Override
							public void onSuccess(JSONObject json) {
								box.close();
							}
							
							@Override
							public void onError(String error, JSONObject json)
							{
								box.close();
								super.onError(error, json);
							}
						});
				hide();
			}
		});
		
	}
	

	private TabItem addTab(String title, String toolTip, String addr, String subj, String body)
	{
		TabItem item = new TabItem(title);
		VerticalPanel vp = new VerticalPanel();

		item.setAutoHeight(true);
		item.setLayout(new FitLayout());
		item.setId(title);
		item.getHeader().setToolTip(toolTip);
		
		vp.setSpacing(10);
		vp.setScrollMode(Style.Scroll.NONE);
		vp.add(email_field(title, "to:", addr, 850, 100));
		vp.add(email_field(title, "subject:", subj, 850, 50));
		vp.add(email_field(title, "body:", body, 850, 400));
		
		item.add(vp );
		return item;
	}

	private HorizontalPanel email_field(String title, String label, String content, int width, int height)
	{
		Map<String, TextArea> ta = tareas.get(title);
		
		HorizontalPanel hp = new HorizontalPanel();
		Text field_label = new Text();
		TextArea field_text = new TextArea();
		field_label.setText(label);
		field_label.setWidth(50);
		field_text.setValue(content);
		field_text.setSize(width, height);
		field_text.setInputStyleAttribute("font-family", "monospace");
		hp.add(field_label);
		hp.add(field_text);
		ta.put(label, field_text);
		return hp;
	}
	
	TabPanel email_panel = new TabPanel();
	String address[] = new String[3];
	String subject[] = new String[3];
	String body[] = new String[3];
	int changedPeriodIds[];
	int obsPeriodIds[];
	
	String tab_title[] = {"observer", "changed", "staff"};
	String tab_tool_tip[] = {"email to scheduled observers", "email to observers of changed periods", "email to staff"};
	String address_key[] = {"observer_address", "changed_address", "staff_address"};
	String subject_key[] = {"observer_subject", "changed_subject", "staff_subject"};
	String body_key[] = {"observer_body", "changed_body", "staff_body"};

	Map<String,Map<String, TextArea>> tareas = new HashMap<String, Map<String, TextArea>>();


}
