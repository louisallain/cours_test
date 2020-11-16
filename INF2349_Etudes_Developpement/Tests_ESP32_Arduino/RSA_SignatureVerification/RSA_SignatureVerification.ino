#if !defined(MBEDTLS_CONFIG_FILE)
#include "mbedtls/config.h"
#else
#include MBEDTLS_CONFIG_FILE
#endif


#include <stdio.h>
#include <stdlib.h>
#include "mbedtls/rsa.h"
#include "mbedtls/md.h"
//#include "mbedtls/platform.h"
#include <string.h>

#define mbedtls_printf          printf
#define mbedtls_snprintf        snprintf
#define mbedtls_exit            exit
#define MBEDTLS_EXIT_SUCCESS    EXIT_SUCCESS
#define MBEDTLS_EXIT_FAILURE    EXIT_FAILURE

int test_verify()
{
    int ret = 1;
    unsigned c;
    int exit_code = MBEDTLS_EXIT_FAILURE;
    size_t i;
    mbedtls_rsa_context rsa;
    unsigned char hash[32];
    char text[] = "louis";
    unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
    char N_str[] = "8678082ea8dae3195a6063948b5a92598593e7fd0b349b25ac02209930215d9856985deb8e4554a360c6ca4bb9880ba0469ec0650437c4d0e2f89788b82b394708d10077d6ad9d4aae5c1415c064dc58e1a0954dc34f63685be3ff68a6f73c1b8d07776d9f702fb627a4701a4a72168f613e5333f8b214a2601aa05a3b29d169570bed0fb527616b4fe1a646ff174007b16a835e1bc50f86731669074c2f24223dcb2ce928eb1f270b634eb1378ddabefdaa5aa488997fbd8404ae38084468434e2f795bea96eb593cdc3f63f16298f0dbcbacbafc66adc7e557fb580c1f2a2425ce8d2575f8e2e5ac1ed0011c0c233f7e0ddba19865d0f44cb1c8f6d04b518d";
    char E_str[] = "10001";
    const unsigned char * sigHex_SHA256withRSA = "85AD1352781BA46348417DC5B28E03A5B55A261A14C097E9068391C023AAB9E6C568BF44C03D1A93892CD384ECCF698D4A9DD7E383BAE2C7226F4DADE1EBE66AE43B36654CBF6FEB94EE9CC7A622AA0298BF3739EE9BBFF84DED050FBA9716CBEB868858074441BEACB1E53A3E10BCD493E333D9564213878E73E84B9A4A65C62DD20E6BFB74107090654FC504CFB35B330A2F59DECB8536126A3F26CC840370ED5E7147A5B715010414453892B1012E11C218549DDF4A5AB9158E03DB9448073632D524A8DD56E70B98E63DE834CDB412B5961D6CDF2984C14D9354FDF49B37029AEFBB72454D7875AA81DAC9619E53E1A68B299662A2ED1861C1578963629A";
    int n = 0;
    unsigned long startTime = millis();
    
    mbedtls_rsa_init( &rsa, MBEDTLS_RSA_PKCS_V15, 0 );

    if( ( ret = mbedtls_mpi_read_string( &rsa.N, 16, N_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &rsa.E, 16, E_str ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_mpi_read_string returned %d\n\n", ret );
        goto exit;
    }

    rsa.len = ( mbedtls_mpi_bitlen( &rsa.N ) + 7 ) >> 3;

    i = 0;
    while( ( n = sscanf( sigHex_SHA256withRSA, "%02X", (unsigned int*) &c ) > 0) && i < (int) sizeof( buf ) ) {
        buf[i++] = (unsigned char) c;
        sigHex_SHA256withRSA += n*2; // n*2 car on lit octet par octet
    }
    
    if( i != rsa.len )
    {
        mbedtls_printf( "\n  ! Invalid RSA signature format \n\n" );
        goto exit;
    }

    /*
     * Compute the SHA-256 hash of given text and
     * verify the signature
     */
    mbedtls_printf( "\n  . Verifying the RSA/SHA-256 signature" );

    if( ( ret = mbedtls_md(
                    mbedtls_md_info_from_type( MBEDTLS_MD_SHA256 ),
                    text, strlen(text), hash ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_md returned -0x%04X\n\n", ret );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_pkcs1_verify( &rsa, NULL, NULL, MBEDTLS_RSA_PUBLIC,
                                  MBEDTLS_MD_SHA256, 20, hash, buf ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_rsa_pkcs1_verify returned -0x%0x\n\n", (unsigned int) -ret );
        goto exit;
    }

    mbedtls_printf( "\n  . OK (the signature is valid)\n\n" );
    mbedtls_printf( "\n  . Time to verify signature = %lu ms\n\n", (millis() - startTime) );

    exit_code = MBEDTLS_EXIT_SUCCESS;

  exit:
  
      mbedtls_rsa_free( &rsa );

      return exit_code;
}

void setup()
{
  Serial.begin(115200);

}

void loop()
{
  int res = 0;

  if ((res = test_verify()) != 0) {
    Serial.printf("\n  . Error RSA_test_gen");
  }

  delay(1000);
}
