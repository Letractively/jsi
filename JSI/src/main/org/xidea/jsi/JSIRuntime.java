package org.xidea.jsi;

import java.net.URL;

public interface JSIRuntime {
	
	public abstract Object eval(URL resource);
	
	public abstract Object eval(String source);

	public abstract Object eval(String code, String path);

	public abstract Object eval(Object thiz,String code, String path,
			Object scope);
	
	public abstract Object eval(String source, String path, Object scope);
	
	public abstract Object invoke(Object thisObj, Object function,
			Object... args);

	public <T> T wrapToJava(final Object thiz, Class<T> clasz);

}