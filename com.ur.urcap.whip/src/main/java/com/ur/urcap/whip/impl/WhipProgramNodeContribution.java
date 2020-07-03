package com.ur.urcap.whip.impl;

// Generic includes
import java.util.Collection;
import java.util.Iterator;

import javax.imageio.ImageIO;



// Must includes
import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.domain.URCapAPI;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.value.Pose;
import com.ur.urcap.api.domain.value.Position;
import com.ur.urcap.api.domain.value.Rotation;
import com.ur.urcap.api.ui.component.DivComponent;
import com.ur.urcap.api.ui.component.ImgComponent;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.LabelComponent;
import com.ur.urcap.api.ui.component.InputTextField;
// App specific includes
import com.ur.urcap.api.ui.component.SelectDropDownList;
import com.ur.urcap.api.ui.component.SelectEvent;
import com.ur.urcap.api.ui.annotation.Img;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Select;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.domain.feature.FeaturePlane;
import com.ur.urcap.api.domain.feature.FeaturePoint;


public class WhipProgramNodeContribution implements ProgramNodeContribution {
	
	private final DataModel model;
	private final URCapAPI api;
	
	public WhipProgramNodeContribution(URCapAPI api, DataModel model) {
		this.api = api;
		this.model = model;
	}
	
	private static final String SelectDropDown = "selFeature";
	private static final String IfSelected = "ifselected";
	private static final String Width = "width";
	private static final String Spacing = "spacing";
	private static final String Speed = "speed";
	
	@Img(id="illustrationImg")
	private ImgComponent illustrationImg;
	
	@Label(id="planestatuslabel")
	private LabelComponent planestatuslabel;
	
	
	/*****************************
	 * INPUT HANDLING METHIDS
	 *****************************/
	
	@Input(id="width")
	private InputTextField width;
	
	@Input(id="spacing")
	private InputTextField spacing;
	
	@Input(id="speed")
	private InputTextField speed;
	
	@Input(id="width")
	public void onWidthChange(InputEvent event){
		if(event.getEventType() == InputEvent.EventType.ON_CHANGE){
			// Read double in meters (inputted in mm as string)
			double w = Double.parseDouble(width.getText())/1000;
			setWidth(w);
			System.out.println("Width set to: "+w);
		}
	}
	
	@Input(id="spacing")
	public void onSpacingChange(InputEvent event){
		if(event.getEventType() == InputEvent.EventType.ON_CHANGE){
			// Read double in meters (inputted in mm as string)
			double s = Double.parseDouble(spacing.getText())/1000;
			setSpacing(s);
			System.out.println("Spacing set to: "+s);
		}
	}
	
	@Input(id="speed")
	public void onSpeedChange(InputEvent event){
		if(event.getEventType() == InputEvent.EventType.ON_CHANGE){
			// Read double in meters (inputted in mm as string)
			double s = Double.parseDouble(speed.getText())/1000;
			setSpeed(s);
			System.out.println("Speed set to: "+s);
		}
	}
	
	private boolean isInputsDefined(){
		if( 	getWidth()>0.0 		&&
				getSpacing()>0.0 	&&
				getSpeed()>0.0		){
			return true;
		}
		else{
			return false;
		}
	}
	
	/*****************************
	 * FEATURE SELECTION METHODS
	 *****************************/
	
	private String NoFeatureSelected = "<select>";
	
	@Select(id = "selObject")
	private SelectDropDownList selFeature;

	@Select(id = "selObject")
	public void onSelectChange(SelectEvent event){
		if (event.getEvent() == SelectEvent.EventType.ON_SELECT){
			System.out.println("Selected: "+selFeature.getSelectedItem().toString());
			String selected = selFeature.getSelectedItem().toString();
			
			// Save selected feature in DataModel
			setSelectedFeature(selected);
			
			// If the selected feature is not <select>, 
			// then save that user have chosen a feature
			if(!selected.equals(NoFeatureSelected)){
				setIfFeatureIsSelected(true);
			}
			
		}
	}
	
