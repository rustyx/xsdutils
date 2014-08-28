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
package net.sf.xsdutils.wsdl;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;
import net.sf.xsdutils.xml.SAXComparator;
import net.sf.xsdutils.xml.SAXCompareResult;

/**
 * WsdlMerger tests
 * 
 * @author Rustam Abdullaev
 * 
 */
public class WsdlMergerTest extends TestCase {

	public void testMergeXSDImport() throws Exception {
		int n;
		n = run(WsdlMergerTest.class.getResource("test1/source.wsdl"), "source_merged.wsdl");
		assertXMLEquals("test1/source_merged.expected", "test1/source_merged.wsdl");
		assertEquals(3, n);
	}

	public void testMergeWSDLImport() throws Exception {
		int n;
		n = run(WsdlMergerTest.class.getResource("test1/source2_concrete.wsdl"), "source2_merged.wsdl");
		assertXMLEquals("test1/source_merged.expected", "test1/source2_merged.wsdl");
		assertEquals(4, n);
	}

	/**
	 * test poorly formatted wsdls
	 * 
	 * @throws Exception
	 */
	public void testBadWsdls() throws Exception {
		int n;
		n = run(WsdlMergerTest.class.getResource("test2/source.wsdl"), "source_merged.wsdl");
		assertXMLEquals("test2/source_merged.expected", "test2/source_merged.wsdl");
		assertEquals(0, n);
		n = run(WsdlMergerTest.class.getResource("test2/source2.wsdl"), "source2_merged.wsdl");
		assertXMLEquals("test2/source2_merged.expected", "test2/source2_merged.wsdl");
		assertEquals(0, n);
		n = run(WsdlMergerTest.class.getResource("test2/source3.wsdl"), "source3_merged.wsdl");
		assertXMLEquals("test2/source3_merged.expected", "test2/source3_merged.wsdl");
		assertEquals(0, n);
	}

	private int run(URL wsdl, String targetWsdl) throws Exception {
		File baseDir = new File(wsdl.toURI()).getParentFile();
		WsdlMerger m = new WsdlMerger();
		File targetFile = new File(baseDir, targetWsdl);
		return m.mergeWSDL(wsdl, targetFile);
	}

	private void assertXMLEquals(String in1, String in2) throws Exception {
		SAXComparator c = new SAXComparator();
		SAXCompareResult res = c.compare(getClass().getResource(in1), getClass().getResource(in2));
		assertNull(in1 + " and " + in2 + " differ: " + res, res);
	}

}
