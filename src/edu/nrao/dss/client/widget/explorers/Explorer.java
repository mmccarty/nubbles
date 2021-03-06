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

package edu.nrao.dss.client.widget.explorers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.DataField;
import com.extjs.gxt.ui.client.data.DataReader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.Record;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.SplitButton;
import com.extjs.gxt.ui.client.widget.button.ToggleButton;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.CheckColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

import edu.nrao.dss.client.data.OptionsFilter;
import edu.nrao.dss.client.util.DynamicHttpProxy;
import edu.nrao.dss.client.util.JSONCallbackAdapter;
import edu.nrao.dss.client.util.JSONRequest;
import edu.nrao.dss.client.util.PagingJsonReader;
import edu.nrao.dss.client.widget.FilterItem;
import edu.nrao.dss.client.widget.FilterMenu;

public class Explorer extends ContentPanel{
	
	public Explorer(String url, String defaultArgs, ModelType mType) {
		rootURL     = url;
		initialArgs = defaultArgs;
		modelType   = mType;
		defaultDate = "";
		pagingToolBar = null;
	}
	
	public Explorer(String url, String defaultArgs, ModelType mType, PagingToolBar ptb)
	{
		rootURL     = url;
		initialArgs = defaultArgs;
		modelType   = mType;
		defaultDate = "";
		pagingToolBar = ptb;
	}
	
	@SuppressWarnings("unchecked")
	protected void initLayout(ColumnModel cm, Boolean createToolBar) {
		
		setHeaderVisible(false);
		setLayout(new FitLayout());
		setCommitState(false);
		//setAutoHeight(true);
		setScrollMode(Scroll.NONE);
				
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, rootURL + initialArgs);

		DataReader reader = new PagingJsonReader<BasePagingLoadResult>(modelType);
		proxy  = new DynamicHttpProxy<BasePagingLoadResult<BaseModelData>>(builder);
		loader = new BasePagingLoader<BasePagingLoadResult<BaseModelData>>(proxy, reader);  
		loader.setRemoteSort(true);

		store = new ListStore<BaseModelData>(loader);
		
	    setGrid(new EditorGrid<BaseModelData>(store, cm));
	    //grid.setAutoHeight(true);
	    grid.setHeight(875);
	    
		GridSelectionModel<BaseModelData> selectionModel = new GridSelectionModel<BaseModelData>();
		selectionModel.setSelectionMode(SelectionMode.MULTI);
		grid.setSelectionModel(selectionModel);
		addPlugins();
		add(grid);
		grid.setBorders(true);
		
		if (createToolBar) {
		    initToolBar();
		}
		columnConfForm  = new ColumnConfigForm(this);
		filterComboForm = new FilterComboForm(this);
		undoredo        = new UndoRedo(this);
		
		initListeners();
		
