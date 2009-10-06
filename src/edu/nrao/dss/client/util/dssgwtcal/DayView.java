package edu.nrao.dss.client.util.dssgwtcal;

import edu.nrao.dss.client.util.dssgwtcal.util.AppointmentUtil;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

public class DayView extends CalendarView {

    // <editor-fold desc="Static Fields" defaultState="collapse">
    private static final String GWT_CALENDAR_STYLE = "gwt-cal";
    // </editor-fold>
    // <editor-fold desc="Private Fields" defaultState="collapse">
    private DayViewHeader dayViewHeader = null;
    private DayViewBody dayViewBody = null;
    private DayViewLayoutStrategy layoutStrategy = null;
    private boolean lastWasKeyDown = false;
    private FocusPanel focusPanel = new FocusPanel();
    // </editor-fold>

    // <editor-fold desc="Constructors" defaultState="collapse">
    public DayView() {
        this(CalendarSettings.DEFAULT_SETTINGS);
    }

    public DayView(CalendarSettings settings) {

        super(settings);

        this.dayViewBody = new DayViewBody(this);
        this.dayViewHeader = new DayViewHeader(this);
        this.layoutStrategy = new DayViewLayoutStrategy(this);

        this.setStyleName(GWT_CALENDAR_STYLE);

        //focusPanel.setVisible(false);
        rootPanel.add(focusPanel);

        rootPanel.add(dayViewHeader);
        rootPanel.add(dayViewBody);



        //border: 0px none ; width: 0px; height: 0px; position: absolute; top: -5px; left: -5px;
        DOM.setStyleAttribute(focusPanel.getElement(), "position", "absolute");
        DOM.setStyleAttribute(focusPanel.getElement(), "top", "-10");
        DOM.setStyleAttribute(focusPanel.getElement(), "left", "-10");
        DOM.setStyleAttribute(focusPanel.getElement(), "height", "0px");
        DOM.setStyleAttribute(focusPanel.getElement(), "width", "0px");
        focusPanel.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                if (!lastWasKeyDown) {
                    keyboardNavigation(event.getNativeEvent().getKeyCode());
                }
                lastWasKeyDown = false;
            }
        });
        focusPanel.addKeyUpHandler(new KeyUpHandler() {

            public void onKeyUp(KeyUpEvent event) {

                lastWasKeyDown = false;
            }
        });
        focusPanel.addKeyDownHandler(new KeyDownHandler() {

            public void onKeyDown(KeyDownEvent event) {
                keyboardNavigation(event.getNativeEvent().getKeyCode());
                lastWasKeyDown = true;
            }
        });


        doLayout();
    }

    // </editor-fold>
    // <editor-fold desc="Public Methods" defaultState="collapse">
    @Override
    public void doLayout() {

        if (layoutSuspended) {
            layoutPending = true;
            return;
        }

        dayViewHeader.setDays((Date) getDate().clone(), getDays());
        dayViewHeader.setYear((Date) getDate().clone());
        dayViewBody.setDays((Date) getDate().clone(), getDays());
        dayViewBody.getTimeline().prepare();

        // we don't want to re-sort a sorted array
        // so we only sort if some action has triggered a new sort
        if (sortPending) {
            Collections.sort(appointments);
            sortPending = false;
        }

        // remove all appointments from their parent
        for (AppointmentInterface appt : appointments) {
            //((Widget) appt).removeFromParent();
        }

        Date tmpDate = (Date) getDate().clone();

        for (int i = 0; i < getDays(); i++) {

            ArrayList<AppointmentInterface> filteredList =
                    AppointmentUtil.filterListByDate(appointments, tmpDate);

            // perform layout
            ArrayList<AppointmentAdapter> appointmentAdapters =
                    layoutStrategy.doLayout(filteredList, i, getDays());

            // add all appointments back to the grid
            //CHANGE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            for (AppointmentAdapter appt : appointmentAdapters) {
                this.dayViewBody.getGrid().grid.add((Widget) appt.getAppointment());

            }

            tmpDate.setDate(tmpDate.getDate() + 1);
        }
    }

    public void scrollToHour(int hour) {
        dayViewBody.getScrollPanel().setScrollPosition(hour *
                getSettings().getIntervalsPerHour() * getSettings().getPixelsPerInterval());
    }

    @Override
    public void setHeight(String height) {

        super.setHeight(height);
        dayViewBody.setHeight(getOffsetHeight() - 2 - dayViewHeader.getOffsetHeight() + "px");
    }

    @Override
    public void setSize(String width, String height) {

        super.setSize(width, height);
        dayViewBody.setHeight(getOffsetHeight() - 2 - dayViewHeader.getOffsetHeight() + "px");
    }

    @Override
    @SuppressWarnings("fallthrough")
    public void onBrowserEvent(Event event) {
        int eventType = DOM.eventGetType(event);

        switch (eventType) {
            case Event.ONMOUSEDOWN:
                 {
                    if (DOM.eventGetCurrentTarget(event) == getElement()) {
                        Element elem = DOM.eventGetTarget(event);
                        Appointment appt =
                                AppointmentUtil.checkAppointmentElementClicked(elem, appointments);
                        if (appt != null) {
                            setValue(appt);
                        //focusPanel.setFocus(true);
                        }
                        //} else if(getSelectedAppointment()!=null) {
                        //    focusPanel.setFocus(true);
                        //}
                        focusPanel.setFocus(true);
                        DOM.eventCancelBubble(event, true);
                        DOM.eventPreventDefault(event);

                        //break;
                        return;
                    }//end if
            } //end case
        } //end switch
        
        super.onBrowserEvent(event);
    }

    private void keyboardNavigation(
            int key) {
        switch (key) {
            case KeyCodes.KEY_DELETE: {
                removeAppointment((Appointment) getSelectedAppointment());
                break;
            }
            case KeyCodes.KEY_LEFT:
            case KeyCodes.KEY_UP: {
                selectPreviousAppointment();
                dayViewBody.getScrollPanel().ensureVisible((UIObject) getSelectedAppointment());
                break;
            }
            case KeyCodes.KEY_RIGHT:
            case KeyCodes.KEY_DOWN: {
                selectNextAppointment();
                dayViewBody.getScrollPanel().ensureVisible((UIObject) getSelectedAppointment());
                break;
            }
        }
    }

    private void keyboardNavigation(Event event) {


        //only proceed if an appointment is selected
        if (getSelectedAppointment() == null) {
            return;
        }

        //get the key
        int key = DOM.eventGetKeyCode(event);
        keyboardNavigation(key);
    //GWT.log("pressed: " + key,null);

    }

    // </editor-fold>
}