	/*
	 * Loads the FeaturePlanes and add them to the SelObjects drop down menu
	 * Both defined and undefined FeaturePlanes will be added
	 * The already chosen Feature will persist
	 * No arguments
	 * No return
	 */
	private void ReadFeaturePlanes(){
		// Since adding items to the drop down fires the select event, we should save the value first
		String PreSelected = getSelectedFeature();
		// Used to test if the selected feature have later been removed
		boolean foundPreSelected = false;
		
		// Read the FeaturePlanes
		Collection<FeaturePlane> FP = api.getFeatures().getGeomFeatures(FeaturePlane.class);
		
		int cntFP = FP.size();
		System.out.println("Number of features: "+cntFP);
		if (cntFP>0){
			// Create Iterator object to iterate the feature planes
			Iterator<FeaturePlane> FPitr = FP.iterator();
			
			// First clear the feature list
			selFeature.removeAllItems();
			
			// If user have not yet chosen a feature,
			// add <select> as the first item
			if(!getIfFeatureSelected()){
				selFeature.addItem(NoFeatureSelected);
			}
						
			// Loop as long as there are more features
			while(FPitr.hasNext()){
				// Get next feature object
				FeaturePlane FeatureP = FPitr.next();
				
				// Get the name of the feature 
				String FeatureName = FeatureP.getName();
				
				// Add the feature to the drop down menu
				selFeature.addItem(FeatureName);
				System.out.println("Feature added: "+FeatureName);
				
				if(FeatureName.equals(PreSelected)){
					foundPreSelected = true;
				}
				
				// Note: When a feature is added, the SelectEvent will fire
			}
			
			if(foundPreSelected){
				// Set selected feature back to original one
				selFeature.selectItem(PreSelected);
			}
			else if(!PreSelected.equals(NoFeatureSelected)){
				// The feature chosen was deleted
				selFeature.addItem(NoFeatureSelected);
				selFeature.selectItem(NoFeatureSelected);
				planestatuslabel.setText(PreSelected+" was removed. Select a feature.");
			}
		}
		else {
			// No feature planes, update drop down
			selFeature.addItem(NoFeatureSelected);
		}
	}
	
	/*
	 * Looks for a FeaturePlane in the features list
	 * Argument: FName; String; the name of the feature to look for
	 * Returns: The found feature (FeaturePlane type) or null if not found
	 */
	private FeaturePlane FindFeature(String FName){
		// Read the existing features
		Collection<FeaturePlane> FP = api.getFeatures().getGeomFeatures(FeaturePlane.class);
		System.out.println("Looking for feature: "+FName);

		// Create iterator for iterating through the features
		Iterator<FeaturePlane> FPitr = FP.iterator();
		while(FPitr.hasNext()){
			// Load the next feature
			FeaturePlane FeatureP = FPitr.next();
			// Check if the names are identical  
			if(FName.equals(FeatureP.getName())){
				System.out.println("Returned feature: "+FeatureP.getName());
				return FeatureP;
			}
		}
		System.out.println("Didn't find feature: "+FName);
		return null;
	} 
	
	/*
	 * Tests if the FeaturePlane saved in DataModel is defined
	 * No arguments
	 * Returns: If the feature is defined, boolean
	 */
	private boolean isFeatureDefined(){
		// Read feature saved in DataModel
		String chosenFeature = getSelectedFeature();
		// If the feature is <select> or <No Plane features defined>, return false
		if(	chosenFeature.equals(NoFeatureSelected) ){
			return false;
		}
		else{
			Collection<FeaturePlane> FP = api.getFeatures().getGeomFeatures(FeaturePlane.class);
			Iterator<FeaturePlane> FPitr = FP.iterator();
			while(FPitr.hasNext()){
				FeaturePlane FeatureP = FPitr.next();
				// Check if chosen feature and iterated feature are the same
				if(chosenFeature.equals(FeatureP.getName())){
					// Check if the iterated feature is defined
					if(FeatureP.isDefined()){
						// Name checks out and feature is defined
						System.out.println("Found defined feature: "+FeatureP.getName());
						planestatuslabel.setVisible(false);
						return true;
					}
					// Name check out, but feature is undefined
					System.out.println("Found undefined feature: "+FeatureP.getName());
					planestatuslabel.setVisible(true);
					planestatuslabel.setText(FeatureP.getName()+" is undefined!");
					return false;
				}
			}
			// Didn't find a feature with that name
			System.out.println("Didn't find feature: "+chosenFeature);
			return false;
		}
	}
	
	/*
	 * Updates ProgramNode view with respect to feature selection
	 * Read the FeaturePlanes and selects the chosen one in the drop down
	 * No arguments
	 * No return
	 */
	private void SetupFeatureView(){
		ReadFeaturePlanes();
		// Check if the user has selected a feature
		if (getIfFeatureSelected()){
			String chosenFeature = getSelectedFeature();
			// Set the selected feature in the drop down
			selFeature.selectItem(chosenFeature);
			System.out.println("Loaded chosen feature: "+chosenFeature);
		}
		else{
			System.out.println("No planes chosen, loading possibilities");
		}
	}
	
	/***************************
	 * DataModel SAVE METHODS
	 ***************************/
	
