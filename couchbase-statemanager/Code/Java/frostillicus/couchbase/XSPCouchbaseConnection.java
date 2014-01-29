package frostillicus.couchbase;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;
import java.net.URISyntaxException;

import com.couchbase.client.CouchbaseClient;
import com.ibm.commons.util.io.json.JsonException;
import com.ibm.xsp.model.DataObject;

import frostillicus.JSFUtil;

public class XSPCouchbaseConnection implements Serializable, DataObject {
	private static final long serialVersionUID = 2385840247028257918L;

	private List<URI> uris_;
	private String bucket_ = "";
	private String userName_ = "";
	private String password_ = "";
	private boolean docStore_ = true;

	public String getUserName() {
		return userName_;
	}

	public void setUserName(final String userName) {
		userName_ = userName;
	}

	public String getPassword() {
		return password_;
	}

	public void setPassword(final String password) {
		password_ = password;
	}

	public boolean isDocStore() {
		return docStore_;
	}

	public void setDocStore(final boolean docStore) {
		docStore_ = docStore;
	}

	private transient CouchbaseClient client_;

	public List<URI> getUris() {
		return Collections.unmodifiableList(uris_);
	}

	public void setUris(final List<URI> uris) {
		uris_ = new ArrayList<URI>(uris);
	}

	public void setUriStrings(final List<String> uriStrings) throws URISyntaxException {
		uris_ = new ArrayList<URI>(uriStrings.size());
		for (String uriString : uriStrings) {
			uris_.add(new URI(uriString));
		}
	}

	public String getBucket() {
		return bucket_;
	}

	public void setBucket(final String bucket) {
		this.bucket_ = bucket;
	}

	public CouchbaseClient getClient() throws IOException {
		if (client_ == null) {
			Logger logger = Logger.getLogger("com.couchbase.client");
		    logger.setLevel(Level.SEVERE);
		    for(Handler h : logger.getParent().getHandlers()) {
		       if(h instanceof ConsoleHandler) {
		         h.setLevel(Level.SEVERE);
		       }
		    }
			client_ = new CouchbaseClient(uris_, bucket_, userName_, password_);
		}
		return client_;
	}

	public Class<?> getType(final Object key) {
		return CouchbaseDocument.class;
	}

	@SuppressWarnings("unchecked")
	public Object getValue(final Object key) {
		try {
			CouchbaseClient client = getClient();
			Object value = client.get(key.toString());
			if (docStore_) {
				// Find this connection in the application scope to make our
				// CouchbaseDocument object
				Map<String, Object> applicationScope = (Map<String, Object>) JSFUtil.getVariableValue("applicationScope");
				for (Map.Entry<String, Object> entry : applicationScope.entrySet()) {
					if (entry.getValue() == this) {
						try {
							return new CouchbaseDocument(entry.getKey(), (String) key);
						} catch (JsonException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				// If it's not a document, just return the value
				return value;
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	public boolean isReadOnly(final Object key) {
		return false;
	}

	public void setValue(final Object key, final Object value) {
		try {
			CouchbaseClient client = getClient();

			if (value instanceof CouchbaseDocument) {
				client.set(key.toString(), ((CouchbaseDocument) value).toString());
			} else {
				client.set(key.toString(), value);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
