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
		super("/periods", new PeriodType(columnTypes));
		initLayout(initColumnModel());
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
//		removeItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
//			@Override
//			public void componentSelected(ButtonEvent be) {
//	            schedule.updateCalendar();
//			}
//		});
		removeApproval.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
	            schedule.updateCalendar();
			}
		});
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
       	new ColumnType("handle",          "Session (Project) ID", 200, SessionField.class),
        new ColumnType("date",            "Day",                   70, DateEditField.class),
        new ColumnType("time",            "Time (UTC)",            70, TimeField.class),
        new ColumnType("duration",        "Duration (Hrs)",        90, Double.class),
       	new ColumnType("backup",          "Backup?",               70, Boolean.class),
	};
}
