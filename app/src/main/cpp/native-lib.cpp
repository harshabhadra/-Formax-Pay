//
// Created by user on 06-01-2020.
//
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_apiKey(JNIEnv *env, jobject object) {
    std::string api_key = "";
    return env->NewStringUTF(api_key.c_str());
}

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_memberId(JNIEnv *env, jobject object) {
    std::string member_id = "";
    return env->NewStringUTF(member_id.c_str());
}

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_apiPassword(JNIEnv *env, jobject object) {
    std::string api_password = "";
    return env->NewStringUTF(api_password.c_str());
}