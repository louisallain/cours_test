/**
   This file contains functions that permits to verify (or generate) RSA signature.
*/
#include <mbedtls/pk.h>
#include <mbedtls/rsa.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>

mbedtls_entropy_context entropy;
mbedtls_ctr_drbg_context ctr_drbg;
char *personalization = "louis";
// public key context
mbedtls_pk_context public_key_context;
// private key context
mbedtls_pk_context private_key_context;

// These keys are for tests only.

const unsigned char pub_key[] =
  "-----BEGIN PUBLIC KEY-----\r\n"
  "MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBmRHJh5b4p+Fl4W0U82+1z\r\n"
  "u89EuNUkBJrZKldxUBRMCdc0B/kkIT92zJMY0CV9urogd+VnG5WghNqNv5z7sORl\r\n"
  "Yno2UwFeAuAja0HbzLXSTiJ24Lk7U7svD+mSR7GTcKOmi7JcfxrxEaI+6HECjBIC\r\n"
  "UKBsUGrF4IdcrXUKpxtRpiBxCqnsRyy9sTU8llT9xmhmwm4aXL2WmEvt4hqHDtQ7\r\n"
  "yIwfKzFMt7QQYNYa74lrkaE3RT35v15LL3T5pRNmA/G72QJ93f5oIzTmDk5P5ER6\r\n"
  "QdeO2ctwY/3UcSYDT0x/Mqq0+jVZLEjEpxhVBRspKO1I8c2AGvGLRFbmF8Vki4HZ\r\n"
  "AgMBAAE=\r\n"
  "-----END PUBLIC KEY-----\r\n";


const unsigned char prv_key[] =
  "-----BEGIN RSA PRIVATE KEY-----\r\n"
  "MIIEowIBAAKCAQBmRHJh5b4p+Fl4W0U82+1zu89EuNUkBJrZKldxUBRMCdc0B/kk\r\n"
  "IT92zJMY0CV9urogd+VnG5WghNqNv5z7sORlYno2UwFeAuAja0HbzLXSTiJ24Lk7\r\n"
  "U7svD+mSR7GTcKOmi7JcfxrxEaI+6HECjBICUKBsUGrF4IdcrXUKpxtRpiBxCqns\r\n"
  "Ryy9sTU8llT9xmhmwm4aXL2WmEvt4hqHDtQ7yIwfKzFMt7QQYNYa74lrkaE3RT35\r\n"
  "v15LL3T5pRNmA/G72QJ93f5oIzTmDk5P5ER6QdeO2ctwY/3UcSYDT0x/Mqq0+jVZ\r\n"
  "LEjEpxhVBRspKO1I8c2AGvGLRFbmF8Vki4HZAgMBAAECggEAOlkzKcf7B9c6tQDe\r\n"
  "MCsK/81YJM2QTkySTPVrjqsx6XBz6Kvj6klN/GocQzv/KA0xxPSjXoDOjhc+20FI\r\n"
  "QBJdPbtCH1ycmxbo60x29wGFe8g4wbrV9JOBaQibK2rcVlMeDBgIW93pcUGuYFYe\r\n"
  "pGVmoQt/fE0lnmDM2oPdE4bkf2CgwhmpTRGinYcJVM4TpYv7tsYPMeSCPsPjAPxF\r\n"
  "+F5dHNE564UxsFBaBlLs8eSe+4yOmKpzS6dqE0aThXN1z5BezyMLyIdbAdZqyo2O\r\n"
  "NqMHcEBETOswrrR2UM0DMJsGuYQZM854uPe3b1YwdtPpMLio6YL1dEBMkdof3tSz\r\n"
  "e15GQQKBgQCnjpgCty6C1r22LNVTuKpr/lASPhi5U2y4h+vXcec4uTVNmI4TyLfg\r\n"
  "eXHQ817tthX44SPUI6lo9HSkxkUY2CgDfxwqnGczH2Y80RKgJOyb1U9fzq0pMGQL\r\n"
  "tbyPGONCB0Ndd6qVNzCHhDkDH+ekHEAu3EmUedlOkFWV6JrvF5vmgwKBgQCcP3zD\r\n"
  "NEEySbEtJwkip88wM9q2LaFRd5lTLFkFfPiTi+XK+ywySnnJIysi6sIdg+QXYF2U\r\n"
  "aAg58tKlnbFKm9aS/fUSvDK+g6l+X75G10TKJEq6QxSpFVpIn0j95tHBX27KSFJ/\r\n"
  "VCaVRujFezEuuIyNjcRh1ZSN7vJbYiAmC/cncwKBgH55R4xlMJpZ9QDZfnyfWyQA\r\n"
  "5fefH5JjwiDXl9EbjOhoBC/6AuQ0EXdCtAKAcRsE4jjl1+F8uZcbTiBB+E30et+I\r\n"
  "xn3zaIJSJR2qwmBW/rHxpOQwYMxCVoHwP8/TfVaNnO+kMAJJkjv4NgCByJs7J8c3\r\n"
  "R+LmxnxivfdVh/0I2Qh5AoGBAJk9Psxn7GU4lvbUqQX/FJmO0bsIh4VRyeMjxG84\r\n"
  "gMacVxO7QT5Vgpm8zyqgmR6/Yq12inDpkt/agbOCNAYbTte9EGV+hDoLAOl7Vy89\r\n"
  "Iy8pZszEy3eFBJXi+oBhp2iCgzMKuTY9vtV6xQIhbzwGLXsLBgZ5pQeKyNDNLQCD\r\n"
  "jbN3AoGBAJxpNbsBk6KoeiA6znrdp4QiWz6Q5trxkRS4OVC6UAaPT2y3mi9FR8Jq\r\n"
  "EtuMqvUIgfRkYA2nUvoEVJk/41VK7KSd0yKoUSferkG7yuck33ZDhNo7d8kuj4ez\r\n"
  "eHvAjEYbPL+Nlp0poWO0gBCOU08CDFTX52eRsW9gTyWES9s/4ZwM\r\n"
  "-----END RSA PRIVATE KEY-----\r\n";

