package controller;

import java.io.IOException;

import javax.faces.context.FacesContext;

import com.couchbase.client.CouchbaseClient;

import frostillicus.JSFUtil;
import frostillicus.controller.BasicXPageController;
import frostillicus.couchbase.XSPCouchbaseConnection;

public class Beer extends BasicXPageController {
	private static final long serialVersionUID = -6317715090686326743L;

	@Override
	public void beforePageLoad() throws Exception {
		super.beforePageLoad();

		CouchbaseClient client = getClient();
		JSFUtil.getViewScope().put("className", client.get("512_brewing_company-512_alt"));
	}

	private CouchbaseClient getClient() throws IOException {
		FacesContext context = FacesContext.getCurrentInstance();
		XSPCouchbaseConnection conn = (XSPCouchbaseConnection) context.getApplication().getVariableResolver().resolveVariable(context, "couchbasePelias");
		return conn.getClient();
	}
}
