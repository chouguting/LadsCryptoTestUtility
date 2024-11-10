package ciphers

import androidx.compose.ui.text.toUpperCase
import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.asn1.sec.SECNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.ECPointUtil
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.math.ec.ECAlgorithms
import org.bouncycastle.math.ec.ECCurve
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.util.encoders.Hex
import org.json.JSONObject
import org.kotlincrypto.hash.sha3.SHAKE128
import org.kotlincrypto.hash.sha3.SHAKE256
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import java.math.BigInteger
import java.security.*
import java.security.spec.*


class ECDSAEngine(curveString: String) {

    enum class EcdsaCurve(val curveName: String) {
        P_256("P-256"), P_384("P-384"), P_521("P-521")
    }

    private val curve: EcdsaCurve = when (curveString.lowercase()) {
        "p-256" -> EcdsaCurve.P_256
        "p-384" -> EcdsaCurve.P_384
        "p-521" -> EcdsaCurve.P_521
        else -> EcdsaCurve.P_256
    }

    var generator: KeyPairGenerator
    var ecSpec: ECNamedCurveParameterSpec

    init {
        Security.addProvider(BouncyCastleProvider())
        ecSpec = ECNamedCurveTable.getParameterSpec(curve.curveName)
        generator = KeyPairGenerator.getInstance("ECDSA", "BC")
        generator.initialize(ecSpec)
    }

    fun generateKeyPair(): Pair<Pair<String, String>, String> {

        val keyPair = generator.generateKeyPair()

        //java.security.interfaces.ECPublicKey
        //        val x = CipherUtils.bytesToHexString(publicKey.w.affineX.toByteArray())
        //        val y = CipherUtils.bytesToHexString(publicKey.w.affineY.toByteArray())
        val publicKey = keyPair.public as ECPublicKey
        val x = CipherUtils.bytesToHexString(publicKey.q.affineXCoord.toBigInteger().toByteArray())
        val y = CipherUtils.bytesToHexString(publicKey.q.affineYCoord.toBigInteger().toByteArray())


        val private = keyPair.private
        val privateKeyValue = (private as ECPrivateKey).d
        val privateKeyHexString = CipherUtils.bytesToHexString(privateKeyValue.toByteArray())

        return Pair(Pair(x, y), privateKeyHexString)
    }


    fun validateECDSAKey(qx: String, qy: String): Boolean {
        val curveParams = ECNamedCurveTable.getParameterSpec(curve.curveName)
        val curve = curveParams.curve

        return try {
            curve.validatePoint(BigInteger(qx, 16), BigInteger(qy, 16))
            true
        } catch (e: Exception) {
            false
        }
    }

    fun fixHashAlgorithmString(hashAlgorithm: String): String {
        var hashAlgorithmString = hashAlgorithm

        //fix the hash algorithm string to match the one in BC
        if (hashAlgorithmString.contains("SHAKE-")) {
            hashAlgorithmString = hashAlgorithmString.replace("SHAKE-", "SHAKE")
        } else if (hashAlgorithmString.contains("SHA2-")) {
            hashAlgorithmString = hashAlgorithmString.replace("SHA2-", "SHA-")
        }
        return hashAlgorithmString
    }


