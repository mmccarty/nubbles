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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Exception;
import java.util.Iterator;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.google.gwt.core.client.GWT;

public class ProjectEmailPagingToolBar extends PagingToolBar {
	
	public ProjectEmailPagingToolBar(final int pageSize)
	{
		super(pageSize);
	}
	
	public List<String> getSelections()
	{
		int j;
		List<String> pcodes = new ArrayList<String>();
		
		getPageSelections();  // need current page too.  Get it before setting up iterator.
		Iterator<Integer> iterator  = selections.keySet().iterator();
		
		while (iterator.hasNext())
		{
			List<String> sl = selections.get(iterator.next());
			
			for (j = 0; j < sl.size(); ++j)
			{
				pcodes.add(sl.get(j));
			}
		}
		
		return pcodes;
		
	}
	
	public void clearSelections()
	{
		selections.clear();
	}
	
	public void setGrid(EditorGrid<BaseModelData> g)
	{
		grid = g;
	}
	
	@Override
	public void first()
	{
		getPageSelections();
		super.first();
	}

	@Override
	public void next()
	{
		getPageSelections();
		super.next();
	}

    @Override
	public void previous()
    {
    	getPageSelections();
    	super.previous();
    }
    
    @Override
    public void setActivePage(int page)
    {
    	getPageSelections();
    	super.setActivePage(page);
    }

    @Override
    public void last()
    {
    	getPageSelections();
    	super.last();
    }
    
    @Override
    public void refresh()
    {
    	getPageSelections();
    	super.refresh();
    }
    
    @Override
    protected void onLoad(LoadEvent event)
    {
    	super.onLoad(event);
    	setPageSelections();
    }
    
    
    private void getPageSelections()
    {
    	String text = "ProjectEmailPagingToolBar getPageSelections: ";
    	List<BaseModelData> selection_list;
    	List<String> selected_pcodes;
    	
    	try  // grid could be uninitialized.
    	{
    		int active_page = getActivePage();
    		selection_list = grid.getSelectionModel().getSelectedItems();
    		selected_pcodes = new ArrayList<String>();
    		
    		if (!selection_list.isEmpty())
    		{
    			for (int i = 0; i < selection_list.size(); ++i)
    			{
    				String name = selection_list.get(i).get("pcode");
    				selected_pcodes.add(name);
    			}
    			
    			selections.put(active_page, selected_pcodes);
    		}
     	}
    	catch (Exception e)
    	{
    		text += "Caught exception " + e;
    	}
    }
    
    private void setPageSelections()
    {
       	String text = "ProjectEmailPagingToolBar getPageSelections: ";
    	List<String> pcodes;
    	
    	try
    	{
    		int active_page = getActivePage();
    		pcodes = selections.get(active_page);
 
    		if (pcodes != null)
    		{
        		if (!pcodes.isEmpty())
        		{
        	   		ListStore<BaseModelData> store = grid.getStore();
        	   		int count = store.getCount();
        	   		
        	   		for (int i = 0; i < count; ++i)
        	   		{
        	   			String pcode = store.getAt(i).get("pcode");

        	   			for (int j = 0; j < pcodes.size(); ++j)
        	   			{
        	   				if (pcodes.get(j).equals(pcode))
        	   				{
        	   					grid.getSelectionModel().select(i, true);
        	   				}
        	   			}
        	   		}
        		}
     		}
     	}
    	catch (Exception e)
       	{
    		text += "Caught exception " + e;
    	}
    }
    
	private EditorGrid<BaseModelData> grid;
	// note "Integer" and not "int".  "int" is a primitive type, and you can only use objects
	// as keys, and Integer is an int object.
	Map<Integer, List<String>> selections = new HashMap<Integer, List<String>>();

}
