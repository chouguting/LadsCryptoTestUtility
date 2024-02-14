#include "tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine.h"
#include <stddef.h>
#include <stdint.h>
#include "params.h"
#include "kem.h"
#include "indcpa.h"
#include "verify.h"
#include "symmetric.h"
#include "randombytes.h"

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_keypair
(JNIEnv * env, jobject jobj, jbyteArray publicKey, jbyteArray secretKey){
    jbyte* publicKeyBufferPtr =  (*env)->GetByteArrayElements(env, publicKey, NULL);
    jint publicKeyLength = (*env)->GetArrayLength(env, publicKey);
    jbyte* secretKeyBufferPtr =  (*env)->GetByteArrayElements(env, secretKey, NULL);
    jint secretKeyLength = (*env)->GetArrayLength(env, secretKey);
    // call the keypair function
    crypto_kem_keypair(publicKeyBufferPtr, secretKeyBufferPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, secretKey, secretKeyBufferPtr, 0);
}

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_encapsulate
(JNIEnv * env, jobject jobj, jbyteArray cipherText, jbyteArray sharedSecret, jbyteArray seed, jbyteArray publicKey){

}

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_decapsulate
(JNIEnv * env, jobject jobj, jbyteArray sharedSecret, jbyteArray cipherText, jbyteArray privateKey){

}