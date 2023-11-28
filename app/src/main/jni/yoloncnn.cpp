#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>

#include "yolo.h"
//
//#include "ndkcamera.h"
//
//#include <opencv2/core/core.hpp>
//#include <opencv2/imgproc/imgproc.hpp>

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON

//static int draw_unsupported(cv::Mat& rgb)
//{
//    const char text[] = "unsupported";
//
//    int baseLine = 0;
//    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);
//
//    int y = (rgb.rows - label_size.height) / 2;
//    int x = (rgb.cols - label_size.width) / 2;
//
//    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
//                    cv::Scalar(255, 255, 255), -1);
//
//    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
//                cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 0, 0));
//
//    return 0;
//}
//
//static int draw_fps(cv::Mat& rgb)
//{
//    // resolve moving average
//    float avg_fps = 0.f;
//    {
//        static double t0 = 0.f;
//        static float fps_history[10] = {0.f};
//        double t1 = ncnn::get_current_time();
//        if (t0 == 0.f)
//        {
//            t0 = t1;
//            return 0;
//        }
//        float fps = 1000.f / (t1 - t0);
//        t0 = t1;
//
//        for (int i = 9; i >= 1; i--)
//        {
//            fps_history[i] = fps_history[i - 1];
//        }
//        fps_history[0] = fps;
//
//        if (fps_history[9] == 0.f)
//        {
//            return 0;
//        }
//
//        for (int i = 0; i < 10; i++)
//        {
//            avg_fps += fps_history[i];
//        }
//        avg_fps /= 10.f;
//    }
//    char text[32];
//    sprintf(text, "FPS=%.2f", avg_fps);
//    int baseLine = 0;
//    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);
//    int y = 0;
//    int x = rgb.cols - label_size.width;
//    cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
//                    cv::Scalar(255, 255, 255), -1);
//    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
//                cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));
//
//    return 0;
//}

static Yolo* g_yolo = 0;
static ncnn::Mutex lock;

//class MyNdkCamera : public NdkCameraWindow
//{
//public:
//    virtual void on_image_render(cv::Mat& rgb) const;
//};
//
//void MyNdkCamera::on_image_render(cv::Mat& rgb) const
//{
//    // nanodet
//    {
//        ncnn::MutexLockGuard g(lock);
//
//        if (g_yolo)
//        {
//            std::vector<Object> objects;
//            g_yolo->detect(rgb, objects);
//
//            g_yolo->draw(rgb, objects);
//        }
//        else
//        {
//            draw_unsupported(rgb);
//        }
//    }
//
//    draw_fps(rgb);
//}
//
//static MyNdkCamera* g_camera = 0;

extern "C" {

static jclass objCls = nullptr;
static jmethodID constructortorId;
static jfieldID xId;
static jfieldID yId;
static jfieldID wId;
static jfieldID hId;
static jfieldID labelId;
static jfieldID probId;

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");

//    g_camera = new MyNdkCamera;

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved)
{
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

    {
        ncnn::MutexLockGuard g(lock);

        delete g_yolo;
        g_yolo = 0;
    }

//    delete g_camera;
//    g_camera = 0;
}

// public native boolean loadModel(AssetManager mgr, int modelid, int cpugpu);
JNIEXPORT jboolean JNICALL Java_com_michaelzhan_enlightenment_YoloV7Ncnn_loadModel(JNIEnv* env, jobject thiz, jobject assetManager, jint modelid, jint cpugpu)
{
    if (modelid < 0 || modelid > 6 || cpugpu < 0 || cpugpu > 1)
    {
        return JNI_FALSE;
    }

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    const char* modeltypes[] =
    {
        "model/yolov7-tiny",
    };

    const int target_sizes[] =
    {
        640,
    };

    const float norm_vals[][3] =
    {
        {1 / 255.f, 1 / 255.f , 1 / 255.f},
    };

    const char* modeltype = modeltypes[(int)modelid];
    int target_size = target_sizes[(int)modelid];
    bool use_gpu = (int)cpugpu == 1;

    // reload
    {
        ncnn::MutexLockGuard g(lock);

        if (use_gpu && ncnn::get_gpu_count() == 0)
        {
            // no gpu
            delete g_yolo;
            g_yolo = 0;
        }
        else
        {
            if (!g_yolo)
                g_yolo = new Yolo;
            g_yolo->load(mgr, modeltype, target_size, norm_vals[(int)modelid], use_gpu);
        }
    }

    jclass localObjCls = env->FindClass("com/michaelzhan/enlightenment/YoloV7Ncnn$Obj");
    objCls = reinterpret_cast<jclass>(env->NewGlobalRef(localObjCls));

    constructortorId = env->GetMethodID(objCls, "<init>", "(Lcom/michaelzhan/enlightenment/YoloV7Ncnn;)V");

    xId = env->GetFieldID(objCls, "x", "F");
    yId = env->GetFieldID(objCls, "y", "F");
    wId = env->GetFieldID(objCls, "w", "F");
    hId = env->GetFieldID(objCls, "h", "F");
    labelId = env->GetFieldID(objCls, "label", "Ljava/lang/String;");
    probId = env->GetFieldID(objCls, "prob", "F");

    return JNI_TRUE;
}

