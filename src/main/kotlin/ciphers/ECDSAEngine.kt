package ciphers

import org.bouncycastle.asn1.ASN1Integer
import org.bouncycastle.asn1.ASN1Sequence
import org.bouncycastle.asn1.DERSequence
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import org.bouncycastle.jce.spec.ECNamedCurveSpec
import org.bouncycastle.util.encoders.Hex

import org.json.JSONObject
import tw.edu.ntu.lads.chouguting.java.cipers.CipherUtils
import java.math.BigInteger
import java.security.*
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint

import java.security.spec.ECPublicKeySpec

class ECDSAEngine(curveString: String) {

    enum class EcdsaCurve(val curveName: String) {
        P_256("P-256"), P_384("P-384"), P_521("P-521")
    }

    private val curve: EcdsaCurve =
        when(curveString.lowercase()){
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
        val privateKey = keyPair.private.encoded
        val publicKey = keyPair.public as java.security.interfaces.ECPublicKey

        val x = CipherUtils.bytesToHexString(publicKey.w.affineX.toByteArray())
        val y = CipherUtils.bytesToHexString(publicKey.w.affineY.toByteArray())
        return Pair(Pair(x, y), CipherUtils.bytesToHexString(privateKey))


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




    companion object {
        fun runEcdsaWithTestCase(currentTestGroupJsonObject: JSONObject,testCaseJsonObject: JSONObject, curve: String, ecdsaOperationMode: String){
            val ecdsaEngine = ECDSAEngine(curve)
            when(ecdsaOperationMode){
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
                    val validationResult = ecdsaEngine.validateECDSAKey(publicKeyCurvePointXHexString, publicKeyCurvePointYHexString)
                    val result = if(validationResult) "true" else "false"
                    testCaseJsonObject.put("testPassed", result)

                }

                "sigGen" -> {
                    if(!currentTestGroupJsonObject.has("d")){
                        val keyPair = ecdsaEngine.generateKeyPair()
                        val publicKeyCurvePointXHexString = keyPair.first.first
                        val publicKeyCurvePointYHexString = keyPair.first.second
                        val privateKeyHexString = keyPair.second
                        currentTestGroupJsonObject.put("qx", publicKeyCurvePointXHexString)
                        currentTestGroupJsonObject.put("qy", publicKeyCurvePointYHexString)
                        currentTestGroupJsonObject.put("d", privateKeyHexString)
//                        println("Generated key pair for sigGen")
                    }


                }

                "sigVer" -> {

                }
            }




        }
    }
}

fun generateECDSAKeyPair() {
    Security.addProvider(BouncyCastleProvider())

    val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("P-256")
    val generator = KeyPairGenerator.getInstance("ECDSA", "BC")
    generator.initialize(ecSpec)

    val keyPair = generator.generateKeyPair()
    val privateKey = keyPair.private.encoded
    val publicKey = keyPair.public as java.security.interfaces.ECPublicKey

    val x = CipherUtils.bytesToHexString(publicKey.w.affineX.toByteArray())
    val y = CipherUtils.bytesToHexString(publicKey.w.affineY.toByteArray())

    println("Private Key: ${CipherUtils.bytesToHexString(privateKey)}")
    println("privateKey length: ${privateKey.size}")
    println("Public Key X: $x")
    println("Public Key Y: $y")
    println("Public Key: ${CipherUtils.bytesToHexString(publicKey.encoded)}")
    println("publicKey length: ${publicKey.encoded.size}")

//    for (i in 1..3) {
//        val keyPair = generator.generateKeyPair()
//        val publicKey = keyPair.public.encoded
//        val privateKey = keyPair.private.encoded
//
//        println("KeyPair $i")
//        println("Public Key: ${CipherUtils.bytesToHexString(publicKey)}")
//        println("Private Key: ${CipherUtils.bytesToHexString(privateKey)}")
//        println("-------------------------")
//    }
}

