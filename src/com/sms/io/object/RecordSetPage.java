package com.sms.io.object;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Result of pageable request, one page of data.
 * @see <a href="http://www.osflash.org/amf/recordset">osflash.org documentation</a>
 */
public class RecordSetPage {
    /**
     * Recordset cursor
     */
	private int cursor;
    /**
     * Data as List
     */
	private List<List<Object>> data;

    /**
     * Creates recordset page from Input object
     * @param input       Input object to use as source for data that has to be deserialized
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public RecordSetPage(Input input) {
		Map mapResult = Deserializer.deserialize(input, Map.class);

		cursor = (Integer) mapResult.get("Cursor");
		data = (List<List<Object>>) mapResult.get("Page");
	}

	/**
     * Getter for recordset cursor
     *
     * @return  Recordset cursor
     */
    protected int getCursor() {
		return cursor;
	}

	/**
     * Getter for page data
     *
     * @return Page data as unmodifiable list
     */
    protected List<List<Object>> getData() {
		return Collections.unmodifiableList(data);
	}

}
