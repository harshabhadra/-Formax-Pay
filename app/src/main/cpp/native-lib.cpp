//
// Created by user on 06-01-2020.
//
#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_apiKey(JNIEnv *env, jobject object) {
    std::string api_key = "5!mk516*pgh154%$#$#@$";
    return env->NewStringUTF(api_key.c_str());
}

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_memberId(JNIEnv *env, jobject object) {
    std::string member_id = "3368";
    return env->NewStringUTF(member_id.c_str());
}

extern "C" JNIEXPORT jstring
JNICALL
Java_com_rechargeweb_rechargeweb_Keys_apiPassword(JNIEnv *env, jobject object) {
    std::string api_password = "12345";
    return env->NewStringUTF(api_password.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_rechargeweb_rechargeweb_Keys_merchantId(JNIEnv *env, jobject thiz) {
    std::string merchantId = "98617";
    return env->NewStringUTF(merchantId.c_str());}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_rechargeweb_rechargeweb_Keys_payementGatePass(JNIEnv *env, jobject thiz) {
    std::string pamentPassword = "074da5a7";
    return env->NewStringUTF(pamentPassword.c_str());
}