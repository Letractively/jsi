package org.xidea.jsi.impl.test;

import static org.junit.Assert.assertEquals;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_DEPENDENCE_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_INTERNAL_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.ALL_EXAMPLE_MAP;
import static org.xidea.jsi.impl.test.AbstractJSIRootTest.createObjectPackageMap;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.xidea.jsi.impl.v2.ClasspathRoot;
import org.xidea.jsi.impl.v2.DefaultPackage;
import org.xidea.jsi.impl.v2.JSIPackage;
import org.xidea.jsi.impl.v2.Java6ScriptPackagePaser;
import org.xidea.jsi.impl.v2.PackageParser;

public class PackageParserTest {

	private ClasspathRoot root;

	@Before
	public void setUp() throws Exception {
		root = new ClasspathRoot();
	}

	@Test
	public void testScriptParser() {
		for (boolean isJDK6 : new Boolean[] { true, false }) {
			test("example", ALL_EXAMPLE_MAP, isJDK6);
			test("example.internal", ALL_EXAMPLE_INTERNAL_MAP, isJDK6);
			test("example.dependence", ALL_EXAMPLE_DEPENDENCE_MAP, isJDK6);
		}
	}

	private void test(String name, Map<String, String> varMap, boolean isJava6) {
		JSIPackage pkg = root.requirePackage(name);
		PackageParser parser;
		if (isJava6) {
			parser = new Java6ScriptPackagePaser(pkg);
		} else {
			parser = new Java6ScriptPackagePaser(pkg);
		}
		DefaultPackage pkg2 = new DefaultPackage(root, "pkg.test");
		parser.setup(pkg2);
		assertEquals(varMap.keySet(), pkg2.getObjectScriptMap()
				.keySet());
	}

	@Test
	public void testParseX() {
		String pkgName = "pkg.test";
		DefaultPackage pkg = new DefaultPackage(null, pkgName){
			@Override
			public String loadText(String fileName){
				return "this.addScript('a.js',['Class1','Class2'],'xxx','Base');"
				+ "this.addScript('b.js','Classb1','xxx','Base');"
				+ "this.addDependence('a.js','b.js');";
				// +
				// "if(/is/.test(this.navigator))this.setImplementation(\"_v1_2\")";
			}
		};
		Java6ScriptPackagePaser paser = new Java6ScriptPackagePaser(pkg);
		paser.setup(pkg);
		assertEquals(createObjectPackageMap(pkgName, "Class1","Class2","Classb1").keySet(), pkg.getObjectScriptMap().keySet());
	}
}
