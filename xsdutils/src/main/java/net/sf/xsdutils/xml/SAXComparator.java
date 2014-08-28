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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.xml.parsers.ParserConfigurationException;

import net.sf.xsdutils.AbstractXSDTool;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * XML file comparator
 * 
 * @author Rustam Abdullaev
 */
public class SAXComparator extends AbstractXSDTool {

	public boolean equals(final URL in1, final URL in2) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
		return compare(in1, in2) == null;
	}
	
	static enum XMLSectionType {
		ELEMENT_START,
		ELEMENT_END,
		TEXT,
		COMMENT,
		EOF,
		ERROR,
	}
	
	static class XMLSection {
		XMLSectionType type;
		String name;
		AttributeMap attributes;
		int line;
		int column;
		Exception exception;
		public XMLSection(XMLSectionType type, int line, int column) {
			this.type = type;
			this.line = line;
			this.column = column;
		}
		public XMLSection(XMLSectionType type, int line, int column, String name) {
			this(type, line, column);
			this.name = name;
		}
		public XMLSection(XMLSectionType type, int line, int column, String name, AttributeMap attributes) {
			this(type, line, column, name);
			this.attributes = attributes;
		}
		public void setException(Exception exception) {
			this.exception = exception;
		}
		public void trim(boolean trimBegin, boolean trimEnd) {
			name = SAXComparator.trim(name, trimBegin, trimEnd);
		}
		@Override
		public String toString() {
			switch (type) {
			case ELEMENT_START:
				return "<" + name + ">"; //$NON-NLS-1$ //$NON-NLS-2$
			case ELEMENT_END:
				return "</" + name + ">";  //$NON-NLS-1$//$NON-NLS-2$
			case TEXT:
				return "'" + name + "'"; //$NON-NLS-1$ //$NON-NLS-2$
			case COMMENT:
				return Messages.getString("SAXComparator.typeComment", name); //$NON-NLS-1$
			case EOF:
				return Messages.getString("SAXComparator.typeEOF"); //$NON-NLS-1$
			case ERROR:
				return exception.toString();
			default:
				return super.toString();
			}
		}
		public String getPosition() {
			return Messages.getString("SAXComparator.line", line); //$NON-NLS-1$
		}
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof XMLSection) {
				XMLSection o2 = (XMLSection)obj;
				return this.type == o2.type && SAXComparator.equals(this.name, o2.name);
			}
			return false;
		}
	}

	static class AbortException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public AbortException() {
			super("interrupted"); //$NON-NLS-1$
		}
		public AbortException(Exception cause) {
			super("interrupted", cause); //$NON-NLS-1$
		}
	}
	
	static class AttributeMap extends HashMap<String, String> {
		private static final long serialVersionUID = 1L;
		public AttributeMap(Attributes attributes) {
			for (int i = 0, n = attributes.getLength(); i < n; i++) {
				put(attributes.getQName(i), attributes.getValue(i));
			}
		}
	}
	
	static class XMLHandler extends DefaultHandler2 {
		private Locator locator;
		private final BlockingQueue<XMLSection> q;
		private XMLSection last;
		public XMLHandler(BlockingQueue<XMLSection> q) {
			this.q = q;
		}
		int getLine() {
			return locator != null ? locator.getLineNumber() : 0;
		}
		int getColumn() {
			return locator != null ? locator.getColumnNumber() : 0;
		}
		private void flush(boolean trimEnd) {
			if (last != null) {
				if (trimEnd) {
					last.trim(false, true);
				}
				try {
	                if (!"".equals(last.name) || last.type != XMLSectionType.TEXT) {
	                    q.put(last);
	                }
					last = null;
				} catch (InterruptedException e) {
					throw new AbortException(e);
				}
			}
		}
		@Override
		public void endDocument() throws SAXException {
			flush(true);
			last = new XMLSection(XMLSectionType.EOF, getLine(), getColumn());
			flush(false);
		}
		@Override
		public void startPrefixMapping(String prefix, String uri)
				throws SAXException {
		}
		@Override
		public void endPrefixMapping(String prefix) throws SAXException {
		}
		@Override
		public void startElement(String uri, String localName,
				String qName, Attributes attributes) throws SAXException {
			flush(true);
			last = new XMLSection(XMLSectionType.ELEMENT_START, getLine(), getColumn(), qName, new AttributeMap(attributes));
		}
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			flush(true);
			last = new XMLSection(XMLSectionType.ELEMENT_END, getLine(), getColumn(), qName);
		}
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			boolean afterElement = (last != null && last.type != XMLSectionType.TEXT);
			flush(false);
			last = new XMLSection(XMLSectionType.TEXT, getLine(), getColumn(), new String(ch, start, length));
			if (afterElement) {
				last.trim(true, false);
			}
		}
		@Override
		public void comment(char[] ch, int start, int length) throws SAXException {
			flush(true);
			last = new XMLSection(XMLSectionType.COMMENT, getLine(), getColumn(), new String(ch, start, length).trim());
		}
		@Override
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
			flush(true);
			last = new XMLSection(XMLSectionType.ELEMENT_START, getLine(), getColumn(), "!DOCTYPE(" + publicId + "," //$NON-NLS-1$ //$NON-NLS-2$
					+ systemId + ")"); //$NON-NLS-1$
		}
		@Override
		public void error(SAXParseException e) throws SAXException {
			throw e;
		}
		@Override
		public void fatalError(SAXParseException e) throws SAXException {
			throw e;
		}
		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}
	}

	class ParseThread extends Thread {
		private BlockingQueue<XMLSection> queue;
		private InputSource source;
		private XMLHandler handler;

		public ParseThread(BlockingQueue<XMLSection> q, InputSource in) {
			this.queue = q;
			this.source = in;
			setName("ParseThread(" + in.getSystemId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
			setDaemon(true);
			handler = new XMLHandler(q);
		}
		public void run() {
			try {
				XMLReader xr = XMLReaderFactory.createXMLReader();
				xr.setContentHandler(handler);
				xr.setDTDHandler(handler);
				xr.setEntityResolver(handler);
				xr.setErrorHandler(handler);
				xr.setProperty("http://xml.org/sax/properties/lexical-handler", handler); //$NON-NLS-1$
				xr.setFeature("http://xml.org/sax/features/namespaces", false); //$NON-NLS-1$
				xr.setFeature("http://xml.org/sax/features/validation", false); //$NON-NLS-1$
				xr.parse(source);
			} catch (AbortException e) {
				// do nothing
			} catch (Exception e) {
				for (int i = 0; i < 10; i++) {
					try {
						XMLSection err = new XMLSection(XMLSectionType.ERROR, handler.getLine(), handler.getColumn());
						err.setException(e);
						queue.put(err);
						break;
					} catch (InterruptedException e1) {
						e.printStackTrace();
						// make space and retry
						queue.clear();
					}
				}
			}
		}
	};

	private void checkForErrors(XMLSection s1) throws SAXException, ParserConfigurationException, IOException {
		if (s1.type == XMLSectionType.ERROR) {
			if (s1.exception instanceof SAXException)
				throw (SAXException) s1.exception;
			else if (s1.exception instanceof ParserConfigurationException)
				throw (ParserConfigurationException) s1.exception;
			else if (s1.exception instanceof IOException)
				throw (IOException) s1.exception;
			else
				throw new IllegalStateException(s1.exception);
		}
	}

	public SAXCompareResult compare(final URL in1, final URL in2) throws ParserConfigurationException, SAXException,
			IOException, InterruptedException {
		return compare(new InputSource(in1.toString()), new InputSource(in2.toString()));
	}

	public SAXCompareResult compare(final InputSource in1, final InputSource in2) throws ParserConfigurationException,
			SAXException, IOException, InterruptedException {
		SAXCompareResult res = null;
		final BlockingQueue<XMLSection> q1 = new ArrayBlockingQueue<XMLSection>(10);
		final BlockingQueue<XMLSection> q2 = new ArrayBlockingQueue<XMLSection>(10);
		ParseThread w1 = new ParseThread(q1, in1);
		ParseThread w2 = new ParseThread(q2, in2);
		w1.start();
		w2.start();

		while (true) {
			XMLSection s1 = q1.take();
			checkForErrors(s1);
			XMLSection s2 = q2.take();
			checkForErrors(s2);
			if (!s1.equals(s2)) {
				res = new SAXCompareResult(Messages.getString("SAXComparator.wrongNode", s1, s2), s2.line, s2.column); //$NON-NLS-1$ //$NON-NLS-2$
				break;
			}
			if (s1.type == XMLSectionType.EOF) {
				break;
			}
			if (s1.type == XMLSectionType.ELEMENT_START && s1.attributes != null) {
				AttributeMap list1 = s1.attributes;
				AttributeMap list2 = s2.attributes;
				for (Map.Entry<String, String> entry : list1.entrySet()) {
					String name1 = entry.getKey();
					String value1 = entry.getValue();
					String value2 = list2.get(name1);
					if (value2 == null) {
						res = new SAXCompareResult(Messages.getString("SAXComparator.missingAttr", s2.name, name1), s2.line, s2.column); //$NON-NLS-1$
						break;
					}
					if (!equals(value1, value2)) {
						res = new SAXCompareResult(Messages.getString("SAXComparator.wrongAttrValue", value1, s1.name, name1, value2), s2.line, s2.column); //$NON-NLS-1$
						break;
					}
				}
				for (Map.Entry<String, String> entry : list2.entrySet()) {
					String name2 = entry.getKey();
					String value1 = list1.get(name2);
					if (value1 == null) {
						res = new SAXCompareResult(Messages.getString("SAXComparator.unexpectedAttr", s1.name, name2), s2.line, s2.column); //$NON-NLS-1$
						break;
					}
				}
			}
		}
		w1.interrupt();
		w2.interrupt();
		w1.join(1000);
		w2.join(1000);
		return res;
	}
	
}