/**
   Convert an hex string (ie: "4851BAA3759A3") to byte string.
   @param hexstr hex string to be converted.
   @return the byte array converted from hexstr.
*/
unsigned char* hexstr_to_char(const char* hexstr) {
  size_t len = strlen(hexstr);
  if (len % 2 != 0)
    return NULL;
  size_t final_len = len / 2;
  unsigned char* chrs = (unsigned char*)malloc((final_len + 1) * sizeof(*chrs));
  for (size_t i = 0, j = 0; j < final_len; i += 2, j++)
    chrs[j] = (hexstr[i] % 32 + 9) % 25 * 16 + (hexstr[i + 1] % 32 + 9) % 25;
  chrs[final_len] = '\0';
  return chrs;
}

/**
   Initialize randomness of RSA algorithms.
   @return != 0 if error.
*/
int init_randomness() {

  int ret = 0;

  // random data generator
  mbedtls_entropy_init(&entropy);

  // randomness with seed
  mbedtls_ctr_drbg_init(&ctr_drbg);

  ret = mbedtls_ctr_drbg_seed(&ctr_drbg , mbedtls_entropy_func, &entropy, (const unsigned char *) personalization, strlen( personalization ));
  if ( ret != 0 ) {
    Serial.printf( " failed\n ! mbedtls_ctr_drbg_seed returned -0x%04x\n", -ret );
  }
  mbedtls_ctr_drbg_set_prediction_resistance(&ctr_drbg, MBEDTLS_CTR_DRBG_PR_ON);

  return ret;
}

/**
   Initialize public key context and parse public key.
   @return != 0 if error.
*/
int init_public_key() {

  int ret = 0;

  mbedtls_pk_init( &public_key_context );
  if (( ret = mbedtls_pk_parse_public_key( &public_key_context, pub_key, sizeof(pub_key)) ) != 0) {
    Serial.printf( " failed\n ! mbedtls_pk_parse_public_keyfile returned -0x%04x\n", -ret );
  }
  return ret;
}

/**
   Initialize private key context and parse private key.
   @return != 0 if error.
*/
int init_private_key() {

  int ret = 0;

  mbedtls_pk_init(&private_key_context);
  if ( ( ret = mbedtls_pk_parse_key( &private_key_context, prv_key, sizeof(prv_key), NULL, 0 ) ) != 0 ) {
    Serial.printf( " failed\n ! mbedtls_pk_parse_keyfile returned -0x%04x\n", -ret );
  }
  return ret;
}

