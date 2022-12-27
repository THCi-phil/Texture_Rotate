package com.pthci.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;

import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.gui.StackWindow;

import ij.measure.ResultsTable;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.Window;


//========================================================
public class WindowPlacement {
//========================================================
	
	private ImageWindow imageWindow                  ;
	private ImageWindow imageWindowReferencePosition ;
	private Dimension   screenSize                   ;
	private Rectangle   imageJcommandWindowBounds    ;
	
	public void initialiseCommandWindowCenterTop() {
		screenSize = new Dimension();
		screenSize = IJ.getScreenSize();   //getHeight()  getWidth()
		
		//set ImageJ window to centre top of screen, otherwise there won't be room for the flood of output images			
		imageJcommandWindowBounds = new Rectangle();
		imageJcommandWindowBounds = IJ.getInstance().getBounds();  // 625 x 110
		//IJ.log("The ImageJ command window is " + imageJcommandWindowBounds.width + " pixels wide and " + imageJcommandWindowBounds.height + "pixels high" );
		
		IJ.getInstance().setLocation( (int)( ( (int)screenSize.getWidth() - (int)imageJcommandWindowBounds.width ) /2 )
		                            , 0
                              );
	} //end public void initialiseCommandWindowCenterTop()
	//-------------------------------------------------

	
	public void setImageScreenLeftBelowCommandWindow(ImagePlus image) {
		//it looks like StackWindow overloads the setLocation method.
		//If you don't explicitly set a stack to StackWindow
		//the call to .setLocation calls the ImageWindow version which redraws without the slice scroll bar
		imageWindow = image.getWindow();
		if( imageWindow == null ) { //if it hasn't been drawn yet
			if( image.getStackSize()>1 ) {
				imageWindow = new StackWindow( image );
			}else{
				imageWindow = new ImageWindow( image );
			}
		}
		//IJ.log("The input data image window is " + imageWindow.getSize().width + " pixels wide and "
    //                                			 + imageWindow.getSize().height + " pixels high"
		//      );

		imageWindow.setLocation( 0
		                       , (int)imageJcommandWindowBounds.height
		                       );

	} //end public void setImageScreenLeftBelowCommandWindow(ImagePlus)
	//-------------------------------------------------------------------------------------------------
	
	
	public void setImageLeftOfCommandWindow(ImagePlus image) {
		//it looks like StackWindow overloads the setLocation method.
		//If you don't explicitly set a stack to StackWindow
		//the call to .setLocation calls the ImageWindow version which redraws without the slice scroll bar
		imageWindow = image.getWindow();
		if( imageWindow == null ) { //if it hasn't been drawn yet
			if( image.getStackSize()>1 ) {
				imageWindow = new StackWindow( image );
			}else{
				imageWindow = new ImageWindow( image );
			}
		}
		//IJ.log("The input data image window is " + imageWindow.getSize().width + " pixels wide and "
    //                                			 + imageWindow.getSize().height + " pixels high"
		//      );
		Point commandWindowLocation = IJ.getInstance().getLocation();
				
		imageWindow.setLocation( (int)commandWindowLocation.getX() - (int)image.getWindow().getSize().width
		                       , (int)commandWindowLocation.getY()
		                       );

	} //end public void setImageScreenLeftBelowCommandWindow(ImagePlus)
	//-------------------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------------------
	public void setImageListedFirstAlignedTopRightOfImageListedSecond( ImagePlus image, ImagePlus imageReferencePosition ) {
		imageWindow = image.getWindow();
		if( imageWindow == null ) { //if it hasn't been drawn yet
			if( image.getStackSize()>1 ) {
				imageWindow = new StackWindow( image );
			}else{
				imageWindow = new ImageWindow( image );
			}
		}
		imageWindowReferencePosition = imageReferencePosition.getWindow();
		//it really, really ought to have been draw already for this call to have been made!
		if( imageWindowReferencePosition == null ) { 
			if( imageReferencePosition.getStackSize()>1 ) {
				imageWindowReferencePosition = new StackWindow( imageReferencePosition );
			}else{
				imageWindowReferencePosition = new ImageWindow( imageReferencePosition );
			}
		}

		imageWindow.setLocation( (int)imageWindowReferencePosition.getBounds().x
		                        +(int)imageWindowReferencePosition.getSize().width
		                       , (int)imageWindowReferencePosition.getBounds().y
		);
	} //end public void setDialogAlignedBottomRightOfImage( GenericDialog, ImagePlus)
	//------------------------------------------------------------------------------------------------------------
	
	
	public void setResultsTableListedFirstAlignedBottomLeftImageListedSecond( ResultsTable rt, ImagePlus image) {
		Window rw = WindowManager.getWindow( rt.getTitle() );
		
		imageWindow = image.getWindow();
		//they really, really ought to have been drawn already for this call to have been made!
		if( (imageWindow == null )||( rw==null) ) { return; };
		
		rw.setLocation( (int)imageWindow.getBounds().x
		              , (int)imageWindow.getBounds().y
		               +(int)imageWindow.getSize().height
		);
		
	} //end public void setResultsTableListedFirstAlignedBottomLeftImageListedSecond( ResultsTable, ImagePlus)
	//------------------------------------------------------------------------------------------------------------
	
	
	//-------------------------------------------------------------------------------------------------
	public void setDialogAlignedTopRightOfImage( GenericDialog gd, ImagePlus image ) {
		if( image.getStackSize()>1 ) {
			imageWindow = new StackWindow( image );
		}else{
			imageWindow = new ImageWindow( image );
		}
		
		gd.setLocation( (int)imageWindow.getBounds().x
		               +(int)imageWindow.getSize().width
		              , (int)imageWindow.getBounds().y
		);
	} //end public void setDialogAlignedBottomRightOfImage( GenericDialog, ImagePlus)
	//-------------------------------------------------------------------------------------------------


	//-------------------------------------------------------------------------------------------------
	public void setDialogAlignedOffsetXYpixelsFromTopRightOfImage( GenericDialog gd, int x, int y, ImagePlus image ) {
		if( image.getStackSize()>1 ) {
			imageWindow = new StackWindow( image );
		}else{
			imageWindow = new ImageWindow( image );
		}
		
		gd.setLocation( (int)imageWindow.getBounds().x
		               +(int)imageWindow.getSize().width
									 + x
		              , (int)imageWindow.getBounds().y
									 + y
		);
	} //end public void setDialogAlignedOffsetXYpixelsFromTopRightOfImage( GenericDialog, int, int, ImagePlus)
	//-------------------------------------------------------------------------------------------------


	public int getMaxAvailableClearScreenHeight() {
		return screenSize.height
		      - (int)imageJcommandWindowBounds.height
				  - 40  //height of windows bar
					;
	} //end public int getMaxAvailableClearScreenHeight()
	//-------------------------------------------------
	
	
//========================================================
} //end public class WindowPlacement
//========================================================

