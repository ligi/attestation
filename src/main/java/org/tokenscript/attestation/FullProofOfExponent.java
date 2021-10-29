package org.tokenscript.attestation;

import org.tokenscript.attestation.core.AttestationCrypto;
import org.tokenscript.attestation.core.ExceptionUtil;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.math.ec.ECPoint;

public class FullProofOfExponent implements ProofOfExponent {
  private static final Logger logger = LogManager.getLogger(FullProofOfExponent.class);
  private final ECPoint riddle;
  private final ECPoint tPoint;
  private final BigInteger challenge;
  private final byte[] unpredictableNumber;
  private final byte[] encoding;

  public FullProofOfExponent(ECPoint riddle, ECPoint tPoint, BigInteger challenge, byte[] unpredictableNumber) {
    this.riddle = riddle;
    this.tPoint = tPoint;
    this.challenge = challenge;
    this.unpredictableNumber = unpredictableNumber;
    this.encoding = makeEncoding(riddle, tPoint, challenge, unpredictableNumber);
  }

  public FullProofOfExponent(ECPoint riddle, ECPoint tPoint, BigInteger challenge) {
    this(riddle, tPoint, challenge, new byte[0]);
  }

  public FullProofOfExponent(byte[] derEncoded) {
    this.encoding = derEncoded;
    try {
      ASN1InputStream input = new ASN1InputStream(derEncoded);
      ASN1Sequence asn1 = ASN1Sequence.getInstance(input.readObject());
      input.close();
      int asn1counter = 0;
      ASN1OctetString riddleEnc = ASN1OctetString.getInstance(asn1.getObjectAt(asn1counter++));
      this.riddle = AttestationCrypto.decodePoint(riddleEnc.getOctets());
      ASN1OctetString challengeEnc = ASN1OctetString.getInstance(asn1.getObjectAt(asn1counter++));
      this.challenge = new BigInteger(challengeEnc.getOctets());
      ASN1OctetString tPointEnc = ASN1OctetString.getInstance(asn1.getObjectAt(asn1counter++));
      this.tPoint = AttestationCrypto.decodePoint(tPointEnc.getOctets());
      this.unpredictableNumber = ASN1OctetString.getInstance(asn1.getObjectAt(asn1counter++)).getOctets();
    } catch (IOException e) {
      throw ExceptionUtil.makeRuntimeException(logger, "Could not decode asn1", e);
    }
  }

  private byte[] makeEncoding(ECPoint riddle, ECPoint tPoint, BigInteger challenge, byte[] unpredictableNumber) {
    try {
      ASN1EncodableVector res = new ASN1EncodableVector();
      res.add(new DEROctetString(riddle.getEncoded(false)));
      res.add(new DEROctetString(challenge.toByteArray()));
      res.add(new DEROctetString(tPoint.getEncoded(false)));
      res.add(new DEROctetString(unpredictableNumber));
      return new DERSequence(res).getEncoded();
    } catch (IOException e) {
      throw ExceptionUtil.makeRuntimeException(logger, "Could not encode asn1", e);
    }
  }

  public ECPoint getRiddle() {
    return riddle;
  }

  @Override
  public ECPoint getPoint() {
    return tPoint;
  }

  @Override
  public BigInteger getChallenge() {
    return challenge;
  }

  @Override
  public byte[] getUnpredictableNumber() { return unpredictableNumber; }

  public UsageProofOfExponent getUsageProofOfExponent() {
    return new UsageProofOfExponent(tPoint, challenge, unpredictableNumber);
  }

  @Override
  public byte[] getDerEncoding() {
    return encoding;
  }

}
