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
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotMaker;
import ij.gui.PlotWindow;
import ij.gui.ProfilePlot;
import ij.gui.Roi;

// TODO: Auto-generated Javadoc
/** Implements the Analyze/Plot Profile and Edit/Options/Profile Plot Options commands. */
public class Profiler implements PlugIn, PlotMaker {
	
	/** The imp. */
	ImagePlus imp;
	
	/** The first time. */
	boolean firstTime = true;

	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		if (arg.equals("set"))
			{doOptions(); return;}
		imp = IJ.getImage();
		Plot plot = getPlot();
		firstTime = false;
		if (plot==null)
			return;
		plot.setPlotMaker(this);
		plot.show();
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.PlotMaker#getPlot()
	 */
	public Plot getPlot() {
		Roi roi = imp.getRoi();
		//if (roi==null && !firstTime)
		//	IJ.run(imp, "Restore Selection", "");
		if (roi==null || !(roi.isLine()||roi.getType()==Roi.RECTANGLE)) {
			if (firstTime)
				IJ.error("Plot Profile", "Line or rectangular selection required");
			return null;
		}
		boolean averageHorizontally = Prefs.verticalProfile || IJ.altKeyDown();
		ProfilePlot pp = new ProfilePlot(imp, averageHorizontally);
		return pp.getPlot();
	}
	
	/* (non-Javadoc)
	 * @see ij.gui.PlotMaker#getSourceImage()
	 */
	public ImagePlus getSourceImage() {
		return imp;
	}

	/**
	 * Do options.
	 */
	public void doOptions() {
		double ymin = ProfilePlot.getFixedMin();
		double ymax = ProfilePlot.getFixedMax();
		boolean fixedScale = ymin!=0.0 || ymax!=0.0;
		boolean wasFixedScale = fixedScale;
		
		GenericDialog gd = new GenericDialog("Profile Plot Options", IJ.getInstance());
		gd.addNumericField("Width (pixels):", PlotWindow.plotWidth, 0);
		gd.addNumericField("Height (pixels):", PlotWindow.plotHeight, 0);
		gd.addNumericField("Minimum Y:", ymin, 2);
		gd.addNumericField("Maximum Y:", ymax, 2);
		gd.addCheckbox("Fixed y-axis scale", fixedScale);
		gd.addCheckbox("Do not save x-values", !PlotWindow.saveXValues);
		gd.addCheckbox("Auto-close", PlotWindow.autoClose);
		gd.addCheckbox("Vertical profile", Prefs.verticalProfile);
		gd.addCheckbox("List values", PlotWindow.listValues);
		gd.addCheckbox("Interpolate line profiles", PlotWindow.interpolate);
		gd.addCheckbox("Draw grid lines", !PlotWindow.noGridLines);
		gd.addCheckbox("Sub-pixel resolution", Prefs.subPixelResolution);
		gd.addHelp(IJ.URL+"/docs/menus/edit.html#plot-options");
		gd.showDialog();
		if (gd.wasCanceled())
			return;
		int w = (int)gd.getNextNumber();
		int h = (int)gd.getNextNumber();
		if (w<100) w = 100;
		if (h<50) h = 50;
		PlotWindow.plotWidth = w;
		PlotWindow.plotHeight = h;
		ymin = gd.getNextNumber();
		ymax = gd.getNextNumber();
		fixedScale = gd.getNextBoolean();
		PlotWindow.saveXValues = !gd.getNextBoolean();
		PlotWindow.autoClose = gd.getNextBoolean();
		Prefs.verticalProfile = gd.getNextBoolean();
		PlotWindow.listValues = gd.getNextBoolean();
		PlotWindow.interpolate = gd.getNextBoolean();
		PlotWindow.noGridLines = !gd.getNextBoolean();
		Prefs.subPixelResolution = gd.getNextBoolean();
		if (!fixedScale && !wasFixedScale && (ymin!=0.0 || ymax!=0.0))
			fixedScale = true;
		if (!fixedScale) {
			ymin = 0.0;
			ymax = 0.0;
		} else if (ymin>ymax) {
			double tmp = ymin;
			ymin = ymax;
			ymax = tmp;
		}
		ProfilePlot.setMinAndMax(ymin, ymax);
		IJ.register(Profiler.class);
	}
		
}

