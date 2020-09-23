package eu.miaplatform.customplugin.springboot.utils;

import eu.miaplatform.customplugin.springboot.models.EmailMessage;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public  class SignatureUtils {
    public static byte[] genSignature(String message, PrivateKey privKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature sign = Signature.getInstance("SHA256withDSA");
        sign.initSign(privKey);
        sign.update(message.getBytes());
        return sign.sign();
    }

    public static boolean checkSignature(String message, byte[] signature, PublicKey pubKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withDSA");
        sign.initVerify(pubKey);
        sign.update(message.getBytes());
        return sign.verify(signature);
    }

    public static KeyPair genKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
        keyPairGen.initialize(2048);
        return keyPairGen.generateKeyPair();
    }

    public static PublicKey getPublicKey(EmailMessage emailResponseBody) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] pubKeyBytes = decoder.decode(emailResponseBody.getPubKey());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("DSA");
        return keyFactory.generatePublic(keySpec);
    }
}