		if (this.loadDataInitially) {
			loadData();
		}
	}
	
	protected void addPlugins() {
		for (CheckColumnConfig cb : checkBoxes) {
			cb.setFiresEvents(true);
			grid.addPlugin(cb);
		}
	}
	
	public void loadData() {
		loader.load(0, getPageSize());
	}
	
	private void initListeners() {
		
		undoredo.initListeners();
		
		store.addStoreListener(new StoreListener<BaseModelData>() {
			@Override
			public void storeUpdate(StoreEvent<BaseModelData> se) {
				save(se.getModel());
			}
		});
	}
	
	private void save(ModelData model) {
		if (!isCommitState()) {
			return;
		}
		ArrayList<String> keys   = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();

        keys.add("_method");
        values.add("put");

        for (String name : model.getPropertyNames()) {
        	Object value = model.get(name);
            if (value != null) {
                keys.add(name);
                values.add(value.toString());
            }
        }
        JSONRequest.post(rootURL + "/" + ((Number) model.get("id")).intValue(),
		         keys.toArray(new String[]{}),
		         values.toArray(new String[]{}),
		         new JSONCallbackAdapter());
        updateObservers();
	}
	
	// to be implemented by children
	public void registerObservers(){
		
	}
	
	// to be implemented by children
	public void updateObservers(){
		
	}
	
	// to be implemented by children
	public void viewObject() {
		//return grid.getSelectionModel().getSelectedItem();
	}
	
	// to be implemented by children
	public void actionOnObject() {
		//return grid.getSelectionModel().getSelectedItem();
	}	
	private void initToolBar() {
		if (pagingToolBar == null)
		{
			pagingToolBar = new PagingToolBar(50);
		}
		
		final TextField<String> pages = new TextField<String>();
		pages.setWidth(30);
		pages.setValue("50");
		pages.addKeyListener(new KeyListener() {
			public void componentKeyPress(ComponentEvent e) {
				if (e.getKeyCode() == 13) {
					int page_size = Integer.valueOf(pages.getValue()).intValue();
					pagingToolBar.setPageSize(page_size);
					setPageSize(page_size);
					loadData();
				}
			}
		});
		pages.setTitle("Page Size");
		pagingToolBar.add(pages);
		setBottomComponent(pagingToolBar);
		pagingToolBar.bind(loader);
		
		toolBar = new ToolBar();
		if (createFilterToolBar) {
			LayoutContainer toolBars = new LayoutContainer();
			setTopComponent(toolBars);
			filterToolBar = new ToolBar();
			toolBars.add(toolBar);
			toolBars.add(filterToolBar);
			initFilterToolBar();
		} else {
			setTopComponent(toolBar);
		}
		
		if (showColumnsMenu) {
			columnsItem = new Button("Columns");
			columnsItem.setToolTip("Manage column configurations.");
			columnsItem.setMenu(initColumnsMenu());
			toolBar.add(columnsItem);
		}
		
		viewItem = new Button("View");
		toolBar.add(viewItem);
		viewItem.setToolTip("View selected row.");
		viewItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
	        @Override
	        public void componentSelected(ButtonEvent ce) {
	            viewObject();	
	        }
	    });
		// hide this button by default
		viewItem.setVisible(false);
		
		addItem = new Button("Add");
		toolBar.add(addItem);
		addItem.setToolTip("Add a new row.");
		addItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
	        @Override
	        public void componentSelected(ButtonEvent ce) {
	        	HashMap<String, Object> fields = new HashMap<String, Object>();
	        	if (defaultDate != "") {
	        		fields.put("date", defaultDate);
	        	}
	        	addRecord(fields);
	        }
	    });
		
		final Button duplicateItem = new Button("Duplicate");
		toolBar.add(duplicateItem);
		duplicateItem.setToolTip("Copy a row.");
		duplicateItem.setEnabled(false);
		duplicateItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent be) {
            	HashMap<String, Object> fields = new HashMap<String, Object>(grid.getSelectionModel()
            			.getSelectedItem().getProperties());
            	fields.put("pcode", fields.get("pcode") + " - 2");
            	addRecord(fields);
                grid.getView().refresh(true);
            }
        });
		
		removeDialog = new Dialog();
		removeDialog.setHeading("Confirmation");
		removeDialog.addText("Remove record?");
		removeDialog.setButtons(Dialog.YESNO);
		removeDialog.setHideOnButtonClick(true);
		setRemoveApproval(removeDialog.getButtonById(Dialog.YES));
		getRemoveApproval().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				Double id = grid.getSelectionModel().getSelectedItem().get("id");
				JSONRequest.delete(rootURL + "/" + id.intValue(),
						new JSONCallbackAdapter() {
							public void onSuccess(JSONObject json) {
								store.remove(grid.getSelectionModel()
										.getSelectedItem());
							}
						});
				updateObservers();
			}
		});	
		removeDialog.hide();

	
		removeItem = new Button("Delete");
		toolBar.add(removeItem);
		removeItem.setToolTip("Delete a row.");
		// make it so that children can override this behavior
		setRemoveItemListener();
		
		columnEditItem = new ToggleButton("Column Edit");
		columnEditItem.setToolTip("Click to enable column edit mode which will repeat the last edit for all the selected rows.");
		toolBar.add(columnEditItem);
		
		// add a generic button that can be changed for whatever purpose a child class may have for it
		actionItem = new Button("Action");
		toolBar.add(actionItem);
		//actionItem.setToolTip("view row.");
		actionItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
	        @Override
	        public void componentSelected(ButtonEvent ce) {
	            actionOnObject();	
	        }
	    });
		// hide this button by default
		actionItem.setVisible(false);
		
		toolBar.add(new SeparatorToolItem());

		toolBar.add(new FillToolItem());
		toolBar.add(new SeparatorToolItem());
		
		undoItem = new Button();
		undoItem.setToolTip("Undo the last edit.");
		undoItem.disable();
		undoItem.setIcon(GXT.IMAGES.paging_toolbar_prev());
		toolBar.add(undoItem);
		
		undoItem.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				undoredo.doUndo();
			}
			
		});
		
		redoItem = new Button();
		redoItem.setToolTip("Redo the last undo.");
		redoItem.disable();
		redoItem.setIcon(GXT.IMAGES.paging_toolbar_next());
		toolBar.add(redoItem);
		
		redoItem.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				undoredo.doRedo();
			}
			
		});

		toolBar.add(new SeparatorToolItem());
		
		cancelItem = new Button("Cancel");
		cancelItem.setToolTip("Cancel changes.  Note: modified fields are indicated with a red trangle in the upper left corn of the cell.");
		toolBar.add(cancelItem);
		
		//  Reject outstanding changes
		cancelItem.addSelectionListener(new SelectionListener<ButtonEvent>() {

			@Override
			public void componentSelected(ButtonEvent ce) {
				store.rejectChanges();
				undoredo.reset();
			}
			
		});
		
		setSaveItem(new Button("Save"));
		getSaveItem().setToolTip("Save changes.  Note: modified fields are indicated with a red trangle in the upper left corn of the cell.");
		toolBar.add(getSaveItem());

		// Commit outstanding changes to the server.
		getSaveItem().addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				setCommitState(true);
				store.commitChanges();
				setCommitState(false);
				//loadData();
				//grid.getView().refresh(true);
				
				undoredo.reset();
			}
		});
		
		// Enable the "Duplicate" button only if there is a selection.
		grid.getSelectionModel().addSelectionChangedListener(
				new SelectionChangedListener<BaseModelData>() {
					@Override
					public void selectionChanged(SelectionChangedEvent<BaseModelData> se) {
						duplicateItem.setEnabled(!grid.getSelectionModel().getSelectedItems().isEmpty());
					}
				});
	}
	
	private void initFilterToolBar() {
		filter = new FilterItem(Explorer.this, false);
		filterToolBar.add(filter.getTextField());

		for (SimpleComboBox<String> f : advancedFilters) {
			filterToolBar.add(new SeparatorToolItem());
			//  Karen wants to take out the labels on the advanced filters and
			//  use the tool tips for now.
			//filterToolBar.add(new LabelToolItem(f.getTitle()));
		    filterToolBar.add(f);
		}
		filterToolBar.add(new SeparatorToolItem());
		Button reset = new Button("Reset");
		reset.setToolTip("Resets filter drop down menus.");
		reset.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				for (SimpleComboBox<String> f : advancedFilters) {
					f.reset();
				}
				filter.getTextField().setValue("");
			}
		});
		filterToolBar.add(reset);
		filterToolBar.add(new SeparatorToolItem());
		if (filterAction != null) {
			filterToolBar.add(filterAction);
			filterAction.setToolTip("Execute filter combination.");
		}
	}
	
	private Menu initColumnsMenu() {
		final Menu menu = new Menu();
		MenuItem saveConfig = new MenuItem("Save Column Combination");
		saveConfig.addSelectionListener(new SelectionListener<MenuEvent>() {

			public void componentSelected(MenuEvent ce) {
				com.extjs.gxt.ui.client.widget.Window w = columnConfForm.getWindow();
				columnConfForm.show();
				w.show();
			}
			
		});
		
		menu.add(saveConfig);
		MenuItem removeConfigs = new MenuItem("Remove Checked Items");
		removeConfigs.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				for(Component mi : menu.getItems()){
					//  Doing this the Python way. ;)
					try {
						final ColumnConfigMenuItem cmi = (ColumnConfigMenuItem) mi;
						if (cmi.isChecked()){
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("method_", "DELETE");
							JSONRequest.post("/scheduler/configurations/explorer/columnConfigs/" + cmi.config_id
									       , data
									       , new JSONCallbackAdapter() {
								public void onSuccess(JSONObject json){
									menu.remove(cmi);
								}
							});
						}
					} catch (ClassCastException e) {
						
					}
					
				}
			}
			
		});
		menu.add(removeConfigs);
		menu.add(new SeparatorMenuItem());
		
		MenuItem all = new MenuItem("Restore All");
		all.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				for (ColumnConfig cc : grid.getColumnModel().getColumns()){
					cc.setHidden(false);
				}
				grid.getView().refresh(true);	
			}
			
		});
		menu.add(all);
		menu.add(new SeparatorMenuItem());
		
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("explorer", rootURL);
		// Get save configurations from the server and populate them as menu items
		JSONRequest.get("/scheduler/configurations/explorer/columnConfigs", data, new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json){
				JSONArray configs = json.get("configs").isArray();
				for (int i = 0; i < configs.size(); ++i) {
					JSONArray config = configs.get(i).isArray();
					String config_id = config.get(1).isNumber().toString();
					ColumnConfigMenuItem mi = 
						new ColumnConfigMenuItem(grid
							                   , config.get(0).isString().stringValue()
							                   , config_id);
					menu.add(mi);
					columnConfigIds.add(config_id);
				}
			}
		});
		return menu;
	}

	protected FilterMenu initFilterMenu() {
		filterMenu = new FilterMenu();
		MenuItem saveCombos = new MenuItem("Save Filter Combination");
		saveCombos.addSelectionListener(new SelectionListener<MenuEvent>() {

			public void componentSelected(MenuEvent ce) {
				com.extjs.gxt.ui.client.widget.Window w = filterComboForm.getWindow();
				filterComboForm.show();
				w.show();
			}
			
		});
		
		filterMenu.add(saveCombos);
		MenuItem removeCombos = new MenuItem("Remove Checked Items");
		removeCombos.addSelectionListener(new SelectionListener<MenuEvent>() {

			@Override
			public void componentSelected(MenuEvent ce) {
				for(Component mi : filterMenu.getItems()){
					//  Doing this the Python way. ;)
					try {
						final FilterComboMenuItem cmi = (FilterComboMenuItem) mi;
						if (cmi.isChecked()){
							HashMap<String, Object> data = new HashMap<String, Object>();
							data.put("method_", "DELETE");
							JSONRequest.post("/scheduler/configurations/explorer/filterCombos/" + cmi.combo_id
									       , data
									       , new JSONCallbackAdapter() {
								public void onSuccess(JSONObject json){
									filterMenu.remove(cmi);
								}
							});
						}
					} catch (ClassCastException e) {
						
					}
					
				}
			}
			
		});
		filterMenu.add(removeCombos);
		filterMenu.add(new SeparatorMenuItem());
		
		// We'll need to pass in this object as to the FilterComboMenuItem constructor below
		// so do this to make sure it will be in scope. 
		final Explorer e = this;
		HashMap<String, Object> data = new HashMap<String, Object>();
		data.put("explorer", rootURL);
		// Get save configurations from the server and populate them as menu items
		JSONRequest.get("/scheduler/configurations/explorer/filterCombos", data, new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json){
				JSONArray configs = json.get("configs").isArray();
				for (int i = 0; i < configs.size(); ++i) {
					JSONArray config = configs.get(i).isArray();
					String filter_id = config.get(1).isNumber().toString();
					FilterComboMenuItem mi = 
						new FilterComboMenuItem(e
							                   , config.get(0).isString().stringValue()
							                   , filter_id);
					filterMenu.add(mi);
					filterComboIds.add(filter_id);
				}
			}
		});
		return filterMenu;
	}
	
	protected void initOptionsFilter() {
		optionsFilter = new OptionsFilter();
		toolBar.insert(new SeparatorToolItem(), 6);
		toolBar.insert(optionsFilter.getSemester(), 7);
		toolBar.insert(optionsFilter.getEnabled(), 8);
		toolBar.insert(optionsFilter.getNotComplete(), 9);
		
	}
	
	protected void setRemoveItemListener() {
		removeItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent be) {
				removeDialog.show();
			}
		});
	}

	public void addRecordInterface(HashMap<String, Object> fields) {
		addRecord(fields);
	}
	
	protected void addRecord(HashMap<String, Object> fields) {
		JSONRequest.post(rootURL, fields, new JSONCallbackAdapter() {
			@Override
			public void onSuccess(JSONObject json) {
				BaseModelData model = new BaseModelData();
				for (int i = 0; i < modelType.getFieldCount(); ++i) {
					DataField field = modelType.getField(i);
					String fName = field.getName();
					if (json.containsKey(fName)) {
						// Set model value dependent on data type
						JSONValue value = json.get(fName);
						if (value.isNumber() != null) {
							double numValue = value.isNumber().doubleValue();
							// Note: we're treating even integer values like doubles here.  Doesn't appear
							// to be an issue.  1.0 is displayed as 1 anyways.
							model.set(fName, numValue);
						} else if (value.isBoolean()!= null) {
							model.set(fName, value.isBoolean().booleanValue());
						} else if (value.isString() != null) {
							model.set(fName, value.isString().stringValue());
						} else if (value.isNull() != null) {
                            // not raising an error seems to be fine.
						} else {
							Window.alert("unknown JSON value type");
						}
					}
				}
				grid.stopEditing();
				store.insert(model, 0);
				//grid.getView().refresh(true);
				grid.getSelectionModel().select(model, false);
			}
		});
	}

	protected SimpleComboBox<String> initCombo(String title, String[] options, int width) {
		SimpleComboBox<String> filter = new SimpleComboBox<String>();
		filter.setTriggerAction(TriggerAction.ALL);
		filter.setWidth(width);
		filter.setEmptyText(title);
		filter.setTitle(title);
		for (String o : options) {
			filter.add(o);
		}
		return filter;
	}
	
	protected CellEditor initCombo(String[] options) {
	    final SimpleComboBox<String> combo = new SimpleComboBox<String>();
	    combo.setForceSelection(true);
	    combo.setTriggerAction(TriggerAction.ALL);
	    for (String o : options) {
	    	combo.add(o);
	    }

	    CellEditor editor = new CellEditor(combo) {
	      @Override
	      public Object preProcessValue(Object value) {
	        if (value == null) {
	          return value;
	        }
	        return combo.findModel(value.toString());
	      }

	      @Override
	      public Object postProcessValue(Object value) {
	        if (value == null) {
	          return value;
	        }
	        return ((ModelData) value).get("value");
	      }
	    };
	    return editor;
	}
	
	public DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> getProxy() {
		return proxy;
	}
	
	public boolean getLoadDataInitially() {
		return loadDataInitially;
	}
	
	public void setLoadDataInitially(boolean state) {
		loadDataInitially = state;
	}
	
	public String getRootURL() {
		return rootURL;
	}
	
	public void setRootURL(String rurl) {
		rootURL = rurl;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, rootURL);
		proxy.setBuilder(builder);
	}
	
	public ToggleButton getColumnEditItem() {
		return columnEditItem;
	}
	
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}
	
	public Button getColumnsItem() {
		return columnsItem;
	}
	
	public SplitButton getFilterAction() {
		return filterAction;
	}
	
	public FilterMenu getFilterMenu() {
		return filterMenu;
	}

	public List<SimpleComboBox<String>> getAdvancedFilters() {
		return advancedFilters;
	}
	
	public void setCommitState(boolean commitState) {
		this.commitState = commitState;
	}

	public boolean isCommitState() {
		return commitState;
	}
	
	public void setShowColumnsMenu(boolean state) {
		showColumnsMenu = state;
	}
	
	public void setCreateFilterToolBar(boolean state) {
		createFilterToolBar = state;
	}
	
	public void setGrid(EditorGrid<BaseModelData> grid) {
		this.grid = grid;
	}

	public EditorGrid<BaseModelData> getGrid() {
		return grid;
	}

	public void setSaveItem(Button saveItem) {
		this.saveItem = saveItem;
	}

	public Button getSaveItem() {
		return saveItem;
	}

	public void setRemoveApproval(Button removeApproval) {
		this.removeApproval = removeApproval;
	}

	public Button getRemoveApproval() {
		return removeApproval;
	}

	/** Provides basic spreadsheet-like functionality. */
	private EditorGrid<BaseModelData> grid;

	/** Use store.add() to remember dynamically created records. */
	protected ListStore<BaseModelData> store;
	
	/** Flag for enforcing saves only on Save button press. **/
	private boolean commitState;
	private int pageSize = 50;
	private ModelType modelType;	
	private ColumnConfigForm columnConfForm;
	private FilterComboForm filterComboForm;
	private UndoRedo undoredo;
	private Button columnsItem;
	private boolean showColumnsMenu = true;
	private FilterMenu filterMenu;
	private boolean createFilterToolBar = true;
	private ToggleButton columnEditItem;
	
	public List<String> filterComboIds = new ArrayList<String>();
	public List<String> columnConfigIds = new ArrayList<String>();

	protected List<CheckColumnConfig> checkBoxes = new ArrayList<CheckColumnConfig>();
	
	/** Use loader.load() to refresh with the list of records on the server. */
	protected PagingLoader<BasePagingLoadResult<BaseModelData>> loader;
	
	protected DynamicHttpProxy<BasePagingLoadResult<BaseModelData>> proxy;
	
	protected String rootURL;
	protected String initialArgs;
	protected boolean loadDataInitially = true;
	
	protected List<SimpleComboBox<String>> advancedFilters = new ArrayList<SimpleComboBox<String>>();
	
	protected SplitButton filterAction;
	private Button saveItem;
	protected Button cancelItem;
	protected Button undoItem;
	protected Button redoItem;
	protected Button viewItem;
	protected Button addItem;
	protected Button removeItem;
	private Button removeApproval;
	protected Dialog removeDialog;
	protected Button actionItem;
	protected ToolBar toolBar;
	protected ToolBar filterToolBar;
	protected PagingToolBar pagingToolBar;
	protected OptionsFilter optionsFilter;
	
	protected FilterItem filter;

	// Note: really should be grabing this list from the server, but it's static 
	// enough to hard code here.
	protected String[] semesters = new String[] {
			  "12B", "12A"
			, "11B", "11A"
		    , "10C", "10B", "10A"
		    , "09C", "09B", "09A"
            , "08C", "08B", "08A"
            , "07C", "07B", "07A"
            , "06C", "06B", "06A"
            , "05C", "05B", "05A"
            , "04A"
            };
	
	protected String defaultDate;
}
