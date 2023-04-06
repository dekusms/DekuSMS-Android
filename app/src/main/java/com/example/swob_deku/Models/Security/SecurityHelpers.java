package com.example.swob_deku.Models.Security;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public class SecurityHelpers {


    public final static String FIRST_HEADER = "--D.E.K.U.start---";
    public final static String END_HEADER = "--D.E.K.U.end---";

    public static X509Certificate generateCertificate(KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeyException, IOException, CertificateException, OperatorCreationException, InvalidKeySpecException {
        // Create self-signed certificate
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        SubjectPublicKeyInfo subjectPublicKeyInfo = SubjectPublicKeyInfo.getInstance(publicKeySpec.getEncoded());

        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                new X500Name("CN=DH Test Certificate"), // subject
                BigInteger.valueOf(new SecureRandom().nextLong()), // serial number
                new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000L), // not before
                new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000L), // not after
                new X500Name("CN=DH Test Certificate"), // issuer
                subjectPublicKeyInfo);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder("SHA256withDSA");
        signerBuilder.setProvider("BC");
        ContentSigner signer = signerBuilder.build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(builder.build(signer));
    }

    public static byte[][] txAgreementFormatter(byte[] agreementKey) {
        byte[] firstHeader = FIRST_HEADER.getBytes(StandardCharsets.US_ASCII);
        byte[] endHeader = END_HEADER.getBytes(StandardCharsets.US_ASCII);

        int dstLen = 140 - firstHeader.length;
        int dstLen1 = agreementKey.length - 140;

        byte[] startKey = new byte[140];
        byte[] endKey = new byte[agreementKey.length - dstLen + endHeader.length];

        System.arraycopy(firstHeader, 0, startKey, 0, firstHeader.length);
        System.arraycopy(agreementKey, 0, startKey, firstHeader.length,  dstLen);

        System.arraycopy(endHeader, 0, endKey, 0, endHeader.length);
        System.arraycopy(agreementKey, dstLen, endKey, endHeader.length,  agreementKey.length-dstLen);

        return new byte[][]{startKey, endKey};
    }

    public static byte[] rxAgreementFormatter(byte[][] agreementKey) {

        byte[] firstHeader = FIRST_HEADER.getBytes(StandardCharsets.US_ASCII);
        byte[] endHeader = END_HEADER.getBytes(StandardCharsets.US_ASCII);

        int dstLen = agreementKey[0].length - firstHeader.length;
        int dstLen1 = agreementKey[1].length - endHeader.length;

        byte[] agreementPubKey = new byte[dstLen + dstLen1];

        System.arraycopy(agreementKey[0], firstHeader.length, agreementPubKey, 0, dstLen);

        System.arraycopy(agreementKey[1], endHeader.length, agreementPubKey, dstLen, dstLen1);

        return agreementPubKey;
    }
}