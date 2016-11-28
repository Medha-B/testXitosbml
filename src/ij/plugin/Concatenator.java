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
import ij.Macro;
import ij.Menus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

// TODO: Auto-generated Javadoc
/** This plugin, which concatenates two or more images or stacks,
 *  implements the Image/Stacks/Tools/Concatenate command.
 *  Gives the option of viewing the concatenated stack as a 4D image.
 *  @author Jon Jackson j.jackson # ucl.ac.uk
 *  last modified June 29 2006
 */
public class Concatenator implements PlugIn, ItemListener{
    
    /** The plugin name. */
    public String pluginName =  "Concatenator";
    
    /** The all option. */
    static boolean all_option = false;
    
    /** The keep. */
    boolean keep = false;
    
    /** The keep option. */
    static boolean keep_option = false;
    
    /** The batch. */
    boolean batch = false;
    
    /** The macro. */
    boolean macro = false;
    
    /** The im 4 D. */
    boolean im4D = true;
    
    /** The im 4 D option. */
    static boolean im4D_option = false;
    
    /** The max entries. */
    public int maxEntries = 18;  // limit number of entries to fit on screen
    
    /** The image titles. */
    String[] imageTitles;
    
    /** The images. */
    ImagePlus[] images;
    
    /** The choices. */
    Vector choices;
    
    /** The all windows. */
    Checkbox allWindows;
    
    /** The none. */
    final String none = "-- None --";
    
    /** The newtitle. */
    String newtitle = "Concatenated Stacks";
    
    /** The new imp. */
    ImagePlus newImp;
    
    /** The stack size. */
    int stackSize;
    
    /** The max. */
    double min = 0, max = Float.MAX_VALUE;
    
    /**
     *  Optional string argument sets the name dialog boxes if called from another plugin.
     *
     * @param arg the arg
     */
    public void run(String arg) {
        macro = ! arg.equals("");
        if (!showDialog()) return;
        ImagePlus imp0 = images!=null&&images.length>0?images[0]:null;
        if (imp0.isComposite() || imp0.isHyperStack())
        	newImp =concatenateHyperstacks(images, newtitle, keep);
        else
        	newImp = createHypervol();
        if (newImp!=null)
            newImp.show();
    }
    
    // Launch a dialog requiring user to choose images
    /**
     * Run.
     *
     * @return the image plus
     */
    // returns ImagePlus of concatenated images
    public ImagePlus run() {
        if (!showDialog()) return null;
        newImp = createHypervol();
        return newImp;
    }
    
    /**
     * Concatenate.
     *
     * @param imp1 the imp 1
     * @param imp2 the imp 2
     * @param keep the keep
     * @return the image plus
     */
    // concatenate two images
    public ImagePlus concatenate(ImagePlus imp1, ImagePlus imp2, boolean keep) {
        images = new ImagePlus[2];
        images[0] = imp1;
        images[1] = imp2;
        return concatenate(images, keep);
    }
    
    /**
     * Concatenate.
     *
     * @param ims the ims
     * @param keepIms the keep ims
     * @return the image plus
     */
    // concatenate more than two images
    public ImagePlus concatenate(ImagePlus[] ims, boolean keepIms) {
        images = ims;
        imageTitles = new String[ims.length];
        for (int i = 0; i < ims.length; i++) {
            if (ims[i] != null) {
                imageTitles[i] = ims[i].getTitle();
            } else {
                IJ.error(pluginName, "Null ImagePlus passed to concatenate(...) method");
                return null;
            }
        }
        keep = keepIms;
        batch = true;
        newImp = createHypervol();
        return newImp;
    }
    
