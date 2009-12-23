package edu.nrao.dss.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;

interface JSONCallback {
	public void onSuccess(JSONObject json);
	public void onError(String error, JSONObject json);
}

class JSONCallbackAdapter implements JSONCallback {
	public void onSuccess(JSONObject json) {
	}

	// Default error response is to alert the user.
	public void onError(String error, JSONObject json) {
		if (json != null && json.containsKey("message")) {
			MessageBox.alert(error, getString(json, "message"), null);
		} else {
			MessageBox.alert("Error", error, null);
		}
	}

	protected String getString(JSONObject json, String key) {
		return JSONRequest.getString(json, key);
	}
}

class JSONRequest implements RequestCallback {
	public JSONRequest(JSONCallback cb) {
		this.cb = cb;
	}

	public void onResponseReceived(Request request, Response response) {
		if (cb == null) {
			// We don't care about the response.
			return;
		}

		//JSONObject json = JSONParser.parse(response.getText()).isObject();
		//cb.onSuccess(json);
		try {
			JSONObject json = JSONParser.parse(response.getText()).isObject();
			if (json == null) {
				MessageBox.alert("Error", "Expected JSON response.", null);
			} else if (json.containsKey("error")) {
				cb.onError(getString(json, "error"), json);
			} else {
				cb.onSuccess(json);
			}
		} catch (Exception e) {
			cb.onError("json parse failed", null);
		}
	}

	public void onError(Request request, Throwable exception) {
	}

	private final JSONCallback cb;

	public static void delete(String uri, JSONCallback cb) {
		post(uri, new String[]{"_method"}, new String[]{"delete"}, cb);
	}

	// uri, cb -> request
	public static void get(String uri, JSONCallback cb) {
		RequestBuilder get = new RequestBuilder(RequestBuilder.GET, uri + "?" + new java.util.Date().getTime());
		get.setHeader("Accept", "application/json");

		try {
			get.sendRequest(null, new JSONRequest(cb));
		} catch (RequestException e) {
		}
	}
	
	// uri, map, cb -> uri, keys, values, cb
	public static void get(String uri, HashMap<String, Object> data, final JSONCallback cb){
		Set <String> keys           = data.keySet();
    	ArrayList<String> strKeys   = new ArrayList<String>();
    	ArrayList<String> strValues = new ArrayList<String>();
    	for(Object k : keys) {
    		strKeys.add(k.toString());
    		strValues.add(data.get(k).toString());
    	}
    	get(uri, strKeys.toArray(new String[]{}), strValues.toArray(new String[]{}), cb);
	}
	
	// uri, keys, values, cb -> request
	public static void get(String uri, String[] keys, String[] values, final JSONCallback cb) {
		StringBuilder urlData = new StringBuilder();
		urlData.append(uri);
		urlData.append("?");
		urlData.append(kv2url(keys, values));
		RequestBuilder get = new RequestBuilder(RequestBuilder.GET, urlData.toString());
		get.setHeader("Accept", "application/json");
		try {
			get.sendRequest(null, new JSONRequest(cb));
		} catch (RequestException e) {
		}
	}
	
	// uri, map, cb -> uri, keys, values, cb
	public static void post(String uri, HashMap<String, Object> data, final JSONCallback cb){
		Set <String> keys           = data.keySet();
    	ArrayList<String> strKeys   = new ArrayList<String>();
    	ArrayList<String> strValues = new ArrayList<String>();
    	for(Object k : keys) {
    		strKeys.add(k.toString());
    		strValues.add(data.get(k).toString());
    	}
    	post(uri, strKeys.toArray(new String[]{}), strValues.toArray(new String[]{}), cb);
	}
	
	// uri, keys, values, cb -> request
	public static void post(String uri, String[] keys, String[] values, final JSONCallback cb) {
		RequestBuilder post = new RequestBuilder(RequestBuilder.POST, uri);
		post.setHeader("Accept", "application/json");
		post.setHeader("Content-Type", "application/x-www-form-encoded");
		try {
			post.sendRequest(kv2url(keys, values), new JSONRequest(cb));
		} catch (RequestException e) {
		}
	}

	@SuppressWarnings("unchecked")
    public static void put(String uri, List<Field> fields, final JSONCallback cb) {
		ArrayList<String> keys   = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();

		keys.add("_method");
		values.add("put");

		for (Field<String> f : fields) {
		    if (f.getValue() != null && ! f.getRawValue().equals("")) {
				keys.add(f.getName());
				values.add(f.getRawValue());
			}
		}

		post(uri, keys.toArray(new String[]{}), values.toArray(new String[]{}), cb);
	}

	public static void save(String uri, ModelData model, final JSONCallback cb) {
        ArrayList<String> keys   = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();

        keys.add("_method");
        values.add("put");

        for (String name : model.getPropertyNames()) {
            if (model.get(name) != null) {
                keys.add(name);
                values.add(model.get(name).toString());
            }
        }

        post(uri, keys.toArray(new String[]{}), values.toArray(new String[]{}), cb);
	}

	// keys + values -> url keyword args
	public static String kv2url(String[] keys, String[] values) {
		StringBuilder urlData = new StringBuilder();
		for (int i = 0; keys != null && i < keys.length; ++i) {
			if (i > 0) {
				urlData.append("&");
			}
			urlData.append(URL.encodeComponent(keys[i])).append("=").append(URL.encodeComponent(values[i]));
		}
		return urlData.toString();
	}

	public static String getString(JSONObject json, String key) {
		return json.get(key).isString().stringValue();
	}
}
