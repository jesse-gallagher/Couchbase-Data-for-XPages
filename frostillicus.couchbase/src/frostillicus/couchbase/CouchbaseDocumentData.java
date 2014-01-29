package frostillicus.couchbase;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.commons.util.io.json.JsonException;
import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;

public class CouchbaseDocumentData extends AbstractDataSource {

	private String connectionName_;
	private String documentId_;

	public CouchbaseDocumentData() {

	}

	public String getConnectionName() {
		return connectionName_;
	}

	public void setConnectionName(final String connectionName) {
		this.connectionName_ = connectionName;
	}

	public String getDocumentId() {
		return documentId_;
	}

	public void setDocumentId(final String documentId) {
		this.documentId_ = documentId;
	}

	@Override
	protected String composeUniqueId() {
		return getClass().getName();
	}

	@Override
	public Object getDataObject() {
		return ((CouchbaseDocumentDataContainer) getDataContainer()).getDocument();
	}

	@Override
	public boolean isReadonly() {
		// Just do all writable for now
		return false;
	}

	@Override
	public DataContainer load(final FacesContext facesContext) throws IOException {
		try {
			CouchbaseDocument data = new CouchbaseDocument(connectionName_, documentId_);
			return new CouchbaseDocumentDataContainer(getBeanId(), getUniqueId(), data);
		} catch (JsonException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public void readRequestParams(final FacesContext facesContext, final Map<String, Object> requestMap) {
		// Never.
	}

	@Override
	public boolean save(final FacesContext facesContext, final DataContainer container) throws FacesExceptionEx {
		try {
			CouchbaseDocument doc = ((CouchbaseDocumentDataContainer) container).getDocument();
			return doc.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object saveState(final FacesContext facesContext) {
		Object[] state = new Object[3];
		state[0] = super.saveState(facesContext);
		state[1] = connectionName_;
		state[2] = documentId_;
		return state;
	}

	@Override
	public void restoreState(final FacesContext facesContext, final Object state) {
		Object[] values = (Object[]) state;
		super.restoreState(facesContext, values[0]);
		connectionName_ = (String) values[1];
		documentId_ = (String) values[2];
	}

	public static class CouchbaseDocumentDataContainer extends AbstractDataContainer {

		private transient CouchbaseDocument document_;

		public CouchbaseDocumentDataContainer() {

		}

		public CouchbaseDocumentDataContainer(final String beanId, final String id, final CouchbaseDocument document) {
			super(beanId, id);
			document_ = document;
		}

		public CouchbaseDocument getDocument() {
			return document_;
		}

		public void deserialize(final ObjectInput in) throws IOException {
			try {
				document_ = (CouchbaseDocument) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		public void serialize(final ObjectOutput out) throws IOException {
			out.writeObject(document_);
		}

	}
}
