package edu.nrao.dss.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

//based off unit tests in nell, using this expected JSON:
//expected = '{"diff": [{"down": [], "up": ["RRI", "342", "450"], "day": "04/06/2009"}, {"down": ["RRI"], "up": ["600"], "day": "04/11/2009"}]
//         , "receivers": ["RRI", "342", "450", "600", "800", "1070", "L", "S", "C", "X", "Ku", "K", "Ka", "Q", "MBA", "Z", "Hol", "KFPA", "W"]
//         , "maintenance": ["2009-04-07 12:00:00"]
//         , "schedule": {"04/11/2009": ["342", "450", "600"], "04/06/2009": ["RRI", "342", "450"]}}'

public class RcvrScheduleJSON extends JSONObject {

	public String[] rx = {"RRI", "342", "450", "600", "800", "1070", "L", "S", "C", "X", "Ku", "K", "Ka", "Q", "MBA", "Z", "Hol", "KFPA", "W"};
	public String maintenanceDay = new String("2009-04-07 12:00:00");	
	
	public RcvrScheduleJSON() {
	    populate();	
	}
	
	public void populate() {
		
		// setup the receivers
		JSONArray receivers = new JSONArray();
		JSONString rcvrJson;
		for (int i = 0; i<rx.length; i++) {
			rcvrJson = new JSONString(rx[i]);
			receivers.set(i, rcvrJson);
		}
		this.put("receivers", receivers);
		
		// setup the maintenance days
		JSONArray days = new JSONArray();
		JSONString dayJson = new JSONString(maintenanceDay); //"2009-04-07 12:00:00");
		days.set(0, dayJson);
		this.put("maintenance", days);
		
		// setup the diff schedule
		JSONArray diffs = new JSONArray();
		addDiff(diffs, 0, "04/06/2009", new String[] {"RRI", "342", "450"}, new String[] {});
		addDiff(diffs, 1, "04/11/2009", new String[] {"600"},               new String[] {"RRI"});
		this.put("diff", diffs);
	
		// setup the traditional schedule
		JSONObject schedule = new JSONObject();
		addScheduleDate(schedule, "04/11/2009", new String[] {"342", "450", "600"});
		addScheduleDate(schedule, "04/06/2009", new String[] {"RRI", "342", "450"});
		this.put("schedule", schedule);
	}

	private void addScheduleDate(JSONObject schedule, String date, String[] rx) {
		JSONArray availableJson = new JSONArray();
		for (int i=0; i<rx.length; i++) {
			availableJson.set(i, new JSONString(rx[i]));
		}
	    schedule.put(date, availableJson);	
	}
	
	private void addDiff(JSONArray diffs, int index, String day, String[] ups, String[] downs) {
		// create the diff object
		JSONObject diff = new JSONObject();
		diff.put("day", new JSONString(day));
		JSONArray jsonUps = new JSONArray();
		for (int i=0; i<ups.length; i++) {
			jsonUps.set(i, new JSONString(ups[i]));
		}
		diff.put("up", jsonUps);
		JSONArray jsonDowns = new JSONArray();
		for (int i=0; i<downs.length; i++) {
			jsonDowns.set(i, new JSONString(downs[i]));
		}
		diff.put("down", jsonDowns);
		// add the diff object
		diffs.set(index, diff);
	}	
}
