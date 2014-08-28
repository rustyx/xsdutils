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
import java.io.IOException;
import java.net.URL;

import net.sf.xsdutils.wsdl.WsdlMerger;

/**
 * XSD Utils command-line interface.
 * 
 * @author Rustam Abdullaev
 */
public class Run {

	/**
	 * CLI interface
	 * 
	 * @param args
	 *            sourceWsdl, targetWsdl
	 */
	public static void main(String[] args) {
		try {
			int argptr = 0;
			boolean quiet = false;

			if (args.length > 0 && args[0].equals(Messages.getString("WsdlMerger.quietAttr"))) { //$NON-NLS-1$
				quiet = true;
				argptr++;
			}
			if (args.length != argptr + 2) {
				printUsage();
				return;
			}
			int idx = args[argptr + 0].indexOf(":/"); //$NON-NLS-1$
			URL sourceWsdl = idx > 3 && idx < 8
				? new URL(args[argptr + 0])
				: new File(args[argptr + 0]).toURI().toURL();
			File targetWsdl = new File(args[argptr + 1]);
			WsdlMerger wm = new WsdlMerger();
			wm.setProgressLogger(new ConsoleLogger(quiet));
			if (!quiet) {
				System.out.println(Messages.getString("WsdlMerger.merging", sourceWsdl, targetWsdl.getAbsolutePath())); //$NON-NLS-1$
			}
			int count = wm.mergeWSDL(sourceWsdl, targetWsdl);
			if (!quiet) {
				System.out.println(Messages.getString("WsdlMerger.done", count)); //$NON-NLS-1$
			}
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * prints usage
	 */
	private static void printUsage() {
		System.out.println(Messages.getString("WsdlMerger.usage", Run.class.getName())); //$NON-NLS-1$
	}
	
}
