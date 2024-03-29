package org.xidea.jsi.impl.v2;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class PackageParser {
	private static final Log log = LogFactory.getLog(PackageParser.class);
	public static final String SET_IMPLEMENTATION = "setImplementation";
	public static final String ADD_SCRIPT = "addScript";
	public static final String ADD_DEPENDENCE = "addDependence";
	final static String BIND_SCRIPT;
	static {
		InputStream in1 = PackageParser.class
				.getResourceAsStream("package-parser.js");
		String script = null;
		try {
			InputStreamReader reader = new InputStreamReader(in1, "utf-8");
			StringWriter out = new StringWriter();
			char[] cbuf = new char[1024];
			int count;
			while ((count = reader.read(cbuf)) >= 0) {
				out.write(cbuf, 0, count);
			}
			out.flush();
			script = out.toString();
		} catch (IOException e) {
			script = "print('Error:" + e.getMessage().replaceAll("['\r\n]", "")
					+ "')";
			throw new RuntimeException(e);
		}
		BIND_SCRIPT = script;
	}
	protected List<List<Object>> addScriptCall = new ArrayList<List<Object>>();
	protected List<List<Object>> addDependenceCall = new ArrayList<List<Object>>();
	public String implementation;

	public PackageParser() {
	}

	public Collection<String> findGlobals(String scriptName, String pattern) {
		throw new PackageSyntaxException("不支持包定义格式");
	}

	@SuppressWarnings("unchecked")
	public void addScript(String scriptName, Object objectNames,
			Object beforeLoadDependences, Object afterLoadDependences) {
		objectNames = filterStrings(objectNames);
		if (objectNames != null) {
			if (objectNames instanceof String) {
				String pattern = (String) objectNames;
				if (pattern.indexOf('*') >= 0) {
					Pattern regexp = Pattern.compile("^"+pattern.replaceAll("[\\*]", ".*")+'$');
					Collection<String> objectNames2 = findGlobals(scriptName, pattern);
					ArrayList<String> objectNames3 = new ArrayList<String>();
					for(String on : objectNames2){
						if(regexp.matcher(on).find()){
							objectNames3.add(on);
						}
					}
					objectNames = objectNames3;
				}
			} else {
				Collection<String> objectNames2 = null;
				for (String pattern : (Collection<String>) objectNames) {
					if (pattern.indexOf('*') >= 0) {
						objectNames2 = (Collection<String>) objectNames;
						break;
					}
				}
				if (objectNames2 != null) {
					for (String pattern : objectNames2) {
						this.addScript(scriptName, pattern,
								beforeLoadDependences, afterLoadDependences);
					}
					return;
				}
			}
		}
		try {
			beforeLoadDependences = filterStrings(beforeLoadDependences);
			afterLoadDependences = filterStrings(afterLoadDependences);
		} catch (RuntimeException e) {
			// log.error(beforeLoadDependences);
			// log.error(afterLoadDependences);
			log.warn(e);
			;
			throw e;
		}
		addScriptCall.add(Arrays.asList(scriptName, objectNames,
				beforeLoadDependences, afterLoadDependences));

	}

	public void addDependence(String thisPath, Object targetPath,
			boolean afterLoad) {
		targetPath = filterStrings(targetPath);
		addDependenceCall.add(Arrays.asList(thisPath, targetPath, afterLoad));
	}

	private Object filterStrings(Object object) {// check type...
		try {
			if (object instanceof Collection) {
				for (Object o : (Collection<?>) object) {
					o = (String) o;
				}
				return object;
			}
			if (Boolean.FALSE.equals(object)) {
				return null;
			} else if (object instanceof Number) {
				if (((Number) object).floatValue() == 0) {// js double number
					return null;
				}
			}
			return (String) object;
		} catch (Exception ex) {
			throw new PackageSyntaxException("非法参数：" + object);
		}
	}

	public void setImplementation(String implementation) {
		if (this.implementation == null) {
			this.implementation = implementation;
		} else {
			throw new RuntimeException("不能多次设置实现包");
		}
	}

	public void setup(JSIPackage pkg) {
		if (this.implementation != null) {
			pkg.setImplementation(implementation);
		} else {
			for (Iterator<List<Object>> it = addScriptCall.iterator(); it
					.hasNext();) {
				List<Object> item = it.next();
				pkg.addScript((String) item.get(0), item.get(1), item.get(2),
						item.get(3));
			}
			for (Iterator<List<Object>> it = addDependenceCall.iterator(); it
					.hasNext();) {
				List<Object> item = it.next();
				pkg.addDependence((String) item.get(0), item.get(1),
						(Boolean) item.get(2));
			}
		}
	}
}