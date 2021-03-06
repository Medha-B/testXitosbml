package jp.ac.keio.bio.fun.xitosbml.pane;

import java.util.Vector;
import java.util.HashMap;

import javax.swing.JTable;

import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.ext.spatial.SpatialSpeciesPlugin;
import org.sbml.jsbml.ext.spatial.SpatialModelPlugin;
import org.sbml.jsbml.ext.spatial.SpatialConstants;
import org.sbml.jsbml.ext.spatial.Geometry;
import org.sbml.jsbml.ext.spatial.SampledField;
import ij.gui.MessageDialog;

// TODO: Auto-generated Javadoc

/**
 * The class SpeciesTable, which inherits SBaseTable and implements add() and edit() method for
 * adding and editing species.
 * This class is used in {@link jp.ac.keio.bio.fun.xitosbml.pane.TabTables}.
 * Date Created: Jan 13, 2016
 *
 * @author Kaito Ii &lt;ii@fun.bio.keio.ac.jp&gt;
 * @author Akira Funahashi &lt;funa@bio.keio.ac.jp&gt;
 */

public class SpeciesTable extends SBaseTable{
	
	/** The header. */
        private final String[] header = { "id","compartment","distribution","initial","quantity","Localization from Image"/* added by Morita */,"substanceUnits","hasOnlySubstanceUnits","boundaryCondition","constant"}; 
	
	/** The JTable object. */
	private JTable table;
	
	/** The SpeciesDialog, which generates a GUI for creating / editing Species. */
	private SpeciesDialog sd;
	
	/** The SBML model. */
	private Model model;

        /** The speciesImage. */
        //public HashMap<String,String> speciesImage = new HashMap<String,String>();

  
	/**
	 * Instantiates a new species table.
	 *
	 * @param los the list of species
	 */
	SpeciesTable(ListOf<Species> los){
		this.model = los.getModel();
		list = los;
		setSpeciesToList(los);
		MyTableModel tm = getTableModelWithSpecies();
		table = new JTable(tm);
		setTableProperties(table);
		pane = setTableToScroll("species",table);
	}
	
	/**
	 * Sets the new species to the list.
	 *
	 * @param los the new species to the list
	 */
	private void setSpeciesToList(ListOf<Species> los){
		long max = los.size();
		for(int i = 0; i < max; i++){
			Species s = los.get(i).clone();
			memberList.add(s.clone());
		}
	}
	