// public native Obj[] Detect(Bitmap bitmap, boolean use_gpu);
JNIEXPORT jobjectArray JNICALL Java_com_michaelzhan_enlightenment_YoloV7Ncnn_Detect(JNIEnv* env, jobject thiz, jobject bitmap, jboolean use_gpu)
{
    if (use_gpu == JNI_TRUE && ncnn::get_gpu_count() == 0)
    {
        return NULL;
        //return env->NewStringUTF("no vulkan capable gpu");
    }

//    double start_time = ncnn::get_current_time();

//    AndroidBitmapInfo info;
//    AndroidBitmap_getInfo(env, bitmap, &info);
//    const int width = info.width;
//    const int height = info.height;
//    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888)
//        return NULL;
//
//    // ncnn from bitmap
//    const int target_size = 640;
//
//    // letterbox pad to multiple of 32
//    int w = width;
//    int h = height;
//    float scale = 1.f;
//    if (w > h)
//    {
//        scale = (float)target_size / w;
//        w = target_size;
//        h = h * scale;
//    }
//    else
//    {
//        scale = (float)target_size / h;
//        h = target_size;
//        w = w * scale;
//    }

    if (g_yolo) {
//        ncnn::Mat in = ncnn::Mat::from_android_bitmap_resize(env, bitmap, ncnn::Mat::PIXEL_RGB, w, h);
        std::vector<Object> objects;
        g_yolo->detect(env, bitmap, objects);

        jobjectArray jObjArray = env->NewObjectArray(objects.size(), objCls, NULL);

        for (size_t i=0; i<objects.size(); i++)
        {
            jobject jObj = env->NewObject(objCls, constructortorId, thiz);

            env->SetFloatField(jObj, xId, objects[i].x);
            env->SetFloatField(jObj, yId, objects[i].y);
            env->SetFloatField(jObj, wId, objects[i].w);
            env->SetFloatField(jObj, hId, objects[i].h);
            env->SetObjectField(jObj, labelId, env->NewStringUTF("question_area")); // class_names[objects[i].label]
            env->SetFloatField(jObj, probId, objects[i].prob);

            env->SetObjectArrayElement(jObjArray, i, jObj);
        }
//
//        g_yolo->draw(rgb, objects);
        __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "return jObjArray");
        return jObjArray;
    }else {
        return nullptr;
    }
}

// // public native boolean openCamera(int facing);
//JNIEXPORT jboolean JNICALL Java_com_michaelzhan_enlightenment_NcnnYolov7_openCamera(JNIEnv* env, jobject thiz, jint facing)
//{
//    if (facing < 0 || facing > 1)
//        return JNI_FALSE;
//
//    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);
//
// //    g_camera->open((int)facing);
//
//    return JNI_TRUE;
//}
//
// // public native boolean closeCamera();
//JNIEXPORT jboolean JNICALL Java_com_michaelzhan_enlightenment_NcnnYolov7_closeCamera(JNIEnv* env, jobject thiz)
//{
//    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");
//
// //    g_camera->close();
//
//    return JNI_TRUE;
//}
//
// // public native boolean setOutputWindow(Surface surface);
//JNIEXPORT jboolean JNICALL Java_com_michaelzhan_enlightenment_NcnnYolov7_setOutputWindow(JNIEnv* env, jobject thiz, jobject surface)
//{
//    ANativeWindow* win = ANativeWindow_fromSurface(env, surface);
//
//    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);
//
// //    g_camera->set_window(win);
//
//    return JNI_TRUE;
//
//
//}

}
