package edu.nrao.dss.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WidgetListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;

public class SessionExplorer extends Explorer {
	public SessionExplorer() {
		super("/sessions", new SessionType(columnTypes));
		initFilters();
		initLayout(initColumnModel(), true);
	}
	
	private void initFilters() {
		String[] frequencies = new String[] {
			"> 2 GHz", "> 5 GHz", "> 10 GHz", "> 20 GHz", "> 30 GHz", "> 40 GHz",
			"<= 2 GHz", "<= 5 GHz", "<= 10 GHz", "<= 20 GHz", "<= 30 GHz", "<= 40 GHz"
		};
		String[] receivers = new String[] {
			"RRI", "342", "450", "600", "800", "1070", "L", "S", "C"
		  , "X", "Ku", "K", "Ka", "Q", "MBA", "Z", "Hol", "KFPA"	
		};
		advancedFilters.add(initCombo("Proj Complete", new String[] {"True", "False"}, 100));
		advancedFilters.add(initCombo("Session Type", new String[] {"Open", "Fixed", "Windowed"}, 100));
		advancedFilters.add(initCombo("Science Type", ScienceField.values, 100));
		advancedFilters.add(initCombo("Receiver", receivers, 80));
		advancedFilters.add(initCombo("Frequency", frequencies, 80));
		advancedFilters.add(initCombo("Trimester", trimesters, 80));
		advancedFilters.add(initCombo("Complete", new String[] {"True", "False"}, 80));
		advancedFilters.add(initCombo("Enabled", new String[] {"True", "False"}, 80));
		advancedFilters.add(initCombo("Authorized", new String[] {"True", "False"}, 80));
		
		initFilterAction();
	}
	
	private void initFilterAction() {
		filterAction = new Button("Filter");
		filterAction.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be){
				HashMap<String, String> freqMap = new HashMap<String, String>();
				freqMap.put("> 2 GHz",  ">2");
				freqMap.put("> 5 GHz",  ">5");
				freqMap.put("> 10 GHz", ">10");
				freqMap.put("> 20 GHz", ">20");
				freqMap.put("> 30 GHz", ">30");
				freqMap.put("> 40 GHz", ">40");
				freqMap.put("<= 2 GHz",  "<=2");
				freqMap.put("<= 5 GHz",  "<=5");
				freqMap.put("<= 10 GHz", "<=10");
				freqMap.put("<= 20 GHz", "<=20");
				freqMap.put("<= 30 GHz", "<=30");
				freqMap.put("<= 40 GHz", "<=40");
				
				String filtersURL = "?";
				String filterVal;
				String[] filterNames = new String[] {"filterProjClp", "filterType", "filterSci", "filterRcvr", "filterFreq", "filterSem", "filterClp", "filterEnb", "filterAuth"};
				for (int i = 0; i < advancedFilters.size(); i++) {
					SimpleComboValue<String> value = advancedFilters.get(i).getValue();
					if (value != null) {
						if (filterNames[i] == "filterFreq") {
							filterVal = freqMap.get(value.getValue());
						} else {
							filterVal = value.getValue();
						}
						filtersURL += (filtersURL.equals("?") ? filterNames[i] + "=" : "&" + filterNames[i] + "=") + filterVal;
					}
				}

				String filterText = filter.getTextField().getValue();
				if (filterText != null) {
					filterText = filtersURL.equals("?") ? "filterText=" + filterText : "&filterText=" + filterText;
				} else {
					filterText = "";
				}
				String url = getRootURL() + filtersURL + filterText;
				RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
				DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> proxy = getProxy();
				proxy.setBuilder(builder);
				loadData();
				
			}
		});
	}
	
	private ColumnModel initColumnModel() {
		configs = new ArrayList<ColumnConfig>();
		CheckColumnConfig checkColumn;
		for (ColumnType ct : columnTypes) {
			if (ct.getClasz() != Boolean.class) {
			    configs.add(new SessionColConfig(ct.getId(), ct.getName(), ct.getLength(), ct.getDisabled(), ct.getClasz()));
			} else {
				checkColumn = new CheckColumnConfig(ct.getId(), ct.getName(), ct.getLength());
			    checkColumn.setEditor(new CellEditor(new CheckBox()));
			    configs.add(checkColumn);
			    checkBoxes.add(checkColumn);
			}
		}
	    return new ColumnModel(configs);
	}
	
	public ColumnConfig getPcodeConfig() {
		return configs.get(0);
	}
	
	private List<ColumnConfig> configs;

    private static final ColumnType[] columnTypes = {
    	new ColumnType("pcode",            "Proj Code",      100, false, PCodeField.class),
       	new ColumnType("project_complete", "Proj Complete",   85, true,  String.class),
        new ColumnType("name",             "Name",           100, false, String.class), 
        new ColumnType("source",           "Source",         100, false, String.class),
        new ColumnType("orig_ID",          "Orig ID",         50, false, Integer.class),
        new ColumnType("type",             "Type",            60, false, STypeField.class),
        new ColumnType("science",          "Science",         75, false, ScienceField.class),
        new ColumnType("PSC_time",         "PSC Time",        60, false, Double.class),
        new ColumnType("total_time",       "Total Time",      60, false, Double.class),
        new ColumnType("sem_time",         "Trimester Time", 100, false, Double.class),
        new ColumnType("remaining",        "Remaining Time", 100, false, Double.class),
        new ColumnType("grade",            "Grade",           50, false, Double.class),
        new ColumnType("freq",             "Freq",            50, false, Double.class),
        new ColumnType("receiver",         "Receiver(s)",     70, false, String.class),
        new ColumnType("req_min",          "Req Min",         60, false, Double.class),
        new ColumnType("req_max",          "Req Max",         60, false, Double.class),
        new ColumnType("coord_mode",       "Coord Mode",      75, false, CoordModeField.class),
        new ColumnType("source_h",         "Source RA",       75, false, HourField.class),
        new ColumnType("source_v",         "Source Dec",      75, false, DegreeField.class),
        new ColumnType("between",          "Between",         60, false, Double.class),
        new ColumnType("xi_factor",        "Xi",              40, false, Double.class),
        new ColumnType("lst_ex",           "LST Exclusion",   90, false, String.class),
        new ColumnType("el_limit",         "EL",              40, false, Double.class),
       	new ColumnType("authorized",       "Authorized?",     70, false, Boolean.class),
       	new ColumnType("enabled",          "Enabled?",        60, false, Boolean.class),
       	new ColumnType("complete",         "Complete?",       65, false, Boolean.class),
       	new ColumnType("backup",           "Backup?",         55, false, Boolean.class),
        new ColumnType("transit",          "Transit?",        55, false, Boolean.class),
        new ColumnType("nighttime",        "Night-time?",     65, false, Boolean.class),
    	};
}
