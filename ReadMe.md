之前有做了下FFmpeg-4.0.3的移植，现在记录一下FFmpeg-4.0.3版本的编译移植过程，虽然网上有很多这方面的资料，但是都是针对比较旧的版本了，在编译过程中会踩到很多坑，这里我只把FFmpeg-4.0.3这个版本编译成功的步骤记录下来，中间的坑就不写了。

# 准备
[FFmpeg下载](https://ffmpeg.org/download.html#releases)（这里我下载的最新版本4.0.3，现在有4.1版本了。）

[NDK下载](https://developer.android.google.cn/ndk/downloads/)（这个不要用Android Studio里面下载的ndk包，不完整，自己去官网下载r16b这个版本的就行）

这两个包下载完成后解压后分别是这样子的

ffmpeg![](https://user-gold-cdn.xitu.io/2019/1/7/168270ca6aa8b8f9?w=200&h=596&f=jpeg&s=45866)


ndk![](https://user-gold-cdn.xitu.io/2019/1/7/168270cc107927c0?w=213&h=334&f=jpeg&s=27394)

# 步骤

在解压后的ffmpeg包的根目录下新建一个.sh文件，这里我创建一个名为build_android.sh的文件，文件编辑内容如下：
```
ADDI_CFLAGS="-marm"
API=19
PLATFORM=arm-linux-androideabi
CPU=armv7-a
NDK=/Users/wepon/android-ndk-r16b # 修改成自己本地的ndk路径。
SYSROOT=$NDK/platforms/android-$API/arch-arm/
ISYSROOT=$NDK/sysroot
ASM=$ISYSROOT/usr/include/$PLATFORM
TOOLCHAIN=$NDK/toolchains/$PLATFORM-4.9/prebuilt/darwin-x86_64
OUTPUT=/Users/wepon/Downloads/ffmpeg-4.0.3/android #自己指定一个输出目录，用来放生成的文件的。
function build
{
./configure \
--prefix=$OUTPUT \
--enable-shared \
--disable-static \
--disable-doc \
--disable-ffmpeg \
--disable-ffplay \
--disable-ffprobe \
--disable-avdevice \
--disable-doc \
--disable-symver \
--cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
--target-os=android \
--arch=arm \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-I$ASM -isysroot $ISYSROOT -Os -fpic -marm" \
--extra-ldflags="-marm" \
$ADDITIONAL_CONFIGURE_FLAG
  make clean
  make
  make install
}

build
```
这里主要修改的两个点我注释在那里了，大家改成自己的目录就行了，第一个地方是你的NDK包下载后解压的路径，第二个地方是生成编译后的文件目录路径。

然后我们使用 sh build_android.sh就可以开始编译了（没错，有的文章写的在这之前还有替换代码什么的操作啥的不需要了），这个时候就需要耐心等待大概5-10分钟的样子（基于我的电脑上的不准确统计）才能编译完成，编译完成后可以查看生成的文件目录是不是跟我的一样。我这里放两张编译成功后的图片：

![](https://user-gold-cdn.xitu.io/2019/1/7/1682734f9df6c93f?w=1170&h=900&f=jpeg&s=213909)

![](https://user-gold-cdn.xitu.io/2019/1/7/16827351bb36eb48?w=374&h=324&f=jpeg&s=43988)



走到这里就可以放Android项目移植使用了。

# 移植到Android中通过FFmpeg解码播放视频

首先，我们新建一个Android项目，记得把Include C++ support勾上。

然后把我们的so文件和include文件放到lib目录下，还有build.gradle文件和CMakeLists.txt文件的代码一并放出，如下：

lib

![](https://user-gold-cdn.xitu.io/2019/1/7/168277402ced564b?w=377&h=800&f=jpeg&s=61385)

cmake
```
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# 定义变量
set(distribution_DIR ../../../../libs)

# 添加库——自己编写的库
# 库名称：native-lib
# 库类型：SHARED，表示动态库，后缀为.so（如果是STATIC，则表示静态库，后缀为.a）
# 库源码文件：src/main/cpp/native-lib.cpp
add_library( native-lib
        SHARED
        src/main/cpp/native-lib.cpp )

# 添加库——外部引入的库
# 库名称：avcodec（不需要包含前缀lib）
# 库类型：SHARED，表示动态库，后缀为.so（如果是STATIC，则表示静态库，后缀为.a）
# IMPORTED表明是外部引入的库
add_library( avcodec
        SHARED
        IMPORTED)
# 设置目标属性
# 设置avcodec目标库的IMPORTED_LOCATION属性，用于说明引入库的位置
# 还可以设置其他属性，格式：PROPERTIES key value
set_target_properties( avcodec
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libavcodec.so)


find_library(
        log-lib
        log)
add_library( avfilter
        SHARED
        IMPORTED)
set_target_properties( avfilter
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libavfilter.so)

add_library( avformat
        SHARED
        IMPORTED)
set_target_properties( avformat
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libavformat.so)

add_library( avutil
        SHARED
        IMPORTED)
set_target_properties( avutil
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libavutil.so)

add_library( swresample
        SHARED
        IMPORTED)
set_target_properties( swresample
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libswresample.so)

add_library( swscale
        SHARED
        IMPORTED)
set_target_properties( swscale
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/${ANDROID_ABI}/libswscale.so)

# 引入头文件
include_directories(libs/include)

# 告诉编译器生成native-lib库需要链接的库
# native-lib库需要依赖avcodec、avfilter等库
target_link_libraries( native-lib
        avcodec
        avfilter
        avformat
        avutil
        swresample
        swscale
        -landroid
        ${log-lib} )


```

build.gradle

```
apply plugin: 'com.android.application'

android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.wepon.ffmpeg4cmake"
        minSdkVersion 19
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags "-frtti -fexceptions -Wno-deprecated-declarations"
            }
            ndk{
                abiFilters "armeabi-v7a"
            }
        }

        sourceSets {
            main {
                jniLibs.srcDirs = ['libs']
            }
        }
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
}

dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
}

```

native-lib.cpp (c文件，主要是使用ffmpeg解码播放视频的代码)
```
#include <jni.h>
#include <string>

extern "C"
{

#include <android/native_window_jni.h>
#include <libavfilter/avfilter.h>
#include <libavcodec/avcodec.h>
//封装格式处理
#include <libavformat/avformat.h>
//像素处理
#include <libswscale/swscale.h>
#include <unistd.h>

JNIEXPORT void JNICALL
Java_com_wepon_ffmpeg4cmake_FFVideoPlayer_render(JNIEnv *env, jobject instance, jstring url_,
                                                 jobject surface) {
    const char *url = env->GetStringUTFChars(url_, 0);

    // 注册。
    av_register_all();
    // 打开地址并且获取里面的内容  avFormatContext是内容的一个上下文
    AVFormatContext *avFormatContext = avformat_alloc_context();
    avformat_open_input(&avFormatContext, url, NULL, NULL);
    avformat_find_stream_info(avFormatContext, NULL);

    // 找出视频流
    int video_index = -1;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        if (avFormatContext->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            video_index = i;
        }
    }
    // 解码  转换  绘制
    // 获取解码器上下文
    AVCodecContext *avCodecContext = avFormatContext->streams[video_index]->codec;
    // 获取解码器
    AVCodec *avCodec = avcodec_find_decoder(avCodecContext->codec_id);
    // 打开解码器
    if (avcodec_open2(avCodecContext, avCodec, NULL) < 0) {
        // 打开失败。
        return;
    }
    // 申请AVPacket和AVFrame，
    // 其中AVPacket的作用是：保存解码之前的数据和一些附加信息，如显示时间戳（pts）、解码时间戳（dts）、数据时长，所在媒体流的索引等；
    // AVFrame的作用是：存放解码过后的数据。
    AVPacket *avPacket = (AVPacket *) av_malloc(sizeof(AVPacket));
    av_init_packet(avPacket);
    // 分配一个AVFrame结构体,AVFrame结构体一般用于存储原始数据，指向解码后的原始帧
    AVFrame *avFrame = av_frame_alloc();
    //分配一个AVFrame结构体，指向存放转换成rgb后的帧
    AVFrame *rgb_frame = av_frame_alloc();
    // rgb_frame是一个缓存区域，所以需要设置。
    // 缓存区
    uint8_t *out_buffer = (uint8_t *) av_malloc(
            avpicture_get_size(AV_PIX_FMT_RGBA, avCodecContext->width, avCodecContext->height));
    // 与缓存区相关联，设置rgb_frame缓存区
    avpicture_fill((AVPicture *) rgb_frame, out_buffer, AV_PIX_FMT_RGBA, avCodecContext->width,
                   avCodecContext->height);
    // 原生绘制，需要ANativeWindow
    ANativeWindow *pANativeWindow = ANativeWindow_fromSurface(env, surface);
    if (pANativeWindow == 0) {
        // 获取native window 失败
        return;
    }
    SwsContext *swsContext = sws_getContext(
            avCodecContext->width,
            avCodecContext->height,
            avCodecContext->pix_fmt,
            avCodecContext->width,
            avCodecContext->height,
            AV_PIX_FMT_RGBA,
            SWS_BICUBIC,
            NULL,
            NULL,
            NULL);
    // 视频缓冲区
    ANativeWindow_Buffer native_outBuffer;
    // 开始解码了。
    int frameCount;
    while (av_read_frame(avFormatContext, avPacket) >= 0) {
        if (avPacket->stream_index == video_index) {
            avcodec_decode_video2(avCodecContext, avFrame, &frameCount, avPacket);
            // 当解码一帧成功过后，我们转换成rgb格式并且绘制。
            if (frameCount) {
                ANativeWindow_setBuffersGeometry(pANativeWindow, avCodecContext->width,
                                                 avCodecContext->height, WINDOW_FORMAT_RGBA_8888);
                // 上锁
                ANativeWindow_lock(pANativeWindow, &native_outBuffer, NULL);
                // 转换为rgb格式
                sws_scale(swsContext, (const uint8_t *const *) avFrame->data, avFrame->linesize, 0,
                          avFrame->height, rgb_frame->data, rgb_frame->linesize);
                uint8_t *dst = (uint8_t *) native_outBuffer.bits;
                int destStride = native_outBuffer.stride * 4;
                uint8_t *src = rgb_frame->data[0];
                int srcStride = rgb_frame->linesize[0];
                for (int i = 0; i < avCodecContext->height; ++i) {
                    memcpy(dst + i * destStride, src + i * srcStride, srcStride);
                }
                ANativeWindow_unlockAndPost(pANativeWindow);
//                usleep(1000 * 16);
            }
        }
        av_free_packet(avPacket);

    }

    ANativeWindow_release(pANativeWindow);
    av_frame_free(&avFrame);
    av_frame_free(&rgb_frame);
    avcodec_close(avCodecContext);
    avformat_free_context(avFormatContext);


    env->ReleaseStringUTFChars(url_, url);
}


JNIEXPORT jstring JNICALL
Java_com_wepon_ffmpeg4cmake_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jstring JNICALL
Java_com_wepon_ffmpeg4cmake_MainActivity_urlprotocolinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    av_register_all();
    struct URLProtocol *pup = NULL;
    struct URLProtocol **p_temp = &pup;
    avio_enum_protocols((void **) p_temp, 0);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s\n", info, avio_enum_protocols((void **) p_temp, 0));
    }
    pup = NULL;
    avio_enum_protocols((void **) p_temp, 1);
    while ((*p_temp) != NULL) {
        sprintf(info, "%sInput: %s\n", info, avio_enum_protocols((void **) p_temp, 1));
    }

    return env->NewStringUTF(info);
}

JNIEXPORT jstring JNICALL
Java_com_wepon_ffmpeg4cmake_MainActivity_avformatinfo(JNIEnv *env, jobject instance) {

    char info[40000] = {0};

    av_register_all();

    AVInputFormat *if_temp = av_iformat_next(NULL);
    AVOutputFormat *of_temp = av_oformat_next(NULL);
    while (if_temp != NULL) {
        sprintf(info, "%sInput: %s\n", info, if_temp->name);
        if_temp = if_temp->next;
    }
    while (of_temp != NULL) {
        sprintf(info, "%sOutput: %s\n", info, of_temp->name);
        of_temp = of_temp->next;
    }
    return env->NewStringUTF(info);
}

JNIEXPORT jstring JNICALL
Java_com_wepon_ffmpeg4cmake_MainActivity_avcodecinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};

    av_register_all();

    AVCodec *c_temp = av_codec_next(NULL);

    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%sdecode:", info);
        } else {
            sprintf(info, "%sencode:", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s(video):", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s(audio):", info);
                break;
            default:
                sprintf(info, "%s(other):", info);
                break;
        }
        sprintf(info, "%s[%10s]\n", info, c_temp->name);
        c_temp = c_temp->next;
    }

    return env->NewStringUTF(info);
}

JNIEXPORT jstring JNICALL
Java_com_wepon_ffmpeg4cmake_MainActivity_avfilterinfo(JNIEnv *env, jobject instance) {
    char info[40000] = {0};
    avfilter_register_all();

    AVFilter *f_temp = (AVFilter *) avfilter_next(NULL);
    while (f_temp != NULL) {
        sprintf(info, "%s%s\n", info, f_temp->name);
        f_temp = f_temp->next;
    }
    return env->NewStringUTF(info);
}
}
```




到这里我们就能写视频文件的播放了，这里我使用了一个网上找的视频文件地址进行播放，记得加上联网的权限。播放效果如下，生成gif太大了就没生成了(只是简单的处理了视频的解压，音频这块暂时没有处理，FFmpeg要学习的东西很多，可以参考其他文档)：

![](https://user-gold-cdn.xitu.io/2019/1/7/16827a608113711a?w=1080&h=1920&f=png&s=827708)


代码放在github[]()

