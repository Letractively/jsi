package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_DEPENDENCE_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_INTERNAL_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.createObjectPackageMap;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.JSILoadContext;
import org.xidea.jsi.impl.v2.ClasspathRoot;

public class DefaultJSILoadContextTest {

	private ClasspathRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathRoot("utf-8");
	}

	@Test
	public void testLoadScript() {
		JSILoadContext context = root.$export("example/hello-world");
		assertEquals(ALL_EXAMPLE_MAP,context.getExportMap());

		context = root.$export("example/internal");
		assertEquals(ALL_EXAMPLE_INTERNAL_MAP,context.getExportMap());
		
		context = root.$export("example/dependence/show-detail");
		assertEquals(ALL_EXAMPLE_DEPENDENCE_MAP,context.getExportMap());
	}

	@Test
	public void testGetExportMap() {
		JSILoadContext context = root.$export("example:sayHello");
		assertEquals(createObjectPackageMap("example","sayHello"),context.getExportMap());

		context = root.$export("example/internal");
		assertEquals(ALL_EXAMPLE_INTERNAL_MAP,context.getExportMap());
		
		context = root.$export("example/dependence");
		assertEquals(ALL_EXAMPLE_DEPENDENCE_MAP,context.getExportMap());
	}

	@Test
	public void testGetScriptList() {
		JSILoadContext context = root.$export("example");
		assertEquals(1,context.getScriptList().size());
		
		context = root.$export("example/internal");
		assertEquals(2,context.getScriptList().size());
		
		context = root.$export("example/dependence");
		assertEquals(2,context.getScriptList().size());
	}

}
