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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.transform.TransformerException;

import net.sf.xsdutils.AbstractXSDTool;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * A tool to merge .wsdl + .xsd(s) into one .wsdl.
 * 
 * @author Rustam Abdullaev
 */
public class WsdlMerger extends AbstractXSDTool {

	/**
	 * The URL of the source WSDL. Required, because linked documents are read
	 * relative to this URL.
	 */
	private URL sourceWsdl;
	/**
	 * The name of the file to which the merged WSDL should be written. Optional.
	 */
	private File targetWsdl;
	/**
	 * The parsed WSDL. If not set, {@link sourceWsdl} will be parsed. The WSDL
	 * is merged in place into this variable.
	 */
	private Document document;

	/**
	 * Merges the given WSDL, saving the merged version in targetWsdl.
	 * 
	 * @param sourceWsdl
	 *            the source WSDL
	 * @param targetWsdl
	 *            the target WSDL
	 * @return int the number of merged files
	 * @throws IOException
	 *             file not found, write access denied, etc.
	 * @throws SAXException
	 *             XML parse error, etc.
	 * @throws TransformerException
	 *             more XML errors
	 * @throws URISyntaxException
	 *             thrown for invalid schemaLocation values
	 */
	public int mergeWSDL(URL sourceWsdl, File targetWsdl) throws SAXException, IOException, TransformerException, URISyntaxException {
		setSourceWsdl(sourceWsdl);
		setTargetWsdl(targetWsdl);
		int rc = merge();
		document = null;
		return rc;
	}

	/**
	 * Performs the actual merging based on sourceWsdl, saves the result in
	 * targetWsdl (if set) and keeps the document in DOM form.
	 * 
	 * @return the number of merged files
	 * @throws SAXException
	 * @throws IOException
	 * @throws TransformerException
	 * @throws URISyntaxException
	 */
	public int merge() throws SAXException, IOException, TransformerException, URISyntaxException {
		parseWSDLIfNeeded();
		// embed <wsdl:import
		Set<URL> handledWsdls = new HashSet<URL>();
		processWSDLImport(sourceWsdl, document.getDocumentElement(), handledWsdls);
		// embed <xsd:import
		Set<URL> handledSchemas = new HashSet<URL>();
		Node typesNode = getElement(document.getDocumentElement(), WSDL_NS, "types"); //$NON-NLS-1$
		if (typesNode != null) {
			List<Node> schemaNodes = getElements(typesNode, XSD_NS, "schema"); //$NON-NLS-1$
			for (Node schema : schemaNodes) {
				processSchemaImport(sourceWsdl, typesNode, schema, handledSchemas);
			}
		}
		// save document
		saveWSDLIfNeeded();
		return handledWsdls.size() + handledSchemas.size();
	}

	private void processWSDLImport(URL context, Node wsdlNode, Set<URL> processedWSDLs) throws SAXException, IOException, URISyntaxException {
		List<Node> importTags = getElements(wsdlNode, WSDL_NS, "import"); //$NON-NLS-1$
		for (Node importTag : importTags) {
			String location = getAttribute(importTag, "location"); //$NON-NLS-1$
			String namespace = getAttribute(importTag, "namespace"); //$NON-NLS-1$
			if (location != null) {
				URL newURL = resolveFile(context, location);
				if (processedWSDLs.add(newURL)) {
					logger.info(Messages.getString("WsdlMerger.importing", newURL)); //$NON-NLS-1$
					Document mainDoc = wsdlNode.getOwnerDocument();
					Document newDoc = parseDocument(newURL);
					Element newWsdl = newDoc.getDocumentElement();
					newWsdl.setAttribute("targetNamespace", namespace); //$NON-NLS-1$
					// recursively check the newly imported schema for nested imports
					processWSDLImport(newURL, newWsdl, processedWSDLs);
					// trim leading whitespace
					Node newNode = newWsdl.getFirstChild();
					if (newNode instanceof Text) {
						newNode.setTextContent(trim(newNode.getTextContent(), true, false));
					}
					// embed the wsdl
					for (; newNode != null; newNode = newNode.getNextSibling()) {
						wsdlNode.insertBefore(mainDoc.importNode(newNode, true), importTag);
					}
				}
				// remove the import tag
				wsdlNode.removeChild(importTag);
			}
		}
	}

	private void processSchemaImport(URL context, Node typesNode, Node schema, Set<URL> handledSchemas) throws SAXException, IOException, URISyntaxException {
		List<Node> importTags = getElements(schema, XSD_NS, "import"); //$NON-NLS-1$
		for (Node imp : importTags) {
			String schemaLocation = getAttribute(imp, "schemaLocation"); //$NON-NLS-1$
			if (schemaLocation != null) {
				imp.getAttributes().removeNamedItem("schemaLocation"); //$NON-NLS-1$
				URL schemaURL = resolveFile(context, schemaLocation);
				if (!handledSchemas.contains(schemaURL)) {
					handledSchemas.add(schemaURL);
					logger.info(Messages.getString("WsdlMerger.importing", schemaURL)); //$NON-NLS-1$
					Document mainDoc = typesNode.getOwnerDocument();
					Document schemaDoc = parseDocument(schemaURL);
					Node loadedSchema = mainDoc.importNode(schemaDoc.getDocumentElement(), true);
					// recursively check the newly imported schema for nested imports
					processSchemaImport(schemaURL, typesNode, loadedSchema, handledSchemas);
					// embed the schema only after nested imports
					typesNode.appendChild(loadedSchema);
				}
			}
		}
		NodeList nodes = schema.getChildNodes();
		boolean hasChildren = false;
		for (int i = 0; i < nodes.getLength(); i++) {
			if (!(nodes.item(i) instanceof Comment || nodes.item(i) instanceof Text || "import".equals(nodes.item(i).getLocalName()))) { //$NON-NLS-1$
				hasChildren = true;
				break;
			}
		}
		if (!hasChildren) {
			typesNode.removeChild(schema);
		}

	}

	private void parseWSDLIfNeeded() throws IOException, SAXException {
		if (document == null) {
			document = parseDocument(sourceWsdl);
		}
	}

	private void saveWSDLIfNeeded() throws TransformerException, IOException {
		if (targetWsdl != null) {
			writeDocument(document, targetWsdl);
		}
	}
	
	public URL getSourceWsdl() {
		return sourceWsdl;
	}

	public void setSourceWsdl(URL sourceWsdl) {
		this.sourceWsdl = sourceWsdl;
	}

	public File getTargetWsdl() {
		return targetWsdl;
	}

	public void setTargetWsdl(File targetWsdl) {
		this.targetWsdl = targetWsdl;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}
	
}
