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
package net.sf.xsdutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Base class for all XML/XSD/WSDL tools. Contains helper methods to deal with
 * logging and DOM.
 * 
 * @author Rustam Abdullaev
 */
public abstract class AbstractXSDTool {

	public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	public static final String WSDL_NS = "http://schemas.xmlsoap.org/wsdl/"; //$NON-NLS-1$

	protected DocumentBuilder documentBuilder;
	protected Transformer transformer;
	protected ProgressLogger logger;

	/**
	 * The instance can be used as a singleton but only in a single thread.
	 */
	public AbstractXSDTool() {
		DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
		bf.setNamespaceAware(true);
		bf.setXIncludeAware(true);
		try {
			documentBuilder = bf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException("Unable to initialize XML reader", e);
		}
		TransformerFactory tf = TransformerFactory.newInstance();
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IllegalStateException("Unable to initialize XML writer", e);
		}
		logger = new NullLogger();
	}

	/**
	 * @param logger
	 *            an object of type {@link net.sf.xsdutils.ProgressLogger} to report
	 *            merge progress
	 */
	public void setProgressLogger(ProgressLogger logger) {
		this.logger = logger;
	}

	protected URL resolveFile(URL context, String name) throws MalformedURLException {
		return new URL(context, name);
	}
	
	protected String getAttribute(Node node, String name) {
		Node attr = node.getAttributes().getNamedItem(name);
		if (attr == null) {
			return null;
		}
		return attr.getNodeValue();
	}
	
	protected Node getElement(Node parent, String schema, String name) {
		NodeList children = parent.getChildNodes();
		for (int i=0; i<children.getLength(); i++) {
			Node item = children.item(i);
			if (name.equals(item.getLocalName()) && schema.equals(item.getNamespaceURI())) {
				return item;
			}
		}
		return null;
	}
	
	protected List<Node> getElements(Node parent, String schema, String name) {
		NodeList children = parent.getChildNodes();
		List<Node> lst = new ArrayList<Node>();
		for (int i=0; i<children.getLength(); i++) {
			Node item = children.item(i);
			if (name.equals(item.getLocalName()) && schema.equals(item.getNamespaceURI())) {
				lst.add(item);
			}
		}
		return lst;
	}

	protected Document parseDocument(URL url) throws IOException, SAXException {
		InputStream inputStream = url.openStream();
		try {
			return documentBuilder.parse(inputStream);
		} finally {
			inputStream.close();
		}
	}

	protected void writeDocument(Document doc, File target) throws TransformerException, IOException {
		FileOutputStream outputStream = new FileOutputStream(target);
		try {
			transformer.setOutputProperty("indent", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
			transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
		} finally {
			outputStream.close();
		}
	}

	protected String trim(String nodeValue) {
		return nodeValue != null ? nodeValue.trim() : null;
	}
	
	protected static boolean equals(String s1, String s2) {
		return s1 == null && s2 == null ||
			(s1 != null && s2 != null && s1.equals(s2));
	}

	protected static String trim(String str, boolean trimBegin, boolean trimEnd) {
		if (str == null) {
			return str;
		}
		int i = 0, n = str.length() - 1;
		if (trimBegin) {
			while (i <= n && Character.isWhitespace(str.charAt(i))) {
				i++;
			}
		}
		if (trimEnd) {
			while (n >= 0 && Character.isWhitespace(str.charAt(n))) {
				n--;
			}
		}
		return str.substring(i, n + 1);
	}
}
