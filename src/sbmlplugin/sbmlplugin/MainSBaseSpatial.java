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
package sbmlplugin.sbmlplugin;

import ij.IJ;
import ij.io.OpenDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import sbmlplugin.image.SpatialImage;
import sbmlplugin.util.PluginConstants;
import sbmlplugin.visual.Viewer;

// TODO: Auto-generated Javadoc
/**
 * Spatial SBML Plugin for ImageJ.
 *
 * @author Kaito Ii <ii@fun.bio.keio.ac.jp>
 * @author Akira Funahashi <funa@bio.keio.ac.jp>
 * Date Created: Jun 17, 2015
 */
public abstract class MainSBaseSpatial extends MainSpatial implements PlugIn{
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	@Override
	public abstract void run(String arg);
	
	/**
	 * Visualize.
	 *
	 * @param spImgList the sp img list
	 */
	protected void visualize(ArrayList<SpatialImage> spImgList){
		Iterator<SpatialImage> it = spImgList.iterator();
		Viewer viewer = new Viewer();
		while(it.hasNext()){
			viewer.view(it.next());
		}
	}
	
	/**
	 * Gets the document.
	 *
	 * @return the document
	 * @throws NullPointerException the null pointer exception
	 * @throws XMLStreamException 
	 */
	protected SBMLDocument getDocument() throws NullPointerException, XMLStreamException{
		JFileChooser chooser = new JFileChooser(OpenDialog.getLastDirectory());
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileNameExtensionFilter("SBML File(*.xml)", "xml"));
		int returnVal = chooser.showOpenDialog(null);

		if (returnVal != JFileChooser.APPROVE_OPTION)
			throw new NullPointerException();
		File f = chooser.getSelectedFile();
		return SBMLReader.read(f.getAbsolutePath());
	}

	/**
	 * Check SBML document.
	 *
	 * @param document the document
	 */
	public void checkSBMLDocument(SBMLDocument document){
		if(document == null || document.getModel() == null) 
			throw new IllegalArgumentException("Non-supported format file");
		model = document.getModel();
		checkLevelAndVersion();
		checkExtension();
	}
	
	/**
	 * Check level and version.
	 */
	protected void checkLevelAndVersion(){
		if(model.getLevel() != PluginConstants.SBMLLEVEL || model.getVersion() != PluginConstants.SBMLVERSION)
			IJ.error("Incompatible level and version");
	}
	
	/**
	 * Check extension.
	 */
	protected void checkExtension(){
		if(!document.getPackageRequired("spatial"))
			IJ.error("Could not find spatial extension");

		if(!document.getPackageRequired("req")) 
			IJ.error("Could not find req extension");
	}

}