	// String, the name of the selected FeaturePlane
	private void setSelectedFeature(String F){
		model.set(SelectDropDown, F);
		System.out.println("Saved feature in model: "+F);
	}
	private String getSelectedFeature(){
		return model.get(SelectDropDown, NoFeatureSelected);
	}
	
	// Boolean, if the user has selected a feature
	private void setIfFeatureIsSelected(boolean s){
		model.set(IfSelected, s);
		System.out.println("User has seleced a feature: "+s);
	}
	private boolean getIfFeatureSelected(){
		return model.get(IfSelected, false);
	}
	
	// Double, setting of width
	private void setWidth(Double w){
		model.set(Width, w);
	}
	private Double getWidth(){
		return model.get(Width, 0.0);
	}
	
	// Double, setting of spacing
	private void setSpacing(Double s){
		model.set(Spacing, s);
	}
	private Double getSpacing(){
		return model.get(Spacing, 0.0);
	}
		
	// Double, setting of spacing
	private void setSpeed(Double s){
		model.set(Speed, s);
	}
	private Double getSpeed(){
		return model.get(Speed, 0.0);
	}
	
	/************************
	 * OVERWRITTEN METHODS
	 ************************/
	
	@Override
	public void openView() {
		System.out.println("Whip: openView called");
		SetupFeatureView();
		
		// Load the illustration image using Img component
		try{
			illustrationImg.setImage(ImageIO.read(getClass().getResource("/com/ur/urcap/whip/impl/whip_illustration.png")));
		}catch (java.io.IOException e){
			e.printStackTrace();
		}

	}


	@Override
	public void closeView() {
		System.out.println("Whip: closeView called");
	}
	
	@Override
	public boolean isDefined() {
		if (	isFeatureDefined()	&&
				isInputsDefined()	){
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public String getTitle() {
		if(isDefined()){
			return "Whip: "+getSelectedFeature();
		}
		else{
			return "Whip";
		}
	}
	
	@Override
	public void generateScript(ScriptWriter writer) {
		if(isFeatureDefined()){
			// Add this just out of 
			writer.appendLine("# Whip script starts here");
			
			// Read and interpret the Feature Plane
			FeaturePlane myFP = FindFeature(getSelectedFeature());
			
			// Reading point 1 of feature
			// TODO Should be actual Feature and not Point 1
			FeaturePoint FP1 = myFP.getPoint1();
			Pose P1 = FP1.getPose();
			Position Po1 = P1.getPosition();
			Rotation Ro1 = P1.getRotation();
			String Point1 = "p["+Po1.getX()+","+Po1.getY()+","+Po1.getZ()+","+Ro1.getRX()+","+Ro1.getRY()+","+Ro1.getRZ()+"]";
			
			// Reading point 2 of feature
			FeaturePoint FP2 = myFP.getPoint2();
			Pose P2 = FP2.getPose();
			Position Po2 = P2.getPosition();
			Rotation Ro2 = P2.getRotation();
			String Point2 = "p["+Po2.getX()+","+Po2.getY()+","+Po2.getZ()+","+Ro2.getRX()+","+Ro2.getRY()+","+Ro2.getRZ()+"]";
			
			// Calculate blend radius
			Double r = getWidth()/4;
			
			// TODO Would be nice to have point_dist function in API, thus first two lines could be pre-calculated
			writer.assign("w_d", "point_dist("+Point1+","+Point2+")");
			writer.assign("w_n", "ceil(w_d/"+getSpacing().toString()+")*2");
			writer.assign("w_endp", "pose_trans("+Point1+",p[0,w_d,0,0,0,0])");
			writer.appendLine("movel("+Point1+",1,"+getSpeed().toString()+")");
			writer.assign("w_j", "1");
			writer.whileCondition("w_j<w_n");
				writer.assign("w_spr", "pose_trans("+Point1+",p[-("+getWidth().toString()+"/2),(w_d*(w_j/w_n)),0,0,0,0])");
				writer.assign("w_spl", "pose_trans("+Point1+",p["+getWidth().toString()+"/2,(w_d*((w_j*2)/w_n)),0,0,0,0])");
				writer.appendLine("movel(w_spr,1,"+getSpeed().toString()+",r="+r.toString()+")");
				writer.appendLine("movel(w_spl,1,"+getSpeed().toString()+",r="+r.toString()+")");
				writer.assign("w_j", "w_j+2");
			writer.end();
			writer.appendLine("movel(w_endp,1,"+getSpeed().toString()+")");
			
			writer.appendLine("# Whip script ends here");
		}
	}
	
}