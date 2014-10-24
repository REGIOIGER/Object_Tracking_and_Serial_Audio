package trackball.ludobots.com;

import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class Tutorial2Activity extends Activity implements CvCameraViewListener2 {
    private static final String    TAG = "OCVSample::Activity";

    EditText mReception;
    
    private SeekBar h_min = null;
    private SeekBar h_max = null;
    private SeekBar s_min = null;
    private SeekBar s_max = null;
    private SeekBar v_min = null;
    private SeekBar v_max = null;
    
    private TextView tV1 = null;
    private TextView tV2 = null;
    private TextView tV3 = null;
    private TextView tV4 = null;
    private TextView tV5 = null;
    private TextView tV6 = null;
    
    int th;
    int P_xy[] = {0,0};
    int HSV[] = {88,104,125,225,111,208};
    
    int H_MIN = 5;
    int H_MAX = 256;
    int S_MIN = 187;
    int S_MAX = 256;
    int V_MIN = 107;
    int V_MAX = 256;
    
    private static final int       VIEW_MODE_RGBA     = 0;
    private static final int       VIEW_MODE_GRAY     = 1;
    private static final int       VIEW_MODE_CANNY    = 2;
    
    private static final int       VIEW_MODE_THRESH = 3;
    
    private static final int       VIEW_MODE_FEATURES = 5;
    
    private static final int       VIEW_MODE_HPLUS = 6;
    private static final int       VIEW_MODE_HMINUS = 7;
  

    private int                    mViewMode;
    private Mat                    mRgba;
    private Mat                    mIntermediateMat;
    private Mat                    mGray;

    private MenuItem               mItemPreviewThresh;
    
    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGray;
    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;
    
    
    private MenuItem               mItemPreviewHplus;
    private MenuItem               mItemPreviewHminus;
    private MenuItem               mItemPreviewSplus;
    private MenuItem               mItemPreviewSminus;
    private MenuItem               mItemPreviewVplus;
    private MenuItem               mItemPreviewVminus;
    
    
    
    

    private CameraBridgeViewBase   mOpenCvCameraView;

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    //System.loadLibrary("mixed_sample");
                    System.loadLibrary("object_tracking");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public Tutorial2Activity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.tutorial2_surface_view);
        initializeVariables();
        

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial2_activity_surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
        

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
        mItemPreviewGray = menu.add("Preview GRAY");
        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Seguimiento de Objeto");
        mItemPreviewThresh = menu.add("Thresh");
        
        mItemPreviewHplus = menu.add("Modo Threshold");
        mItemPreviewHminus = menu.add("Regresar a RGBA");

        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        final int viewMode = mViewMode;
        switch (viewMode) {
        case VIEW_MODE_GRAY:
            // input frame has gray scale format
            Imgproc.cvtColor(inputFrame.gray(), mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            th=3;
            break;
        case VIEW_MODE_RGBA:
            // input frame has RBGA format
            mRgba = inputFrame.rgba();
            break;
        case VIEW_MODE_CANNY:
            // input frame has gray scale format
            mRgba = inputFrame.rgba();
            Imgproc.Canny(inputFrame.gray(), mIntermediateMat, 80, 100);
            Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            break;
        case VIEW_MODE_FEATURES:
            // input frame has RGBA format
            mRgba = inputFrame.rgba();
            mGray = inputFrame.gray();
            int P_xy[]=ObjectTracking(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), HSV, th);
            Log.i(TAG, P_xy[0]+", ");
            Log.i(TAG, P_xy[1]+"\n");
            
            if(P_xy[0]>399){
            
					Log.i(TAG,"Anda la Osa!\n");
					//e.printStackTrace();
				
            } else{
            
    		    	Log.i(TAG,"Anda la Osa!\n");
    				//e.printStackTrace();
    			      	
            }
            break;
            
        case VIEW_MODE_THRESH:
        	mRgba = inputFrame.rgba();
        	int maxValue = 255;
        	int blockSize = 61;
        	int meanOffset = 15;
        	Imgproc.adaptiveThreshold(inputFrame.gray(), mIntermediateMat, maxValue, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, blockSize, meanOffset);
        	Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
        	break;
        	
        case VIEW_MODE_HPLUS:       	
        	mViewMode = VIEW_MODE_FEATURES;
        	 mRgba = inputFrame.rgba();
             mGray = inputFrame.gray();
             th=1;
             //ObjectTracking(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), H_MAX, H_MIN);
        	break;        	
        case VIEW_MODE_HMINUS:
        	mViewMode = VIEW_MODE_FEATURES;
        	 mRgba = inputFrame.rgba();
             mGray = inputFrame.gray();
             th=0;
             //ObjectTracking(mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), H_MAX, H_MIN);
        	break;
    	
        	
        }
        return mRgba;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        } else if (item == mItemPreviewGray) {
            mViewMode = VIEW_MODE_GRAY;
        } else if (item == mItemPreviewCanny) {
            mViewMode = VIEW_MODE_CANNY;
        } else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
        } else if (item == mItemPreviewThresh) {
        	mViewMode = VIEW_MODE_THRESH;
        } else if (item == mItemPreviewHplus) {
        	mViewMode = VIEW_MODE_HPLUS;
        } else if (item == mItemPreviewHminus) {
        	mViewMode = VIEW_MODE_HMINUS;
        }

        return true;
    }
    
    
    private void initializeVariables() {
    	

    	          h_min = (SeekBar) findViewById(R.id.seekBar1);
    	          tV1 = (TextView) findViewById(R.id.textView1);
    	          h_min.setProgress(H_MIN);
    	          h_min.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	        	  
    	   		  @Override
    	   		  public void onProgressChanged(SeekBar h_min, int progresValue, boolean fromUser) {
    	   			tV1.setText("H_MIN: " + progresValue + "/" + h_min.getMax());
    	   			H_MIN=progresValue;
    	   			HSV[0]=progresValue;
    	    	  }    	   		
    	   		  @Override
    	   		  public void onStartTrackingTouch(SeekBar h_min) {
    	          }    	   		
    	   		  @Override
    	   		  public void onStopTrackingTouch(SeekBar h_min) {
    	   		  }
    	   	      });
    	         
    	          
    	          h_max = (SeekBar) findViewById(R.id.seekBar2);
    	          tV2 = (TextView) findViewById(R.id.textView2);
    	          h_max.setProgress(H_MAX);
    	          h_max.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	     		  
    	          @Override
    	     	  public void onProgressChanged(SeekBar h_max, int progresValue, boolean fromUser) {
    	         	tV2.setText("H_MAX: " + progresValue + "/" + h_max.getMax());
    	         	H_MAX=progresValue;
    	         	HSV[1]=progresValue;
    	    	  }
    	    	  @Override
    	     	  public void onStartTrackingTouch(SeekBar h_max) {
    	          }
    	     	  @Override
    	     	  public void onStopTrackingTouch(SeekBar h_max) {   		 
    	    	  }
    	    	  });
    	          
    	          s_min = (SeekBar) findViewById(R.id.seekBar3);
    	          tV3 = (TextView) findViewById(R.id.textView3);
    	          s_min.setProgress(S_MIN);
    	          s_min.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	        	  
    	   		  @Override
    	   		  public void onProgressChanged(SeekBar s_min, int progresValue, boolean fromUser) {
    	   			tV3.setText("S_MIN: " + progresValue + "/" + s_min.getMax());
    	   			S_MIN=progresValue;
    	   			HSV[2]=progresValue;
    	    	  }    	   		
    	   		  @Override
    	   		  public void onStartTrackingTouch(SeekBar s_min) {
    	          }    	   		
    	   		  @Override
    	   		  public void onStopTrackingTouch(SeekBar s_min) {
    	   		  }
    	   	      });
    	         
    	          
    	          s_max = (SeekBar) findViewById(R.id.seekBar4);
    	          tV4 = (TextView) findViewById(R.id.textView4);
    	          s_max.setProgress(S_MAX);
    	          s_max.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	     		  
    	          @Override
    	     	  public void onProgressChanged(SeekBar s_max, int progresValue, boolean fromUser) {
    	         	tV4.setText("S_MAX: " + progresValue + "/" + s_max.getMax());
    	         	S_MAX=progresValue;
    	         	HSV[3]=progresValue;
    	    	  }
    	    	  @Override
    	     	  public void onStartTrackingTouch(SeekBar s_max) {
    	          }
    	     	  @Override
    	     	  public void onStopTrackingTouch(SeekBar s_max) {   		 
    	    	  }
    	    	  });    	          
    	          
    	          v_min = (SeekBar) findViewById(R.id.seekBar5);
    	          tV5 = (TextView) findViewById(R.id.textView5);
    	          v_min.setProgress(V_MIN);
    	          v_min.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	        	  
    	   		  @Override
    	   		  public void onProgressChanged(SeekBar v_min, int progresValue, boolean fromUser) {
    	   			tV5.setText("V_MIN: " + progresValue + "/" + v_min.getMax());
    	   			V_MIN=progresValue;
    	   			HSV[4]=progresValue;
    	    	  }    	   		
    	   		  @Override
    	   		  public void onStartTrackingTouch(SeekBar v_min) {
    	          }    	   		
    	   		  @Override
    	   		  public void onStopTrackingTouch(SeekBar v_min) {
    	   		  }
    	   	      });
    	         
    	          
    	          v_max = (SeekBar) findViewById(R.id.seekBar6);
    	          tV6 = (TextView) findViewById(R.id.textView6);
    	          v_max.setProgress(V_MAX);
    	          v_max.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    	     		  
    	          @Override
    	     	  public void onProgressChanged(SeekBar v_max, int progresValue, boolean fromUser) {
    	         	tV6.setText("V_MAX: " + progresValue + "/" + v_max.getMax());
    	         	V_MAX=progresValue;
    	         	HSV[5]=progresValue;
    	    	  }
    	    	  @Override
    	     	  public void onStartTrackingTouch(SeekBar v_max) {
    	          }
    	     	  @Override
    	     	  public void onStopTrackingTouch(SeekBar v_max) {   		 
    	    	  }
    	    	  });    	          
    	          
    	          
    	          
    	          
    	          
    	         
    	     }

    
   

    //public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native int[] ObjectTracking(long matAddrGr, long matAddrRgba, int [] hsv_values, int flag);
}
