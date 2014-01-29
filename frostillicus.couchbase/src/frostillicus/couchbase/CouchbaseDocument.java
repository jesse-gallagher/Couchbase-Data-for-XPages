package frostillicus.couchbase;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.context.FacesContext;

import com.couchbase.client.CouchbaseClient;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.commons.util.io.json.JsonJavaFactory;
import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.commons.util.io.json.JsonParser;
import com.ibm.xsp.model.DataObject;

public class CouchbaseDocument implements Serializable, DataObject {
	private static final long serialVersionUID = -6401841857090580063L;

	private final String connectionName_;
	private final String key_;
	private final JsonJavaObject data_;

	public CouchbaseDocument(final String connectionName, final String key) throws IOException, JsonException {
		connectionName_ = connectionName;
		key_ = key;

		CouchbaseClient client = getClient();
		// Assume it's always a document
		String json = (String) client.get(key_);
		data_ = (JsonJavaObject) JsonParser.fromJson(JsonJavaFactory.instanceEx, json);
	}

	public Class<?> getType(final Object key) {
		return data_.get(String.valueOf(key)).getClass();
	}

	public Object getValue(final Object key) {
		return data_.get(String.valueOf(key));
	}

	public boolean isReadOnly(final Object key) {
		if ("documentId".equals(key)) {
			return true;
		}
		return false;
	}

	public void setValue(final Object key, final Object value) {
		if (key == null) {
			throw new IllegalArgumentException("Key cannot be null.");
		}
		if ("documentId".equals(key)) {
			throw new IllegalArgumentException("Document ID is immutable.");
		}
		data_.put(key.toString(), value);
	}

	public boolean save() throws IOException {
		CouchbaseClient client = getClient();
		client.set(key_, toString());
		return true;
	}

	@Override
	public String toString() {
		return data_.toString();
	}

	private CouchbaseClient getClient() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		XSPCouchbaseConnection conn = (XSPCouchbaseConnection) context.getApplication().getVariableResolver().resolveVariable(context, connectionName_);
		return conn.getClient();
	}
}
