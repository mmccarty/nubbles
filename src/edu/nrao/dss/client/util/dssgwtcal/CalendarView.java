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

import com.google.gwt.event.dom.client.HasKeyPressHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import java.util.ArrayList;
import java.util.Date;

/**
 * This is a base class all Calendar Views (i.e. Day view, month view, list view)
 * should build upon. It defines and or implements all required methods and
 * properties.
 * @author Brad Rydzewski
 */
public abstract class CalendarView extends Composite implements HasValue<Appointment>, HasSettings {

    protected AbsolutePanel rootPanel = new AbsolutePanel();
    protected boolean layoutSuspended = false;
    protected boolean layoutPending = false;
    protected boolean sortPending = true;
    private Date date = new Date();
    private int days = 3;
    protected ArrayList<Appointment> appointments = new ArrayList<Appointment>();
    protected Appointment selectedAppointment = null;
    protected ArrayList<Event> events = new ArrayList<Event>();
    protected Event selectedEvent = null;
    private CalendarSettings settings = null;

    public CalendarView(CalendarSettings settings) {
        
        initWidget(rootPanel);
        this.settings = settings;
        sinkEvents(Event.ONMOUSEDOWN | Event.KEYEVENTS); 
    }
    
    public abstract void doLayout();

    public void suspendLayout() {
        layoutSuspended = true;
    }

    public void resumeLayout() {
        layoutSuspended = false;
        if (layoutPending) {
            doLayout();
        }
    }

    public CalendarSettings getSettings() {
        return settings;
    }

    public void setSettings(CalendarSettings settings) {
        this.settings = settings;
        doLayout();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date, int days) {
        this.date = date;
        this.days = days;
        doLayout();
    }

    public void setDate(Date date) {
        this.date = date;
        doLayout();
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
        doLayout();
    }
    
    protected ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public int getAppointmentCount() {
        return appointments.size();
    }

    public AppointmentInterface getAppointmentAtIndex(int index) {
        return appointments.get(index);
    }

    public AppointmentInterface getSelectedAppointment() {
        return selectedAppointment;
    }

    public boolean selectNextAppointment() {
        
        if(getSelectedAppointment()==null) return false;
        int index = appointments.indexOf(getSelectedAppointment());
        if(index>=appointments.size()) return false;
        Appointment appt = appointments.get(index+1);
        if(appt.isVisible()==false) return false;
        this.setValue(appt);
        return true;
    }
    
    public boolean selectPreviousAppointment() {
        if(getSelectedAppointment()==null) return false;
        int index = appointments.indexOf(getSelectedAppointment());
        if(index<=0) return false;
        Appointment appt = appointments.get(index-1);
        if(appt.isVisible()==false) return false;
        this.setValue(appt);
        return true;
    }

    public void setSelectedAppointment(Appointment appointment) {

        // add appointment if doesn't exist
        if (!appointments.contains(appointment)) {
            appointments.add(appointment);
        }

        // de-select currently selected appointment
        if (selectedAppointment != null) {
            selectedAppointment.setSelected(false);
        }

        // set newly selected appointment
        this.selectedAppointment = appointment;
        appointment.setSelected(true);
    }

    public void updateAppointment(Appointment appointment) {

        if (!appointments.contains(appointment)) {
            appointments.add(appointment);
        }

        sortPending = true;
        doLayout();
    }

    public void clearAppointments() {
        appointments.clear();
        doLayout();
    }

    public void removeAppointment(Appointment appointment) {
        appointments.remove(appointment);
        selectedAppointment = null;
        sortPending = true;
        doLayout();
    }
    
    //public void addEvent(Event event) {
    //	addAppointments(event.getAppointments());
    //	
    //}
    
    public void addAppointment(Appointment appointment) {
        this.appointments.add(appointment);
        this.sortPending = true;

        doLayout();
    }

    public void addAppointments(ArrayList<Appointment> appointments) {
        for (Appointment appointment : appointments) {
            addAppointment(appointment);
        }
    }

    public Appointment getValue() {
        return selectedAppointment;
    }

    public void setValue(Appointment value) {
        setValue(value, true);
    }

    public void setValue(Appointment value, boolean fireEvents) {

        Appointment oldValue = selectedAppointment;
        Appointment newValue = value;

        // de-select currently selected appointment
        if (oldValue != null) {
            oldValue.setSelected(false);

        }

        // set newly selected appointment
        selectedAppointment = newValue;
        newValue.setSelected(true);

        if (fireEvents) {
            ValueChangeEvent.fire(this, newValue);
        }

    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Appointment> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }
}
