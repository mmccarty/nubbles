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

package edu.nrao.dss.client.util.dssgwtcal;

import java.util.Date;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.Widget;

// This class acts as a wrapper around the DayViewGrid and DayViewTimeline Classes.
// A scroll panel is the parent (for scrolling - duh!), followed by a FlexTable, which
// actually positions the timeline and grid properly.

public class DayViewBody extends Composite {
	private FlexTable layout = new FlexTable();
	private ScrollPanel scrollPanel = new ScrollPanel();
	private DayViewTimeline timeline = null;
	private DayViewGrid grid = null;
	private HasSettings settings = null;

	public void add(Widget w) {
            scrollPanel.add(w);
        }
	
	public ScrollPanel getScrollPanel() {
		return scrollPanel;
	}

	public DayViewGrid getGrid() {
		return grid;
	}
	
	public DayViewTimeline getTimeline() {
		return timeline;
	}

	public DayViewGrid getDayViewGrid() {
		return grid;
	}

	public DayViewTimeline getDayViewTimeline() {
		return timeline;
	}

	public DayViewBody(HasSettings settings) {
		initWidget(scrollPanel);
		this.settings = settings;
		this.timeline = new DayViewTimeline(settings);
		this.grid = new DayViewGrid(settings);
		scrollPanel.setStylePrimaryName("scroll-area");
		DOM.setStyleAttribute(scrollPanel.getElement(), "overflowX",
				"hidden");
		DOM.setStyleAttribute(scrollPanel.getElement(), "overflowY",
				"scroll");

		// create the calendar body layout table
		layout.setCellPadding(0);
		layout.setBorderWidth(0);
		layout.setCellSpacing(0);
		layout.getColumnFormatter().setWidth(1, "99%");
		
		// set vertical alignment
		VerticalAlignmentConstant valign = HasVerticalAlignment.ALIGN_TOP;
		layout.getCellFormatter().setVerticalAlignment(0, 0, valign);
		layout.getCellFormatter().setVerticalAlignment(0, 1, valign);

		grid.setStyleName("gwt-appointment-panel");
                
        layout.getCellFormatter().setWidth(0, 0, "50px");
		DOM.setStyleAttribute(layout.getElement(), "tableLayout", "fixed");                
        
		// here we actually add the timeline and grid to the correct positions
		layout.setWidget(0, 0, timeline);
		layout.setWidget(0, 1, grid);
		
		// finally, add everything to the scroll panel
		scrollPanel.add(layout);
	}

	public void setDays(Date date, int days) {
		grid.build(settings.getSettings().getWorkingHourStart(), settings.getSettings().getWorkingHourEnd(), days);
	}
	
	public void addLabel(Label lb) {
		getGrid().grid.add((Widget) lb);
	}
}