    fun generateSignature(
        hashAlgorithm: String, messageHexString: String, privateKeyHexString: String
    ): Pair<String, String> {

        //fix the hash algorithm string to match the one in BC
        val hashAlgorithmString = fixHashAlgorithmString(hashAlgorithm)

        //convert hex string to private key
//        val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(curve.curveName)
        val privateKeySpec = ECPrivateKeySpec(BigInteger(privateKeyHexString, 16), ecSpec)
        val keyFactory = KeyFactory.getInstance("ECDSA", "BC")
        val privateKey = keyFactory.generatePrivate(privateKeySpec)

        //hash the message
        val digest = MessageDigest.getInstance(hashAlgorithmString, "BC") //SHA256, SHA3-512, SHAKE128, SHAKE256...
        var hashedMessageByteArray = digest.digest(CipherUtils.hexStringToBytes(messageHexString)) //hashed message

        //shake 128 and 256 are special cases
        if (hashAlgorithmString.contains("SHAKE")) {
            val shakeAlgorithm = when (hashAlgorithmString) {
                "SHAKE128" -> SHAKE128(16) //128 bit = 16 byte output
                "SHAKE256" -> SHAKE256(32) //256 bit = 32 byte output
                else -> SHAKE128(32)
            }
            //update the hashed message
            hashedMessageByteArray = shakeAlgorithm.digest(CipherUtils.hexStringToBytes(messageHexString))
        }

        //sign the hashed message
        val ecdsaSign = Signature.getInstance("NONEwithECDSA", "BC")
        ecdsaSign.initSign(privateKey)
        ecdsaSign.update(hashedMessageByteArray)
        val signature = ecdsaSign.sign()

        val asn1Sequence = ASN1Sequence.getInstance(signature)
        val r = CipherUtils.bytesToHexString((asn1Sequence.getObjectAt(0) as ASN1Integer).value.toByteArray())
        val s = CipherUtils.bytesToHexString((asn1Sequence.getObjectAt(1) as ASN1Integer).value.toByteArray())
        return Pair(r, s)
    }


    fun verifySignature(message: String, hashAlgorithm: String, qx: String, qy: String, r: String, s: String): Boolean {
        val hashAlgorithmString = fixHashAlgorithmString(hashAlgorithm)  //fix the hash algorithm string to match the one in BC
//
//        val curveSpec = ECNamedCurveSpec(ecSpec.name, ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h, ecSpec.seed)
//        val pointSpec = ECPoint(BigInteger(qx, 16), BigInteger(qy, 16))
//        val pubKeySpec = ECPublicKeySpec(pointSpec, curveSpec)
//
//        val kf = KeyFactory.getInstance("ECDSA", "BC")
//        val publicKey = kf.generatePublic(pubKeySpec)
//
//        val ecdsaVerify = Signature.getInstance("NONEwithECDSA", "BC")
//        ecdsaVerify.initVerify(publicKey)
//
//        val digest = MessageDigest.getInstance(hashAlgorithmString, "BC")
//        val hashedMessage = digest.digest(message.toByteArray())
//        ecdsaVerify.update(hashedMessage)
//
//        val signatureBytes = DERSequence(arrayOf(ASN1Integer(BigInteger(r, 16)), ASN1Integer(BigInteger(s, 16)))).encoded
//
//        return ecdsaVerify.verify(signatureBytes)
        val params = ECNamedCurveTable.getParameterSpec(curve.curveName)
        val curve = ECDomainParameters(params.curve, params.g, params.n, params.h)
//        val publicKey = FixedPointCombMultiplier().multiply(curve.g, BigInteger(qx,16)).add(curve.g.multiply(BigInteger(qy,16)))
        val publicKey = curve.curve.createPoint(BigInteger(qx, 16), BigInteger(qy, 16))
        val signer = ECDSASigner()
        signer.init(false, ECPublicKeyParameters(publicKey, curve))

        val digest = MessageDigest.getInstance(hashAlgorithmString, "BC")
        var hashedMessage = digest.digest(CipherUtils.hexStringToBytes(message))
        //shake 128 and 256 are special cases
        if (hashAlgorithmString.contains("SHAKE")) {
            val shakeAlgorithm = when (hashAlgorithmString) {
                "SHAKE128" -> SHAKE128(16) //128 bit = 16 byte output
                "SHAKE256" -> SHAKE256(32) //256 bit = 32 byte output
                else -> SHAKE128(32)
            }
            //update the hashed message
            hashedMessage = shakeAlgorithm.digest(CipherUtils.hexStringToBytes(message))
        }


        return signer.verifySignature(hashedMessage, BigInteger(r, 16), BigInteger(s, 16))
    }


