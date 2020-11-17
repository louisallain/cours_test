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
    char N_str[] = "00b668a6eb353a5bee87c9860f545b45e540ef493ff3580b09d67b339f478696db7f4e0766b2d3c39a86721416a16bb802db034728aa9d617b4c0a2683a967d3195d808d2e78ef9991d6472bec94fb4a1a2a1a6975dfd2902625b576f7a6b40d8f869a32156845488e030b836368933e1a89353fd4d3eb9a07057aaada8500d24d2881bc2b401d673144037aaaebb1fd0877069e9d148633822833150f40f0c3c27b3e58e8e617f013c01a5129279cf80db2a9855cd0638c94a5c85737a5600b0c59d990fb968ce1e322feaba07c9a40ab5a1b5b72ddbc8aeb6298d0844ca9a7d01f1e509a9897f9bc7c19a635598f68a3cd0faae90784aa1151aba44d258e3e8d";
    char E_str[] = "010001";
    const unsigned char * sigHex_SHA256withRSA = "1F205ABB9CD436C326FF582E4A8E63EA030A38E04066FE12DD099FF00621A6613EC8D96C47FD3C59ECD220176FCB193AAFD494D0538B3E2733F6726F67A94404C8E44E631EE4D02A8E25D3F7CA6BC989226CFFBEE53692D3C9ABB91E9532DB367CA3B63D7219A38268E54538613753B4FDD63ED3483994283B9EDF0F3D58AF3596DD152461F34951478F1F18DBD28DCF0219A4CBACFCE9B62D7178FB42405BDB86F4C7AD127BC7F6626C8155D6B704BA0154C0CD7BECB02390629C3638BBD7C6F74297B1AB215C2DF0C853DD50CE57EC54E0032156528B874955EACA33A2F6154993668BEEADA1D64E0BFFF8760ADD406043386A5B808FE9795238C8404F6A0B";
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
