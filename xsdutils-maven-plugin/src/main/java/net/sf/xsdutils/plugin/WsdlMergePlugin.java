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
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sf.xsdutils.wsdl.WsdlMerger;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Merges a WSDL with imported WSDLs and XSDs into one file.
 * 
 * @author Rustam Abdullaev
 * @goal wsdlmerge
 * @execute phase="generate-resources"
 */
public class WsdlMergePlugin extends AbstractMojo {

	private static final String WSDL_EXT = ".wsdl"; //$NON-NLS-1$
	private static final String MERGED_SUFFIX = "_merged"; //$NON-NLS-1$

	/**
	 * The source WSDL directory.
	 * 
	 * @parameter default-value="${basedir}/src/wsdl"
	 * @required
	 */
	protected File wsdlDirectory;

	/**
	 * The list of source wsdl file names or URLs. If not set, all .wsdl files
	 * in the wsdlDirectory will be processed.
	 * 
	 * @parameter
	 */
	protected List<String> wsdls;

	/**
	 * The directory where to store merged wsdl's.
	 * 
	 * If set the same as wsdlDirectory, original files will not be overwritten,
	 * instead merged files will get a name suffix "_merged".
	 * 
	 * @parameter default-value="${project.build.directory}/merged-wsdl"
	 * @required
	 */
	protected File targetDirectory;

	/**
	 * Whether or not to log execution details.
	 * 
	 * @parameter default-value="false"
	 */
	private boolean verbose;

	/**
	 * Specifies the target wsdl file name. Can be only used with exactly one
	 * source wsdl.
	 * 
	 * @parameter
	 */
	protected String targetWsdl;
	
	/**
	 * Whether or not to use system default line separators. On Windows it is CR LF,
	 * on UNIX LF. If set to false, UNIX LF will always be used.
	 * 
	 * @parameter default-value="false"
	 */
	protected boolean systemLineSep;

	/**
	 * Executes the wsdlmerge task
	 */
	public void execute() throws MojoExecutionException {
		
		String savedLineSep = System.getProperty("line.separator"); //$NON-NLS-1$
		try {
			WsdlMerger wm = new WsdlMerger();
			if (!systemLineSep) {
				// set line separator to UNIX NL
				System.setProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			wm.setProgressLogger(new MavenLogger(getLog(), verbose));

			if (wsdls == null) {
				// discover *.wsdl in wsdlDirectory
				wsdls = new ArrayList<String>();
				for (String file : wsdlDirectory.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(WSDL_EXT) && !name.endsWith(MERGED_SUFFIX + WSDL_EXT);
					}
				})) {
					wsdls.add(file);
				}
			}

			if (targetWsdl != null && wsdls.size() > 1) {
				// targetWsdl can only be used with a single source wsdl
				getLog().warn(Messages.getString("WsdlMergePlugin.targetWsdlIgnored")); //$NON-NLS-1$
				targetWsdl = null;
			}

			targetDirectory.mkdirs();

			for (String wsdl : wsdls) {
				// process each WSDL
				int idx = wsdl.indexOf(":/"); //$NON-NLS-1$
				URL sourceWsdlURL = idx > 3 && idx < 8 ? new URL(wsdl) : new File(wsdlDirectory, wsdl).toURI().toURL();
				String targetWsdlName = targetWsdl == null ? wsdl : targetWsdl;
				File targetWsdlFile = new File(targetDirectory, targetWsdlName);
				if (targetWsdlFile.toURI().toURL().sameFile(sourceWsdlURL)) {
					// target same as source? append _merged
					String path = targetWsdlFile.getAbsolutePath();
					idx = path.lastIndexOf('.');
					path = path.substring(0, idx) + MERGED_SUFFIX + path.substring(idx);
					targetWsdlFile = new File(path);
				}

				getLog().info(Messages.getString("WsdlMergePlugin.merging", wsdl, targetWsdlFile.getAbsolutePath())); //$NON-NLS-1$
				
				int count = wm.mergeWSDL(sourceWsdlURL, targetWsdlFile);
				
				getLog().info(Messages.getString("WsdlMergePlugin.merged", count)); //$NON-NLS-1$
			}

		} catch (Exception e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} finally {
			// restore original line separator
			System.setProperty("line.separator", savedLineSep); //$NON-NLS-1$
		}
		
	}
	
	public void setWsdlDirectory(File wsdlDirectory) {
		this.wsdlDirectory = wsdlDirectory;
	}
	
	public void setWsdls(List<String> wsdls) {
		this.wsdls = wsdls;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void setTargetWsdl(String targetWsdl) {
		this.targetWsdl = targetWsdl;
	}
	
	public void setSystemLineSep(boolean systemLineSep) {
		this.systemLineSep = systemLineSep;
	}

}
