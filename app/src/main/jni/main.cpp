#include "com_example_user_ndktest_MainActivity.h"
#include"opencv2/core/core.hpp"
#include"opencv2/highgui/highgui.hpp"
#include"opencv2/imgproc/imgproc.hpp"
#include <android/log.h>

using namespace cv;
using namespace std;

JNIEXPORT jint JNICALL Java_com_example_user_ndktest_MainActivity_getMatHeight  (JNIEnv * env, jobject obj, jint w, jint h){
__android_log_print(ANDROID_LOG_DEBUG, "naing", "getMatHeight");
Mat mat = *(Mat_<int>(4, 4) << 5,0,0,0,   0,1,0,0,  0,0,1,0,  0,0,0,1);
__android_log_print(ANDROID_LOG_DEBUG, "naing", "height : %d",mat.at<int>(0,0));
 return (jint)mat.at<int>(0,0);
}
