#include <mbedtls/pk.h>
#include <mbedtls/rsa.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include <mbedtls/md.h>
#include "os.h"
#include <stdio.h>
#include <stdlib.h>



/**
 * Convert an hex string (ie: "4851BAA3759A3") to byte string.
 * @param hexstr hex string to be converted.
 * @return the byte array converted from hexstr.
 */
unsigned char* hexstr_to_char(const char* hexstr) {
    size_t len = strlen(hexstr);
    if(len % 2 != 0)
        return NULL;
    size_t final_len = len / 2;
    unsigned char* chrs = (unsigned char*)malloc((final_len+1) * sizeof(*chrs));
    for (size_t i=0, j=0; j<final_len; i+=2, j++)
        chrs[j] = (hexstr[i] % 32 + 9) % 25 * 16 + (hexstr[i+1] % 32 + 9) % 25;
    chrs[final_len] = '\0';
    return chrs;
}

int RSA_test() {

  int ret=0;

  // random data generator
  mbedtls_entropy_context entropy;
  mbedtls_entropy_init( &entropy );

  // randomness with seed
  mbedtls_ctr_drbg_context ctr_drbg;
  char *personalization = "My RSA demo";
  mbedtls_ctr_drbg_init( &ctr_drbg );

  ret = mbedtls_ctr_drbg_seed( &ctr_drbg , mbedtls_entropy_func, &entropy,
                     (const unsigned char *) personalization,
                     strlen( personalization ) );
  if( ret != 0 )
  {
        // ERROR HANDLING CODE FOR YOUR APP
  }
  mbedtls_ctr_drbg_set_prediction_resistance( &ctr_drbg, MBEDTLS_CTR_DRBG_PR_ON );
////////////////////////////////////////////////////////////////////////  
  
  const unsigned char pub_key[]=
"-----BEGIN PUBLIC KEY-----\r\n"
"MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBmRHJh5b4p+Fl4W0U82+1z\r\n"
"u89EuNUkBJrZKldxUBRMCdc0B/kkIT92zJMY0CV9urogd+VnG5WghNqNv5z7sORl\r\n"
"Yno2UwFeAuAja0HbzLXSTiJ24Lk7U7svD+mSR7GTcKOmi7JcfxrxEaI+6HECjBIC\r\n"
"UKBsUGrF4IdcrXUKpxtRpiBxCqnsRyy9sTU8llT9xmhmwm4aXL2WmEvt4hqHDtQ7\r\n"
"yIwfKzFMt7QQYNYa74lrkaE3RT35v15LL3T5pRNmA/G72QJ93f5oIzTmDk5P5ER6\r\n"
"QdeO2ctwY/3UcSYDT0x/Mqq0+jVZLEjEpxhVBRspKO1I8c2AGvGLRFbmF8Vki4HZ\r\n"
"AgMBAAE=\r\n"
"-----END PUBLIC KEY-----\r\n";


  const unsigned char prv_pwd[]="password";
  const unsigned char prv_key[]=
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

  mbedtls_pk_context pk;
  mbedtls_pk_init( &pk );
  if( ( ret = mbedtls_pk_parse_public_key( &pk, pub_key, sizeof(pub_key)) ) != 0 ) {
    Serial.printf( " failed\n ! mbedtls_pk_parse_public_keyfile returned -0x%04x\n", -ret );
    return -1;
  }


  mbedtls_pk_context pk1;
  mbedtls_pk_init(&pk1);
  if( ( ret = mbedtls_pk_parse_key( &pk1, prv_key, sizeof(prv_key), NULL, 0 ) ) != 0 ) {
    Serial.printf( " failed\n ! mbedtls_pk_parse_keyfile returned -0x%04x\n", -ret );
    return -1;
  }

  const unsigned char hashOfTheMessageToSign[] = "cbe2ce6f2403e3abf3aeb9bb06766d3d3547e710d56e11044d8352e976bf7e473232639509b4822ff2d1b88e95d5bbb883e551ef031d0ff7699d945b72c3c1ab";

  /*
  // SIGNATURE
  unsigned char signature[256];
  size_t signatureLength = 0;
  
  Serial.printf( "\n  . Signing the message" );
  
  if( ( ret = mbedtls_pk_sign( &pk1, MBEDTLS_MD_SHA512, hashOfTheMessageToSign, 0, signature, &signatureLength, mbedtls_ctr_drbg_random, &ctr_drbg ) ) != 0 )
  {
      Serial.printf( " failed\n  ! mbedtls_pk_sign returned -0x%04x\n", -ret );
  }
  else {
 
    Serial.printf("\nLength of signature = %d\n", signatureLength);
    Serial.printf(" \n Signature = \n\n", signature);
    for (size_t i = 0; i < signatureLength; ++i) Serial.printf("%02X", signature[i]);
    Serial.printf("\n End of signature \n");
  }
  */

  // VERIFICATION
  Serial.printf( "\n  . Verifying the message" );
  const char* hexSignature = "4851BAA3759A33514D86725EE48BB17528A214837B7A039CF6714DF0B487ED2FF143CD08CD3020FF5C8043A86654D0A58B22F48032990CB037BD61D4C2EA7266910CD649B7573D4E8844D6DB32797D6F4FC15D285DECAD0E7DFB709198C3901C699AABB129B179F6575957AC8D3D74F21B3219952B3D70E1838E879404CA8F3075B5705F8045591F449ED14FE07C249510E19488696CA8AFC27AFAD9C38B01E6B704D843694A93DA4CA24E05F4D9331E78B33FEFE101E2473B86188288ECDD869E37A45EEBC43020A8997C4CC5345B05F7E0CCF6A97AE3A93140FEFA364B4CE8571B5CCBFBD705C14DB5A5D62E6454BD6D1263EB17F054FCF3F84F0AC3EAE1C7";
  unsigned char * parsedSignature = hexstr_to_char(hexSignature);
  
  if( ( ret = mbedtls_pk_verify( &pk, MBEDTLS_MD_SHA512, hashOfTheMessageToSign, 0, parsedSignature, 256) ) != 0 ) {
      Serial.printf( " failed\n  ! mbedtls_pk_verify returned -0x%04x\n", -ret );
  }
  else {
      Serial.printf("\n Signature verified !");
  }
  free(parsedSignature); // free because hexstr_to_char malloc this
  

  return 0;
}

void setup()
{
  Serial.begin(115200);

}

void loop()
{
  int res = 0;
  
  if((res = RSA_test()) != 0) {
    Serial.printf("\n  . Error RSA_test_gen");
  }
 
  delay(5000);
}
