package jp.ac.keio.bio.fun.xitosbml.xitosbml;

import ij.IJ;
import jp.ac.keio.bio.fun.xitosbml.geometry.GeometryDatas;
import jp.ac.keio.bio.fun.xitosbml.util.ModelSaver;
import jp.ac.keio.bio.fun.xitosbml.util.ModelValidator;

/**
 * The class MainModelEdit, which implements "run Model Editor" function.
 * Date Created: Aug 28, 2015
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */
public class MainModelEdit extends MainSBaseSpatial {

	/**
	 * Overrides ij.plugin.PlugIn#run(java.lang.String).
	 * A dialog for editing the model will be displayed.
     * Users can add, modify following SBML elements and save the model through the dialog.
	 * <ul>
     * <li>Species</li>
	 * <li>Parameter</li>
	 * <li>Advection coefficient</li>
	 * <li>Diffusion coefficient</li>
	 * <li>Boundary condition</li>
	 * <li>Reaction</li>
	 * </ul>
	 *
     * Once the model is saved as SBML, XitoSBML will visualize the model in 3D space,
	 * and execute a syntax validation for both SBML core and spatial extension by using
	 * SBML online validator.
     * With this plugin, users can create a reaction-diffusion model in spatial SBML format.
	 * @param arg name of the method defined in plugins.config
	 */
	@Override
	public void run(String arg) {
		try {
			document = getDocument();
		} catch (NullPointerException e){
			e.getStackTrace();
			return;
		} catch (Exception e) {
			IJ.error("Error: File is not an SBML Model");
			return;
		}
		
		checkSBMLDocument(document);
		
		addSBases();
		ModelSaver saver = new ModelSaver(document);
		saver.save();
		showDomainStructure();
		GeometryDatas gData = new GeometryDatas(model);
		visualize(gData.getSpImgList());
		
		print();
		
		ModelValidator validator = new ModelValidator(document);
		validator.validate();
	}
}
