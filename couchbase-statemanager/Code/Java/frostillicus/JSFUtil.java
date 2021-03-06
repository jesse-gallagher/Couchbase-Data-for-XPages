package frostillicus;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import sun.misc.BASE64Decoder;

import com.ibm.xsp.component.UIViewRootEx2;

//import com.ibm.xsp.extlib.util.JdbcUtil;
//import java.sql.Connection;
//import java.sql.SQLException;

import lotus.domino.*;
import lotus.domino.local.NotesBase;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.ibm.xsp.extlib.beans.PeopleBean;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public class JSFUtil {

	private JSFUtil() {
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getViewScope() {
		return (Map<String, Object>) getVariableValue("viewScope");
	}

	public static Object getBindingValue(final String ref) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		return application.createValueBinding(ref).getValue(context);
	}

	public static void setBindingValue(final String ref, final Object newObject) {
		FacesContext context = FacesContext.getCurrentInstance();
		Application application = context.getApplication();
		ValueBinding binding = application.createValueBinding(ref);
		binding.setValue(context, newObject);
	}

	public static Object getVariableValue(final String varName) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getVariableResolver().resolveVariable(context, varName);
	}

	public static String getUserName() {
		return (String) getBindingValue("#{context.user.name}");
	}

	public static Session getSession() {
		return (Session) getVariableValue("session");
	}

	public static Session getSessionAsSigner() {
		return (Session) getVariableValue("sessionAsSigner");
	}

	public static Database getDatabase() {
		return (Database) getVariableValue("database");
	}

	public static UIViewRootEx2 getViewRoot() {
		return (UIViewRootEx2) getVariableValue("view");
	}

	public static PeopleBean getPeopleBean() {
		return (PeopleBean) getVariableValue("peopleBean");
	}

	public static String pluralize(final String input) {
		if (input.endsWith("s")) {
			return input + "es";
		} else if (input.endsWith("y")) {
			return input.substring(0, input.length() - 2) + "ies";
		}
		return input + "s";
	}

	public static String singularize(final String input) {
		if (input.endsWith("ses")) {
			return input.substring(0, input.length() - 2);
		} else if (input.endsWith("ies")) {
			return input.substring(0, input.length() - 3) + "y";
		} else if (input.endsWith("s")) {
			return input.substring(0, input.length() - 1);
		}
		return input;
	}

	public String fetchURL(final String urlString) throws Exception {
		URL url = new URL(urlString);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestProperty("User-Agent", "Firefox/2.0");

		BufferedReader in = new BufferedReader(new InputStreamReader((InputStream) conn.getContent()));
		StringWriter resultWriter = new StringWriter();
		String inputLine;
		while ((inputLine = in.readLine()) != null) {
			resultWriter.write(inputLine);
		}
		in.close();

		return resultWriter.toString().replace("<HTTP-EQUIV", "<meta http-equiv");

	}

	public static byte[] getFileResourceData(final Document fileResource) throws NotesException, IOException {
		String dxl = fileResource.generateXML();
		int fileDataPos = dxl.indexOf("<filedata>");
		int fileDataEnd = dxl.indexOf("</filedata>");
		String fileDataB64 = dxl.substring(fileDataPos + 10, fileDataEnd - 1);
		return new BASE64Decoder().decodeBuffer(fileDataB64);
	}

	@SuppressWarnings("unchecked")
	public static String xor(final String input, final Vector key) {
		StringBuilder output = new StringBuilder();

		for (int i = 0; i < input.length(); i++) {
			int character = input.codePointAt(i);
			int keyNode = ((Double) key.get(i % key.size())).intValue();

			int onePass = character ^ keyNode;

			output.append((char) onePass);
		}

		return output.toString();
	}

	public static String xmlEncode(final String text) {
		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			char currentChar = text.charAt(i);
			if (!((currentChar >= 'a' && currentChar <= 'z') || (currentChar >= 'A' && currentChar <= 'Z') || (currentChar >= '0' && currentChar <= '9'))) {
				result.append("&#" + (int) currentChar + ";");
			} else {
				result.append(currentChar);
			}
		}

		return result.toString();
	}

	public static String strLeft(final String input, final String delimiter) {
		return input.substring(0, input.indexOf(delimiter));
	}

	public static String strRight(final String input, final String delimiter) {
		return input.substring(input.indexOf(delimiter) + delimiter.length());
	}

	public static String strLeftBack(final String input, final String delimiter) {
		return input.substring(0, input.lastIndexOf(delimiter));
	}

	public static String strLeftBack(final String input, final int chars) {
		return input.substring(0, input.length() - chars);
	}

	public static String strRightBack(final String input, final String delimiter) {
		return input.substring(input.lastIndexOf(delimiter) + delimiter.length());
	}

	public static String strRightBack(final String input, final int chars) {
		return input.substring(input.length() - chars);
	}

	@SuppressWarnings("unchecked")
	public static List<String> toStringList(final Object columnValue) {
		List<String> result = new Vector<String>();
		if (columnValue.getClass().getName().equals("java.util.Vector")) {
			for (Object reader : (Vector) columnValue) {
				result.add((String) reader);
			}
		} else if (((String) columnValue).length() > 0) {
			result.add((String) columnValue);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<Integer> toIntegerList(final Object columnValue) {
		List<Integer> result = new Vector<Integer>();
		if (columnValue.getClass().getName().equals("java.util.Vector")) {
			for (Object element : (Vector) columnValue) {
				result.add(((Double) element).intValue());
			}
		} else {
			result.add(((Double) columnValue).intValue());
		}
		return result;
	}

	public static int toInteger(final Object columnValue) {
		int result = 0;
		if (columnValue.getClass().getName().equals("java.lang.String")) {
			result = 0;
		} else {
			result = ((Double) columnValue).intValue();
		}
		return result;
	}

	public static Date toDate(final Object columnValue) throws NotesException {
		return ((DateTime) columnValue).toJavaDate();
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> getFlashScope() {
		return (Map<Object, Object>) getVariableValue("flashScope");
	}

	@SuppressWarnings("unchecked")
	public static void addMessage(final String type, final String message) {
		Map<Object, Object> flashScope = getFlashScope();
		List<Object> messages = (List<Object>) flashScope.get(type + "Messages");
		if (messages == null) {
			messages = new ArrayList<Object>();
			flashScope.put(type + "Messages", messages);
		}
		messages.add(message);
	}

	// Via
	// http://stackoverflow.com/questions/12740889/what-is-the-least-expensive-way-to-test-if-a-view-has-been-recycled
	public static boolean isRecycled(final Base object) {
		if (!(object instanceof NotesBase)) {
			// No reason to test non-NotesBase objects -> isRecycled = true
			return true;
		}

		try {
			NotesBase notesObject = (NotesBase) object;
			Method isDead = notesObject.getClass().getSuperclass().getDeclaredMethod("isDead");
			isDead.setAccessible(true);

			return (Boolean) isDead.invoke(notesObject);
		} catch (Throwable exception) {
		}

		return true;
	}

	public static Serializable restoreState(final Document doc, final String itemName) throws Exception {
		Session session = ExtLibUtil.getCurrentSession();
		boolean convertMime = session.isConvertMime();
		session.setConvertMime(false);

		Serializable result = null;
		Stream mimeStream = session.createStream();
		MIMEEntity entity = doc.getMIMEEntity(itemName);
		entity.getContentAsBytes(mimeStream);

		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		mimeStream.getContents(streamOut);
		mimeStream.recycle();

		byte[] stateBytes = streamOut.toByteArray();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(stateBytes);
		ObjectInputStream objectStream;
		if (entity.getHeaders().toLowerCase().contains("content-encoding: gzip")) {
			GZIPInputStream zipStream = new GZIPInputStream(byteStream);
			objectStream = new ObjectInputStream(zipStream);
		} else {
			objectStream = new ObjectInputStream(byteStream);
		}
		Serializable restored = (Serializable) objectStream.readObject();
		result = restored;

		entity.recycle();

		session.setConvertMime(convertMime);

		return result;
	}

	public static void saveState(final Serializable object, final Document doc, final String itemName) throws NotesException {
		try {
			Session session = ExtLibUtil.getCurrentSession();
			boolean convertMime = session.isConvertMime();
			session.setConvertMime(false);

			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			ObjectOutputStream objectStream = new ObjectOutputStream(new GZIPOutputStream(byteStream));
			objectStream.writeObject(object);
			objectStream.flush();
			objectStream.close();

			Stream mimeStream = session.createStream();
			MIMEEntity previousState = doc.getMIMEEntity(itemName);
			MIMEEntity entity = previousState == null ? doc.createMIMEEntity(itemName) : previousState;
			ByteArrayInputStream byteIn = new ByteArrayInputStream(byteStream.toByteArray());
			mimeStream.setContents(byteIn);
			entity.setContentFromBytes(mimeStream, "application/x-java-serialized-object", MIMEEntity.ENC_NONE);
			MIMEHeader header = entity.getNthHeader("Content-Encoding");
			if (header == null) {
				header = entity.createHeader("Content-Encoding");
			}
			header.setHeaderVal("gzip");

			header.recycle();
			entity.recycle();
			mimeStream.recycle();

			session.setConvertMime(convertMime);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}