package edu.nrao.dss.client;

import java.util.Date;
import java.util.HashMap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.extjs.gxt.ui.client.widget.MessageBox;

public class ScoresAccess {
	private String banner;
	private String headers[];
	private String factors[][];
	MessageBox box;

	public void request(final ScoresDisplay display, Integer sessionId,
			final String label, final Date start, Integer minutes,
			final String timezone) {
		JSONRequest.get("/factors", formKeys(sessionId, label, start, minutes,
				timezone), new JSONCallbackAdapter() {
			public void onSuccess(JSONObject json) {
				float[] scores = extractScores(json);
				display.show(label, scores);
				box.close();
			}
		});
	}

	private HashMap<String, Object> formKeys(Integer index, String label,
			Date start, Integer minutes, String timezone) {
		HashMap<String, Object> keys = new HashMap<String, Object>();
		keys.put("id", index);
		keys.put("tz", timezone);
		String startStr = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss")
				.format(start);
		keys.put("start", startStr);
		keys.put("duration", minutes);
		String msg = "Generating scheduling scores for " + label;
		box = MessageBox.wait("Getting scores", msg, "Be Patient ...");
		return keys;
	}

//	private void populateBanner(JSONObject json, String label) {
//		String ra   = json.get("ra").isString().stringValue();
//		String dec  = json.get("dec").isString().stringValue();
//		String freq = json.get("freq").isString().stringValue();
//		boolean schedulable = json.get("alive").isBoolean().booleanValue();
//		boolean type = json.get("type").isBoolean().booleanValue();
//		boolean time = json.get("time").isBoolean().booleanValue();
//		boolean not_complete = json.get("not_complete").isBoolean().booleanValue();
//		boolean enabled = json.get("enabled").isBoolean().booleanValue();
//		boolean authorized = json.get("authorized").isBoolean().booleanValue();
//		boolean observers = json.get("observers").isBoolean().booleanValue();
//		banner = label + ": (RA, Dec) = (" +
//		         ra + " Hr, " +
//		         dec + " Deg); Freq = " +
//		         freq + " GHz";
//		if (!schedulable) {
//			StringBuilder buf = new StringBuilder();
//			buf.append(";    Not schedulable: ");
//			if (!type) {
//			    buf.append("Not open or no viable windows.  ");
//		    }
//			if (!time) {
//				buf.append("Time exhausted.  ");
//			}
//			if (!not_complete) {
//				buf.append("Marked completed.  ");
//			}
//			if (!enabled) {
//				buf.append("Not enabled.  ");
//			}
//			if (!authorized) {
//				buf.append("Not authorized.  ");
//			}
//			if (!observers) {
//				buf.append("No observers.  ");
//			}
//			if (!type && !time && !not_complete && !enabled && !authorized) {
//				buf.append("Do you need any other reasons this cannot be scheduled?");
//			}
//			banner += buf.toString();
//		}
//	}

//	private void populateHeadersFactors(JSONObject json, Date start, String tz) {
//		JSONArray fs = json.get("factors").isArray();
//		JSONArray fs0 = fs.get(0).isArray();
//		int length = fs0.size();
//		// Grid size including times, but not headers
//		int rows = fs.size();
//		int cols = length + 1;
//		// Extract column header names
//		headers = new String[cols];
//		headers[0] = "Date [" + tz + "]";
//		for (int i = 0; i < length; ++i) {
//			String str = fs0.get(i).isArray().get(0).toString();
//			headers[i + 1] = str.substring(1, str.indexOf('"', 1));
//		}
//		// Extract factor values
//		factors = new String[rows][cols];
//		long msecs = start.getTime();
//		Date quarter = new Date();
//		DateTimeFormat dtf = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss");
//		for (int t = 0; t < rows; ++t) {
//			JSONArray row = fs.get(t).isArray();
//			quarter.setTime(msecs);
//			msecs += 15 * 60 * 1000;
//			factors[t][0] = dtf.format(quarter);
//			for (int f = 0; f < length; ++f) {
//				JSONObject obj = row.get(f).isArray().get(1).isObject();
//				String repr;
//				if (obj.containsKey("Nothing")) {
//					repr = "?";
//				} else {
//					double value = obj.get("Just").isNumber().doubleValue();
//					repr = NumberFormat.getFormat("#0.000").format(value);
//				}
//				factors[t][f + 1] = repr;
//			}
//		}
//	}
	
	private float[] extractScores(JSONObject json) {
		float scoreValue;
		
		JSONArray fs = json.get("factors").isArray();
		int numScores = fs.size();
		float[] scores = new float[numScores];
		
		for (int i = 0; i < numScores; ++i) {
			JSONArray factors = fs.get(i).isArray();
			int numFactors = factors.size();
			for (int j = 0; j < numFactors; j++) {
				JSONArray factor = factors.get(j).isArray();
				// for some reason, each factor is an array of [name , {"Just" : value}]
				String factorName = factor.get(0).isString().stringValue();
				if (factorName.compareTo("score") == 0) {
					JSONObject scoreObj = factor.get(1).isObject();
					if (scoreObj.containsKey("Just")) {
						scoreValue = (float) scoreObj.get("Just").isNumber().doubleValue();
					} else {
						scoreValue = -1.0f;
					}
		            scores[i] = scoreValue;			
				}
				
			}
			
		}
//		float scores[] = new float[24 * 4 * 3];
//		for (int i = 0; i < scores.length; i++) {
//			scores[i] = (float) i;
//		}	
		return scores;
	}

}