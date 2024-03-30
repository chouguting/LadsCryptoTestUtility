#include "tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine.h"
#include <stdint.h>
#include "kem.h"
#include "indcpa.h"

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_kemKeypair
(JNIEnv * env, jobject jobj, jbyteArray publicKey, jbyteArray privateKey){
    jbyte* publicKeyBufferPtr =  (*env)->GetByteArrayElements(env, publicKey, NULL);
    jint publicKeyLength = (*env)->GetArrayLength(env, publicKey);
    jbyte* privateKeyBufferPtr =  (*env)->GetByteArrayElements(env, privateKey, NULL);
    jint privateKeyLength = (*env)->GetArrayLength(env, privateKey);

    char* publicKeyCharPtr = (char*)publicKeyBufferPtr;
    char* privateKeyCharPtr = (char*)privateKeyBufferPtr;
    // call the keypair function
    crypto_kem_keypair(publicKeyCharPtr, privateKeyCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyBufferPtr, 0);
}

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_kemEncapsulate
(JNIEnv * env, jobject jobj, jbyteArray cipherText, jbyteArray sharedSecret, jbyteArray seed, jbyteArray publicKey){
    jbyte* cipherTextBufferPtr =  (*env)->GetByteArrayElements(env, cipherText, NULL);
    jbyte* sharedSecretBufferPtr =  (*env)->GetByteArrayElements(env, sharedSecret, NULL);
    jbyte* seedBufferPtr =  (*env)->GetByteArrayElements(env, seed, NULL);
    jbyte* publicKeyBufferPtr =  (*env)->GetByteArrayElements(env, publicKey, NULL);

    char* cipherTextCharPtr = (char*)cipherTextBufferPtr;
    char* sharedSecretCharPtr = (char*)sharedSecretBufferPtr;
    char* seedCharPtr = (char*)seedBufferPtr;
    char* publicKeyCharPtr = (char*)publicKeyBufferPtr;
    // call the encapsulate function
    crypto_kem_enc(cipherTextCharPtr, sharedSecretCharPtr, seedCharPtr, publicKeyCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, cipherText, cipherTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, sharedSecret, sharedSecretBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, seed, seedBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyBufferPtr, 0);


}

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_kemDecapsulate
(JNIEnv * env, jobject jobj, jbyteArray sharedSecret, jbyteArray cipherText, jbyteArray privateKey){
    jbyte* sharedSecretBufferPtr =  (*env)->GetByteArrayElements(env, sharedSecret, NULL);
    jbyte* cipherTextBufferPtr =  (*env)->GetByteArrayElements(env, cipherText, NULL);
    jbyte* privateKeyBufferPtr =  (*env)->GetByteArrayElements(env, privateKey, NULL);

    char* sharedSecretCharPtr = (char*)sharedSecretBufferPtr;
    char* cipherTextCharPtr = (char*)cipherTextBufferPtr;
    char* privateKeyCharPtr = (char*)privateKeyBufferPtr;
    // call the decapsulate function
    crypto_kem_dec(sharedSecretCharPtr, cipherTextCharPtr, privateKeyCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, sharedSecret, sharedSecretBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, cipherText, cipherTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyBufferPtr, 0);

}


JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_pkeKeypair
(JNIEnv * env, jobject obj, jbyteArray publicKey, jbyteArray privateKey){
    jbyte* publicKeyBufferPtr =  (*env)->GetByteArrayElements(env, publicKey, NULL);
    jint publicKeyLength = (*env)->GetArrayLength(env, publicKey);
    jbyte* privateKeyBufferPtr =  (*env)->GetByteArrayElements(env, privateKey, NULL);
    jint privateKeyLength = (*env)->GetArrayLength(env, privateKey);

    char* publicKeyCharPtr = (char*)publicKeyBufferPtr;
    char* privateKeyCharPtr = (char*)privateKeyBufferPtr;
    // call the keypair function
    indcpa_keypair(publicKeyCharPtr, privateKeyCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyBufferPtr, 0);

 }

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_pkeEncrypt
(JNIEnv * env, jobject obj, jbyteArray cipherText, jbyteArray plainText, jbyteArray seed, jbyteArray publicKey){
    jbyte* cipherTextBufferPtr =  (*env)->GetByteArrayElements(env, cipherText, NULL);
    jbyte* plainTextBufferPtr =  (*env)->GetByteArrayElements(env, plainText, NULL);
    jbyte* seedBufferPtr =  (*env)->GetByteArrayElements(env, seed, NULL);
    jbyte* publicKeyBufferPtr =  (*env)->GetByteArrayElements(env, publicKey, NULL);

    char* cipherTextCharPtr = (char*)cipherTextBufferPtr;
    char* plainTextCharPtr = (char*)plainTextBufferPtr;
    char* seedCharPtr = (char*)seedBufferPtr;
    char* publicKeyCharPtr = (char*)publicKeyBufferPtr;
    // call the encrypt function
    indcpa_enc(cipherTextCharPtr, plainTextCharPtr, publicKeyCharPtr, seedCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, cipherText, cipherTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, plainText, plainTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, seed, seedBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, publicKey, publicKeyBufferPtr, 0);
}

JNIEXPORT void JNICALL Java_tw_edu_ntu_lads_chouguting_java_cipers_KyberEngine_pkeDecrypt
(JNIEnv * env, jobject obj, jbyteArray plainText, jbyteArray cipherText, jbyteArray privateKey){
    jbyte* plainTextBufferPtr =  (*env)->GetByteArrayElements(env, plainText, NULL);
    jbyte* cipherTextBufferPtr =  (*env)->GetByteArrayElements(env, cipherText, NULL);
    jbyte* privateKeyBufferPtr =  (*env)->GetByteArrayElements(env, privateKey, NULL);

    char* plainTextCharPtr = (char*)plainTextBufferPtr;
    char* cipherTextCharPtr = (char*)cipherTextBufferPtr;
    char* privateKeyCharPtr = (char*)privateKeyBufferPtr;
    // call the decrypt function
    indcpa_dec(plainTextCharPtr, cipherTextCharPtr, privateKeyCharPtr);
    // release the buffer
    (*env)->ReleaseByteArrayElements(env, plainText, plainTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, cipherText, cipherTextBufferPtr, 0);
    (*env)->ReleaseByteArrayElements(env, privateKey, privateKeyBufferPtr, 0);

}