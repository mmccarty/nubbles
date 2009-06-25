package edu.nrao.dss.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

class RowDefinition {
    public ColumnModel getColumnModel(ColumnConfig column) {
        return columns.getColumnModel(column);
    }

    public List<String> getAllFieldNames() {
        return columns.getAllFieldNames();
    }

    public List<String> getAllFieldIds() {
        return columns.getAllFieldIds();
    }

    public List<RowType> getAllRows() {
        return Arrays.asList(rows);
    }

    public ColumnDefinition getColumnDefinition() {
        return columns;
    }
    
    public boolean isColumnDefault(String name, Map<String, Object> map) {
    	Object value = map.get(name);
    	Object preset = columns.getValue(name, null, map);
    	if (preset == null) {
    		return false;
    	} else {
	    	return preset.toString().equals(value.toString());
    	}
    }
    
    public boolean hasColumnDefault(String name) {
    	return columns.hasColumnDefault(name);
    }
    
    public List<String> getRowNames() {
    	ArrayList<String> retval = new ArrayList<String>();
    	for (RowType r : rows) {
    		retval.add(r.getName());
    	}
    	return retval;
    }
    
    public HashMap<String,List<String>> getRowNamesAndIds() {
    	HashMap<String,List<String>> retval = new HashMap<String,List<String>>();
    	for (RowType r : rows) {
    		retval.put(r.getName(), r.getFieldNames());
    	}
    	return retval;
    }
    
    public RowType findRowType(String name) {
    	for (RowType r : rows) {
    		if (name == r.getName()) {
    			return r;
    		}
    	}
    	return null;
    }
    
    public List<String> findFieldIds(String name) {
    	RowType row = findRowType(name);
    	if (row != null) {
    		return row.getFieldNames();
    	}
    	else {
    		return getAllFieldIds();
    	}
    }

    private final ColumnDefinition columns = new ColumnDefinition();

    private class BaseRowType extends RowType {
        public BaseRowType(String name) {
            super(columns);
            addColumn(ColumnDefinition.NAME,           name);
        }
    }

    private final RowType empty        = new BaseRowType("Empty") {
        {
        }
    };
    
    private final RowType all          = new BaseRowType("All Fields") {
    	{
    		addColumn(ColumnDefinition.CODE,           null);
    		addColumn(ColumnDefinition.SOURCE,         null);
            addColumn(ColumnDefinition.ORIG_ID,        0);
            addColumn(ColumnDefinition.ID,             0);
            addColumn(ColumnDefinition.TYPE,           new STypeField("open"));
            addColumn(ColumnDefinition.SCIENCE,        null); //new ScienceField("spectral line"));
            addColumn(ColumnDefinition.PSC_TIME,       null);
            addColumn(ColumnDefinition.TOTAL_TIME,     null);
            addColumn(ColumnDefinition.SEM_TIME,       null);
            addColumn(ColumnDefinition.GRADE,          null);
            addColumn(ColumnDefinition.AUTHORIZED,     true);
            addColumn(ColumnDefinition.ENABLED,        false);
            addColumn(ColumnDefinition.COMPLETE,       false);
            addColumn(ColumnDefinition.COORD_MODE,     new CoordModeField("J2000"));
            addColumn(ColumnDefinition.SOURCE_H,       null);
            addColumn(ColumnDefinition.SOURCE_V,       null);
            addColumn(ColumnDefinition.FREQ,           null);
            addColumn(ColumnDefinition.RECEIVER,       null);
            addColumn(ColumnDefinition.REQ_MIN,        2.0);
            addColumn(ColumnDefinition.REQ_MAX,        6.0);
            addColumn(ColumnDefinition.BETWEEN,        0.0);
            addColumn(ColumnDefinition.BACKUP,         null);
    	}
    };

    // The defaults found in these table are specific to the initial version of the
    // given row type.  Therefore those sessions having
    // these defaults will forward them to the server and database.
    private final RowType lowFreqNoRFI = new BaseRowType("Low Frequency With No RFI") {
        {
            addColumn(ColumnDefinition.CODE,           null);
            addColumn(ColumnDefinition.ORIG_ID,        0);
            addColumn(ColumnDefinition.ID,             0);
            addColumn(ColumnDefinition.TYPE,           new STypeField("open"));
            addColumn(ColumnDefinition.SCIENCE,        null); //new ScienceField("spectral line"));
            addColumn(ColumnDefinition.PSC_TIME,       null);
            addColumn(ColumnDefinition.TOTAL_TIME,     null);
            addColumn(ColumnDefinition.GRADE,          null);
            addColumn(ColumnDefinition.AUTHORIZED,     true);
            addColumn(ColumnDefinition.ENABLED,        false);
            addColumn(ColumnDefinition.COMPLETE,       false);
            addColumn(ColumnDefinition.COORD_MODE,     new CoordModeField("J2000"));
            addColumn(ColumnDefinition.SOURCE_H,       null);
            addColumn(ColumnDefinition.SOURCE_V,       null);
            addColumn(ColumnDefinition.FREQ,           null);
            addColumn(ColumnDefinition.RECEIVER,       null);
            addColumn(ColumnDefinition.REQ_MIN,        2.0);
            addColumn(ColumnDefinition.REQ_MAX,        6.0);
            addColumn(ColumnDefinition.BETWEEN,        0.0);
        }
    };

