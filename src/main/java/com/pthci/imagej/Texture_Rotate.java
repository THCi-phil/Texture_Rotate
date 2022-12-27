/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package com.pthci.imagej;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.WindowManager;

import ij.gui.Roi;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;

import ij.plugin.PlugIn;

import ij.process.ByteProcessor;

import ij.measure.ResultsTable;

import java.awt.*;
import java.awt.Window;


/**
 * Example of texture fast rotate.
 *
 * @author Phil Threlfall-Holmes
 */
public class Texture_Rotate implements PlugIn {
	
	//display fields chosen to work OK and display on  2560 x 1440 screen
	private int widthTexture      =  81;  //just chosen to be big enough and not square
	private int heightTexture     =  51;  //to show roatation was working correctly
	
	private int widthWholeField   = 1200; //chosen to be not square and fit easily
	private int heightWholeField  =  900; //on 2560 x 1440 screen
	
	private int width_screenExtentsMimic_onFieldImage  =  800; //border of 200 smaller than whole field
	private int height_screenExtentsMimic_onFieldImage =  500; //so texture definitely fits on even at 45°	
	
	private int x0_screenExtentsMimic_onFieldImage = (int)( ( (double)widthWholeField  - (double)width_screenExtentsMimic_onFieldImage  ) / 2.0 ) ;
	private int y0_screenExtentsMimic_onFieldImage = (int)( ( (double)heightWholeField - (double)height_screenExtentsMimic_onFieldImage ) / 2.0 ) ;
	
	
	//Java defaults to double not float
	//so to avoid lots of casts which won't help the understanding of example code, I am using double even if float would do
	private double inputTextureRotationAngle ;
	private double input_X_CentreTexture     ;
	private double input_Y_CentreTexture     ;	
	
	//this is the class that does the work
	private Rotated_Texture rotated_Texture ;
	

	//this is just an ImageJ construct so it can be interactive
	private GetAngleAndCentre_dialog getAngleAndCentre_dialog ;			
	
	
	//this lot just ImageJ object types to set up the display images
	private ImagePlus imageTextureOriginal          ;
	private ImagePlus imageWholeField_width_No_Clip ;
	private ImagePlus imageWholeField_width_Clipped ;	

	private ByteProcessor bpTextureOriginal         ;
	private ByteProcessor bpWholeField_width_No_Clip;
	private ByteProcessor bpWholeField_width_Clipped;	
	
	private byte[] texturePixelArray; 
	
	private Roi roiScreenExtentsMimic;
	
	
	//this is just for housekeeping so the display is tidy
	private WindowPlacement          windowPlacement          ;



	@Override
	public void run(String arg) {
		windowPlacement = new WindowPlacement();
		windowPlacement.initialiseCommandWindowCenterTop();
		
		initialiseImages_fieldBlankWhite_TextureRamp();		
		
		rotated_Texture = new Rotated_Texture();
		rotated_Texture.initialise();
		
		getAngleAndCentre_dialog = new GetAngleAndCentre_dialog();
		if( !getAngleAndCentre_dialog.initialise() ) return; //exit if dialog cancelled
		
	}   //end @Override	public void run(String)
	//----------------------------------------------------------------------------------------------------


	private void initialiseImages_fieldBlankWhite_TextureRamp() {
		bpTextureOriginal          = new ByteProcessor( widthTexture, heightTexture );
		bpWholeField_width_No_Clip = new ByteProcessor( widthWholeField, heightWholeField );
		bpWholeField_width_Clipped = new ByteProcessor( widthWholeField, heightWholeField );		
		
		fill_RAMP(  bpTextureOriginal );           
		fill_WHITE( bpWholeField_width_No_Clip );
		fill_WHITE( bpWholeField_width_Clipped );
		
		texturePixelArray = (byte[])bpTextureOriginal.getPixels();
		
		imageTextureOriginal = new ImagePlus( "The Texture", bpTextureOriginal );
		imageWholeField_width_No_Clip = new ImagePlus( "The Screen NO edge clipping of texture", bpWholeField_width_No_Clip );
		imageWholeField_width_Clipped = new ImagePlus( "The Screen WITH clipping of texture"   , bpWholeField_width_Clipped );
	
		windowPlacement.setImageLeftOfCommandWindow( imageTextureOriginal );		
		windowPlacement.setImageScreenLeftBelowCommandWindow(                  imageWholeField_width_No_Clip                                );
		windowPlacement.setImageListedFirstAlignedTopRightOfImageListedSecond( imageWholeField_width_Clipped, imageWholeField_width_No_Clip );	
	
		imageTextureOriginal.show();         
		imageWholeField_width_No_Clip.show();
		imageWholeField_width_Clipped.show();
		
		roiScreenExtentsMimic = new Roi( x0_screenExtentsMimic_onFieldImage
		                               , y0_screenExtentsMimic_onFieldImage
		                               , width_screenExtentsMimic_onFieldImage
		                               , height_screenExtentsMimic_onFieldImage
		                               );
		imageWholeField_width_No_Clip.setRoi( roiScreenExtentsMimic );
		imageWholeField_width_Clipped.setRoi( roiScreenExtentsMimic );
		
		
	} //end private void initialiseImages_fieldBlankWhite_TextureRamp()
	//----------------------------------------------------------------------------------------------------
	
	
	private void resetImages() {
		fill_WHITE( bpWholeField_width_No_Clip );
		fill_WHITE( bpWholeField_width_Clipped );
	} //end private void resetImages()
	//-------------------------------------------------------
	
	
	private void fill_WHITE( ByteProcessor bp ) {
		byte[] pixels = (byte[]) bp.getPixels();
		int width     = bp.getWidth() ;
		int height    = bp.getHeight();
		for( int pixelPos=0; pixelPos<(width*height); pixelPos++ ) {
			pixels[pixelPos] = (byte)255 ;
		}
	} //end private void fill_WHITE(ByteProcessor)
  //----------------------------------------------------------------------------------------------------


	private void fill_RAMP( ByteProcessor bp ) {
		byte[] pixels = (byte[]) bp.getPixels();
		int width     = bp.getWidth() ;
		int height    = bp.getHeight();
		
		double greyscaleMinOfRamp = 255.0 ;  //0 black to 255 whote		
		
		for (int i=0; i<width; i++) {
			pixels[i] = (byte)((i*greyscaleMinOfRamp)/width);
		}

		int offset =0;
		int prevRowOffsetIncrement = 0;
		for (int y=1; y<height; y++) {
			prevRowOffsetIncrement = (y-1)*width + 1 ;			
			offset = y*width;
			for (int x=0; x<(width-1); x++) {
				pixels[offset++] = pixels[prevRowOffsetIncrement++] ;
			}
			//last pixel in row to first of previous row
  		pixels[offset++]=pixels[(y-1)*width];
		}

/*	//simple ramp, same each line
		byte[] ramp = new byte[width];
		for (int i=0; i<width; i++)
			ramp[i] = (byte)((i*greyscaleMinOfRamp)/width);
		
		int offset;
		for (int y=0; y<height; y++) {
			offset = y*width;
			for (int x=0; x<(width-1); x++)
				pixels[offset++] = ramp[x+1];
		}
*/
	} //end privatec void fill_RAMP(ByteProcessor)
  //----------------------------------------------------------------------------------------------------