fun generateSignatureComponents(message: String, privateKey: java.security.PrivateKey): Pair<String, String> {
    val digest = MessageDigest.getInstance("SHA3-512", "BC") //SHA256, SHA3-512, SHAKE128, SHAKE256...
    val hashedMessage = digest.digest(message.toByteArray())

    val ecdsaSign = Signature.getInstance("NONEwithECDSA", "BC")
    ecdsaSign.initSign(privateKey)
    ecdsaSign.update(hashedMessage)
    val signature = ecdsaSign.sign()

    val asn1Sequence = ASN1Sequence.getInstance(signature)
    val r = Hex.toHexString((asn1Sequence.getObjectAt(0) as ASN1Integer).value.toByteArray())
    val s = Hex.toHexString((asn1Sequence.getObjectAt(1) as ASN1Integer).value.toByteArray())
    val ecdsaVerify = Signature.getInstance("SHA3-256withECDSA", "BC")

    return Pair(r, s)
}


//fun main() {
//    Security.addProvider(BouncyCastleProvider())
//
//    val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("P-256")
//    val generator = KeyPairGenerator.getInstance("ECDSA", "BC")
//    generator.initialize(ecSpec)
//
//    val keyPair = generator.generateKeyPair()
//    val privateKey = keyPair.private
//
//    val message = "0F8037C060FB6F74247C340A1616E77579996CF1F14197FEB0212CBD6180D90976C0BAC1626ED4CEBDC52DA3F87821F0B69F0A7E2EA475C9429A2E35CA2C3A511F2D8237069D1583189A96660BD998DF57F6EA9CD2F936F8F7C05F77F1ED69532C397C915778E6A03BDC77659D6D8AA11120223380678B9CBFD9B0ED3E505F8F"
//    val (r, s) = generateSignatureComponents(message, privateKey)
//
//    println("Signature Component R: $r")
//    println("Signature Component S: $s")
//}

fun verifySignature(message: String, qx: String, qy: String, r: String, s: String): Boolean {
    Security.addProvider(BouncyCastleProvider())

    val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec("P-521")
    val curveSpec = ECNamedCurveSpec(ecSpec.name, ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h, ecSpec.seed)
    val pointSpec = ECPoint(BigInteger(qx, 16), BigInteger(qy, 16))
    val pubKeySpec = ECPublicKeySpec(pointSpec, curveSpec)


    val kf = KeyFactory.getInstance("ECDSA", "BC")
    val publicKey = kf.generatePublic(pubKeySpec)

    val ecdsaVerify = Signature.getInstance("NONEwithECDSA", "BC")
    ecdsaVerify.initVerify(publicKey)
    val digest = MessageDigest.getInstance("SHAKE128", "BC")
    val hashedMessage = digest.digest(message.toByteArray())
    ecdsaVerify.update(hashedMessage)

    val signatureBytes = DERSequence(arrayOf(ASN1Integer(BigInteger(r, 16)), ASN1Integer(BigInteger(s, 16)))).encoded

    return ecdsaVerify.verify(signatureBytes)
}

fun main() {
    val message = "B5E82AD0F2108DB990B868375935534934F53E4DC2A8995FF32BCAB1A4F250FEEEC6ABDA8C775CD3A86E2823043BCE8FDAC8F6DE6BCA2A60C3B6484FE33628B8DC6D7327C42D122AD3A918ED085C153B7BADE8CCAB2C6C61EE6F089EA2307DC05B31801A77493DC295A10AD72C18D0CB55EA7F60F683EA5D88EA7101EFA7CCDD"
    val qx = "008247B7595003994274C760C418BCFCE62743B5D52D59C03EDB4D9B0FCFCF8E01DDAD254B75B55AE92A73B09272BB317AF868F5C1D370D227F245097F8FB36C22F3"
    val qy = "01ED6AECEEF9558D1EA4AF2B9386B32464FB3FE11F1B96AE10F40F38E0910DB10992E4160DBEBF98F0575F429F7D6B122BDFB7EEBD098427FC23DF9BE56B8B8603B6"
    val r = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
    val s = "01E84C23CD1307ADE0A1A2A111AF3B19D1504A082BA349039E85EA63C41CBBCA5E7EC9F61D5D63D0E2195CE9761885F2FA9BBA37FE89D5A848BE41A033A3A28A50DA"

    val isVerified = verifySignature(message, qx, qy, r, s)

    println("Signature Verified: $isVerified")
}