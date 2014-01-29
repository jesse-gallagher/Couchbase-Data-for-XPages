package frostillicus.couchbase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import lotus.domino.Database;

import com.couchbase.client.CouchbaseClient;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.application.BasicStateManagerImpl;
import com.ibm.xsp.application.StateManagerImpl;
import com.ibm.xsp.application.UniqueViewIdManager;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.util.Delegation;

public class CouchbaseStateManager extends StateManagerImpl {
	private final StateManager delegate_;
	private final static boolean debug_ = true;
	private final static boolean gzip_ = false;

	public CouchbaseStateManager(StateManager delegate) {
		super(delegate);
		delegate_ = delegate;
		print("created using " + (delegate_ != null ? delegate_.getClass().getName() : "null"));
	}

	public CouchbaseStateManager() throws FacesException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		StateManager priorStateManager = (StateManager) Delegation.getImplementation("state-manager");
		this.delegate_ = priorStateManager;

		print("created empty using " + (delegate_ != null ? delegate_.getClass().getName() : "null"));
	}

	@Override
	public SerializedView saveSerializedView(FacesContext facesContext) {
		print("--------------------------");
		print("running saveSerializedView");

		try {

			UIViewRootEx view = (UIViewRootEx) facesContext.getViewRoot();
			String key = view.getUniqueViewId();

			Database database = (Database) getVariableValue("database");
			key = database.getReplicaID() + key;
			print("storage key is " + key);

			// Connect to our Couchbase server
			CouchbaseClient client = getClient();

			// Fetch or create a holder object and store the view
			BasicStateManagerImpl.ViewHolder holder = (BasicStateManagerImpl.ViewHolder) client.get(key);
			if (holder == null) {
				Class<BasicStateManagerImpl.ViewHolder> holderClass = BasicStateManagerImpl.ViewHolder.class;
				Constructor<?> cons = holderClass.getDeclaredConstructors()[1];
				cons.setAccessible(true);
				holder = (BasicStateManagerImpl.ViewHolder) cons.newInstance(1);
			}
			holder.add(facesContext, view);

			// Do our Externalization here to avoid trouble with the
			// CouchbaseClient ClassLoader not finding the holder class later
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos;
			if (gzip_) {
				oos = new ObjectOutputStream(new GZIPOutputStream(bos));
			} else {
				oos = new ObjectOutputStream(bos);
			}
			holder.writeExternal(oos);
			oos.flush();
			bos.flush();

			byte[] bytes = bos.toByteArray();

			// Set an expiration value
			XSPContext context = ExtLibUtil.getXspContext();
			String timeout = context.getProperty("xsp.session.timeout");
			int expiration;
			if (StringUtil.isEmpty(timeout)) {
				expiration = 30 * 60;
			} else {
				expiration = Integer.parseInt(timeout) * 60;
			}

			client.set(key, expiration, bytes);
			print("added view to holder");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		print("--------------------------");

		return null;
	}

	@Override
	public UIViewRoot restoreView(FacesContext facesContext, String viewId, String renderKitId) {
		long startTime = System.nanoTime();

		print("--------------------------");
		print("reading view " + viewId + ", " + renderKitId);

		try {
			String key = UniqueViewIdManager.getRequestUniqueViewId(facesContext);
			Database database = (Database) getVariableValue("database");
			key = database.getReplicaID() + key;
			print("loading key is " + key);

			CouchbaseClient client = getClient();
			byte[] bytes = (byte[]) client.get(key);
			if (bytes != null) {
				print("bytes length in is " + bytes.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
				ObjectInputStream ois;
				if (gzip_) {
					ois = new ObjectInputStream(new GZIPInputStream(bis));
				} else {
					ois = new ObjectInputStream(bis);
				}
				BasicStateManagerImpl.ViewHolder holder = new BasicStateManagerImpl.ViewHolder();
				holder.readExternal(ois);
				print("fetched from couchbase");
				long endTime = System.nanoTime();
				print("fetch took " + ((endTime - startTime) * 1.0 / 1000 / 1000) + "ms");
				print("--------------------------");
				return holder.get(facesContext, viewId);
			} else {
				print("view not found in DB");
				print("--------------------------");
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void print(Object message) {
		if (debug_)
			System.out.println("StateManager>> " + message);
	}

	private CouchbaseClient getClient() throws Exception {
		XSPCouchbaseConnection conn = (XSPCouchbaseConnection) getVariableValue("couchbaseStateHolder");
		return conn.getClient();
	}

	private static Object getVariableValue(String name) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, name);
	}
}