	//===================================================================
	private class GetAngleAndCentre_dialog implements DialogListener {
	//DialogListener is an ImageJ class to allow a modal dialog box to live-update from user input
	//==================================================================	
		
		
		public boolean initialise() {
			String titleString = "Choose centre and rotation angle of texture";
			
			GenericDialog gd = new GenericDialog( titleString );
			gd.addMessage( "Screen mimic on field is "
									+ width_screenExtentsMimic_onFieldImage	+ " pixels wide and "
									+ height_screenExtentsMimic_onFieldImage	+ "pixels high."
			+ "\n"      +"Origin of screen mimic origin on whole field image is [ "
									+ x0_screenExtentsMimic_onFieldImage+ " , " + y0_screenExtentsMimic_onFieldImage + " ]"
			+ "\n"      +"Choose location of centre of texture referenced to the screen mimic"
			);
			gd.addSlider( "x centre of texture :", 1, width_screenExtentsMimic_onFieldImage , (int)width_screenExtentsMimic_onFieldImage /2.0 );
			gd.addSlider( "y centre of texture :", 1, height_screenExtentsMimic_onFieldImage, (int)height_screenExtentsMimic_onFieldImage/2.0 );
			gd.addSlider( "Angle of rotation :"  , 0, 360, 0 );
			
			gd.setCancelLabel("Exit");
			gd.addDialogListener(this);
			gd.showDialog();

			if( gd.wasCanceled() ) {  //OK handled in dialogItemChanged DialogListener
				return false;
			}else{
				return true;
			}
		} //end public boolean initialise()
		//----------------------------------------------------------------


		public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
			input_X_CentreTexture     = gd.getNextNumber();
			input_Y_CentreTexture     = gd.getNextNumber();
			inputTextureRotationAngle = gd.getNextNumber();
			
			//disable the OK button and fast return if user tries to enter out of range numbers
			//allow x and y centres to be outside the screen mimic, but make sure within whole field bounds to avoid overflow
			//0.8 is just a quick and dirty: should allow whole texture to be out of screen mimic even at 45° (gives max extent)
			if( ( input_X_CentreTexture < (                                     -0.8*widthTexture) )
			  ||( input_X_CentreTexture > (width_screenExtentsMimic_onFieldImage+0.8*widthTexture) )
			  ||( input_Y_CentreTexture < (                                      -0.8*heightTexture) )
			  ||( input_Y_CentreTexture > (height_screenExtentsMimic_onFieldImage+0.8*heightTexture) )
			  ) {
				return false;  //disables the OK button
			}
			
			//actions are identical for a live change or OK, so we don't need to trap OK seperately
			
			resetImages(); //just blank them before each new rotation input

			rotated_Texture.run();

			imageWholeField_width_No_Clip.updateAndRepaintWindow();
      imageWholeField_width_Clipped.updateAndRepaintWindow();

			return true;
		} //end public boolean dialogItemChanged(GenericDialog, AWTEvent )
		//----------------------------------------------------------------


