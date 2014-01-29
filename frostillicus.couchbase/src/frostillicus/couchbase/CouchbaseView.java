package frostillicus.couchbase;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.xsp.model.TabularDataModel;
import com.ibm.xsp.model.TabularDataSource;
import com.ibm.xsp.model.ViewRowData;

public class CouchbaseView extends TabularDataModel implements Serializable, TabularDataSource {
	private static final long serialVersionUID = -5477753905452511583L;

	private static final int PAGE_SIZE = 30;

	private final String connectionName_;
	private final String designDoc_;
	private final String viewName_;
	private transient View view_;
	private Map<Integer, CouchbaseViewEntry> cache_ = new HashMap<Integer, CouchbaseViewEntry>();
	private Set<Integer> cached_ = new HashSet<Integer>();
	private int rowCount_;

	public CouchbaseView(final String connectionName, final String designDoc, final String viewName) throws IOException {
		connectionName_ = connectionName;
		designDoc_ = designDoc;
		viewName_ = viewName;

		CouchbaseClient client = getClient();
		View view = getView();
		Query query = new Query();
		query.setIncludeDocs(false);
		ViewResponse result = client.query(view, query);
		rowCount_ = result.size();
	}

	@Override
	public int getRowCount() {
		return rowCount_;
	}

	@Override
	public Object getRowData() {
		try {
			if (!cached_.contains(getRowIndex())) {
				int min = getRowIndex() / PAGE_SIZE * PAGE_SIZE;
				CouchbaseClient client = getClient();
				View view = getView();
				Query query = new Query();
				query.setIncludeDocs(true);
				query.setSkip(min);
				query.setLimit(PAGE_SIZE);

				ViewResponse result = client.query(view, query);

				int i = min;
				for (ViewRow row : result) {
					cache_.put(i, new CouchbaseViewEntry(row));
					cached_.add(i);
					i++;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return cache_.get(this.getRowIndex());
	}

	@Override
	public boolean isColumnSortable(final String columnName) {
		return false;
	}

	@Override
	public int getResortType(final String columnName) {
		return TabularDataModel.RESORT_NONE;
	}

	private CouchbaseClient getClient() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		XSPCouchbaseConnection conn = (XSPCouchbaseConnection) context.getApplication().getVariableResolver().resolveVariable(context, connectionName_);
		return conn.getClient();
	}

	private View getView() throws IOException {
		if (view_ == null) {
			CouchbaseClient client = getClient();
			view_ = client.getView(designDoc_, viewName_);
		}
		return view_;
	}

	public static class CouchbaseViewEntry implements ViewRowData, Serializable {
		private static final long serialVersionUID = -5754157302649701110L;

		private final Map<String, Object> data_;
		private final String bbox_;
		private final String geometry_;
		private final String id_;
		private final String key_;
		private final Object value_;

		@SuppressWarnings("unchecked")
		protected CouchbaseViewEntry(final ViewRow row) throws JsonException {
			String bbox = null;
			String geometry = null;
			try {
				bbox = row.getBbox();
				geometry = row.getGeometry();
			} catch (UnsupportedOperationException uoe) {
			}
			bbox_ = bbox;
			geometry_ = geometry;
			id_ = row.getId();
			key_ = row.getKey();
			value_ = row.getValue();

			Object docObject = row.getDocument();
			if (docObject != null) {
				String json = (String) docObject;
				JsonJavaObject doc = (JsonJavaObject) JsonParser.fromJson(JsonJavaFactory.instanceEx, json);
				data_ = Collections.unmodifiableMap(new HashMap<String, Object>((Map<String, Object>) doc));
			} else {
				data_ = null;
			}
		}

		public ColumnInfo getColumnInfo(final String columnName) {
			return new CouchbaseColumnInfo(columnName);
		}

		public Object getColumnValue(final String columnName) {
			return getValue(columnName);
		}

		public String getOpenPageURL(final String pageName, final boolean readOnly) {
			try {
				return pageName + "?documentId=" + java.net.URLEncoder.encode(id_, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// This won't happen
				throw new RuntimeException(e);
			}
		}

		public Object getValue(final String columnName) {
			if ("bbox".equals(columnName)) {
				return bbox_;
			} else if ("geometry".equals(columnName)) {
				return geometry_;
			} else if ("id".equals(columnName)) {
				return id_;
			} else if ("key".equals(columnName)) {
				return key_;
			} else if ("value".equals(columnName)) {
				return value_;
			}
			if (data_ != null) {
				return data_.get(columnName);
			}
			return null;
		}

		public boolean isReadOnly(final String columnName) {
			return true;
		}

		public void setColumnValue(final String columnName, final Object value) {

		}

		class CouchbaseColumnInfo implements ViewRowData.ColumnInfo {
			private final String _columnName;

			public CouchbaseColumnInfo(final String columnName) {
				this._columnName = columnName;
			}

			public Object getValue() {
				return CouchbaseViewEntry.this.getColumnValue(this._columnName);
			}
		}
	}
}
