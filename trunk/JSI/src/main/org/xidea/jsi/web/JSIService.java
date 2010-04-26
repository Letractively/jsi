package org.xidea.jsi.web;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xidea.jsi.JSIExportor;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.JSIRoot;
import org.xidea.jsi.ScriptNotFoundException;
import org.xidea.jsi.impl.DataRoot;
import org.xidea.jsi.impl.DefaultExportorFactory;
import org.xidea.jsi.impl.DefaultLoadContext;
import org.xidea.jsi.impl.ResourceRoot;
import org.xidea.jsi.impl.JSIText;

public class JSIService extends ResourceRoot {
	@SuppressWarnings("unused")
	private static final Log log = LogFactory.getLog(JSIService.class);
	protected String exportService = "http://litecompiler.appspot.com/scripts/export.action";
	protected SDNService sdn = new SDNService(this);

	protected static Collection<String> imgages = Arrays.asList("png", "gif",
			"jpeg", "jpg");

	protected String getContentType(String path, Map<String, String[]> params,
			String defaultContentType) {
		String contentType = defaultContentType;
		int p = path.lastIndexOf('.');
		if (p > 0) {
			String ext = path.substring(p + 1).toLowerCase();
			if (imgages.contains(ext)) {
				return "image/" + ext;
			}
		}
		
		if (path.endsWith(".css")) {
			contentType = "text/css";
		} else if (path.endsWith(".js")) {// for debug
			contentType = "text/plain";
		}else if(path.startsWith("export/")){
			contentType = "text/paint;charset=UTF-8";
		}
		if (contentType != null) {
			contentType = contentType + ";charset=" + this.getEncoding();
		}
		return contentType;
	}

	/**
	 * @param path
	 * @param params
	 * @param context
	 * @param out
	 * @throws IOException
	 */
	public void service(String path, Map<String, String[]> params,
			OutputStream out,Object context) throws IOException {
		if (path == null || path.length() == 0) {
			String[] services = params.get("service");
			// ByteArrayOutputStream out2 = new ByteArrayOutputStream();
			processAction(services == null ? "":services[0], params,
					out, context);
		} else {
			if(!this.writeResource(path, out)){
				processAction(path, params,
						out, context);
			}
		}
		out.flush();
	}

	protected String processAction(String service,
			Map<String, String[]> params, OutputStream out, Object context)
			throws IOException, UnsupportedEncodingException {
		String encoding = this.getEncoding();
		if ("data".equals(service)) {
			String data = params.get("data")[0];
			int dataContentEnd = data.indexOf(',');
			JSIText.writeBase64(data.substring(dataContentEnd + 1), out);
			return data.substring(dataContentEnd);
		} else if ("export".equals(service)) {
			String result = export(params);
			if (result == null) {
				throw new FileNotFoundException();
			}
			out.write(result.getBytes(encoding));
			return "text/plain;charset=" + encoding;
		} else if (service!=null && service.length()>0) {
			sdn.process(service, String.valueOf(context), out);
			return "text/plain;charset=" + encoding;
		} else {
			out.write(document().getBytes(encoding));
			return "text/html;" + encoding;
		}
	}

	protected boolean writeResource(String path, Writer out) throws IOException {
		String purePath = toSourcePath(path);
		return this.output(path, out, path != purePath);
	}

	protected boolean writeResource(String path, OutputStream out)
			throws IOException {
		String purePath = toSourcePath(path);
		return this.output(purePath, out, path != purePath);
	}

	protected String document() {
		StringWriter out = new StringWriter();
		List<String> scriptList = this.findPackageList(false);
		out.append("<html><frameset rows='100%'>"
				+ "<frame src='org/xidea/jsidoc/index.html?" + "group={");
		if (!scriptList.isEmpty()) {
			out.append("\"Source Packages\":");
			out.append(buildJSArray(scriptList));
		}
		List<String> libList = this.findPackageList(true);
		libList.removeAll(scriptList);
		if (!libList.isEmpty()) {
			if (!scriptList.isEmpty()) {
				out.append(",");
			}

			out.append("\"Library Packages\":");
			out.append(buildJSArray(libList));
		}
		out.append("}'> </frameset></html>");
		return out.toString();

	}

	protected String export(Map<String, String[]> param) throws IOException {
		String[] exports = param.get("exports");
		if (exports == null) {
			return exportHome(param);
		} else {
			return exportResult(param,exports);
		}

	}

	private String exportHome(Map<String, String[]> param) throws UnsupportedEncodingException {
		StringWriter out = new StringWriter();
		List<String> allList = this.findPackageList(true);
		out.append("<html><frameset rows='100%'>"
				+ "<frame src='org/xidea/jsidoc/export.html");
		if (!allList.isEmpty()) {
			out.append("#");
			out.append(URLEncoder.encode(buildJSArray(allList),"UTF-8"));
		}
		out.append("'> </frameset></html>");
		return out.toString();
	}

	private String exportResult(Map<String, String[]> param,String[] exports)
			throws IOException, MalformedURLException, ProtocolException,
			UnsupportedEncodingException {
		String[] contents = param.get("content");
		final JSIRoot root;
		if (contents != null) {
			root = new DataRoot(contents[0]);
		} else {
			root = this;
		}
		JSIExportor exportor = DefaultExportorFactory.getInstance()
				.createExplorter(param);
		if (exportor == null) {
			if (!param.containsKey(exportService)) {
				HttpURLConnection url = (HttpURLConnection) new URL(
						exportService).openConnection();
				url.setRequestMethod("POST");
				url.setDoOutput(true);
				url.setRequestProperty("Content-Length", "");
				StringBuilder buf = new StringBuilder();
				buf.append(URLEncoder.encode(exportService, "UTF-8") + "=1");
				for (String key : param.keySet()) {
					String[] values = param.get(key);
					for (String value : values) {
						buf.append('&');
						buf.append(URLEncoder.encode(key, "UTF-8"));
						buf.append('=');
						buf.append(URLEncoder.encode(value, "UTF-8"));
					}
				}
				url.getOutputStream().write(buf.toString().getBytes("UTF-8"));
				return org.xidea.jsi.impl.JSIText.loadText(
						url.getInputStream(), "UTF-8");
			} else {
				return null;
			}
		}
		JSILoadContext context = new DefaultLoadContext();
		if (exports != null) {
			// 只有Data Root 才能支持这种方式
			for (String item : exports) {
				// PHP 不支持同名参数
				for (String subitem : item.split("[^\\w\\$\\:\\.\\-\\*]+")) {
					root.$import(subitem, context);
				}
			}
		}
		return exportor.export(context);
	}

	private String buildJSArray(List<String> packageList) {
		StringWriter out = new StringWriter();
		out.append("[\"");
		boolean isFirst = true;
		for (String packageName : packageList) {
			if (isFirst) {
				isFirst = false;
			} else {
				out.append("\",\"");
			}
			out.append(packageName);
		}
		out.append("\"]");
		return out.toString();
	}

	private String toSourcePath(String path) {
		if (path.endsWith(JSIText.PRELOAD_FILE_POSTFIX)) {
			return path.substring(0, path.length()
					- JSIText.PRELOAD_FILE_POSTFIX.length())
					+ ".js";
		}
		return path;
	}
}