/**
   Sign a hash (SHA512) of a message using the private key context initialize with method init_private_key.
   @param hash the SHA512 hash of a message.
   @param signature a bytes array of 256 bytes representing the signature of the param hash using the private key context initialize with method init_private_key.
   @param signatureLength the output length of the signature (should be 256).
   @return
*/
int sign_hash(const unsigned char * hash, unsigned char * signature, size_t * signatureLength) {
  

  int ret = 0;
  size_t hash_len = 1024;
  if ( ( ret = mbedtls_pk_sign( &private_key_context, MBEDTLS_MD_NONE, hash, hash_len, signature, signatureLength, mbedtls_ctr_drbg_random, &ctr_drbg ) ) != 0 ) {
    Serial.printf( " failed\n  ! mbedtls_pk_sign returned -0x%04x\n", -ret );
  }

  return ret;
}

/**
   Verify a signature of a hash (SHA512) message using the public key context initialize with method init_public_key.
   @param hexSignature the signature represented by an string of hex (ie. "4851BAA3759A3...").
   @param hash the hash (SHA512) of the message to be signature verified.
   @return 0 if the signature is verified, !=0 otherwise.
*/
int verify_signature_of_hash(const char * hexSignature, const unsigned char * hash) {

  int ret = 0;
  unsigned char * parsedSignature = hexstr_to_char(hexSignature); // convert hex string to byte array.

  if ( ( ret = mbedtls_pk_verify( &public_key_context, MBEDTLS_MD_SHA512, hash, 0, parsedSignature, 256) ) != 0 ) {
    Serial.printf( " failed\n  ! mbedtls_pk_verify returned -0x%04x\n", -ret );
  }
  free(parsedSignature); // free because hexstr_to_char malloc this

  return ret;
}

/**
   Test the functions above.
*/
int RSA_test() {

  int ret = 0;

  // Initialization
  init_randomness();
  init_public_key();
  init_private_key();

  unsigned char hash[128] = "cbe2ce6f2403e3abf3aeb9bb06766d3d3547e710d56e11044d8352e976bf7e473232639509b4822ff2d1b88e95d5bbb883e551ef031d0ff7699d945b72c3c1ab";
  unsigned char* hashBytes = hexstr_to_char(hash);
  
  // Measure
  unsigned long endTime, startTime;

  // SIGNATURE
  unsigned char signature[256];
  size_t signatureLength = 0;
  startTime = millis();
  ret = sign_hash(hashBytes, signature, &signatureLength);
  endTime = millis();
  Serial.printf("\nMeasure of sign (ms) = %lu", (endTime - startTime));

  if (ret == 0) {
    Serial.printf("\nLength of signature = %d\n", signatureLength);
    Serial.printf(" \n Signature = \n\n");
    for (size_t i = 0; i < signatureLength; ++i) Serial.printf("%02X", signature[i]); // print hex of signature
    Serial.printf("\n End of signature \n");
  }

  /*
  // VERIFICATION
  // hexSignature has been generated thanks to sign_hash function. (see commented bloc just above).
  const char* hexSignature = "6B127EAC1A5FD97C6E15BA0987FE1E40FF2FAD99812FE58257CF5CB658F1FBBCFF04AECD87742D561A5B0C9F523C49513DD88832C5E034800E0C7CCF992C4BC17762748CAC252980BE6B5FF4ACB66FB2AF748F10925D87AC83E1710FA5F53F4464302DA9D92AFA849B9B476E96D00D6F70D7B8CF43EA46D981C85ADFF37664BF5A8A71D365FCF9340EE9D0BF5585ED73EDD3839BFC2F9AEF2356E2474D60BFB85366CC7FA03866B822709CF4B8B62DE6B4AC8646751ED88AC68E15CF07EE8697AEA1091BD925DAE6EB4C46D2683D84F0F8D38294AEC24F00C8C4872A88C927B695D1F8C89DBF9CA79430646C05CB3024D1D9DD917A48CB51A49630412BB33B44";
  startTime = millis();
  ret = verify_signature_of_hash(hexSignature, SHA512HashedMessage);
  endTime = millis();
  Serial.printf("\nMeasure of verify (ms) = %lu", (endTime - startTime));
  if (ret != 0) {
    Serial.println("\nVERFICATION FAILED...\n");
  }
  else {
    Serial.println("\nSignature verified !\n");
  }
  */
  return 0;
}

void setup()
{
  Serial.begin(115200);

}

void loop()
{
  int res = 0;

  if ((res = RSA_test()) != 0) {
    Serial.printf("\n  . Error RSA_test_gen");
  }

  delay(5000);
}
