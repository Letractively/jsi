package org.xidea.jsi.impl;

import java.io.IOException;

public abstract class RhinoSupport {

	private static final String EVAL = loadText("org/xidea/jsi/impl/initialize.js");

	Object topScope;
	/**
	 * 1.初始化 freeEval，加上调试姓习
	 * 2.$JSI.scriptBase 设置为 classpath:///
	 * 3.返回 loadTextByURL
	 * @param arguments
	 * @return
	 */
	public static Object initialize(Object arguments,Object topScope){
		RhinoSupport s = get(topScope);
		Object initializer = s.eval(EVAL,"<setup>");
		return s.call(initializer, null, arguments);
	}
	/**
	 * 按类路径装载文本（utf-8）
	 * 如：org/xidea/jsi/impl/initialize.js
	 * @param path 类路径
	 * @return
	 */
	public static String loadText(String path) {
		try {
			return ClasspathRoot.loadText(path,ClasspathRoot.class.getClassLoader(),"utf-8");
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * @internal
	 * @param thiz
	 * @param path
	 * @return
	 */
	public static Object createEvaler(Object thiz,String path){
		RhinoSupport s = get(thiz);
		Object result =  s.eval("(function(){eval(arguments[0])})",path);
		return result;
	}
	private static RhinoSupport get(Object topScope){
		String cn = topScope.getClass().getName();
		RhinoSupport sp;
		if(cn.startsWith("com.sun.") || cn.startsWith("sun.")){
			sp = new Java6Impl();
		}else{
			sp = new RhinoImpl();
		}
		sp.topScope = topScope;
		return sp;
	}
	public abstract Object eval(String code,String path);
	protected abstract Object call(Object function,Object thisObj,Object... args);
}
class Java6Impl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object call(Object function,Object thisObj,Object... args){
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return ((sun.org.mozilla.javascript.internal.Function)function).call(cx, 
				(sun.org.mozilla.javascript.internal.Scriptable)topScope, 
				(sun.org.mozilla.javascript.internal.Scriptable)thisObj, args);
	}
	public Object eval(String code,String path){
		sun.org.mozilla.javascript.internal.Context cx = sun.org.mozilla.javascript.internal.Context.getCurrentContext();
		return cx.evaluateString(
				(sun.org.mozilla.javascript.internal.Scriptable)topScope, code, path, 1, null);

	}
}
class RhinoImpl extends RhinoSupport{
	//function(code){return evaler(this,code);}
	public Object call(Object function,Object thisObj,Object... args){
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return ((org.mozilla.javascript.Function)function).call(cx, 
				(org.mozilla.javascript.Scriptable)topScope, 
				(org.mozilla.javascript.Scriptable)thisObj, args);
	}
	public Object eval(String code,String path){
		org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.getCurrentContext();
		return cx.evaluateString(
				(org.mozilla.javascript.Scriptable)topScope, code, path, 1, null);
	}
}