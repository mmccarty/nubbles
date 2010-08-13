package edu.nrao.dss.client;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.i18n.client.NumberFormat;

public class UserExplorer extends Explorer {

	public UserExplorer() {
		super("/users", new UserType());
		initFilters();
		initLayout(initColumnModel(), true);
		//initUserToolBar();
		
	}
	
	private void initFilters() {
		filterAction = new SplitButton("Filter");
		filterAction.setMenu(initFilterMenu());
		filterAction.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be){
				String filtersURL = "?";
				
				SimpleComboValue<String> value;
				String[] filterNames = new String[] {};
				for (int i = 0; i < advancedFilters.size(); i++) {
					value = advancedFilters.get(i).getValue();
					if (value != null) {
						filtersURL += (filtersURL.equals("?") ? filterNames[i] + "=" : "&" + filterNames[i] + "=") + value.getValue();
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
		List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
		
		ColumnConfig column = new ColumnConfig("username", "Username", 100);
		CellEditor editor   = new CellEditor(new TextField<String>());
	    column.setEditor(editor);
	    configs.add(column);
	    
	    column = new ColumnConfig("first_name", "First Name", 100);
	    column.setEditor(new CellEditor(new TextField<String>()));
	    configs.add(column);
	    
	    column = new ColumnConfig("last_name", "Last Name", 100);
	    column.setEditor(new CellEditor(new TextField<String>()));
	    configs.add(column);
	    
	    column = new ColumnConfig("pst_id", "PST ID", 100);
	    column.setEditor(new CellEditor(new TextField<String>()));
	    column.setNumberFormat(NumberFormat.getFormat("#"));
	    configs.add(column);
	    
	    column = new ColumnConfig("original_id", "Original ID", 100);
	    column.setEditor(new CellEditor(new TextField<String>()));
	    column.setNumberFormat(NumberFormat.getFormat("#"));
	    configs.add(column);
	    
	    CheckColumnConfig checkColumn = new CheckColumnConfig("sanctioned", "Sanctioned?", 70);
	    checkColumn.setEditor(new CellEditor(new CheckBox()));
	    configs.add(checkColumn);
	    checkBoxes.add(checkColumn);
	    
	    column = new ColumnConfig("role", "Role", 80);
	    column.setEditor(initCombo(new String[]{"Administrator", "Observer", "Operator"}));
	    configs.add(column);
	    
	    checkColumn = new CheckColumnConfig("staff", "Staff?", 65);
	    checkColumn.setEditor(new CellEditor(new CheckBox()));
	    configs.add(checkColumn);
	    checkBoxes.add(checkColumn);
	    
	    column = new ColumnConfig("projects", "Projects", 800);
	    editor = new CellEditor(new TextField<String>());
	    editor.disable();
	    column.setEditor(editor);
	    configs.add(column);
	    
	    return new ColumnModel(configs);
	}

}
