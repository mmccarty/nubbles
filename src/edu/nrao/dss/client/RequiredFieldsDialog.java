package edu.nrao.dss.client;

import java.util.ArrayList;
import java.util.HashMap;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;

class RequiredFieldsDialog extends Dialog {
	@SuppressWarnings("unchecked")
    public RequiredFieldsDialog(final SessionExplorer sx, final RowType row) {
		
		// Configure the context for the dialog
		setHeading(row.getName());
		addText("Please enter the required fields.");
		setButtons(Dialog.OKCANCEL);
		setHeight(400);
		setWidth(350);

		FormPanel fp = new FormPanel();

		// Created needed fields and add to panel for the chosen session/row
		final ArrayList<Field> formFields = new ArrayList<Field>();
		for (String rf : row.getRequiredFields()) {
			Field f = row.getField(rf);
			fp.add(f);
			formFields.add(f);
			//Window.alert(f.getValue());
		}

		// Add panel to dialog
		add(fp);
		show();
		
		// Somebody decided to back out
		Button cancel = getButtonById(Dialog.CANCEL);
		cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				//close();
			}
		});

		// Request the new row/session
		Button ok = getButtonById(Dialog.OK);
		ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {
				HashMap<String, Object> sFieldsP =
				    sx.populateSessionFields(row.getRequiredFields(), formFields);
				sx.createNewSessionRow(row, sFieldsP);
				//close();
			}
		});
	}
}
