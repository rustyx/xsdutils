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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.xsdutils.AbstractXSDTool;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML DOM tree comparator.
 * 
 * @author Rustam Abdullaev
 */
public class DOMComparator extends AbstractXSDTool {

	private final Map<Short, String> nodeTypes;

	public DOMComparator() {
		nodeTypes = new HashMap<Short, String>();
		nodeTypes.put(Node.ELEMENT_NODE, "element");
		nodeTypes.put(Node.ATTRIBUTE_NODE, "attribute");
		nodeTypes.put(Node.CDATA_SECTION_NODE, "CDATA");
		nodeTypes.put(Node.COMMENT_NODE, "comment");
		nodeTypes.put(Node.DOCUMENT_FRAGMENT_NODE, "fragment");
		nodeTypes.put(Node.DOCUMENT_NODE, "document");
		nodeTypes.put(Node.DOCUMENT_TYPE_NODE, "doctype");
		nodeTypes.put(Node.ENTITY_NODE, "entity");
		nodeTypes.put(Node.ENTITY_REFERENCE_NODE, "entityref");
		nodeTypes.put(Node.NOTATION_NODE, "notation");
		nodeTypes.put(Node.PROCESSING_INSTRUCTION_NODE, "processing instruction");
		nodeTypes.put(Node.TEXT_NODE, "text");
	}
	
	public boolean equals(Node in1, Node in2) {
		return compare(in1, in2) == null;
	}
	
	public boolean equals(URL in1, URL in2) throws IOException, SAXException {
		return compare(in1, in2) == null;
	}
	
	public DOMCompareResult compare(URL in1, URL in2) throws IOException, SAXException {
		Document doc1 = parseDocument(in1);
		Document doc2 = parseDocument(in2);
		return compare(doc1, doc2);
	}
	
	public DOMCompareResult compare(Node in1, Node in2) {
		if (in1 == null && in2 == null) {
			return null;
		}
		if (in1 == null) {
			return new DOMCompareResult("unexpected " + nodeTypes.get(in2.getNodeType()) + " '" + in2.getNodeName()
					+ "' with value '" + trim(in2.getNodeValue()) + "'");
		}
		if (in2 == null) {
			return new DOMCompareResult("missing " + nodeTypes.get(in1.getNodeType()) + " '" + in1.getNodeName()
					+ "' with value '" + trim(in1.getNodeValue()) + "'");
		}
		String value1 = trim(in1.getNodeValue());
		String value2 = trim(in2.getNodeValue());
		if (in1.getNodeType() != in2.getNodeType()) {
			return new DOMCompareResult(nodeTypes.get(in1.getNodeType()) + " '" + in1.getNodeName()
					+ "' expected, but found instead " + nodeTypes.get(in2.getNodeType()) + " '" + in2.getNodeName()
					+ "'");
		}
		if (!equals(in1.getNodeName(), in2.getNodeName())) {
			return new DOMCompareResult("expected " + nodeTypes.get(in1.getNodeType()) + " '" + in1.getNodeName()
					+ "' but found instead '" + in2.getNodeName() + "'");
		}
		if (!equals(in1.getNamespaceURI(), in2.getNamespaceURI())) {
			return new DOMCompareResult("expected element '{" + in1.getNamespaceURI() + "}" + in1.getNodeName()
					+ "' but found instead '{" + in2.getNamespaceURI() + "}" + in2.getNodeName() + "'");
		}
		if (!equals(value1, value2)) {
			return new DOMCompareResult("expected the value '" + value1 + "' for "
					+ nodeTypes.get(in1.getNodeType()) + " '" + in1.getNodeName() + "' but found instead '"
					+ value2 + "'");
		}
		if (in1 instanceof Element) {
			Element el1 = (Element) in1;
			Element el2 = (Element) in2;
			NamedNodeMap list1 = el1.getAttributes();
			NamedNodeMap list2 = el2.getAttributes();
			for (int i = 0; i < list1.getLength(); i++) {
				Node attr1 = list1.item(i);
				Node attr2 = list2.getNamedItem(attr1.getNodeName());
				if (attr2 == null) {
					return new DOMCompareResult("element '" + in2.getNodeName() + "' is missing attribute '"
							+ attr1.getNodeName() + "'");
				}
				if (!equals(attr1.getNodeValue(), attr2.getNodeValue())) {
					return new DOMCompareResult("expected the value '" + attr1.getNodeValue() + "' for attribute '"
							+ in1.getNodeName() + "/" + attr1.getNodeName() + "' but found instead '"
							+ attr2.getNodeValue() + "'");
				}
			}
			for (int i = 0; i < list2.getLength(); i++) {
				Node attr2 = list2.item(i);
				Node attr1 = list1.getNamedItem(attr2.getNodeName());
				if (attr1 == null) {
					return new DOMCompareResult("element '" + in2.getNodeName() + "' contains unexpected attribute '"
							+ attr2.getNodeName() + "'");
				}
			}
		}
		NodeList children1 = in1.getChildNodes();
		NodeList children2 = in2.getChildNodes();
		for (int i = 0; i < children1.getLength(); i++) {
			Node node1 = children1.item(i);
			Node node2 = children2.item(i);
			DOMCompareResult rc = compare(node1, node2);
			if (rc != null) {
				return rc;
			}
		}
		if (children2.getLength() > children1.getLength()) {
			return compare(null, children2.item(children1.getLength()));
		}
		switch (in1.getNodeType()) {
		case Node.DOCUMENT_TYPE_NODE:
		case Node.NOTATION_NODE:
		case Node.PROCESSING_INSTRUCTION_NODE:
		case Node.ENTITY_REFERENCE_NODE:
			if (!in1.isEqualNode(in2)) {
				return new DOMCompareResult(nodeTypes.get(in1.getNodeType()) + " '" + in1.getNodeName()
						+ "' do not match");
			}
		}
		return null;
	}
	
	
	
}
