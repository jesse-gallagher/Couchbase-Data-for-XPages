package frostillicus.couchbase;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.faces.context.FacesContext;

import com.ibm.xsp.FacesExceptionEx;
import com.ibm.xsp.model.AbstractDataContainer;
import com.ibm.xsp.model.AbstractDataSource;
import com.ibm.xsp.model.DataContainer;

public class CouchbaseViewData extends AbstractDataSource {

	private String connectionName_;
	private String designDoc_;
	private String viewName_;

	public CouchbaseViewData() {

	}

	public String getConnectionName() {
		return connectionName_;
	}

	public void setConnectionName(final String connectionName) {
		this.connectionName_ = connectionName;
	}

	public String getDesignDoc() {
		return designDoc_;
	}

	public void setDesignDoc(final String designDoc) {
		this.designDoc_ = designDoc;
	}

	public String getViewName() {
		return viewName_;
	}

	public void setViewName(final String viewName) {
		this.viewName_ = viewName;
	}

	@Override
	public CouchbaseView getDataObject() {
		return ((CouchbaseViewDataContainer) getDataContainer()).getData();
	}

	@Override
	public Object saveState(final FacesContext facesContext) {
		Object[] state = new Object[4];
		state[0] = super.saveState(facesContext);
		state[1] = connectionName_;
		state[2] = designDoc_;
		state[3] = viewName_;
		return state;
	}

	@Override
	public void restoreState(final FacesContext facesContext, final Object state) {
		Object[] values = (Object[]) state;
		super.restoreState(facesContext, values[0]);
		connectionName_ = (String) values[1];
		designDoc_ = (String) values[2];
		viewName_ = (String) values[3];
	}

	@Override
	protected String composeUniqueId() {
		return getClass().getName();
	}

	@Override
	public boolean isReadonly() {
		return true;
	}

	@Override
	public DataContainer load(final FacesContext facesContext) throws IOException {
		CouchbaseView data = new CouchbaseView(connectionName_, designDoc_, viewName_);
		return new CouchbaseViewDataContainer(getBeanId(), getUniqueId(), data);
	}

	@Override
	public void readRequestParams(final FacesContext facesContext, final Map<String, Object> requestMap) {
		// Never.
	}

	@Override
	public boolean save(final FacesContext facesContext, final DataContainer container) throws FacesExceptionEx {
		// Not applicable
		return false;
	}

	protected static class CouchbaseViewDataContainer extends AbstractDataContainer {

		private transient CouchbaseView data_;

		public CouchbaseViewDataContainer() {

		}

		public CouchbaseViewDataContainer(final String beanId, final String id, final CouchbaseView data) {
			super(beanId, id);
			data_ = data;
		}

		public CouchbaseView getData() {
			return data_;
		}

		public void deserialize(final ObjectInput in) throws IOException {
			try {
				data_ = (CouchbaseView) in.readObject();
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		public void serialize(final ObjectOutput out) throws IOException {
			out.writeObject(data_);
		}
	}
}
