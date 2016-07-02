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
import ij.ImagePlus;
import ij.plugin.PlugIn;

import java.util.HashMap;
import java.util.Map.Entry;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.req.ReqSBasePlugin;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;

import sbmlplugin.image.CreateImage;
import sbmlplugin.image.Filler;
import sbmlplugin.image.ImageBorder;
import sbmlplugin.image.ImageEdit;
import sbmlplugin.image.ImageExplorer;
import sbmlplugin.image.Interpolater;
import sbmlplugin.image.SpatialImage;
import sbmlplugin.pane.TabTables;
import sbmlplugin.visual.DomainStruct;
import sbmlplugin.visual.Viewer;


// TODO: Auto-generated Javadoc
/**
 * The Class MainSpatial.
 */
public abstract class MainSpatial implements PlugIn{

	/** The document. */
	protected SBMLDocument document;
	
	/** The model. */
	protected Model model;
	
	/** The spatialplugin. */
	protected SpatialModelPlugin spatialplugin;
	
	/** The reqplugin. */
	protected ReqSBasePlugin reqplugin;
	
	/** The imgexp. */
	private ImageExplorer imgexp;
	
	/** The hash domain types. */
	private HashMap<String, Integer> hashDomainTypes;
	
	/** The hash sampled value. */
	protected HashMap<String, Integer> hashSampledValue;
	
	/** The viewer. */
	protected Viewer viewer;
	
	/** The sp img. */
	protected SpatialImage spImg;
	
	/**
	 * Gui.
	 */
	protected void gui() {
		hashDomainTypes = new HashMap<String, Integer>();
		hashSampledValue = new HashMap<String, Integer> ();
		imgexp = new ImageExplorer(hashDomainTypes,hashSampledValue);
		while (hashDomainTypes.isEmpty() && hashSampledValue.isEmpty()) {
			synchronized (hashDomainTypes) {
				synchronized (hashSampledValue) {
					
				}
			}
		}
	}
	
	/**
	 * Compute img.
	 */
	protected void computeImg(){
		Interpolater interpolater = new Interpolater();
		HashMap<String, ImagePlus> hashDomFile = imgexp.getDomFile();
		interpolater.interpolate(hashDomFile);
		Filler fill = new Filler();

		for(Entry<String, ImagePlus> e : hashDomFile.entrySet())
			hashDomFile.put(e.getKey(), fill.fill(e.getValue()));
		
		CreateImage creIm = new CreateImage(imgexp.getDomFile(), hashSampledValue);
		spImg = new SpatialImage(hashSampledValue, hashDomainTypes, creIm.getCompoImg());
		ImagePlus img = fill.fill(spImg);
		spImg.setImage(img);
		ImageBorder imgBorder = new ImageBorder(spImg);
		spImg.updateImage(imgBorder.getStackImage());

		new ImageEdit(spImg);
	}
	
	/**
	 * Visualize.
	 *
	 * @param spImg the sp img
	 */
	protected void visualize (SpatialImage spImg){
		viewer = new Viewer();
		viewer.view(spImg);
	}
	
	/**
	 * Adds the S bases.
	 */
	protected void addSBases(){
		ListOf<Parameter> lop = model.getListOfParameters();
		ListOf<Species> los = model.getListOfSpecies();
		TabTables tt = new TabTables(model);
		
		while(tt.isRunning()){
			synchronized(lop){
				synchronized(los){
					
				}
			}
		}
	}
	
	/**
	 * Show domain structure.
	 */
	protected void showDomainStructure(){
		spatialplugin = (SpatialModelPlugin)model.getPlugin("spatial");
		Geometry g = spatialplugin.getGeometry();
		new DomainStruct().show(g);	
	}
	
	/**
	 * Show step.
	 *
	 * @param spImg the sp img
	 */
	protected void showStep(SpatialImage spImg){
		visualize(spImg);
	}
}