package org.devcon.ticket;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.tokenscript.attestation.FullProofOfExponent;
import org.tokenscript.attestation.IdentifierAttestation.AttestationType;
import org.tokenscript.attestation.core.AttestationCrypto;
import org.tokenscript.attestation.core.DERUtility;
import org.tokenscript.attestation.core.URLUtility;

public class Validator {

  static SecureRandom rand = new SecureRandom();
  static AttestationCrypto crypto = new AttestationCrypto(rand);

  public static void main(String... args) {
    BigInteger sharedSecret = crypto.makeSecret();

    if (args.length != 4) {
      System.err.println("Commandline Options:");
      System.err.println(
          "{key.pem}\tPath to the PEM file that contains the issuer's public elliptic curve key in RFC 5915 format.");
      System.err.println("{ticket}\tThe signed DevCon ticket as a string in URL encoding.");
      System.err.println(
          "{pok}\tThe proof that the supplied email is the one the ticket was issued towards as a string in URL encoding.");
      System.err.println("{mail}\tThe email address of the ticket owner.");
    } else {
      try {
        Path keyFile = Paths.get(args[0]);
        String ticketInUrl = args[1];
        String pokInUrl = args[2];
        String mail = args[3];

        byte[] dataCER = DERUtility.restoreBytes(Files.readAllLines(keyFile));
        AsymmetricKeyParameter issuerPubKey = DERUtility.restoreRFCRFC5915Key(dataCER);
        TicketDecoder ticketDecoder = new TicketDecoder(issuerPubKey);
        Ticket ticket = ticketDecoder.decode(URLUtility.decodeData(ticketInUrl));
        if (!ticket.checkValidity()) {
          throw new RuntimeException(
              "Something went wrong and the constructed ticket could not be validated");
        }
        if (!ticket.verify()) {
          throw new RuntimeException(
              "Something went wrong and the constructed ticket could not be verified");
        }
        FullProofOfExponent internalPok = new FullProofOfExponent(URLUtility.decodeData(pokInUrl));
        PublicIdentifierProof pok = new PublicIdentifierProof(ticket.getCommitment(), mail,
            AttestationType.EMAIL, internalPok);
        if (!pok.verify()) {
          throw new RuntimeException(
              "Something went wrong and the commitment in the ticket could not be verified according to the email");
        }
        System.out.println("Ticket is VALID and was issued to email " + mail);
      } catch (Exception e) {
        System.err.println(
            "Something went wrong. Ticket is NOT validated! Please check the supplied arguments again and ensure that the public key is an elliptic curve key in RFC 5915 format.");
        throw new RuntimeException("Could not validate ticket", e);
      }
    }
  }
}