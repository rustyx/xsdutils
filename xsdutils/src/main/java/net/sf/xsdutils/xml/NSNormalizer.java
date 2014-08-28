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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import net.sf.xsdutils.AbstractXSDTool;

/**
 * XML namespace normalizer.
 * 
 * @author Rustam Abdullaev
 */
public class NSNormalizer extends AbstractXSDTool {

	private static final String COMMON_PREFIXES_PROPERTIES = "common-prefixes.properties";
	private final Properties commonNSToPrefixMap;
	private final Properties commonPrefixToNSMap;
	
	public NSNormalizer() {
		commonNSToPrefixMap = new Properties();
		commonPrefixToNSMap = new Properties();
		try {
			InputStream in = getClass().getResourceAsStream(COMMON_PREFIXES_PROPERTIES);
			if (in == null) {
				throw new IOException("File not found: " + COMMON_PREFIXES_PROPERTIES);
			}
			try {
				commonPrefixToNSMap.load(in);
			} finally {
				in.close();
			}
			for (Map.Entry<?, ?> entry : commonPrefixToNSMap.entrySet()) {
				String ns = (String)entry.getValue();
				String prefix = (String)entry.getKey();
				commonNSToPrefixMap.setProperty(ns, prefix);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("unable to read " + COMMON_PREFIXES_PROPERTIES, e);
		}
	}
	
	public void normalizeNS(URL sourceXML, File targetXML) {
		/*
		 * use common prefixes for common ns
		 *   - possible conflict: prefix already in use
		 * use same prefix for same ns
		 *   - possible conflict: prefix already in use locally
		 * remove duplicate ns decl
		 *   - possible conflict: prefix is redefined locally or above
		 * avoid redefining prefixes except for default ns
		 * pull up freq. used ns to top level
		 *   - freq used means: used more than X times, locality is low
		 * 
		 */
		//TODO finish this
		
	}
	
}
