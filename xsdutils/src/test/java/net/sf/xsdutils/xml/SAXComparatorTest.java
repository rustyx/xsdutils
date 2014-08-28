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
package net.sf.xsdutils.xml;

import junit.framework.TestCase;

public class SAXComparatorTest extends TestCase {

	private SAXComparator comparator;

	protected void setUp() throws Exception {
		comparator = new SAXComparator();
	}
	
	public void testCompareIdentical() throws Exception {
		SAXCompareResult res = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc1.xml"));
		assertTrue(String.valueOf(res), res == null);
	}

	public void testCompareSimilar() throws Exception {
		SAXCompareResult res = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc2.xml"));
		assertTrue(String.valueOf(res), res == null);
	}

	public void testCompareWrongAttrValue() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc3.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("attr1"));
	}

	public void testCompareExtraElement() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc5.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("el3"));
	}

	public void testCompareBadValue() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc6.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("true"));
	}

	public void testCompareMissingValue() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc7.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("true"));
	}

	public void testCompareMissingAttr() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc8.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("attr2"));
	}

	public void testCompareExtraAttr() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("doc9.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("attrC"));
	}

	public void testCompareNSAttr() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("doc1.xml"),
				getClass().getResource("docA.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("ns1"));
	}

	public void testCompareNSElement() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docB.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("ns1:build"));
	}

	public void testCompareNS() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docC.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("qwer"));
	}

	public void testCompareWrongEntity() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docD.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains(">"));
	}

	public void testCompareXmlDecl() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docE.xml"));
		assertTrue(rc == null);
	}

	public void testCompareXmlDeclDiff() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docF.xml"));
		assertTrue(rc == null);
	}

	public void testCompareXmlDeclDiffEnc() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docF.xml"),
				getClass().getResource("docG.xml"));
		assertTrue(rc == null);
	}

	public void testCompareComment() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docA.xml"),
				getClass().getResource("docH.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("asdfg"));
	}

	public void testCompareDoctype() throws Exception {
		SAXCompareResult rc = comparator.compare(
				getClass().getResource("docK.xml"),
				getClass().getResource("docL.xml"));
		assertTrue(rc != null);
		assertTrue(rc.getMessage(), rc.getMessage().contains("DOCTYPE"));
	}

}
