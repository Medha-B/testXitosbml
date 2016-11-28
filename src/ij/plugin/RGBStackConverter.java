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
import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Undo;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.Rectangle;

// TODO: Auto-generated Javadoc
/** Converts a 2 or 3 slice stack, or a hyperstack, to RGB. */
public class RGBStackConverter implements PlugIn, DialogListener {
	
	/** The frames 1. */
	private int channels1, slices1, frames1;
	
	/** The frames 2. */
	private int slices2, frames2;
	
	/** The height. */
	private int width, height;
	
	/** The image size. */
	private double imageSize;
	
	/** The static keep. */
	private static boolean staticKeep = true;
	
	/** The keep. */
	private boolean keep;
	
	/** The image. */
	private ImagePlus image;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String arg) {
		ImagePlus imp = image;
		if (imp==null)
			imp = IJ.getImage();
		if (!IJ.isMacro()) keep = staticKeep;
		CompositeImage cimg = imp.isComposite()?(CompositeImage)imp:null;
		int size = imp.getStackSize();
		if ((size<2||size>3) && cimg==null) {
			IJ.error("A 2 or 3 image stack, or a HyperStack, required");
			return;
		}
		int type = imp.getType();
		if (cimg==null && !(type==ImagePlus.GRAY8 || type==ImagePlus.GRAY16)) {
			IJ.error("8-bit or 16-bit grayscale stack required");
			return;
		}
		if (!imp.lock())
			return;
		Undo.reset();
		String title = imp.getTitle()+" (RGB)";
		if (cimg!=null)
			compositeToRGB(cimg, title);
		else if (type==ImagePlus.GRAY16) {
			sixteenBitsToRGB(imp);
		} else {
			ImagePlus imp2 = imp.createImagePlus();
			imp2.setStack(title, imp.getStack());
	 		ImageConverter ic = new ImageConverter(imp2);
			ic.convertRGBStackToRGB();
			imp2.show();
		}
		imp.unlock();
	}
	
	/**
	 *  Converts the specified multi-channel (composite) image to RGB.
	 *
	 * @param imp the imp
	 */
	public static void convertToRGB(ImagePlus imp) {
		if (!imp.isComposite())
			throw new IllegalArgumentException("Multi-channel image required");
		RGBStackConverter converter = new RGBStackConverter();
		ImageWindow win = imp.getWindow();
		Point location = null;
		if (win!=null) {
			location = win.getLocation();
			imp.hide();
		}
		converter.image = imp;
		converter.run("");
		if (win!=null) {
			ImageWindow.setNextLocation(location);
			imp.show();
		}
	}
	
	/**
	 * Composite to RGB.
	 *
	 * @param imp the imp
	 * @param title the title
	 */
	void compositeToRGB(CompositeImage imp, String title) {
		int channels = imp.getNChannels();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		int images = channels*slices*frames;
		if (channels==images) {
			compositeImageToRGB(imp, title);
			return;
		}
		width = imp.getWidth();
		height = imp.getHeight();
		imageSize = width*height*4.0/(1024.0*1024.0);
		channels1 = imp.getNChannels();
		slices1 = slices2 = imp.getNSlices();
		frames1 = frames2 = imp.getNFrames();
		int c1 = imp.getChannel();
		int z1 = imp.getSlice();
		int t2 = imp.getFrame();
		if (image!=null) {
			slices2 = slices1;
			frames2 = frames1;
			keep = false;
		} else {
			if (!showDialog())
				return;
		}
		//IJ.log("HyperStackReducer-2: "+keep+" "+channels2+" "+slices2+" "+frames2);
		String title2 = keep?WindowManager.getUniqueName(imp.getTitle()):imp.getTitle();
		ImagePlus imp2 = imp.createHyperStack(title2, 1, slices2, frames2, 24);
		convertHyperstack(imp, imp2);
		if (imp.getWindow()==null && !keep) {
			imp.setImage(imp2);
			imp.setOverlay(imp2.getOverlay());
			return;
		}
		imp2.setOpenAsHyperStack(slices2>1||frames2>1);
		imp2.show();
		if (!keep) {
			imp.changes = false;
			imp.close();
		}
	}

	/**
	 * Convert hyperstack.
	 *
	 * @param imp the imp
	 * @param imp2 the imp 2
	 */
	public void convertHyperstack(ImagePlus imp, ImagePlus imp2) {
		int slices = imp2.getNSlices();
		int frames = imp2.getNFrames();
		int c1 = imp.getChannel();
		int z1 = imp.getSlice();
		int t1 = imp.getFrame();
		int i = 1;
		int c = 1;
		ImageStack stack = imp.getStack();
		ImageStack stack2 = imp2.getStack();
		imp.setPositionWithoutUpdate(c1, 1, 1);
		ImageProcessor ip = imp.getProcessor();
		double min = ip.getMin();
		double max = ip.getMax();
		for (int z=1; z<=slices; z++) {
			if (slices==1) z = z1;
			for (int t=1; t<=frames; t++) {
				//IJ.showProgress(i++, n);
				if (frames==1) t = t1;
				//ip = stack.getProcessor(n1);
				imp.setPositionWithoutUpdate(c1, z, t);
				Image img = imp.getImage();
				int n2 = imp2.getStackIndex(c1, z, t);
				stack2.setPixels((new ColorProcessor(img)).getPixels(), n2);
			}
		}
		imp.setPosition(c1, z1, t1);
		imp2.resetStack();
		imp2.setPosition(1, 1, 1);
		
		//Added by Marcel Boeglin 2013.09.26
		Overlay overlay = imp.getOverlay();
		if (overlay!=null) {
			int firstC = c1, lastC = c1, firstZ = z1, lastZ = z1, firstT = t1, lastT = t1;
			if (imp.isComposite() && ((CompositeImage)imp).getMode()==IJ.COMPOSITE) {
				firstC = 1;
				lastC = imp.getNChannels();
			}
			if (slices2>1) {firstZ = 1; lastZ = slices2;}
			if (frames2>1) {firstT = 1; lastT = frames2;}
			Overlay overlay2 = overlay.duplicate();
			if (slices2>1 && frames2>1)
				overlay2.crop(firstC, lastC, firstZ, lastZ, firstT, lastT);//imp2 is hyperstack : ROI's hypercoordinates are conserved but only those with C = 1 are displayed
			else
				overlay2.crop(c1, c1, firstZ, lastZ, firstT, lastT); //simple stack
			imp2.setOverlay(overlay2);
		}
	}

	/**
	 * Composite image to RGB.
	 *
	 * @param imp the imp
	 * @param title the title
	 */
	void compositeImageToRGB(CompositeImage imp, String title) {
		if (imp.getMode()==IJ.COMPOSITE) {
			ImagePlus imp2 = imp.createImagePlus();
			imp.updateImage();
			imp2.setProcessor(title, new ColorProcessor(imp.getImage()));
			//Added by Marcel Boeglin 2013.09.26
			Overlay overlay = imp.getOverlay();
			Overlay overlay2 = null;
			if (overlay!=null) {
				overlay2 = overlay.duplicate();
				overlay2.crop(1, imp.getNChannels());
				imp2.setOverlay(overlay2);
			}
			if (image!=null && imp.getWindow()==null) {
				imp.setImage(imp2);
				imp.setOverlay(overlay2);
			} else
				imp2.show();
			return;
		}
		ImageStack stack = new ImageStack(imp.getWidth(), imp.getHeight());
		int c = imp.getChannel();
		int n = imp.getNChannels();
		for (int i=1; i<=n; i++) {
			imp.setPositionWithoutUpdate(i, 1, 1);
			stack.addSlice(null, new ColorProcessor(imp.getImage()));
		}
		imp.setPosition(c, 1, 1);
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(title, stack);
		Object info = imp.getProperty("Info");
		if (info!=null) imp2.setProperty("Info", info);
		Overlay overlay = imp.getOverlay();
		Overlay overlay2 = null;
		if (overlay!=null) {
			overlay2 = overlay.duplicate();
			overlay2.crop(1, imp.getNChannels());
			imp2.setOverlay(overlay2);
		}
		if (image!=null && imp.getWindow()==null) {
			imp.setImage(imp2);
			imp.setOverlay(overlay2);
		} else {
			imp2.show();
			imp2.setSlice(c);
		}
	}

	/**
	 * Sixteen bits to RGB.
	 *
	 * @param imp the imp
	 */
	void sixteenBitsToRGB(ImagePlus imp) {
		Roi roi = imp.getRoi();
		int width, height;
		Rectangle r;
		if (roi!=null) {
			r = roi.getBounds();
			width = r.width;
			height = r.height;
		} else
			r = new Rectangle(0,0,imp.getWidth(),imp.getHeight());
		ImageProcessor ip;
		ImageStack stack1 = imp.getStack();
		ImageStack stack2 = new ImageStack(r.width, r.height);
		for (int i=1; i<=stack1.getSize(); i++) {
			ip = stack1.getProcessor(i);
			ip.setRoi(r);
			ImageProcessor ip2 = ip.crop();
			ip2 = ip2.convertToByte(true);
			stack2.addSlice(null, ip2);
		}
		ImagePlus imp2 = imp.createImagePlus();
		imp2.setStack(imp.getTitle()+" (RGB)", stack2);
	 	ImageConverter ic = new ImageConverter(imp2);
		ic.convertRGBStackToRGB();
		imp2.show();
	}
	
	/**
	 * Show dialog.
	 *
	 * @return true, if successful
	 */
	boolean showDialog() {
		GenericDialog gd = new GenericDialog("Convert to RGB");
		gd.setInsets(10, 20, 5);
		gd.addMessage("Create RGB image with:");
		gd.setInsets(0, 35, 0);
		if (slices1!=1) gd.addCheckbox("Slices ("+slices1+")", true);
		gd.setInsets(0, 35, 0);
		if (frames1!=1) gd.addCheckbox("Frames ("+frames1+")", true);
		gd.setInsets(5, 20, 0);
		gd.addMessage(getNewDimensions()+"      ");
		gd.setInsets(15, 20, 0);
		gd.addCheckbox("Keep source", keep);
		gd.addDialogListener(this);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		else
			return true;
	}

	/* (non-Javadoc)
	 * @see ij.gui.DialogListener#dialogItemChanged(ij.gui.GenericDialog, java.awt.AWTEvent)
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (IJ.isMacOSX()) IJ.wait(100);
		if (slices1!=1) slices2 = gd.getNextBoolean()?slices1:1;
		if (frames1!=1) frames2 = gd.getNextBoolean()?frames1:1;
		keep = gd.getNextBoolean();
		if (!IJ.isMacro()) staticKeep = keep;
		((Label)gd.getMessage()).setText(getNewDimensions());
		return true;
	}
	
	/**
	 * Gets the new dimensions.
	 *
	 * @return the new dimensions
	 */
	String getNewDimensions() {
		String s1 = slices2>1?"x"+slices2:"";
		String s2 = frames2>1?"x"+frames2:"";
		String s = width+"x"+height+s1+s2;
		s += " ("+(int)Math.round(imageSize*slices2*frames2)+"MB)";
		return(s);
	}

	
}
