//Written by  Kyle Hounslow 2013
//Modified by Raul Vargas 2014

//Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software")
//, to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
//and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

//The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
//IN THE SOFTWARE.


#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
//#include <opencv2/objdetect/objdetect.hpp>
#include <vector>

//#include <iostream>
#include "cv.h"

//initial min and max HSV filter values.
//these will be changed using trackbars
    int H_MIN = 10;
    int H_MAX = 94;
    int S_MIN = 125;
    int S_MAX = 239;
    int V_MIN = 113;
    int V_MAX = 245;
//default capture width and height
const int FRAME_WIDTH = 640;
const int FRAME_HEIGHT = 480;
//max number of objects to be detected in frame
const int MAX_NUM_OBJECTS=50;
//minimum and maximum object area
const int MIN_OBJECT_AREA = 10*10;
const int MAX_OBJECT_AREA = FRAME_HEIGHT*FRAME_WIDTH/1.5;

using namespace std;
using namespace cv;

string intToString(int number){


	std::stringstream ss;
	ss << number;
	return ss.str();
}

void drawObject(int x, int y,Mat &frame){

	//use some of the openCV drawing functions to draw crosshairs
	//on your tracked image!

    //UPDATE:JUNE 18TH, 2013
    //added 'if' and 'else' statements to prevent
    //memory errors from writing off the screen (ie. (-25,-25) is not within the window!)

	circle(frame,Point(x,y),20,Scalar(0,255,0),2);
    if(y-25>0)
    line(frame,Point(x,y),Point(x,y-25),Scalar(0,255,0),2);
    else line(frame,Point(x,y),Point(x,0),Scalar(0,255,0),2);
    if(y+25<FRAME_HEIGHT)
    line(frame,Point(x,y),Point(x,y+25),Scalar(0,255,0),2);
    else line(frame,Point(x,y),Point(x,FRAME_HEIGHT),Scalar(0,255,0),2);
    if(x-25>0)
    line(frame,Point(x,y),Point(x-25,y),Scalar(0,255,0),2);
    else line(frame,Point(x,y),Point(0,y),Scalar(0,255,0),2);
    if(x+25<FRAME_WIDTH)
    line(frame,Point(x,y),Point(x+25,y),Scalar(0,255,0),2);
    else line(frame,Point(x,y),Point(FRAME_WIDTH,y),Scalar(0,255,0),2);

	putText(frame,intToString(x)+","+intToString(y),Point(x,y+30),1,1,Scalar(0,255,0),2);

}


void morphOps(Mat &thresh){

	//create structuring element that will be used to "dilate" and "erode" image.
	//the element chosen here is a 3px by 3px rectangle

	Mat erodeElement = getStructuringElement( MORPH_RECT,Size(3,3));
    //dilate with larger element so make sure object is nicely visible
	Mat dilateElement = getStructuringElement( MORPH_RECT,Size(8,8));

	erode(thresh,thresh,erodeElement);
	erode(thresh,thresh,erodeElement);


	dilate(thresh,thresh,dilateElement);
	dilate(thresh,thresh,dilateElement);



}
void trackFilteredObject(int &x, int &y, Mat threshold, Mat &cameraFeed){

	Mat temp;
	threshold.copyTo(temp);
	//these two vectors needed for output of findContours
	vector< vector<Point> > contours;
	vector<Vec4i> hierarchy;
	//find contours of filtered image using openCV findContours function
	findContours(temp,contours,hierarchy,CV_RETR_CCOMP,CV_CHAIN_APPROX_SIMPLE );
	//use moments method to find our filtered object
	double refArea = 0;
	bool objectFound = false;
	if (hierarchy.size() > 0) {
		int numObjects = hierarchy.size();
        //if number of objects greater than MAX_NUM_OBJECTS we have a noisy filter
        if(numObjects<MAX_NUM_OBJECTS){
			for (int index = 0; index >= 0; index = hierarchy[index][0]) {

				Moments moment = moments((cv::Mat)contours[index]);
				double area = moment.m00;

				//if the area is less than 20 px by 20px then it is probably just noise
				//if the area is the same as the 3/2 of the image size, probably just a bad filter
				//we only want the object with the largest area so we safe a reference area each
				//iteration and compare it to the area in the next iteration.
                if(area>MIN_OBJECT_AREA && area<MAX_OBJECT_AREA && area>refArea){
					x = moment.m10/area;
					y = moment.m01/area;
					objectFound = true;
					refArea = area;
				}else objectFound = false;


			}
			//let user know you found an object
			if(objectFound ==true){
				putText(cameraFeed,"Objeto de seguimiento",Point(0,50),1,1,Scalar(0,255,0),2);
				//draw object location on screen
				drawObject(x,y,cameraFeed);}

		}else putText(cameraFeed,"TOO MUCH NOISE! ADJUST FILTER",Point(0,50),1,2,Scalar(0,0,255),2);
	}
}


