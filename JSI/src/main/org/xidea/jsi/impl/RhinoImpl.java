package org.xidea.jsi.impl;

import java.util.ArrayList;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.UniqueTag;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.mozilla.javascript.WrapFactory;

/**
 * @see Java6Impl
 * @author test
 * 
 */

class RhinoImpl extends RuntimeSupport {
	private static final WrapFactory wrap = new WrapFactory() {
		@SuppressWarnings("unchecked")
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope,
				final Object javaObject, Class staticType) {
			if (javaObject == null || javaObject == Undefined.instance
					|| javaObject == UniqueTag.NOT_FOUND
					|| javaObject == UniqueTag.NULL_VALUE) {
				return null;
			}
			if (staticType != null) {
				if (NodeList.class == staticType) {// apache xml error
					return super.wrapAsJavaObject(cx, scope, new NodeList() {
						public Node item(int index) {
							return ((NodeList) javaObject).item(index);
						}

						public int getLength() {
							return ((NodeList) javaObject).getLength();
						}
					}, staticType);
				}
			}
			return super.wrapAsJavaObject(cx, scope, javaObject, staticType);
		}
	};

	public static RuntimeSupport create(boolean newEngine) {
		return newEngine ? new NewRhinoImpl() : new RhinoImpl();
	}

	static Context getContext() {
		Context context = Context.getCurrentContext();
		context.setWrapFactory(wrap);
		wrap.setJavaPrimitiveWrap(false);
		return context;
	}

	protected Object jsToJava(Class<? extends Object> type, Object result) {
		return Context.jsToJava(result, type);
	}

	public Object invoke(Object thisObj, Object function, Object... args) {
		Context cx = getContext();
		Scriptable thiz = Context.toObject(thisObj, (Scriptable) globals);
		if (!(function instanceof Function)) {
			function = ScriptableObject.getProperty(thiz, function.toString());
		}
		if (function instanceof Function) {
			return ((Function) function).call(cx, (Scriptable) globals, thiz,
					args);
		} else {
			return null;
		}
	}

	public Object eval(Object thiz, String code, String path,
			Map<String, Object> varMap) {
		Context cx = getContext();
		Scriptable localScope = (Scriptable) globals;
		if (varMap != null) {
			Scriptable globals = localScope;
			localScope = cx.newObject(globals);
			for (String key : varMap.keySet()) {
				localScope.put(key, localScope, varMap.get(key));
			}
		}
		if (thiz instanceof Scriptable) {
			Object[] args = EMPTY_ARG;
			StringBuilder buf = new StringBuilder("function(");
			if (varMap != null && !varMap.isEmpty()) {
				ArrayList<Object> list = new ArrayList<Object>();
				for (Map.Entry<String, Object> e : varMap.entrySet()) {
					if (list.size() > 0) {
						buf.append(",");
					}
					buf.append(e.getKey());
					list.add(e.getValue());
				}
				args = list.toArray();
			}
			buf.append("){");
			buf.append(code);
			buf.append("\n}");
			Function fn = cx.compileFunction(localScope, buf.toString(), path,
					1, null);
			return fn.call(cx, localScope, (Scriptable) thiz, args);
		} else {
			return cx.evaluateString(localScope, code, path, 1, null);
		}

	}

}

class NewRhinoImpl extends RhinoImpl {
	{

		try {
			Context context = Context.enter();
			globals = ScriptRuntime.getGlobal(context);
		} finally {
			Context.exit();
		}
	}

	public Object invoke(Object thisObj, Object function, Object... args) {

		try {
			Context.enter();
			return super.invoke(thisObj, function, args);
		} finally {
			Context.exit();
		}
	}

	public Object eval(Object thiz, String code, String path,
			Map<String, Object> varMap) {
		try {
			Context.enter();
			return super.eval(thiz, code, path, varMap);
		} finally {
			Context.exit();
		}
	}
}