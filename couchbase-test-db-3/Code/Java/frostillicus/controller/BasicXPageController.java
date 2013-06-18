package frostillicus.controller;

import javax.faces.event.PhaseEvent;

import frostillicus.JSFUtil;

public class BasicXPageController implements XPageController {
	private static final long serialVersionUID = 1L;

	public BasicXPageController() {
	}

	public void beforePageLoad() throws Exception {
	}

	public void afterPageLoad() throws Exception {
	}

	public void beforeRenderResponse(final PhaseEvent event) throws Exception {
	}

	public void afterRenderResponse(final PhaseEvent event) throws Exception {
	}

	public void afterRestoreView(final PhaseEvent event) throws Exception {
	}

	protected static Object resolveVariable(final String varName) {
		return JSFUtil.getVariableValue(varName);
	}
}
