/*******************************************************************************
 * Copyright 2015 Kaito Ii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package ij.plugin;
import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.util.Tools;

import java.util.Properties;

// TODO: Auto-generated Javadoc
/** This plugin implements the Plugins/Utilities/Proxy Settings command. It sets
* 	the JVM proxy properties to allow the Help/Update ImageJ command
*	and File/Open Samples menu to work on networks behind a proxy server. 
* 
*     @author	Dimiter Prodanov
*/
public class ProxySettings implements PlugIn {
	
	/** The props. */
	private Properties props = System.getProperties();
	
	/** The proxyhost. */
	private String proxyhost = Prefs.get("proxy.server", "");
	
	/** The proxyport. */
	private int proxyport = (int)Prefs.get("proxy.port", 8080);
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (IJ.getApplet()!=null) return;
		String host = System.getProperty("http.proxyHost");
		if (host!=null) proxyhost = host;
		String port = System.getProperty("http.proxyPort");
		if (port!=null) {
			double portNumber = Tools.parseDouble(port);
			if (!Double.isNaN(portNumber))
				proxyport = (int)portNumber;
		}
		if (!showDialog()) return;
		if (!proxyhost.equals(""))
			props.put("proxySet", "true");
		else
			props.put("proxySet", "false");
		props.put("http.proxyHost", proxyhost);
		props.put("http.proxyPort", ""+proxyport);
		Prefs.set("proxy.server", proxyhost);
		Prefs.set("proxy.port", proxyport);
		try {
			System.setProperty("java.net.useSystemProxies", Prefs.useSystemProxies?"true":"false");
		} catch(Exception e) {}
		if (IJ.debugMode)
			logProperties();
	}
	
	/**
	 * Log properties.
	 */
	public void logProperties() {
		IJ.log("proxy set: "+ System.getProperty("proxySet"));
		IJ.log("proxy host: "+ System.getProperty("http.proxyHost"));
		IJ.log("proxy port: "+System.getProperty("http.proxyPort"));
		IJ.log("java.net.useSystemProxies: "+System.getProperty("java.net.useSystemProxies"));
	}

	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	boolean showDialog()   {
		GenericDialog gd=new GenericDialog("Proxy Settings");
		gd.addStringField("Proxy server:", proxyhost, 15);
		gd.addNumericField("Port:", proxyport , 0);
		gd.addCheckbox("Or, use system proxy settings", Prefs.useSystemProxies);
		gd.addHelp(IJ.URL+"/docs/menus/edit.html#proxy");
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		proxyhost = gd.getNextString();
		proxyport = (int)gd.getNextNumber();
		Prefs.useSystemProxies = gd.getNextBoolean();
		return true;
	}

}