    private final RowType lowFreqRFI = new BaseRowType("Low Frequency With RFI") {
        {
            addColumn(ColumnDefinition.CODE,           null);
            addColumn(ColumnDefinition.ORIG_ID,        0);
            addColumn(ColumnDefinition.ID,             0);
            addColumn(ColumnDefinition.TYPE,           new STypeField("open"));
            addColumn(ColumnDefinition.SCIENCE,        null); //new ScienceField("spectral line"));
            addColumn(ColumnDefinition.PSC_TIME,       null);
            addColumn(ColumnDefinition.TOTAL_TIME,     null);
            //addColumn(ColumnDefinition.TRI_TIME,       100);
            addColumn(ColumnDefinition.GRADE,          null);
            addColumn(ColumnDefinition.AUTHORIZED,     true);
            addColumn(ColumnDefinition.ENABLED,        false);
            addColumn(ColumnDefinition.COMPLETE,       false);
            addColumn(ColumnDefinition.COORD_MODE,     new CoordModeField("J2000"));
            addColumn(ColumnDefinition.SOURCE_H,       null);
            addColumn(ColumnDefinition.SOURCE_V,       null);
            addColumn(ColumnDefinition.FREQ,           null);
            addColumn(ColumnDefinition.RECEIVER,       null);
            addColumn(ColumnDefinition.REQ_MIN,        2.0);
            addColumn(ColumnDefinition.REQ_MAX,        6.0);
            addColumn(ColumnDefinition.BETWEEN,        0.0);
        }
    };

    private final RowType hiFreqSpecLine = new BaseRowType("High Frequency Spectral Line") {
        {
            addColumn(ColumnDefinition.CODE,           null);
            addColumn(ColumnDefinition.ORIG_ID,        0);
            addColumn(ColumnDefinition.ID,             0);
            addColumn(ColumnDefinition.TYPE,           new STypeField("open"));
            addColumn(ColumnDefinition.SCIENCE,        null); //new ScienceField("spectral line"));
            addColumn(ColumnDefinition.PSC_TIME,       null);
            addColumn(ColumnDefinition.TOTAL_TIME,     null);
            //addColumn(ColumnDefinition.TRI_TIME,       100);
            addColumn(ColumnDefinition.GRADE,          null);
            addColumn(ColumnDefinition.AUTHORIZED,     true);
            addColumn(ColumnDefinition.ENABLED,        false);
            addColumn(ColumnDefinition.COMPLETE,       false);
            addColumn(ColumnDefinition.COORD_MODE,     new CoordModeField("J2000"));
            addColumn(ColumnDefinition.SOURCE_H,       null);
            addColumn(ColumnDefinition.SOURCE_V,       null);
            addColumn(ColumnDefinition.FREQ,           null);
            addColumn(ColumnDefinition.RECEIVER,       null);
            addColumn(ColumnDefinition.REQ_MIN,        2.0);
            addColumn(ColumnDefinition.REQ_MAX,        6.0);
            addColumn(ColumnDefinition.BETWEEN,        0.0);
        }
    };

    private final RowType hiFreqCont = new BaseRowType("High Frequency Continuum") {
        {
            addColumn(ColumnDefinition.CODE,           null);
            addColumn(ColumnDefinition.ORIG_ID,        0);
            addColumn(ColumnDefinition.ID,             0);
            addColumn(ColumnDefinition.TYPE,           new STypeField("open"));
            addColumn(ColumnDefinition.SCIENCE,        null); //new ScienceField("continuum"));
            addColumn(ColumnDefinition.PSC_TIME,       null);
            addColumn(ColumnDefinition.TOTAL_TIME,     null);
            //addColumn(ColumnDefinition.TRI_TIME,       100);
            addColumn(ColumnDefinition.GRADE,          null);
            addColumn(ColumnDefinition.AUTHORIZED,     true);
            addColumn(ColumnDefinition.ENABLED,        false);
            addColumn(ColumnDefinition.COMPLETE,       false);
            addColumn(ColumnDefinition.COORD_MODE,     new CoordModeField("J2000"));
            addColumn(ColumnDefinition.SOURCE_H,       null);
            addColumn(ColumnDefinition.SOURCE_V,       null);
            addColumn(ColumnDefinition.FREQ,           null);
            //addColumn(ColumnDefinition.FREQ_RNGE_L,    null);
            //addColumn(ColumnDefinition.FREQ_RNGE_H,    null);
            addColumn(ColumnDefinition.RECEIVER,       null);
            addColumn(ColumnDefinition.REQ_MIN,        2.0);
            addColumn(ColumnDefinition.REQ_MAX,        6.0);
            addColumn(ColumnDefinition.BETWEEN,        0.0);
        }
    };

    private final RowType[] rows = new RowType[] {
    		all,
            empty,
/*            lowFreqNoRFI,
            lowFreqRFI,
            hiFreqSpecLine,
            hiFreqCont,
            new BaseRowType("Low frequency monitoring with RFI"),
            new BaseRowType("Low and high frequency"),
            new BaseRowType("Large proposal"),
            new BaseRowType("Polarization project"),
            new BaseRowType("PTCS night time"),
            new BaseRowType("PTCS in high winds"),
            new BaseRowType("Fixed maintenance"),
            new BaseRowType("Windowed maintenance"),
            new BaseRowType("Dynamic VLBI"),
            new BaseRowType("Fixed radar"),
            new BaseRowType("Tsys calibration measurement")
            */
    };
}