    companion object {
        fun runEcdsaWithTestCase(
            currentTestGroupJsonObject: JSONObject,
            testCaseJsonObject: JSONObject,
            curve: String,
            ecdsaOperationMode: String
        ) {
            val ecdsaEngine = ECDSAEngine(curve)
            when (ecdsaOperationMode) {
                "keyGen" -> {
                    val keyPair = ecdsaEngine.generateKeyPair()
                    val publicKeyCurvePointXHexString = keyPair.first.first
                    val publicKeyCurvePointYHexString = keyPair.first.second
                    val privateKeyHexString = keyPair.second
                    testCaseJsonObject.put("qx", publicKeyCurvePointXHexString)
                    testCaseJsonObject.put("qy", publicKeyCurvePointYHexString)
                    testCaseJsonObject.put("d", privateKeyHexString)
                }

                "keyVer" -> {
                    val publicKeyCurvePointXHexString = testCaseJsonObject.getString("qx")
                    val publicKeyCurvePointYHexString = testCaseJsonObject.getString("qy")
                    val validationResult =
                        ecdsaEngine.validateECDSAKey(publicKeyCurvePointXHexString, publicKeyCurvePointYHexString)
                    val result = if (validationResult) "true" else "false"
                    testCaseJsonObject.put("testPassed", result)

                }

                "sigGen" -> {
                    if (!currentTestGroupJsonObject.has("d")) { //if no key, generate one
                        val keyPair = ecdsaEngine.generateKeyPair()
                        val publicKeyCurvePointXHexString = keyPair.first.first
                        val publicKeyCurvePointYHexString = keyPair.first.second
                        val privateKeyHexString = keyPair.second
                        currentTestGroupJsonObject.put(
                            "qx", publicKeyCurvePointXHexString
                        ) //have to add public key to test group
                        currentTestGroupJsonObject.put("qy", publicKeyCurvePointYHexString)
                        currentTestGroupJsonObject.put(
                            "d", privateKeyHexString
                        ) //have to add private key to test group too
                    }
                    val privateKeyHexString = currentTestGroupJsonObject.getString("d")
                    val messageHexString = testCaseJsonObject.getString("message")
                    val hashAlgorithm = currentTestGroupJsonObject.getString("hashAlg")
                    val signatureComponents =
                        ecdsaEngine.generateSignature(hashAlgorithm, messageHexString, privateKeyHexString)
                    val r = signatureComponents.first
                    val s = signatureComponents.second
                    testCaseJsonObject.put("r", r)
                    testCaseJsonObject.put("s", s)

                }

                "sigVer" -> {
                    val messageHexString = testCaseJsonObject.getString("message")
                    val qx = testCaseJsonObject.getString("qx")
                    val qy = testCaseJsonObject.getString("qy")
                    val hashAlgorithm = currentTestGroupJsonObject.getString("hashAlg")
                    val r = testCaseJsonObject.getString("r")
                    val s = testCaseJsonObject.getString("s")
                    val result = ecdsaEngine.verifySignature(messageHexString, hashAlgorithm, qx, qy, r, s)
                    val resultString = if (result) "true" else "false"
                    testCaseJsonObject.put("testPassed", resultString)

                }
            }


        }
    }
}


fun validateKey(curveName: String, qx: String, qy: String): Boolean {
    val curveParams = ECNamedCurveTable.getParameterSpec(curveName)
    val curve = curveParams.curve

    return try {
        curve.validatePoint(BigInteger(qx, 16), BigInteger(qy, 16))
        true
    } catch (e: Exception) {
        false
    }
}


fun verifySignature(
    message: String, curveName: String, hashAlgorithm: String, qx: String, qy: String, r: String, s: String
): Boolean {
    Security.addProvider(BouncyCastleProvider())

    val ecP = ECNamedCurveTable.getParameterSpec(curveName)

    val g = ecP.g
    val n = ecP.n
    val h = ecP.h

    val curve = ecP.curve
    val domain = ECDomainParameters(curve, g, n, h)

    val Q = curve.createPoint(BigInteger(qx, 16), BigInteger(qy, 16))

    val pubKey = ECPublicKeyParameters(Q, domain)

    val signer = ECDSASigner()
    signer.init(false, pubKey)

    var hash: ByteArray = MessageDigest.getInstance(hashAlgorithm).digest(CipherUtils.hexStringToBytes(message))
    println("hash: ${CipherUtils.bytesToHexString(hash)}")

    if(hashAlgorithm.contains("SHAKE")){
        val shakeAlgorithm = when (hashAlgorithm) {
            "SHAKE128" -> SHAKE128(16) //128 bit = 16 byte output
            "SHAKE256" -> SHAKE256(32) //256 bit = 32 byte output
            else -> SHAKE128(32)
        }
        //update the hashed message
        hash = shakeAlgorithm.digest(CipherUtils.hexStringToBytes(message))
        println("hash: ${CipherUtils.bytesToHexString(hash)}")
    }

    val bigIntR = BigInteger(r, 16)
    val bigIntS = BigInteger(s, 16)

    val result = signer.verifySignature(hash, bigIntR, bigIntS)
    return result
}