	/**
	 * Gets the table model.
	 *
	 * @return the table model with species
	 */
	private MyTableModel getTableModelWithSpecies(){
		int max = memberList.size();
		Object[][] data  = new Object[max][header.length];
		for(int i = 0; i < max; i++){
			Species s = (Species) memberList.get(i);
                        String SFid = s.getId() + "_initialConcentration";//added by Morita
			data[i][0] = s.getId();
			data[i][1] = s.getCompartment();
                        
                        SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial") ;
                        Geometry geometry = spatialplugin.getGeometry();
                        ListOf<SampledField> losf =  geometry.getListOfSampledFields();
                        
                        for(int listsize = 0; listsize < (losf.size() + 1); listsize++ ){
                                if( listsize < losf.size() ){
                                        if( losf.get(listsize).getId().equals( SFid ) ){
                                                if(s.isSetInitialAmount()){
                                                        data[i][2] = "local";
                                                        data[i][3] = "amount";
                                                        data[i][4] = "max:" + String.valueOf(s.getInitialAmount());
                                                } else if(s.isSetInitialConcentration()){
                                                        data[i][2] = "local";
                                                        data[i][3] = "concentration";
                                                        data[i][4] = "max :" + String.valueOf(s.getInitialConcentration());
                                                }                                  
                                                break;
                                        }
                                } else if( listsize == losf.size() ){                                          
                                        if(s.isSetInitialAmount()){
                                                data[i][2] = "uniform";
                                                data[i][3] = "amount";
                                                data[i][4] = String.valueOf(s.getInitialAmount());
                                        } else if(s.isSetInitialConcentration()){
                                                data[i][2] = "uniform";
                                                data[i][3] = "concentration";
                                                data[i][4] = String.valueOf(s.getInitialConcentration());
                                        }
                                }
                        }                                
                        data[i][5] = SFid;
			data[i][6] = s.getSubstanceUnits();
			data[i][7] = s.getHasOnlySubstanceUnits();
			data[i][8] = s.getBoundaryCondition();
			data[i][9] = s.getConstant();
		}
		
		MyTableModel tm = new MyTableModel(data, header) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int Column) {
				switch (Column) {
				case 0: // id
				case 1: // Compartment
                                case 2: // distribution
                                        return String.class;
				case 3: // initial
					return String.class;
				case 4: // quantity
					return String.class;
				case 5: // Localization //***added by Morita
                                        return String.class;
				case 6: // substance unit
				case 7: // hasOnlySubstanceUnits
				case 8: // boundaryCondition
				case 9:	// constant
					return String.class;
				default:
					return String.class;
				}
			}};
		
		tm.setColumnIdentifiers(header);
			
		return tm;
	}

	/**
	 * Converts Species to a Vector. The converted vector will contain species information as follows:
	 * <ul>
	 *     <li>String:Id</li>
	 *     <li>double:initialAmount or initialConcentration</li>
	 *     <li>String:compartment</li>
	 *     <li>Kind:substance units</li>
	 *     <li>boolean:has only substance units</li>
	 *     <li>boolean:boundary condition</li>
	 *     <li>boolean:constant</li>
	 * </ul>
	 *
	 * @param s the JSBML Species object
	 * @return the converted vector
	 */
        private Vector<Object> speciesToVector( Species s, String imagename ){

                Vector<Object> v = new Vector<Object>();
                // id
                v.add(s.getId());
                // compartment
		v.add(s.getCompartment());

                SpatialModelPlugin spatialplugin = (SpatialModelPlugin) model.getPlugin("spatial") ;
                Geometry geometry = spatialplugin.getGeometry();
                ListOf<SampledField> losf =  geometry.getListOfSampledFields();

                String SFid = s.getId() + "_initialConcentration";
                String noimage = "No image";                
                //imagename
                for(int listsize = 0; listsize < (losf.size() + 1); listsize++ ){
                        if( listsize < losf.size() ){
                          if( losf.get(listsize).getId().equals( SFid ) ){
                                  if(s.isSetInitialAmount()){
                                          v.add("local");
                                          v.add("amount");
                                          v.add("max:" + String.valueOf(s.getInitialAmount()));
                                  } else if(s.isSetInitialConcentration()){
                                          v.add("local");
                                          v.add("concentration");
                                          v.add("max :" + String.valueOf(s.getInitialConcentration()));
                                  }                                  
                                  v.add( imagename );
                                  break;
                          }
                        } else if( listsize == losf.size() ){                                          
                                if(s.isSetInitialAmount()){
                                        v.add("uniform");
                                        v.add("amount");
                                        v.add(String.valueOf(s.getInitialAmount()));
                                } else if(s.isSetInitialConcentration()){
                                        v.add("uniform");
                                        v.add("concentration");
                                        v.add(String.valueOf(s.getInitialConcentration()));
                                }
                                v.add( noimage );                           
                        }
                }                                
		v.add(s.getSubstanceUnits());
		v.add(s.getHasOnlySubstanceUnits());
		v.add(s.getBoundaryCondition());
		v.add(s.getConstant());
		
		return v;
	}
	
	/**
	 * Adds the Species object to a table.
	 * This method expects that the SBase object which will be added to a table
	 * should be created / specified through GUI (ex. SpeciesDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#add()
	 *
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	@Override
	void add() throws IllegalArgumentException{
		if(sd == null)
			sd = new SpeciesDialog(model);

                //sd.setHashMap(speciesImage);
                
		Species s = sd.showDialog();
		if(s == null) return;
                
		String SFid = s.getId() + "_initialConcentration";
                String sImage = sd.getSpeciesImage( SFid ).toString();

		memberList.add(s.clone());
		((MyTableModel)table.getModel()).addRow(speciesToVector(s.clone(), sImage));
	}

	/* (non-Javadoc)
	 * @see sbmlplugin.pane.SBaseTable#edit(int)
	 */

	/**
	 * Edits the Species object which is specified by the index.
	 * This method expects that the SBase object which will be edited
	 * should be modified through GUI (ex. SpeciesDialog).
	 * @see jp.ac.keio.bio.fun.xitosbml.pane.SBaseTable#edit(int index)
	 *
	 * @param index the index of the species
	 * @throws IllegalArgumentException the illegal argument exception
	 */
	@Override
	void edit(int index) throws IllegalArgumentException{
		if(index == -1 ) return ;
		if(sd == null)
			sd = new SpeciesDialog(model);

                //sd.setHashMap(speciesImage);

		Species s = sd.showDialog((Species) memberList.get(index)); // s is new input species
		if(s == null) return;

		String SFid = s.getId() + "_initialConcentration";
                String sImage = sd.getSpeciesImage( SFid ).toString();                
		
		memberList.set(index, s);
		// copy contents of Species(JTable) to Species(Model)
                Species sp = (Species) list.getElementBySId(s.getId());
                if( sp == null ){ // TO DO added by morita
                  sp = model.createSpecies(s.getId());
                  sp.setName(s.getName());
                  SpatialSpeciesPlugin ssp = (SpatialSpeciesPlugin) s.getPlugin(SpatialConstants.shortLabel);
                  ssp.setSpatial(true);
                }
                SBMLProcessUtil.copySpeciesContents(s, sp);

		((MyTableModel)table.getModel()).updateRow(index, speciesToVector(s, sImage));
		
	}
}
