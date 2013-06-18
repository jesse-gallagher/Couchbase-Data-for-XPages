package frostillicus;

import java.util.*;
import java.io.Serializable;
import lotus.domino.*;
import com.ibm.xsp.model.*;

@SuppressWarnings("unchecked")
public class SortableMapTable extends TabularDataModel implements Serializable, TabularDataSource {
	private static final long serialVersionUID = 3475977562822554265L;
	private List<Map<String, Comparable>> data = new ArrayList<Map<String, Comparable>>();
	private List<Map<String, Comparable>> originalData;

	private String sortColumn = "";
	private String sortOrder = "";

	public SortableMapTable(final List<Map<String, Comparable>> data) throws NotesException {
		this.data = data;

		this.originalData = new ArrayList<Map<String, Comparable>>(this.data);
	}

	@Override
	public int getRowCount() {
		return this.data.size();
	}

	@Override
	public Object getRowData() {
		return this.data.get(this.getRowIndex());
	}

	@Override
	public boolean isColumnSortable(final String paramString) {
		return true;
	}

	@Override
	public int getResortType(final String paramString) {
		return TabularDataModel.RESORT_BOTH;
	}

	@Override
	public void setResortOrder(final String columnName, final String sortOrder) {
		if (!columnName.equals(this.sortColumn)) {
			// Switching columns means switch back to ascending by default
			this.sortOrder = sortOrder.equals("descending") ? "descending" : "ascending";
			Collections.sort(this.data, new MapComparator(columnName, true));
			this.sortColumn = columnName;
		} else {
			this.sortColumn = columnName;
			if (sortOrder.equals("ascending") || (sortOrder.equals("toggle") && this.sortOrder.length() == 0)) {
				this.sortOrder = "ascending";
				Collections.sort(this.data, new MapComparator(columnName, true));
			} else if (sortOrder.equals("descending") || (sortOrder.equals("toggle") && this.sortOrder.equals("ascending"))) {
				this.sortOrder = "descending";
				Collections.sort(this.data, new MapComparator(columnName, false));
			} else {
				this.sortOrder = "";
				this.data = new ArrayList<Map<String, Comparable>>(this.originalData);
			}
		}
	}

	@Override
	public int getResortState(final String paramString) {
		return this.sortOrder.equals("ascending") ? TabularDataModel.RESORT_ASCENDING : this.sortOrder.equals("descending") ? TabularDataModel.RESORT_DESCENDING : TabularDataModel.RESORT_NONE;
	}

	@Override
	public String getResortColumn() {
		return this.sortColumn;
	}

	// View Panels know how to deal with ViewRowData better than Maps,
	// apparently, so just pass through
	// the ViewRowData methods to their Map equivalents
	public static class FakeEntryData extends HashMap<String, Comparable> implements ViewRowData {
		private static final long serialVersionUID = 5946100397649532083L;

		public Object getColumnValue(final String arg0) {
			return this.get(arg0);
		}

		public Object getValue(final String arg0) {
			return this.get(arg0);
		}

		public ColumnInfo getColumnInfo(final String arg0) {
			return null;
		}

		public String getOpenPageURL(final String arg0, final boolean arg1) {
			return null;
		}

		public boolean isReadOnly(final String arg0) {
			return false;
		}

		public void setColumnValue(final String arg0, final Object arg1) {
			if (!(arg1 instanceof Comparable)) {
				this.put(arg0, arg1.toString());
			} else {
				this.put(arg0, (Comparable) arg1);
			}
		}
	}

	// A basic class to compare two Maps by a given comparable key common in
	// each,
	// allowing for descending order
	private class MapComparator implements Comparator<Map<String, Comparable>> {
		private String key;
		private boolean ascending;

		public MapComparator(final String key, final boolean ascending) {
			this.key = key;
			this.ascending = ascending;
		}

		public int compare(final Map<String, Comparable> o1, final Map<String, Comparable> o2) {
			if (o1 == null || o1.get(key) == null) {
				return -1;
			} else if (o2 == null || o2.get(key) == null) {
				return 1;
			}
			return (ascending ? 1 : -1) * o1.get(key).compareTo(o2.get(key));
		}
	}
}