    /**
     * Creates the hypervol.
     *
     * @return the image plus
     */
    ImagePlus createHypervol() {
        boolean firstImage = true;
        boolean duplicated;
        Properties[] propertyArr = new Properties[images.length];
        ImagePlus currentImp = null;
        ImageStack concat_Stack = null;
        stackSize = 0;
        int dataType = 0, width= 0, height = 0;
        Calibration cal = null;
        int count = 0;
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null) { // Should only find null imp if user has closed an image after starting plugin (unlikely...)
                currentImp = images[i];
                if (firstImage) { // Initialise based on first image
                    //concat_Imp = images[i];
                    cal = currentImp.getCalibration();
                    width = currentImp.getWidth();
                    height = currentImp.getHeight();
                    stackSize = currentImp.getNSlices();
                    dataType = currentImp.getType();
                    concat_Stack = currentImp.createEmptyStack();
                    min = currentImp.getProcessor().getMin();
                    max = currentImp.getProcessor().getMax();
                    firstImage = false;
                }
                
                // Safety Checks
                if (currentImp.getNSlices() != stackSize && im4D) {
                    IJ.error(pluginName, "Cannot create 4D image because stack sizes are not equal.");
                    return null;
                }
                if (currentImp.getType() != dataType) {
                    IJ.log("Omitting " + imageTitles[i] + " - image type not matched");
                    continue;
                }
                if (currentImp.getWidth() != width || currentImp.getHeight() != height) {
                    IJ.log("Omitting " + imageTitles[i] + " - dimensions not matched");
                    continue;
                }
                
               // concatenate
                duplicated = isDuplicated(currentImp, i);
                concat(concat_Stack, currentImp.getStack(), (keep || duplicated));
                propertyArr[count] = currentImp.getProperties();
                imageTitles[count] = currentImp.getTitle();
                if (! (keep || duplicated)) {
                    currentImp.changes = false;
                    currentImp.hide();
                }
                count++;
            }
        }
        
        // Copy across info fields
        ImagePlus imp = new ImagePlus(newtitle, concat_Stack);
        imp.setCalibration(cal);
        imp.setProperty("Number of Stacks", new Integer(count));
        imp.setProperty("Stacks Properties", propertyArr);
        imp.setProperty("Image Titles", imageTitles);
        imp.getProcessor().setMinAndMax(min, max);
        if (im4D) {
        	imp.setDimensions(1, stackSize, imp.getStackSize()/stackSize);
        	imp.setOpenAsHyperStack(true);
        }
        return imp;
    }
    
    /**
     * Concat.
     *
     * @param stack3 the stack 3
     * @param stack1 the stack 1
     * @param dup the dup
     */
    // taken from WSR's Concatenator_.java
    void concat(ImageStack stack3, ImageStack stack1, boolean dup) {
        int slice = 1;
        int size = stack1.getSize();
        for (int i = 1; i <= size; i++) {
            ImageProcessor ip = stack1.getProcessor(slice);
            String label = stack1.getSliceLabel(slice);
            if (dup) {
                ip = ip.duplicate();
                slice++;
            } else
                stack1.deleteSlice(slice);
            stack3.addSlice(label, ip);
        }
    } 
    
    /**
     * Concatenate hyperstacks.
     *
     * @param images the images
     * @param newTitle the new title
     * @param keep the keep
     * @return the image plus
     */
    public ImagePlus concatenateHyperstacks(ImagePlus[] images, String newTitle, boolean keep) {
        int n = images.length;
        int width = images[0].getWidth();
        int height = images[0].getHeight();
        int bitDepth = images[0].getBitDepth();
        int channels = images[0].getNChannels();
        int slices =  images[0].getNSlices();
        int frames = images[0].getNFrames();
        boolean concatSlices = slices>1 && frames==1;
        for (int i=1; i<n; i++) {
            if (images[i].getNFrames()>1) concatSlices = false;
            if (images[i].getWidth()!=width
            || images[i].getHeight()!=height
            || images[i].getBitDepth()!=bitDepth
            || images[i].getNChannels()!=channels
            || (!concatSlices && images[i].getNSlices()!=slices)) {
            	IJ.error(pluginName, "Images do not all have the same dimensions or type");
                return null;
            }
        }
        ImageStack stack2 = new ImageStack(width, height);
        int slices2=0, frames2=0;
        for (int i=0;i<n;i++) {
            ImageStack stack = images[i].getStack();
            slices = images[i].getNSlices();
            if (concatSlices) {
                slices = images[i].getNSlices();
                slices2 += slices;
                frames2 = frames;
            } else {
                frames = images[i].getNFrames();
                frames2 += frames;
                slices2 = slices;
            }
            for (int f=1; f<=frames; f++) {
                for (int s=1; s<=slices; s++) {
                    for (int c=1; c<=channels; c++) {
                        int index = (f-1)*channels*slices + (s-1)*channels + c;
                        ImageProcessor ip = stack.getProcessor(index);
                        if (keep)
                            ip = ip.duplicate();
                        String label = stack.getSliceLabel(index);
                        stack2.addSlice(label, ip);
                    }
                }
            }
        }
        ImagePlus imp2 = new ImagePlus(newTitle, stack2);
        imp2.setDimensions(channels, slices2, frames2);
        if (channels>1) {
            int mode = 0;
            if (images[0].isComposite())
                mode = ((CompositeImage)images[0]).getMode();
            imp2 = new CompositeImage(imp2, mode);
            ((CompositeImage)imp2).copyLuts(images[0]);
        }
        if (channels>1 && frames2>1)
            imp2.setOpenAsHyperStack(true);
        if (!keep) {
            for (int i=0; i<n; i++) {
                images[i].changes = false;
                images[i].close();
            }
        }
        return imp2;
    }   
    
    /**
     * Show dialog.
     *
     * @return true, if successful
     */
    boolean showDialog() {
        boolean all_windows = false;
        batch = Interpreter.isBatchMode();
        macro = macro || (IJ.isMacro()&&Macro.getOptions()!=null);
        im4D = Menus.commandInUse("Stack to Image5D") && ! batch;
        if (macro) {
            String options = Macro.getOptions();
            if (options.contains("stack1")&&options.contains("stack2"))
                Macro.setOptions(options.replaceAll("stack", "image"));
            int macroImageCount = 0;
            options = Macro.getOptions();
            while (true) {
            	if (options.contains("image"+(macroImageCount+1)))
            		macroImageCount++;
            	else
            		break;
            }
            maxEntries = macroImageCount;
        }
        
        // Checks
        int[] wList = WindowManager.getIDList();
        if (wList==null) {
            IJ.error("No windows are open.");
            return false;
        } else if (wList.length < 2) {
            IJ.error("Two or more windows must be open");
            return false;
        }
        int nImages = wList.length;
        
        String[] titles = new String[nImages];
        String[] titles_none = new String[nImages + 1];
        for (int i=0; i<nImages; i++) {
            ImagePlus imp = WindowManager.getImage(wList[i]);
            if (imp!=null) {
                titles[i] = imp.getTitle();
                titles_none[i] = imp.getTitle();
            } else {
                titles[i] = "";
                titles_none[i] = "";
            }
        }
        titles_none[nImages] = none;
        
        GenericDialog gd = new GenericDialog(pluginName);
        gd.addCheckbox("All_open windows", all_option);
        gd.addChoice("Image1:", titles, titles[0]);
        gd.addChoice("Image2:", titles, titles[1]);
        for (int i = 2; i < ((nImages+1)<maxEntries?(nImages+1):maxEntries); i++)
            gd.addChoice("Image" + (i+1)+":", titles_none, titles_none[i]);
        gd.addStringField("Title:", newtitle, 16);
        gd.addCheckbox("Keep original images", keep_option);
        gd.addCheckbox("Open as 4D_image", im4D_option);
        if (!macro) { // Monitor user selections
            choices = gd.getChoices();
            for (Enumeration e = choices.elements() ; e.hasMoreElements() ;)
                ((Choice)e.nextElement()).addItemListener(this);
            Vector v = gd.getCheckboxes();
            allWindows = (Checkbox)v.firstElement();
            allWindows.addItemListener(this);
            if (all_option) itemStateChanged(new ItemEvent(allWindows, ItemEvent.ITEM_STATE_CHANGED, null, ItemEvent.SELECTED));
        }
        gd.showDialog();
        
        if (gd.wasCanceled())
            return false;
        all_windows = gd.getNextBoolean();
        all_option = all_windows;
        newtitle = gd.getNextString();
        keep = gd.getNextBoolean();
        keep_option = keep;
        im4D = gd.getNextBoolean();
        im4D_option = im4D;
        ImagePlus[] tmpImpArr = new ImagePlus[nImages+1];
        String[] tmpStrArr = new String[nImages+1];
        int index, count = 0;
        for (int i=0; i<(nImages+1); i++) { // compile a list of images to concatenate from user selection
            if (all_windows) { // Useful to not have to specify images in batch mode
                index = i;
            } else {
                if (i == ((nImages+1)<maxEntries?(nImages+1):maxEntries) ) break;
                index = gd.getNextChoiceIndex();
            }
            if (index >= nImages) break; // reached the 'none' string or handled all images (in case of all_windows)
            if (! titles[index].equals("")) {
                tmpStrArr[count] = titles[index];
                tmpImpArr[count] = WindowManager.getImage(wList[index]);
                count++;
            }
        }
        if (count<2) {
            IJ.error(pluginName, "Please select at least 2 images");
            return false;
        }
        
        imageTitles = new String[count];
        images = new ImagePlus[count];
        System.arraycopy(tmpStrArr, 0, imageTitles, 0, count);
        System.arraycopy(tmpImpArr, 0, images, 0, count);
        return true;
    }
    
    /**
     * Checks if is duplicated.
     *
     * @param imp the imp
     * @param index the index
     * @return true, if is duplicated
     */
    // test if this imageplus appears again in the list
    boolean isDuplicated(ImagePlus imp, int index) {
        int length = images.length;
        if (index >= length - 1) return false;
        for (int i = index + 1; i < length; i++) {
            if (imp == images[i]) return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
     */
    public void itemStateChanged(ItemEvent ie) {
        Choice c;
        if (ie.getSource() == allWindows) { // User selected / unselected 'all windows' button
            int count = 0;
            if (allWindows.getState()) {
                for (Enumeration e = choices.elements() ; e.hasMoreElements() ;) {
                    c = (Choice)e.nextElement();
                    c.select(count++);
                    c.setEnabled(false);
                }
            } else {
                for (Enumeration e = choices.elements() ; e.hasMoreElements() ;) {
                    c = (Choice)e.nextElement();
                    c.setEnabled(true);
                }
            }
        } else { // User image selection triggered event
            boolean foundNone = false;
            // All image choices after an occurance of 'none' are reset to 'none'
            for (Enumeration e = choices.elements() ; e.hasMoreElements() ;) {
                c = (Choice)e.nextElement();
                if (! foundNone) {
                    c.setEnabled(true);
                    if (c.getSelectedItem().equals(none)) foundNone = true;
                } else { // a previous choice was 'none'
                    c.select(none);
                    c.setEnabled(false);
                }
            }
        }
    }
    
    /**
     * Sets the im 5 D.
     *
     * @param bool the new im 5 D
     */
    public void setIm5D(boolean bool) {
        im4D_option = bool;
    }

}