extern "C" {
JNIEXPORT jintArray JNICALL Java_trackball_ludobots_com_Tutorial2Activity_ObjectTracking(JNIEnv*  env, jobject obj, jlong addrGray, jlong addrRgba,  jintArray HSV_values , jint flag);

JNIEXPORT jintArray JNICALL Java_trackball_ludobots_com_Tutorial2Activity_ObjectTracking(JNIEnv*  env, jobject obj, jlong addrGray, jlong addrRgba,  jintArray HSV_values, jint flag)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& cameraFeed = *(Mat*)addrRgba;

    jint size = 2;
    jintArray P_xy;

    P_xy = env->NewIntArray(size);

    //jsize len = env->GetArrayLength(HSV_values);
    jint *body = env->GetIntArrayElements(HSV_values, 0);
    H_MIN = body[0];
    H_MAX = body[1];
    S_MIN = body[2];
    S_MAX = body[3];
    V_MIN = body[4];
    V_MAX = body[5];
    env->ReleaseIntArrayElements(HSV_values, body, 0);

	//some boolean variables for different functionality within this
	//program
    bool trackObjects = true;
    bool useMorphOps = true;
	//Matrix to store each frame of the webcam feed
	//Mat cameraFeed;
	//matrix storage for HSV image
	Mat HSV;
	//matrix storage for binary threshold image
	Mat threshold;
	//x and y values for the location of the object
	int x=0, y=0;

	        if(P_xy == NULL) {
	                return NULL;
	        }

	        jint fill[size];

		//convert frame from BGR to HSV colorspace
		cvtColor(cameraFeed,HSV,COLOR_BGR2HSV);
		//filter HSV image between values and store filtered image to
		//threshold matrix
		inRange(HSV,Scalar(H_MIN,S_MIN,V_MIN),Scalar(H_MAX,S_MAX,V_MAX),threshold);
		//perform morphological operations on thresholded image to eliminate noise
		//and emphasize the filtered object(s)

		if(useMorphOps)
				morphOps(threshold);


		    // some smoothing of the image
		    //cvSmooth( thresholded, thresholded, CV_GAUSSIAN, 9, 9 );

		    // find circle pattterns

		if(flag == 3){
		    vector<Vec3f> circles;
		        HoughCircles(threshold, circles, CV_HOUGH_GRADIENT,
		                     2, threshold.rows/4,  100, 40, 15, 80 );


		        for( size_t i = 0; i < circles.size(); i++ )
		        {
		             Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
		             int radius = cvRound(circles[i][2]);
		             // draw the circle center
		             circle( cameraFeed, center, 3, Scalar(0,255,0), -1, 8, 0 );
		             // draw the circle outline
		             circle( cameraFeed, center, radius, Scalar(0,0,255), 3, 8, 0 );
		        }


			fill[0] = 0;
			fill[1] = 0;
			//if(flag)threshold.copyTo(cameraFeed);
			//putText(cameraFeed,"H+:"+intToString(H_MAX)+", H-:"+intToString(H_MIN),Point(0,50),1,1,Scalar(255,255,255),2);

			env->SetIntArrayRegion(P_xy, 0, size, fill);
			return P_xy;

		} else {



		//pass in thresholded frame to our object tracking function
		//this function will return the x and y coordinates of the
		//filtered object
		if(trackObjects)
			trackFilteredObject(x,y,threshold,cameraFeed);
		fill[0] = x;
		fill[1] = y;
		if(flag == 1)threshold.copyTo(cameraFeed);
		//putText(cameraFeed,"H+:"+intToString(H_MAX)+", H-:"+intToString(H_MIN),Point(0,50),1,1,Scalar(255,255,255),2);

		env->SetIntArrayRegion(P_xy, 0, size, fill);
		return P_xy;
		}
}
}
