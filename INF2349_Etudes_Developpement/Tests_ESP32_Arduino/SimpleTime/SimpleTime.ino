#include <mbedtls/pk.h>
#include <mbedtls/rsa.h>
#include <mbedtls/entropy.h>
#include <mbedtls/ctr_drbg.h>
#include "os.h"
#include "base64.h"

static const unsigned char base64_table[65] =
  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

/**
 * base64_encode - Base64 encode
 * @src: Data to be encoded
 * @len: Length of the data to be encoded
 * @out_len: Pointer to output length variable, or %NULL if not used
 * Returns: Allocated buffer of out_len bytes of encoded data,
 * or %NULL on failure
 *
 * Caller is responsible for freeing the returned buffer. Returned buffer is
 * nul terminated to make it easier to use as a C string. The nul terminator is
 * not included in out_len.
 */
unsigned char * base64_encode(const unsigned char *src, size_t len,
            size_t *out_len)
{
  unsigned char *out, *pos;
  const unsigned char *end, *in;
  size_t olen;
  int line_len;

  olen = len * 4 / 3 + 4; /* 3-byte blocks to 4-byte */
  olen += olen / 72; /* line feeds */
  olen++; /* nul termination */
  if (olen < len)
    return NULL; /* integer overflow */
  out = os_malloc(olen);
  if (out == NULL)
    return NULL;

  end = src + len;
  in = src;
  pos = out;
  line_len = 0;
  while (end - in >= 3) {
    *pos++ = base64_table[in[0] >> 2];
    *pos++ = base64_table[((in[0] & 0x03) << 4) | (in[1] >> 4)];
    *pos++ = base64_table[((in[1] & 0x0f) << 2) | (in[2] >> 6)];
    *pos++ = base64_table[in[2] & 0x3f];
    in += 3;
    line_len += 4;
    if (line_len >= 72) {
      *pos++ = '\n';
      line_len = 0;
    }
  }

  if (end - in) {
    *pos++ = base64_table[in[0] >> 2];
    if (end - in == 1) {
      *pos++ = base64_table[(in[0] & 0x03) << 4];
      *pos++ = '=';
    } else {
      *pos++ = base64_table[((in[0] & 0x03) << 4) |
                (in[1] >> 4)];
      *pos++ = base64_table[(in[1] & 0x0f) << 2];
    }
    *pos++ = '=';
    line_len += 4;
  }

  if (line_len)
    *pos++ = '\n';

  *pos = '\0';
  if (out_len)
    *out_len = pos - out;
  return out;
}


/**
 * base64_decode - Base64 decode
 * @src: Data to be decoded
 * @len: Length of the data to be decoded
 * @out_len: Pointer to output length variable
 * Returns: Allocated buffer of out_len bytes of decoded data,
 * or %NULL on failure
 *
 * Caller is responsible for freeing the returned buffer.
 */
unsigned char * base64_decode(const unsigned char *src, size_t len,
            size_t *out_len)
{
  unsigned char dtable[256], *out, *pos, block[4], tmp;
  size_t i, count, olen;
  int pad = 0;

  os_memset(dtable, 0x80, 256);
  for (i = 0; i < sizeof(base64_table) - 1; i++)
    dtable[base64_table[i]] = (unsigned char) i;
  dtable['='] = 0;

  count = 0;
  for (i = 0; i < len; i++) {
    if (dtable[src[i]] != 0x80)
      count++;
  }

  if (count == 0 || count % 4)
    return NULL;

  olen = count / 4 * 3;
  pos = out = os_malloc(olen);
  if (out == NULL)
    return NULL;

  count = 0;
  for (i = 0; i < len; i++) {
    tmp = dtable[src[i]];
    if (tmp == 0x80)
      continue;

    if (src[i] == '=')
      pad++;
    block[count] = tmp;
    count++;
    if (count == 4) {
      *pos++ = (block[0] << 2) | (block[1] >> 4);
      *pos++ = (block[1] << 4) | (block[2] >> 2);
      *pos++ = (block[2] << 6) | block[3];
      count = 0;
      if (pad) {
        if (pad == 1)
          pos--;
        else if (pad == 2)
          pos -= 2;
        else {
          /* Invalid padding */
          os_free(out);
          return NULL;
        }
        break;
      }
    }
  }

  *out_len = pos - out;
  return out;
}

int RSA_test()
{

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
  mbedtls_ctr_drbg_set_prediction_resistance( &ctr_drbg, 
                                              MBEDTLS_CTR_DRBG_PR_ON );
////////////////////////////////////////////////////////////////////////  
  mbedtls_pk_context pk;
  mbedtls_pk_init( &pk );
  unsigned char to_encrypt[]="louis";
  unsigned char to_decrypt[MBEDTLS_MPI_MAX_SIZE];
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

  if( ( ret = mbedtls_pk_parse_public_key( &pk, pub_key, sizeof(pub_key) ) ) != 0 )
  {
    Serial.printf( " failed\n ! mbedtls_pk_parse_public_keyfile returned -0x%04x\n", -ret );
    return -1;
  }

  unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
  size_t olen = 0;

  Serial.printf( "\nGenerating the encrypted value\n" );

  if( ( ret = mbedtls_pk_encrypt( &pk, to_encrypt, sizeof(to_encrypt),
                                  buf, &olen, sizeof(buf),
                                  mbedtls_ctr_drbg_random, &ctr_drbg ) ) != 0 )
  {
    Serial.printf( " failed\n ! mbedtls_pk_encrypt returned -0x%04x\n", -ret );
    return -1;
  }

  for(int idx=0; idx<strlen(buf); Serial.printf("%02x", buf[idx++]));
  Serial.printf ("\n");

  mbedtls_pk_context pk1;

  mbedtls_pk_init(&pk1);
  if( ( ret = mbedtls_pk_parse_key( &pk1, prv_key, sizeof(prv_key), NULL, 0 ) ) != 0 )
  {
    Serial.printf( " failed\n ! mbedtls_pk_parse_keyfile returned -0x%04x\n", -ret );
    return -1;
  }

  unsigned char result[7];
  size_t olen1 = 0;

  Serial.printf( "\nGenerating the decrypted value" );

  if( ( ret = mbedtls_pk_decrypt( &pk1, buf, olen, result, &olen1, sizeof(result),
                                    mbedtls_ctr_drbg_random, &ctr_drbg ) ) != 0 )
  {
        Serial.printf( " failed\n! mbedtls_pk_decrypt returned -0x%04x\n", -ret );
        return -1;
  }
  else
  {

    Serial.printf("\n\n%s----------------\n\n", result);
  }

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
  delay(10000);
}