	//==================================================================
	} //end private class GetAngleAndCentre_dialog
	//==================================================================


	//==================================================================
	private class Rotated_Texture {
	//==================================================================

		//keep all the true positions precise with doubles
		protected double real_x_pixelIncrementWhenTraversingAlongRow ;
		protected double real_y_pixelIncrementWhenTraversingAlongRow ;
		protected double real_x_pixelIncrementWhenSwitchingToNextRow ;
		protected double real_y_pixelIncrementWhenSwitchingToNextRow ;

		protected double real_x0_onScreenMimic ; //position of texture [X0,Y0] on the screen mimic
		protected double real_y0_onScreenMimic ;
		
		protected double real_x0_onWholeField ; //we only need this for this demo
		protected double real_y0_onWholeField ; //where we show a filed larger than the intended screen
		
		private double cosAngle ;
		private double sinAngle ;

		private double x_onScreenMimic = 0;
		private double y_onScreenMimic = 0;
		
		private double x_onWholeFieldImage = real_x0_onWholeField;
		private double y_onWholeFieldImage = real_y0_onWholeField;
		
		//it's just the final mapping onto the image where we need to round to integer
		private int pixelPos_inWholeFieldPixelArray;	
		private int int_xforX_onWholeFieldImage;
		private int int_yforY_onWholeFieldImage;
		
		//selection of method based on rotation speed and position c.f. the screen
		private TextureAllFourCorners        textureAllFourCorners;
		private FastRotationMethods          fastRotationMethods  ;
		private InterpolationRotationMethods interpolationRotationMethods;
		
		//for rotation speed method selection, not yet implemented
		//protected double lastCallTime ;
		//protected double thisCallTime ;
		//protected double fastSlowSwitchTimeIncrement ;
		
		//these are just tables to dump line results to, for debugging
		ResultsTable rt_rowEnd         ;
		ResultsTable rt_breakPoint     ;
		ResultsTable rt_CornerPositions;
		//-------------------------------------------------------------------------------
		
		
		protected void initialise() {
			textureAllFourCorners = new TextureAllFourCorners();
			textureAllFourCorners.initialise();
			fastRotationMethods          = new FastRotationMethods()         ;
			interpolationRotationMethods = new InterpolationRotationMethods();
			
			//to implement in real C++ code - no point in this ImageJ demonstrator
			//being dialog driven, it will never move fast enough to use the selector
			//lastCallTime = now();
			//Set switch time so that anything under 30Hz, uses the slower but more accurate interpolation method
			//Above this speed, the next draw will be so fast that the user never notices that the fast methods
			//are a bit grainy.
			//For best performance, you really need this time switcher to be set by a public method called from
			//the calling watcher routines, so a slow interpolation draw is called directly done
      //if a rotation hasn't happened for 1/30th of a second
			//and otherwise the slow switch is called
			//fastSlowSwitchTimeIncrement= 1.0 / 30.0 ; //assuming it's a 1.0 = 1 second time base unit
			
			/*Results Tables are just for debugging / code checking in ImageJ
				*additions to them and .show() commented out unless needed.
				*/
			rt_rowEnd         = new ResultsTable();
			rt_breakPoint     = new ResultsTable();
			rt_CornerPositions= new ResultsTable();
		} //end void initialise()
		//------------------------------------------------------------------------------------------
		
		
		protected void run() {
			setCosAndSinJustOnceForThisRotationAngle();
			setXYincrements();
			setOriginInScreenCoordinates();
			//time test here to choose fast or slow (or have different run method for each)
			//don't forget to reset last called time to now after the test
			fastRotationMethods.drawWithoutClipping( bpWholeField_width_No_Clip );
			fastRotationMethods.drawFastAtEdgesByClipping( bpWholeField_width_Clipped );
		} //end protected void run()
		//------------------------------------------------------------------------------------------
		

		private void setCosAndSinJustOnceForThisRotationAngle() {
			cosAngle = Math.cos( Math.toRadians( inputTextureRotationAngle ) );
			sinAngle = Math.sin( Math.toRadians( inputTextureRotationAngle ) );
		} //end private void setCosAndSinJustOnceForThisRotationAngle
		//--------------------------------------------------------------------------------------------


		private void setXYincrements() {
			real_x_pixelIncrementWhenTraversingAlongRow = cosAngle ;
			real_y_pixelIncrementWhenTraversingAlongRow	= sinAngle ;
			real_x_pixelIncrementWhenSwitchingToNextRow = -sinAngle ;  //just swapped!
			real_y_pixelIncrementWhenSwitchingToNextRow = cosAngle ;
		} //end private void setXYincrements()
		//------------------------------------------------------------------- 


		/* the centre of rotation is the middle of the texture
		 * but we want the mapping onto the image to be simple X++ and Y++ from texture origin
		 * So we need to find that reference point to start
		 */
		private void setOriginInScreenCoordinates() {
			double half_width  = widthTexture  / 2.0 ;
			double half_height = heightTexture / 2.0 ;
			
			real_x0_onScreenMimic = input_X_CentreTexture - half_width*cosAngle + half_height*sinAngle ;
			real_y0_onScreenMimic = input_Y_CentreTexture - half_width*sinAngle - half_height*cosAngle ;
			
			real_x0_onWholeField = real_x0_onScreenMimic + x0_screenExtentsMimic_onFieldImage ;
			real_y0_onWholeField = real_y0_onScreenMimic + y0_screenExtentsMimic_onFieldImage;

		} //end private void setOriginInScreenCoordinates()
		//------------------------------------------------------------------- 
		
		
		private boolean hasMovedMoreThanAPixel() {
			//test if topLeftCorner has moved at least a pixel (i.e. round to int)
			//from topLeftCorner_fromLastRotation
			// &&
			// bottomRightCorner
			//If two opposite corners BOTH haven't moved at least a pixel
			//then none of the others in the texture will have, either
			//return false and trap, for a fast return
			//without wasting processor cycles re-drawing the texture in exactly the same place
			
			return true;
		} //end private boolean hasMovedMoreThanAPixel()
		//-------------------------------------------------------------------
		
		
		private void resetAngle0to360deg() {
			//the sine and cosine methods will be just fine with negative or >360° angles
			//but it's a pain when working out what quadrant of orientation the rotated texture is in,
			//needed for the selection of mapping method when there's only one corner on screen.
			//IJ.log("input angle was " + inputTextureRotationAngle + "°" );
			if( inputTextureRotationAngle < 0.0 ) {
				inputTextureRotationAngle = -inputTextureRotationAngle - ( 360.0 * Math.floor( -inputTextureRotationAngle / 360.0 ) );
				//IJ.log("angle reset to " + inputTextureRotationAngle + "°" );
			}else if( inputTextureRotationAngle > 360.0 ) {
				inputTextureRotationAngle = +inputTextureRotationAngle - ( 360.0 * Math.floor( +inputTextureRotationAngle / 360.0 ) );
				//IJ.log("angle reset to " + inputTextureRotationAngle + "°" );
			} //else do nothing
		} //end private void resetAngle0to360deg()
		//------------------------------------------------------------------------------------------		
		
		
		private boolean isOnScreen( double x, double y ) {
			return (  ( x > 0.0 )&&( x < width_screenExtentsMimic_onFieldImage )
			        &&( y > 0.0 )&&( y < height_screenExtentsMimic_onFieldImage )
			       );
		} //end private boolean isOnScreen( double, double )
		//------------------------------------------------------------------- 	
		

		private void showResultsTableResizeToMinWidthMaxHeight( ResultsTable rt
				                                                  , String       windowTitle
				                                                  ) {
			//have to .show() it to start with, for there to be a Window object for the methods we need
			rt.show( windowTitle );
		
			Window rw = WindowManager.getWindow( windowTitle );
		
			//quite messy resizing it so it is only just wide enough for the columns:
			//we can't access the column objects directly: the TextPanel object with the widths in,
			//is only created internally and transiently by the ResultsTable.show() method
			
			//ResultsTable.show() method adds 100 to the width: just use the similar method
			//without the minimum widths, but with the addition of the title	
			//and using FontMetrics to get actual advance of the string, not just number of chars
			//times a constant.
			//Imperfect as the ResultsTable puts some border on column headings
			
			//the algorithm in ResultsTable.show() gets the number of characters
			//IJ.log( rt.getColumnHeadings() + "  length(num chars) = " +  rt.getColumnHeadings().length() );
			//IJ.log( rt.getTitle()          + "  length(num chars) = " +  rt.getTitle().length()          );
			
			//here we do not need to do the rt.getRowAsString(0).length() test
			//as the numbers are always narrower than the descriptive column headings			
			
			//ResultsTable.show() writes with the TextPanel, which uses TextCanvas tc which has the tc.fFont 
			//field, which it isn't clear what is default font: doesn't seem to be set anywhere.
			//TextCanvas.DrawColumnLabels calls Graphics gImage.drawString and Paint calls Graphics gImage.drawChars
			//still no obvious point at which the font is chosen.
			//But the headings look like the SansSerif PLAIN 12 point used in dialog boxes
			Graphics g = rw.getGraphics();
			
			Font        font_WindowTitles        = new Font("SansSerif", Font.PLAIN, 10 );
			FontMetrics fontMetrics_WindowTitles = g.getFontMetrics( font_WindowTitles );
			int         windowTitleWidth         = fontMetrics_WindowTitles.stringWidth( rt.getTitle() );
			
			Font        font_rt_ColumnTitles        = new Font("SansSerif", Font.PLAIN, 12 );
			FontMetrics fontMetrics_rt_ColumnTitles = g.getFontMetrics( font_WindowTitles );
			int         columnHeadingsWidth         = 0;
			for( int i=0; i<rt.getLastColumn()+1 ; i++ ) {
			//IJ.log( "advance of " + rt.getColumnHeading(i) + " is " + fontMetrics_rt_ColumnTitles.stringWidth( rt.getColumnHeading(i) ) );
				columnHeadingsWidth = columnHeadingsWidth + fontMetrics_rt_ColumnTitles.stringWidth( rt.getColumnHeading(i) );
			}
			//IJ.log("columnHeadingsWidth = " + columnHeadingsWidth );
			//Bizararely it's still not right, even if it is the kerned length it's not reporting to screen pixels properly
			//for {y,filament width/pixels,leftEdge/pixels,rightEdge/pixels}, actual measured {7,123,89,98}=317 reported {5,97,70,76}=248
			//which is at least a consistent multiple of 1.28 we can fudge it with 
			columnHeadingsWidth = (int)Math.round( 1.28*columnHeadingsWidth)
			                    + 18 //just a fudge as I'm sick of this
			                    ;
			
			//far from foolproof as the column widths may be enlarged by the values being larger than the heading
			//in our case, that pretty much only happens for the first x and y column, so we add the 32 pixel padding manually
			int spacingBetweenColumnHeadings = 32 + rt.getLastColumn()*17 ;
			int leftAndRightWindowBorder     = 3 + 17 ;
			
			//IJ.log( rt.getColumnHeadings() + "  length(pixels on screen in font) = " +  columnHeadingsWidth );
			//IJ.log( rt.getTitle()          + "  length(pixels on screen in font) = " +  windowTitleWidth    );
			
			Dimension dimension_rw = new Dimension();
			dimension_rw.width  = Math.max( windowTitleWidth
			                               + 137             //width of application icon, and min max close buttons
			                              , columnHeadingsWidth + spacingBetweenColumnHeadings + leftAndRightWindowBorder
																		);
			dimension_rw.height = windowPlacement.getMaxAvailableClearScreenHeight()
			                    - imageWholeField_width_Clipped.getWindow().getSize().height
			                    ;
			rw.setSize( dimension_rw );
		} //end private void showResultsTableResizeToMinWidthMaxHeight( ResultsTable, String )
		//--------------------------------------------------------------------------------------------------------
		
		
		
		//===================================================================
		private class InterpolationRotationMethods  {
		//===================================================================
			//see for example java.awt.geom.AffineTransform for methods
			//will be many C++ libraries that do this, too
		//==================================================================
		} //end private class InterpolationRotationMethods
		//==================================================================
		
		
		
		//===================================================================
		private class FastRotationMethods  {
		//===================================================================
			int posInTexturePixelArray;
	
	
			protected void drawWithoutClipping( ByteProcessor bp ) {
				byte[] pixels = (byte[]) bp.getPixels();
	
				for( int Y=0; Y<heightTexture ; Y++ ) {
					posInTexturePixelArray = Y * widthTexture;
					x_onWholeFieldImage = real_x0_onWholeField + Y * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onWholeFieldImage = real_y0_onWholeField + Y * real_y_pixelIncrementWhenSwitchingToNextRow;
					for( int X=0 ; X<widthTexture ; X++ ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray++;
						//a really optimised fast code would do the ++ in the =texturePixelArray[posInTexturePixelArray++];
						//but it's harder to understand, and a good compiler optimiser will do that anyway
						x_onWholeFieldImage = x_onWholeFieldImage + real_x_pixelIncrementWhenTraversingAlongRow;
						y_onWholeFieldImage = y_onWholeFieldImage + real_y_pixelIncrementWhenTraversingAlongRow;
					} //end for row
				} //end for columns
				
			} //end protected void drawWithoutClipping(ByteProcessor)
			//------------------------------------------------------------------- 
		
		
			protected void drawFastAtEdgesByClipping( ByteProcessor bp ) {
				byte[] pixels = (byte[]) bp.getPixels();
				
				/*Results Tables are just for debugging / code checking in ImageJ
				*additions to them and .show() commented out unless needed.
				*/
				rt_rowEnd.reset();
				rt_breakPoint.reset();
				rt_CornerPositions.reset();
				
				//just get the texture corners relative to the screen to select mapping method
				textureAllFourCorners.setCornersXY();
				textureAllFourCorners.setAreCornersOnScreen();
				//fast return if rotating quite slowly. So slowly that this method is being called 
				//from screen update code, faster than it is worth redrawing it.
				if( !hasMovedMoreThanAPixel() ) return;
				
				selectMethodFromPositionTexturemappedToRelativeToScreenBounds(pixels);
				
				//tidy up: prepare for next rotation call (only used for testing if has moved)
				textureAllFourCorners.resetPreviousRotationCornersToThisRotation();
				
				//rt_rowEnd.show(    "texture positions at row end" );
				//rt_breakPoint.show("broke at"                     );
				showResultsTableResizeToMinWidthMaxHeight( rt_CornerPositions
				                                         , "corner positions"
				                                         );
				windowPlacement.setResultsTableListedFirstAlignedBottomLeftImageListedSecond( rt_CornerPositions, imageWholeField_width_Clipped );
			} //end protected void drawFastAtEdgesByClipping(ByteProcessor)
			//------------------------------------------------------------------- 	
			
			
			private void selectMethodFromPositionTexturemappedToRelativeToScreenBounds(byte[] pixels) {
				//use bitwise truth tests so that can make a readable truth tree
				// [topLeftCorner in image][topRightCorner in image][bottomLeftCorner in image][bottomRightCorner in image]
				// topLeftCorner in image    = 00001000 = 8
				// topRightCorner in image   = 00000100 = 4
				// bottomLeftCorner in image = 00000010 = 2
				// bottomRightCorner         = 00000001 = 1
				//
				// Order of prefererence of selection;
				// 1111 => all corners are in image, can scan fast without a loop break check for if off screen
				//         (fastest as never need to check if pixels are on screen)
				// then
				// 1x1x => at least both left corners on screen => scanFast_fromTopLeftCorner_Xplus_Yplus
				//         (because ++ is just much more intuitive to read and understand the code)
				// then
				// x1x1 => at least both right corners on screen => scanFast_fromTopRightCorner_Xminus_Yplus;
				//         (just as fast as ++ but needs some brain bending to get it right: less intuitive)
				// then
				// 11xx => at least both top corners on screen => scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop
				//         (but actually only case 12=1100 remains after first three filters)
				//         (slower as Y increment inner loop is Y+widthTexture rather than Y++)
				// then
				// xx11 => at least both bottom corners on screen => scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop
				//         (but actually only case 3=0011 remains after above filters)
				//         (same speed as above, but Yminus is less intuitive code
				// then the "one corner only" cases
				// finally the no corners in case
				//          which are more complicated becasue the selection of algorithm is position and orientation specific
				//          Do additional tests only if these special cases are called.
	
				//15=1111 all corners on screen inside listed first, as this is the most frequent case and we want to jump straight to it
				//The others listed in descending order to make the code more readable. 0000 certianly will be the least likely
				//case, but it's less easy to list the others in descending order of frequency of calling, the speeed payoff
				//will be trivial, and any other order would make the code much harder to understand and maintain.
				switch (textureAllFourCorners.cornersInImageMap) {
					case 15 : { // 1111 all four corners are in image (normal case) no need for break test in pixel mapping loop
										scanFast_fromTopLeftCorner_Xplus_Yplus_NO_BREAK(pixels);
										}
										break;
					case 14 : { /* 1110 */  scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);	              }
										break;
					case 13 : { /* 1101 */  scanFast_fromTopRightCorner_Xminus_Yplus(pixels);             }
										break;
					case 12 : { /* 1100 */  scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(pixels);    }
										break;
					case 11 : { /* 1011 */  scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);               }
										break;
					case 10 : { /* 1010 */  scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);               }
										break;
					case  9 : { /* 1001 not feasible do nothing */                                        }
					          // infeasible unless the texture is wide compared to screen height or high c.f. screen width
										break;
					case  8 : { /* 1000 */  onlyTopLeftCornerInImage(pixels);                             }
										break;
					case  7 : { /* 0111 */  scanFast_fromTopRightCorner_Xminus_Yplus(pixels);             }
										break;
					case  6 : { /* 0110 not feasible do nothing */                                        }
										// infeasible unless the texture is wide compared to screen height or high c.f. screen width
										break;
					case  5 : { /* 0101 */  scanFast_fromTopRightCorner_Xminus_Yplus(pixels);             }
										break;
					case  4 : { /* 0100 */ 	onlyTopRightCornerInImage(pixels);                            }
										break;
					case  3 : { /* 0011 */  scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop(pixels);}
										break;
					case  2 : { /* 0010 */  onlyBottomLeftCornerInImage(pixels);                          }
										break;
					case  1 : { /* 0001 */  onlyBottomRightCornerInImage(pixels);                         }
										break;
					case  0 : { /* 0000 */  noCornersOfTextureInimage(pixels);                            }
										break;
				} //end switch case
			} //end private void selectMethodFromPositionTexturemappedToRelativeToScreenBounds(byte[])
			//------------------------------------------------------------------- -------------------------------------------------
			
			
			private void scanFast_fromTopLeftCorner_Xplus_Yplus_NO_BREAK(byte[] pixels) {
				//IJ.log("scanFast_fromTopLeftCorner_Xplus_Yplus_NO_BREAK");
				for( int Y=0; Y<heightTexture ; Y++ ) {
					posInTexturePixelArray = Y * widthTexture;
					//don't actually use the onScreenMimic variables in this demo routine
					//x_onScreenMimic = real_x0_onScreenMimic + Y * real_x_pixelIncrementWhenSwitchingToNextRow;
					//y_onScreenMimic = real_y0_onScreenMimic + Y * real_y_pixelIncrementWhenSwitchingToNextRow;
					x_onWholeFieldImage = real_x0_onWholeField + Y * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onWholeFieldImage = real_y0_onWholeField + Y * real_y_pixelIncrementWhenSwitchingToNextRow;
					for( int X=0 ; X<widthTexture ; X++ ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray++;
						//x_onScreenMimic = x_onScreenMimic + real_x_pixelIncrementWhenTraversingAlongRow;
						//y_onScreenMimic = y_onScreenMimic + real_y_pixelIncrementWhenTraversingAlongRow;
						x_onWholeFieldImage = x_onWholeFieldImage + real_x_pixelIncrementWhenTraversingAlongRow;
						y_onWholeFieldImage = y_onWholeFieldImage + real_y_pixelIncrementWhenTraversingAlongRow;
					} //end for row
				} //end for columns
			} //end private void scanFast_fromTopLeftCorner_Xplus_Yplus(byte[])
			//------------------------------------------------------------------- 
			
			
			private void scanFast_fromTopLeftCorner_Xplus_Yplus(byte[] pixels) {
				//IJ.log("scanFast_fromTopLeftCorner_Xplus_Yplus");
				for( int Y=0; Y<heightTexture ; Y++ ) {
					posInTexturePixelArray = Y * widthTexture;
					x_onScreenMimic = real_x0_onScreenMimic + Y * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onScreenMimic = real_y0_onScreenMimic + Y * real_y_pixelIncrementWhenSwitchingToNextRow;
					//could be faster by just testing in the direction you are going, rather than all four screen sides
					if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
						//rt_breakPoint.addValue("texture Y"      ,       Y         );
						//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
						//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
						continue; //go to next Y, not jump out of the loop altogether
					}
					x_onWholeFieldImage = real_x0_onWholeField + Y * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onWholeFieldImage = real_y0_onWholeField + Y * real_y_pixelIncrementWhenSwitchingToNextRow;
					for( int X=0 ; X<widthTexture ; X++ ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray++;
						x_onScreenMimic = x_onScreenMimic + real_x_pixelIncrementWhenTraversingAlongRow;
						y_onScreenMimic = y_onScreenMimic + real_y_pixelIncrementWhenTraversingAlongRow;
						x_onWholeFieldImage = x_onWholeFieldImage + real_x_pixelIncrementWhenTraversingAlongRow;
						y_onWholeFieldImage = y_onWholeFieldImage + real_y_pixelIncrementWhenTraversingAlongRow;
						//could be faster by just testing in the direction you are going, rather than all four screen sides
						if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
							//rt_breakPoint.addValue("texture X"      ,       X         );
							//rt_breakPoint.addValue("texture Y"      ,       Y         );
							//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
							//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
							break; //jump out of X loop into next Y
						}
					} //end for row
					//rt_rowEnd.incrementCounter();
					//rt_rowEnd.addValue("texture Y"      ,       Y         );
					//rt_rowEnd.addValue("x_onScreenMimic", x_onScreenMimic );
					//rt_rowEnd.addValue("y_onScreenMimic", y_onScreenMimic );
				} //end for columns
			} //end private void scanFast_fromTopLeftCorner_Xplus_Yplus(byte[])
			//------------------------------------------------------------------- 
		
		
			private void scanFast_fromTopRightCorner_Xminus_Yplus(byte[] pixels) {
				//IJ.log("scanFast_fromTopRightCorner_Xminus_Yplus");
				for( int Y=0; Y<heightTexture ; Y++ ) {
					posInTexturePixelArray = (Y+1) * widthTexture -1;
					x_onScreenMimic = real_x0_onScreenMimic + Y * real_x_pixelIncrementWhenSwitchingToNextRow + widthTexture*real_x_pixelIncrementWhenTraversingAlongRow;
					y_onScreenMimic = real_y0_onScreenMimic + Y * real_y_pixelIncrementWhenSwitchingToNextRow + widthTexture*real_y_pixelIncrementWhenTraversingAlongRow;
					//could be faster by just testing in the direction you are going, rather than all four screen sides
					if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
						//rt_breakPoint.addValue("texture Y"      ,       Y         );
						//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
						//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
						continue; //go to next Y, not jump out of the loop altogether
					}
					x_onWholeFieldImage = real_x0_onWholeField + Y * real_x_pixelIncrementWhenSwitchingToNextRow + widthTexture*real_x_pixelIncrementWhenTraversingAlongRow;
					y_onWholeFieldImage = real_y0_onWholeField + Y * real_y_pixelIncrementWhenSwitchingToNextRow + widthTexture*real_y_pixelIncrementWhenTraversingAlongRow;
					for( int X=widthTexture-1 ; X>-1 ; X-- ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray--;
						x_onScreenMimic = x_onScreenMimic - real_x_pixelIncrementWhenTraversingAlongRow;
						y_onScreenMimic = y_onScreenMimic - real_y_pixelIncrementWhenTraversingAlongRow;
						x_onWholeFieldImage = x_onWholeFieldImage - real_x_pixelIncrementWhenTraversingAlongRow;
						y_onWholeFieldImage = y_onWholeFieldImage - real_y_pixelIncrementWhenTraversingAlongRow;
						//could be faster by just testing in the direction you are going, rather than all four screen sides
						if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
							//rt_breakPoint.addValue("texture X"      ,       X         );
							//rt_breakPoint.addValue("texture Y"      ,       Y         );
							//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
							//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
							break; //jump out of X loop into next Y
						}
					} //end for row
					//rt_rowEnd.incrementCounter();
					//rt_rowEnd.addValue("texture Y"      ,       Y         );
					//rt_rowEnd.addValue("x_onScreenMimic", x_onScreenMimic );
					//rt_rowEnd.addValue("y_onScreenMimic", y_onScreenMimic );
				} //end for columns
			} //end private void scanFast_fromTopRightCorner_Xminus_Yplus(byte[])
			//------------------------------------------------------------------- 		
			
			
			private void scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(byte[] pixels) {
				//IJ.log("scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop");
				//If this was called a lot, it would be quicker to initialise an x-y swapped
				//second version of the texture, so could still ++ or -- through the pixel array
				//But, that would be quite messy to set up with the current structure
				//and hard to get your head around the transformed axes - and origin
				//Just not worth it for what is a single unusual case
				//Just accept +width increment is marginally slower than ++
				
				for( int X=0 ; X<widthTexture ; X++ ) {
					posInTexturePixelArray = X ;					
					//Y is always 0 for reset of loop, so can cut out the +Y * real_x_pixelIncrementWhenSwitchingToNextRow
					x_onScreenMimic = real_x0_onScreenMimic + X * real_x_pixelIncrementWhenTraversingAlongRow;
					y_onScreenMimic = real_y0_onScreenMimic + X * real_y_pixelIncrementWhenTraversingAlongRow;
					if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
						//rt_breakPoint.addValue("texture Y"      ,       Y         );
						//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
						//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
						continue; //go to next X, not jump out of the loop altogether
					}
					x_onWholeFieldImage = real_x0_onWholeField + X * real_x_pixelIncrementWhenTraversingAlongRow;
					y_onWholeFieldImage = real_y0_onWholeField + X * real_y_pixelIncrementWhenTraversingAlongRow;
					for( int Y=0; Y<heightTexture ; Y++ ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray += widthTexture;
						x_onScreenMimic = x_onScreenMimic + real_x_pixelIncrementWhenSwitchingToNextRow;
						y_onScreenMimic = y_onScreenMimic + real_y_pixelIncrementWhenSwitchingToNextRow;
						x_onWholeFieldImage = x_onWholeFieldImage + real_x_pixelIncrementWhenSwitchingToNextRow;
						y_onWholeFieldImage = y_onWholeFieldImage + real_y_pixelIncrementWhenSwitchingToNextRow;
						if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
							//rt_breakPoint.addValue("texture X"      ,       X         );
							//rt_breakPoint.addValue("texture Y"      ,       Y         );
							//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
							//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
							break; //jump out of Y loop into next X
						}
					} //end for column
					//rt_rowEnd.incrementCounter();
					//rt_rowEnd.addValue("texture Y"      ,       Y         );
					//rt_rowEnd.addValue("x_onScreenMimic", x_onScreenMimic );
					//rt_rowEnd.addValue("y_onScreenMimic", y_onScreenMimic );
				} //end for row	
			} //end private void scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(byte[] )
			//------------------------------------------------------------------- 	
			
			
			private void scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop(byte[] pixels) {
				IJ.log("scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop");
				//see intro comments in scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop
				
				int offsetEachColumn = (heightTexture-1)*widthTexture ;
				for( int X=0 ; X<widthTexture ; X++ ) {
					posInTexturePixelArray = X + offsetEachColumn ;					
					//Y is always (heightTexture-1) for reset of loop
					x_onScreenMimic = real_x0_onScreenMimic + X * real_x_pixelIncrementWhenTraversingAlongRow + (heightTexture-1) * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onScreenMimic = real_y0_onScreenMimic + X * real_y_pixelIncrementWhenTraversingAlongRow + (heightTexture-1) * real_y_pixelIncrementWhenSwitchingToNextRow;
					if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
						//rt_breakPoint.addValue("texture Y"      ,       Y         );
						//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
						//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
						continue; //go to next X, not jump out of the loop altogether
					}
					x_onWholeFieldImage = real_x0_onWholeField + X * real_x_pixelIncrementWhenTraversingAlongRow + (heightTexture-1) * real_x_pixelIncrementWhenSwitchingToNextRow;
					y_onWholeFieldImage = real_y0_onWholeField + X * real_y_pixelIncrementWhenTraversingAlongRow + (heightTexture-1) * real_y_pixelIncrementWhenSwitchingToNextRow;
					for( int Y=(heightTexture-1); Y>-1 ; Y-- ) {
						int_xforX_onWholeFieldImage = (int)Math.round( x_onWholeFieldImage ) ;
						int_yforY_onWholeFieldImage = (int)Math.round( y_onWholeFieldImage ) ;
						pixelPos_inWholeFieldPixelArray = int_xforX_onWholeFieldImage + int_yforY_onWholeFieldImage * widthWholeField ;
						pixels[ pixelPos_inWholeFieldPixelArray ] = texturePixelArray[posInTexturePixelArray];
						posInTexturePixelArray -= widthTexture;
						x_onScreenMimic = x_onScreenMimic - real_x_pixelIncrementWhenSwitchingToNextRow;
						y_onScreenMimic = y_onScreenMimic - real_y_pixelIncrementWhenSwitchingToNextRow;
						x_onWholeFieldImage = x_onWholeFieldImage - real_x_pixelIncrementWhenSwitchingToNextRow;
						y_onWholeFieldImage = y_onWholeFieldImage - real_y_pixelIncrementWhenSwitchingToNextRow;
						if( !isOnScreen( x_onScreenMimic, y_onScreenMimic ) ) {
							//rt_breakPoint.addValue("texture X"      ,       X         );
							//rt_breakPoint.addValue("texture Y"      ,       Y         );
							//rt_breakPoint.addValue("x_onScreenMimic", x_onScreenMimic );
							//rt_breakPoint.addValue("y_onScreenMimic", y_onScreenMimic );
							break; //jump out of Y loop into next X
						}
					} //end for column
					//rt_rowEnd.incrementCounter();
					//rt_rowEnd.addValue("texture Y"      ,       Y         );
					//rt_rowEnd.addValue("x_onScreenMimic", x_onScreenMimic );
					//rt_rowEnd.addValue("y_onScreenMimic", y_onScreenMimic );
				} //end for row
			} //end private void scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop(byte[])
			//------------------------------------------------------------------- 	
			
			
			private void onlyTopLeftCornerInImage(byte[] pixels) {
				//IJ.log("just top left corner in image");
				//can afford the processor cycles for more input checks.  As by definition
				//we won't be plotting more than a quarter of the pixels in the texture.
				//So the time saving in input checks more than compensates in increased speed.
				resetAngle0to360deg();
				if(      inputTextureRotationAngle <  90 ) {  // >=  0
					if( textureAllFourCorners.topLeftCorner.real_y_onScreenMimic > (height_screenExtentsMimic_onFieldImage/2.0) ) {
						scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);
					}else{
						scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(pixels);
					}
				}else if( inputTextureRotationAngle < 180 ) { // >= 90
					if( textureAllFourCorners.topLeftCorner.real_y_onScreenMimic > (height_screenExtentsMimic_onFieldImage/2.0) ) {
						scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);
					}else{
						scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(pixels);
					}
				}else if( inputTextureRotationAngle < 270 ) { // >=180
					if( textureAllFourCorners.topLeftCorner.real_x_onScreenMimic < (width_screenExtentsMimic_onFieldImage/2.0) ) {
						scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);
					}else{
						scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(pixels);
					}
				}else{ //must be in 4th quadrant >=270   <360
					if( textureAllFourCorners.topLeftCorner.real_y_onScreenMimic < (height_screenExtentsMimic_onFieldImage/2.0) ) {
						scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);
					}else{
						scanFast_fromTopLeftCorner_Yplus_inXplusOuterLoop(pixels);
					}
				}
			} //end private void onlyTopLeftCornerInImage(byte[])
			//------------------------------------------------------------------- -------------------------------------------------
			
			
			private void onlyTopRightCornerInImage(byte[] pixels) {
				IJ.log("just top right corner in image");
				resetAngle0to360deg();
			} //end private void onlyTopRightCornerInImage(byte[])
			//------------------------------------------------------------------- -------------------------------------------------
			
			
			private void onlyBottomLeftCornerInImage(byte[] pixels) {
				IJ.log("just bottom left corner in image");
				resetAngle0to360deg();
			} //end private void onlyBottomLeftCornerInImage(byte[])
			//------------------------------------------------------------------- -------------------------------------------------
			
			
			private void onlyBottomRightCornerInImage(byte[] pixels) {
				IJ.log("just bottom right corner in image");
				resetAngle0to360deg();
			} //end private void onlyBottomRightCornerInImage(byte[])
			//------------------------------------------------------------------- -------------------------------------------------
			
			
			private void noCornersOfTextureInimage(byte[] pixels) {
				IJ.log("no corners in image");
				//an if tree is messy but still the most easily understood way through the logic to select between the 16 cases
				if(      inputTextureRotationAngle <  90 ) {  // >=  0
					IJ.log("in first quadrant");
					if( textureAllFourCorners.pointsStraddleScreenBottomRight_Q13( textureAllFourCorners.topLeftCorner ) ) {
						IJ.log("straddles screen bottom right");
						scanFast_fromTopLeftCorner_Xplus_Yplus(pixels);
					}else{
						//see drawing page 11 of notes
						//if staddle bottom left corner is same method as [11xx]
						//if straddle top left corner of image is scanFast_fromTopRightCorner_Xminus_Yplus
						//if staddle top right corner is scanFast_fromBottomLeftCorner_Yminus_inXplusOuterLoop
					}
				//...............................................................................................................
				}else if( inputTextureRotationAngle < 180 ) { // >= 90
					//etc body
				//...............................................................................................................
				}else if( inputTextureRotationAngle < 270 ) { // >=180
					//etc body
				//...............................................................................................................
				}else{ //must be in 4th quadrant >=270   <360
					//etc body
				}
				//...............................................................................................................
				//if none have been selected, the texture is completely off the screen and not etested at all
				
				
			} //end private void noCornersOfTextureInimage(byte[])
			//------------------------------------------------------------------- -------------------------------------------------		
		
		
		//==================================================================
		} //end private class FastRotationMethods
		//==================================================================
		

		
		//==================================================================
		private class TextureAllFourCorners {
		//==================================================================
			protected Texture_Corner topLeftCorner     ; 
			protected Texture_Corner topRightCorner    ;
			protected Texture_Corner bottomLeftCorner  ;
			protected Texture_Corner bottomRightCorner ;
			protected int cornersInImageMap ;  //possible uint8_t or even uint4_t efficiently in C++
			protected Texture_Corner topLeftCorner_fromLastRotation ;
			protected Texture_Corner bottomRightCorner_fromLastRotation ;
			
			
			protected void initialise() {
				topLeftCorner     = new Texture_Corner();
				topRightCorner    = new Texture_Corner();
				bottomLeftCorner  = new Texture_Corner();
				bottomRightCorner = new Texture_Corner();
			} //end protected void initialise()
			//-------------------------------------------------------------------
		
		
			protected void setCornersXY( ) {
				topLeftCorner.setXY( real_x0_onScreenMimic
													 , real_y0_onScreenMimic
													 );
				topRightCorner.setXY( real_x0_onScreenMimic + widthTexture * real_x_pixelIncrementWhenTraversingAlongRow
														, real_y0_onScreenMimic + widthTexture * real_y_pixelIncrementWhenTraversingAlongRow
														);
				bottomLeftCorner.setXY( real_x0_onScreenMimic + heightTexture * real_x_pixelIncrementWhenSwitchingToNextRow
															, real_y0_onScreenMimic + heightTexture * real_y_pixelIncrementWhenSwitchingToNextRow
															);
				bottomRightCorner.setXY( bottomLeftCorner.real_x_onScreenMimic + widthTexture * real_x_pixelIncrementWhenTraversingAlongRow
															 , bottomLeftCorner.real_y_onScreenMimic + widthTexture * real_y_pixelIncrementWhenTraversingAlongRow
															 );
			} //end protected void setCornersXY()
			//-------------------------------------------------------------------
			
			
			protected void setAreCornersOnScreen() {
				topLeftCorner.setIsCornerOnScreen()     ;
				topRightCorner.setIsCornerOnScreen()    ;
				bottomLeftCorner.setIsCornerOnScreen()  ;
				bottomRightCorner.setIsCornerOnScreen() ;
				cornersInImageMap = (topLeftCorner.isWithinScreenBounds    ? 8 : 0 )
				                  | (topRightCorner.isWithinScreenBounds   ? 4 : 0 )
			                    | (bottomLeftCorner.isWithinScreenBounds ? 2 : 0 )
				                  | (bottomRightCorner.isWithinScreenBounds? 1 : 0 )
				                  ;
				//rt_CornerPositions.incrementCounter();
				//rt_CornerPositions.addValue("topLeft"          , (topLeftCorner.isWithinScreenBounds    ? 8 : 0 ) );
				//rt_CornerPositions.addValue("topRight"         , (topRightCorner.isWithinScreenBounds   ? 4 : 0 ) );
				//rt_CornerPositions.addValue("bottomLeft"       , (bottomLeftCorner.isWithinScreenBounds ? 2 : 0 ) );
				//rt_CornerPositions.addValue("bottomRight"      , (bottomRightCorner.isWithinScreenBounds? 1 : 0 ) );
				//rt_CornerPositions.addValue("cornersInImageMap", cornersInImageMap                      );
			} //end protected void setAreCornersOnScreen()
			//-------------------------------------------------------------------
			
			
			protected void resetPreviousRotationCornersToThisRotation() {
				topLeftCorner_fromLastRotation      = topLeftCorner     ; 
				bottomRightCorner_fromLastRotation  = bottomRightCorner ;
			} //end protected void resetPreviousRotationCornersToThisRotation()
			//-------------------------------------------------------------------
			
			
			//truly return the staddle (c.f. 2 Texture_Corner argument version following)
			//but a pain as need multiple versions (or a boolean argument for * or / tan)
			private boolean pointsStraddleScreenBottomRight_Q13( Texture_Corner tc ) {
				double Xs = tc.real_x_onScreenMimic - width_screenExtentsMimic_onFieldImage ;
				double Ys = Xs / Math.tan( Math.toRadians( inputTextureRotationAngle ) );
				double testDebugForLogWindow = tc.real_y_onScreenMimic + Ys ;
				IJ.log("Xs = " + Xs + " , Ys = " + Ys + "(tc.real_y_onScreenMimic + Ys)= " + testDebugForLogWindow );
				return( (Xs > 0)
				      &&( (tc.real_y_onScreenMimic + Ys) < height_screenExtentsMimic_onFieldImage)
				      );
			} //end private boolean pointsStraddleScreenBottomRight( Texture_Corner, Texture_Corner )
			//-----------------------------------------------------------------------------------------------------------
			
			
			private boolean pointsStraddleScreenBottomRight_Q24( Texture_Corner tc ) {
				double Xs = tc.real_x_onScreenMimic - width_screenExtentsMimic_onFieldImage ;
				double Ys = Xs * Math.tan( Math.toRadians( inputTextureRotationAngle ) );
				return( (Xs > 0)
				      &&( (tc.real_y_onScreenMimic + Ys) < height_screenExtentsMimic_onFieldImage)
				      );
			} //end private boolean pointsStraddleScreenBottomRight( Texture_Corner, Texture_Corner )
			//-----------------------------------------------------------------------------------------------------------
			
			
			private boolean pointsStraddleScreenBottomRight( Texture_Corner tc, boolean isTextureRotationAngleInQuadrant1or3 ) {
				double Xs = tc.real_x_onScreenMimic - width_screenExtentsMimic_onFieldImage ;
				double Ys;
				if( isTextureRotationAngleInQuadrant1or3 ) {
					Ys = Xs / Math.tan( Math.toRadians( inputTextureRotationAngle ) );
				}else{
					Ys = Xs * Math.tan( Math.toRadians( inputTextureRotationAngle ) );
				}
				return( (Xs > 0)
				      &&( (tc.real_y_onScreenMimic + Ys) < height_screenExtentsMimic_onFieldImage)
				      );
			} //end private boolean pointsStraddleScreenBottomRight( Texture_Corner, Texture_Corner )
			//-----------------------------------------------------------------------------------------------------------
			
			
			//this method is much faster to implement, but computationally wasteful
			//as it returns true for straddling, until BOTH tcA and tcB are clear of the image bounds
			//the line between them will have cleared the screen corner some time ago
			private boolean pointsStraddleScreenBottomRight( Texture_Corner tcA, Texture_Corner tcB ) {
				return( ( tcA.real_x_onScreenMimic > width_screenExtentsMimic_onFieldImage  )
				      &&( tcA.real_y_onScreenMimic < height_screenExtentsMimic_onFieldImage )
				      &&( tcB.real_x_onScreenMimic < width_screenExtentsMimic_onFieldImage )
				      &&( tcB.real_y_onScreenMimic > height_screenExtentsMimic_onFieldImage )
				      );
			} //end private boolean pointsStraddleScreenBottomRight( Texture_Corner, Texture_Corner )
			//-----------------------------------------------------------------------------------------------------------		
		
		
		
		
		//==================================================================
		} //end private class TextureAllFourCorners
		//==================================================================
	
	
	
		//==================================================================
		private class Texture_Corner {
		//==================================================================
			protected double real_x_onScreenMimic ;
			protected double real_y_onScreenMimic ;
			protected boolean isWithinScreenBounds ;
		
		
			protected void setXY( double x, double y ) {
				real_x_onScreenMimic = x ;
				real_y_onScreenMimic = y ;
			} //end protected void setXY( double, double )
			//------------------------------------------------------
		
		
			protected void setIsCornerOnScreen() {
				isWithinScreenBounds = isOnScreen( real_x_onScreenMimic, real_y_onScreenMimic );
			} //end protected void setIsCornerOnScreen()
			//------------------------------------------------------
		
		
		//==================================================================
		} //end private class Texture_Corner
		//==================================================================		
		
		

	//==================================================================
	} //end private class Rotated_Texture
	//==================================================================



/*=================================================================================*/

	/**
	 * Main method for debugging.
	 *
	 * For debugging, it is convenient to have a method that starts ImageJ, loads
	 * an image and calls the plugin, e.g. after setting breakpoints.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) throws Exception {
		Class<?> clazz = Texture_Rotate.class;
		new ImageJ();
		IJ.runPlugIn(clazz.getName(), "");
	}  //end public static void main(String[] args)
  
/*=================================================================================*/
  
}  //end public class Texture_Rotate
//========================================================================================
//                         end public class Texture_Rotate
//========================================================================================