//fun main() {
//    val curveName = "P-521"
//    val hashAlgorithm = "SHAKE128"
//    val message =
//        "0518AD682D655AE064976BCB9A34E97D03A2E0C8FB0321A8937E5B15D0EE1A1E731DE02120E7C8C78C981C74E901AF8889ECF365D6F278E53ACD4FF52DE679810C54CD4ABD7269F608B7BB73E85C76E37488FC496F267341689D948ADDD07E470C05D05FE423118635628B4934E083CF572DC6AB2E8587DCB4B26DCEF99F5994"
//    val qx =
//        "01817C7EA306914A835AED2772BBC4CC2498899C964F396DCDE70AAB5C04813253AC5BF4E7FD0553C73DDBA998714B75C54C96499C404FDC9BF282485DED8087A3C1"  //The public key curve point x
//    val qy =
//        "01C2B1DF21C6BCF3D016B8E1736AD8B853C331A97B3E2FDC051594EDBEABF52481EF090B17E754B80EDDF625E8FF13E7F2CDA5D4511F43507611D2A1BD1ED092CE88"  //The public key curve point y
//    val keyResult = validateKey(curveName, qx, qy)
//    println(keyResult)
//    println("message: $message")
//    val r =
//        "0186BF05C4D751F7120A9B48D6632B8F2A900FC8633578E8907CCC3BF53A25AEAFEAC19093760DD27C374F73E2E3B8F43CE2A8784B7AC252397E12D785D8E85F7137"  //The signature component r
//    val s =
//        "0148505AD1B3E1C23E060536D485BF49293C9200FE0D515F1BDE85AB019C032553816EDBC57DE250396AEA17FFCEF2D201CC3C08A860B9E673697EC8AB431E388575"  //The signature component s
//    val result = verifySignature(message, curveName, hashAlgorithm, qx, qy, r, s)
//    println(result)
//}

fun main() {
    val curveName = "P-521"
    val qx =
        "D1115BF26986064CE358753B7930421A15A165FFCCEC78AF302F8C87F0638E41FC0920BCEB7C861D764A74E8F8CCD181881E6E3EAC8F30FF9960AC9CB9A91122B1"  //The public key curve point x
    val qy =
        "013B1B65B6F4D6BDC32C5C440135CF220BA79A09FB6FDC0F2B0457F2B7CDA7AF4A899BFEF74BCF39732DCFB730F282E91C9C431CE5F7F2D7A5F4C1AA47EDF192DBCA"  //The public key curve point y
    println(ECDSAEngine(curveName).validateECDSAKey(qx, qy))

//    val message = "B9715F1622BCA9EF6D06BA6456724AA020B554F0100D00F743B30026FC63D8D5DB75BFE0DD1C95E98CF3509C5E01B3AC42D7AB9C5E67FA78523D32B7A635E2BEA31866091E8C77CB51B3570A865234766AA65BDF217BEE417EDD6D54E8E79DE4FED6EF93771E539EB7CD184DA87856BF921A9F4319E19593ABDD27712A2E996A"
//    val r = "1C3B06A134BDE4DDBA984F04CA90F6A342EB4A09ED4B23E0C5247268799E8680859B3EA9A12D8490F1FE7E7D995CAF5C7FDB1C836C55922D22305E1A3F5F5C74E6A"
//    val s = "DA555287AFFD2F5D048FCBED7AE2DCB1DCB908D67D9FBA577355E08F3E2338FA7077AB591299FAEC5F34663E004B8486C2A07C4422BB483D3A2EC572230FFBAB13"
//    val ecdsaEngine = ECDSAEngine(curveName)
//    println(ecdsaEngine.verifySignature(message, "SHAKE-256", qx, qy, r, s))
//    println(ecdsaEngine.validateECDSAKey(qx, qy))
}
