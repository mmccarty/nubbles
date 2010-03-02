package edu.nrao.dss.client;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;

public class PeriodExplorer extends Explorer {
	public PeriodExplorer() {
		super("/periods/UTC", new PeriodType(columnTypes));
		initLayout(initColumnModel());
		
		// TODO: for now we're disabling delete so that time accounting is done correctly,
		// we need it to eventually behave like the Delete Period button in the Period Summary
		removeItem.disable();
	}
	
	private ColumnModel initColumnModel() {
		configs = new ArrayList<ColumnConfig>();
		CheckColumnConfig checkColumn;
		for (ColumnType ct : columnTypes) {
			if (ct.getClasz() != Boolean.class) {
			    configs.add(new PeriodColConfig(ct.getId(), ct.getName(), ct.getLength(), ct.getClasz()));
			} else {
				checkColumn = new CheckColumnConfig(ct.getId(), ct.getName(), ct.getLength());
			    checkColumn.setEditor(new CellEditor(new CheckBox()));
			    configs.add(checkColumn);
			    checkBoxes.add(checkColumn);
			}
		}
	    return new ColumnModel(configs);
	}
	
	public void addButtonsListener(final Schedule schedule) {
		saveItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
	            schedule.updateCalendar();
			}
		});
		addItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
	            schedule.updateCalendar();
			}
		});
		removeApproval.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
	            schedule.updateCalendar();
			}
		});
/*		// TODO Instead of listening to each button, we should just listen to the store!
		store.addListener(Events.Update, new Listener<StoreEvent>() {
			public void handleEvent(StoreEvent se) {
				GWT.log("hhhheeeeeeeelllllllllloooooo?", null);
	            GWT.log(se.toString(), null);
			}
		});*/
	}
	
	public void setDefaultDate(Date date) {
		DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
		defaultDate = fmt.format(date);
	}
	
	public ColumnConfig getSessionConfig() {
		return configs.get(0);
	}
	
	private List<ColumnConfig> configs;

	private static final ColumnType[] columnTypes = {
       	new ColumnType("handle",                "Session (Project) VP", 220, SessionField.class),
       	new ColumnType("stype",                 "T",                     20, DisplayField.class),
       	new ColumnType("state",                 "S",                     20, DisplayField.class),
        new ColumnType("date",                  "Day",                   70, DateEditField.class),
        new ColumnType("time",                  "Time",                  40, TimeField.class),
        new ColumnType("lst",                   "LST",                   55, DisplayField.class),
        new ColumnType("duration",              "Duration",              55, Double.class),
        new ColumnType("score",                 "Score",                 40, ScoreField.class),
       	new ColumnType("not_billable",          "Not Bill",              50, Double.class),
       	new ColumnType("backup",                "Backup?",               70, Boolean.class),
       	
	};
}
