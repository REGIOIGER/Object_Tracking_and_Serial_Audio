package trackball.ludobots.com;

import java.io.IOException;
import java.util.Arrays;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.imgproc.Imgproc;

import trackball.ludobots.com.AudioSerialOutMono;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
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
    static public final char cr = (char) 13; // because i don't want to type that in every time
    EditText mReception;
    
    //******************************************************************
    //Parte del audio
    
    private  static byte generatedSnd[] = null;

	// set that can be edited externally
	public static int max_sampleRate = 48000;
	public static int min_sampleRate = 4000;
	public static int new_baudRate = 1200; // assumes N,8,1 right now
	public static int new_sampleRate = 48000; // min 4000 max 48000 
	public static int new_characterdelay = 0; // in audio frames, so depends on the sample rate. Useful to work with some microcontrollers.
	public static boolean new_levelflip = true;
	public static String prefix="";
	public static String postfix="";

	// set that is actually used: this is so they get upadted all in one go (safer)
	private static int baudRate = 1200;
	private static int sampleRate = 48000;
	private static int characterdelay = 02;
	private static boolean levelflip = true;
	
	private static String logtag = "AudioApp";//for use as the tag when logging 
	
    Thread t;
    int sr = 44100;
    int flag=0;
    boolean isRunning = true;
    
    //******************************************************************
    
    
    
    
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
    int P_xy[] = {0,0,0};
    int HSV[] = {10,81,0,256,0,256};
    
    int H_MIN = 10;
    int H_MAX = 81;
    int S_MIN = 0;
    int S_MAX = 256;
    int V_MIN = 0;
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
        
        UpdateParameters(true);
        // start a new thread to synthesise audio
        t = new Thread() {
         public void run() {
         // set process priority
         setPriority(Thread.MAX_PRIORITY);
         // set the buffer size
         //int buffsize =AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        		
        		//AudioTrack.getMinBufferSize(sr,
                //AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
        // create an audiotrack object
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                                          sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                                  AudioFormat.ENCODING_PCM_8BIT, minbufsize,
                                  AudioTrack.MODE_STREAM);

        short samples[] = new short[minbufsize];
        int amp = 10000;
        double twopi = 8.*Math.atan(1.);
        double fr = 440.f;
        double ph = 0.0;

        // start audio
       audioTrack.play();

       // synthesis loop
       while(isRunning){
        //fr =  440 + 440*sliderval;
    	 fr =  440*flag;
        //for(int i=0; i < buffsize; i++){
        //  samples[i] = (short) (amp*Math.sin(ph));
        //  ph += twopi*fr/sr;
        //}
    	 
    	 
    	 
    	if(flag==1) 
       audioTrack.write(SerialDAC((cr+"w"+cr+"w"+cr+"w"+cr).getBytes()), 0, minbufsize);
    	if(flag==2) 
       audioTrack.write(SerialDAC((cr+"z"+cr+"z"+cr+"z"+cr).getBytes()), 0, minbufsize);
    	if(flag==3) 
       audioTrack.write(SerialDAC((cr+"a"+cr+"a"+cr+"a"+cr).getBytes()), 0, minbufsize);
        if(flag==4) 
       audioTrack.write(SerialDAC((cr+"s"+cr+"s"+cr+"s"+cr).getBytes()), 0, minbufsize);  
        if(flag==5) 
       audioTrack.write(SerialDAC((cr+"o"+cr).getBytes()), 0, minbufsize);   
      }
      audioTrack.stop();
      audioTrack.release();
    }
   };
   t.start();        
        
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
        isRunning = false;
        try {
          t.join();
         } catch (InterruptedException e) {
           e.printStackTrace();
         }
         t = null;
         
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
            Log.i(TAG, "Area:"+P_xy[2]+"\n");
                       
            
            flag=5;
        
            if(P_xy[2]>7000){
            	flag=1;
				Log.i(TAG,"Avance\n");
            }
            if(P_xy[2]<4000){
            	flag=2;
				Log.i(TAG,"Retroceso\n");
            }
            
            if(P_xy[0]<220){            
            	flag=3;
				Log.i(TAG,"Derecha\n");				
            } 
            if(P_xy[0]>260){           	
            	flag=4;
    		    Log.i(TAG,"Izquierda\n");    			      	
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
    
    
    private static byte jitter = (byte) (4);
	private static byte logichigh = (byte) (-128);
	private static byte logiclow = (byte) (16);

	private static int bytesinframe=10+characterdelay;
	private static int i=0; // counter 
	private static int j=0; // counter 
	private static int k=0; // counter 
	private static int m=0; // counter 
	private static int n=sampleRate / baudRate;
	private static byte l=jitter; // intentional jitter used to prevent the DAC from flattening the waveform prematurely

	public static byte[] SerialDAC(byte[] sendme)
	{
		if (levelflip)
		{
			logiclow = (byte) (-128);
			logichigh = (byte) (16);
			jitter = (byte) (4);
		}
		else
		{
			logichigh = (byte) (-128);
			logiclow = (byte) (16);
			jitter = (byte) (4);
		}
			
		bytesinframe=10+characterdelay;
		i=0; // counter 
		j=0; // counter 
		k=0; // counter 
		m=0; // counter 
		n=sampleRate / baudRate;
		boolean[] bits = new boolean[sendme.length*bytesinframe];
		byte[] waveform = new byte[(sendme.length*bytesinframe*sampleRate / baudRate)]; // 8 bit, no parity, 1 stop
		//Arrays.fill(waveform, (byte) 0);
		Arrays.fill(bits, true); // slight opti to decide what to do with stop bits

		// generate bit array first: makes it easier to understand what's going on
		for (i=0;i<sendme.length;++i)
		{
			m=i*bytesinframe;
			bits[m]=false;
			bits[++m]=((sendme[i]&1)==1);//?false:true;
			bits[++m]=((sendme[i]&2)==2);//?false:true;
			bits[++m]=((sendme[i]&4)==4);//?false:true;
			bits[++m]=((sendme[i]&8)==8);//?false:true;
			bits[++m]=((sendme[i]&16)==16);//?false:true;
			bits[++m]=((sendme[i]&32)==32);//?false:true;
			bits[++m]=((sendme[i]&64)==64);//?false:true;
			bits[++m]=((sendme[i]&128)==128);//?false:true;
			// cheaper to prefill to true
			// now we need a stop bit, BUT we want to be able to add more (character delay) to play-nice with some microcontrollers such as the Picaxe or BS1 that need it in order to do decimal conversion natively.
			//			for(k=0;k<bytesinframe-9;k++) 
			//				bits[++m]=true;
		}

		// now generate the actual waveform using l to wiggle the DAC and prevent it from zeroing out
		for (i=0;i<bits.length;i++)
		{
			for (k=0;k<n;k++)
			{
				waveform[j++]=(bits[i])?((byte) (logichigh+l)):((byte) (logiclow-l));
				l = (l==(byte)0)?jitter:(byte)0;
			}
		}


		bits=null;
		return waveform;
	}
    
	private static int minbufsize;
	private static int length;
    
	public static void UpdateParameters(boolean AutoSampleRate){
		baudRate = new_baudRate; // we're not forcing standard baud rates here specifically because we want to allow odd ones
		if (AutoSampleRate == true)
		{
			new_sampleRate = new_baudRate;
			while(new_sampleRate <= (max_sampleRate))
			{
				new_sampleRate *=2;//+= new_baudRate;
			}
			new_sampleRate/=2;
		}


		if (new_sampleRate > max_sampleRate)
			new_sampleRate = max_sampleRate;
		if (new_sampleRate < min_sampleRate)
			new_sampleRate = min_sampleRate;

		sampleRate = new_sampleRate; // min 4000 max 48000 
		if (new_characterdelay < 0)
			new_characterdelay = 0;
		characterdelay = new_characterdelay;
		levelflip = new_levelflip;
		//int buffsize=AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
		minbufsize=AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_8BIT);
	}
    

    //public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native int[] ObjectTracking(long matAddrGr, long matAddrRgba, int [] hsv_values, int flag);
}
