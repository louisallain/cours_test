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

/**
 * Vérifie une signature SHA256WithRSA d'une chaine de caractère.
 * @param N_str le module de la clef publique en hexadécimal.
 * @param E_str l'exposant de la clef publique en hexadécimal.
 * @param sig_SHA256WithRsa la signature en hexadécimal du texte.
 * @param text le texte dont la signature doit être vérifiée.
 * @return 0 si la signature est vérifiée sinon autre.
 */
int verifySHA256WithRSA(char * N_str, char * E_str, char * sigHex_SHA256withRSA, char * text)
{
    int ret = 1;
    int n = 0;
    unsigned c;
    size_t i;
    unsigned char hash[32];
    unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
    mbedtls_rsa_context rsa;
    
    // char text[] = "louis";
    // char N_str[] = "00b668a6eb353a5bee87c9860f545b45e540ef493ff3580b09d67b339f478696db7f4e0766b2d3c39a86721416a16bb802db034728aa9d617b4c0a2683a967d3195d808d2e78ef9991d6472bec94fb4a1a2a1a6975dfd2902625b576f7a6b40d8f869a32156845488e030b836368933e1a89353fd4d3eb9a07057aaada8500d24d2881bc2b401d673144037aaaebb1fd0877069e9d148633822833150f40f0c3c27b3e58e8e617f013c01a5129279cf80db2a9855cd0638c94a5c85737a5600b0c59d990fb968ce1e322feaba07c9a40ab5a1b5b72ddbc8aeb6298d0844ca9a7d01f1e509a9897f9bc7c19a635598f68a3cd0faae90784aa1151aba44d258e3e8d";
    // char E_str[] = "010001";
    // const unsigned char * sigHex_SHA256withRSA = "1F205ABB9CD436C326FF582E4A8E63EA030A38E04066FE12DD099FF00621A6613EC8D96C47FD3C59ECD220176FCB193AAFD494D0538B3E2733F6726F67A94404C8E44E631EE4D02A8E25D3F7CA6BC989226CFFBEE53692D3C9ABB91E9532DB367CA3B63D7219A38268E54538613753B4FDD63ED3483994283B9EDF0F3D58AF3596DD152461F34951478F1F18DBD28DCF0219A4CBACFCE9B62D7178FB42405BDB86F4C7AD127BC7F6626C8155D6B704BA0154C0CD7BECB02390629C3638BBD7C6F74297B1AB215C2DF0C853DD50CE57EC54E0032156528B874955EACA33A2F6154993668BEEADA1D64E0BFFF8760ADD406043386A5B808FE9795238C8404F6A0B";
          
    mbedtls_rsa_init( &rsa, MBEDTLS_RSA_PKCS_V15, 0 );

    if( ( ret = mbedtls_mpi_read_string( &rsa.N, 16, N_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &rsa.E, 16, E_str ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_mpi_read_string returned %d\n\n", ret );
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
        Serial.printf( "\n  ! Invalid RSA signature format \n\n" );
        goto exit;
    }

    /*
     * Compute the SHA-256 hash of given text and
     * verify the signature
     */
    Serial.printf( "\n  . Verifying the RSA/SHA-256 signature" );

    if( ( ret = mbedtls_md(
                    mbedtls_md_info_from_type( MBEDTLS_MD_SHA256 ),
                    text, strlen(text), hash ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_md returned -0x%04X\n\n", ret );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_pkcs1_verify( &rsa, NULL, NULL, MBEDTLS_RSA_PUBLIC,
                                  MBEDTLS_MD_SHA256, 20, hash, buf ) ) != 0 )
    {
        Serial.printf( " failed\n  ! mbedtls_rsa_pkcs1_verify returned -0x%0x\n\n", (unsigned int) -ret );
        goto exit;
    }

    Serial.printf( "\n  . OK (the signature is valid)\n\n" );
  exit:
  
      mbedtls_rsa_free( &rsa );
      return ret;
}

void setup()
{
  Serial.begin(115200);

}

void loop()
{
  int res = 0;
  char * text = "cyberkey";
  char * N_str = "00a9fe90c648a309a5194f095086c7da59479c3b943c51908ff4d468e9b0b75a7be2ec901757e41daea8b12f6e16a78a9e7571804dc9cf7f849072e43a4088eec9d6aa025e3e0a122673c15239927d4fcae726a3191d7f11ef9e6f24c7119ce71795616e978b36d2ae185adb251479d12ade65e6ef208d55dd519e047706d4c32f1e5fc5f86001464edca3fe0b473a65d3fa6f1a3cde948e992ff77d13c395ec0b6194f20ddc9ab716c7d17e7420b106a2c9049830db718bb900b21b2bb8e6d49a9096e3ed2d236edde0738ff6b72958626d09d9049118857176b32630d46e0ae92d87c31a7235b6d4a76d18346d8236b6e56c431d66f72208a441314a0a004863";// "00ad62829e491e259474ae3e1e55896bbcf14ec247595e2b3e6a74c4e6fb5b2c8e17ac216d77ae4f0739ac51e9d17b57528de6d6a4af7061b2bc0d7ba5a096752d300d29afaf39f07a6712216c5df625ea160fc14e14c43cb0b517a131f5fe6fef9ab017aefe5c0922add7d1dc0350f1d13e79d077a35be533e37f3ace3e22576a27e185c21c1003bf083f5d718dd110cd7c2efd0c31c5cb70fe837d83c070a9270770dde15816ae3f6df574023287b71d75970672442355a878b2e01bd18221e200d4d016d8c08c626dd20aabb478bccf8be81c850309a358208873c48968607ab833aa97a54f4ade9ab32da901bae67580b9d8f31f57c0ca86eba88f95693501";
  char * E_str = "010001";
  const unsigned char * sigHex_SHA256withRSA = "8BACBEDBAABA33B0424672E5243F07AA666A5C85C71709BDDAD56B9A7772A97ED919440E15FB6F9EAB751CFF914FC76313BE88BCC42A9DE02CACB4C36591068DB609E5BF2744156CF68A8E279A46D4BF9D762A70F9662548537633983DD7AA0FF421F5F448437F17C4614ABDFBDC90C4B7EB8CE8A40A77750E7A0FC109927D123A8E5B6A42600C8B943153770A50025D228D401E9380D90352A78CCAA97F97FC5049B163445A7D8BE710918C02A0E256877863780FB477D78E931606774A05556689EF3BDCFC270927253D15972AC74C5B77DDE8177DD8595037521C7CF2ED1B4D96F3F4CADFC46E4F6429733AF02B6BF0B413CADC854BCA5F3EF3F39D849015";//"449D6F5E4D99AE3ADD003A37A4DB7C5107E5CFC0C8AB3E91453CFCC3DC26B70C4A8227D93CD03CC484CA82BD9FAFCB42C1495DA70AD398AB6DC60B68A63E1D99F21E61F1C7EC90422768652EB76FC85EE18B2C9380500033A5B2C9E78A5E183010267A369CB7F495B24459DF7BA4A01123F3D61841554A1D041C49A853D0D644EA40F682DD4407384F34BD9FA06324D8EA4D042AF63F556D6F87133D340B800930CC8A6E58C066374BE309B714A8BFD4EE238EAE293297494081BF37B646240BA96B94D3AD599D6E1EEACDA6536FA1A55ADCAC3F90C161EBC14346AA29B1E8191108B01EBF99CFBEA72B6843BFB243E3F27551C28F8ED1CB574F556A388BED10";
 

  if ((res = verifySHA256WithRSA(N_str, E_str, sigHex_SHA256withRSA, text)) != 0) {
    Serial.printf("\n  . Error RSA_test_gen");
  }
  else {
    Serial.printf("\n . Signature vérifiée");
  }

  delay(1000);
}
