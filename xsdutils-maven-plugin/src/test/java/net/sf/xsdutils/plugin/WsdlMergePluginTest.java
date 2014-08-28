/*
 * Copyright (c) 2009, Rustam Abdullaev. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.xsdutils.plugin;

import java.io.File;

import net.sf.xsdutils.xml.SAXComparator;
import net.sf.xsdutils.xml.SAXCompareResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * WsdlMergePlugin tests
 * 
 * @author Rustam Abdullaev
 *
 */
public class WsdlMergePluginTest extends AbstractMojoTestCase {

	/**
	 * test wsdl auto discovery (omitted "wsdls" tag)
	 * 
	 * @throws Exception
	 */
	public void test1() throws Exception {
		File baseDir = run("test1", "plugin-config.xml");
		assertXMLEquals(baseDir, "source_merged.expected", "source_merged.wsdl");
		assertXMLEquals(baseDir, "source2_merged.expected", "source2_merged.wsdl");
	}
	
	/**
	 * test targetDirectory tag
	 * 
	 * @throws Exception
	 */
	public void test2() throws Exception {
		File baseDir = run("test2", "plugin-config2.xml");
		assertXMLEquals(baseDir, "source_merged.expected", "merged/source.wsdl");
	}

	/**
	 * test targetWsdl tag
	 * 
	 * @throws Exception
	 */
	public void test2b() throws Exception {
		File baseDir = run("test2", "plugin-config2b.xml");
		assertXMLEquals(baseDir, "source_merged.expected", "target.wsdl");
	}

	/**
	 * test poorly formatted wsdls
	 * 
	 * @throws Exception
	 */
	public void test3() throws Exception {
		File baseDir = run("test3", "plugin-config3.xml");
		assertXMLEquals(baseDir, "source3_merged.expected", "source3_merged.wsdl");
		assertXMLEquals(baseDir, "source3b_merged.expected", "source3b_merged.wsdl");
		assertXMLEquals(baseDir, "source3c_merged.expected", "source3c_merged.wsdl");
	}

	private File run(String name, String pom) throws Exception {
		File baseDir = new File(getBasedir(), "/target/test-classes/" + name);
		File testPom = new File(baseDir, pom);
		WsdlMergePlugin mojo = (WsdlMergePlugin) lookupMojo("wsdlmerge", testPom);
		assertNotNull(mojo);
		try {
			mojo.execute();
		} catch (MojoExecutionException e) {
			e.printStackTrace();
			// fail explicitly; MojoExecutionException is ignored if test case is run outside maven
			fail(e.getMessage());
		}
		return baseDir;
	}

	private void assertXMLEquals(File baseDir, String in1, String in2) throws Exception {
		SAXComparator c = new SAXComparator();
		SAXCompareResult res = c.compare(new File(baseDir, in1).toURI().toURL(), new File(baseDir, in2).toURI().toURL());
		assertNull(in1 + " and " + in2 + " differ: " + res, res);
	}
	
}
