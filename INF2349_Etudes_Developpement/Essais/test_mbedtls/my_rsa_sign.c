/*
 *  RSA/SHA-256 signature creation program
 *
 *  Copyright The Mbed TLS Contributors
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may
 *  not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

#if !defined(MBEDTLS_CONFIG_FILE)
#include "mbedtls/config.h"
#else
#include MBEDTLS_CONFIG_FILE
#endif

#if defined(MBEDTLS_PLATFORM_C)
#include "mbedtls/platform.h"
#else
#include <stdio.h>
#include <stdlib.h>
#define mbedtls_fprintf         fprintf
#define mbedtls_printf          printf
#define mbedtls_snprintf        snprintf
#define mbedtls_exit            exit
#define MBEDTLS_EXIT_SUCCESS    EXIT_SUCCESS
#define MBEDTLS_EXIT_FAILURE    EXIT_FAILURE
#endif /* MBEDTLS_PLATFORM_C */

#if !defined(MBEDTLS_BIGNUM_C) || !defined(MBEDTLS_RSA_C) ||  \
    !defined(MBEDTLS_SHA256_C) || !defined(MBEDTLS_MD_C) || \
    !defined(MBEDTLS_FS_IO)
int main( void )
{
    mbedtls_printf("MBEDTLS_BIGNUM_C and/or MBEDTLS_RSA_C and/or "
            "MBEDTLS_MD_C and/or "
            "MBEDTLS_SHA256_C and/or MBEDTLS_FS_IO not defined.\n");
    mbedtls_exit( 0 );
}
#else

#include "mbedtls/rsa.h"
#include "mbedtls/md.h"

#include <stdio.h>
#include <string.h>


int main( int argc, char *argv[] )
{
    FILE *f;
    int ret = 1;
    int exit_code = MBEDTLS_EXIT_FAILURE;
    size_t i;
    mbedtls_rsa_context rsa;
    unsigned char hash[32];
    unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
    char filename[512];
    mbedtls_mpi N, P, Q, D, E, DP, DQ, QP;
    char N_str[] = "008678082EA8DAE3195A6063948B5A92598593E7FD0B349B25AC02209930215D9856985DEB8E4554A360C6CA4BB9880BA0469EC0650437C4D0E2F89788B82B394708D10077D6AD9D4AAE5C1415C064DC58E1A0954DC34F63685BE3FF68A6F73C1B8D07776D9F702FB627A4701A4A72168F613E5333F8B214A2601AA05A3B29D169570BED0FB527616B4FE1A646FF174007B16A835E1BC50F86731669074C2F24223DCB2CE928EB1F270B634EB1378DDABEFDAA5AA488997FBD8404AE38084468434E2F795BEA96EB593CDC3F63F16298F0DBCBACBAFC66ADC7E557FB580C1F2A2425CE8D2575F8E2E5AC1ED0011C0C233F7E0DDBA19865D0F44CB1C8F6D04B518D";
    char E_str[] = "010001";
    char D_str[] = "7A31652E115F26F388FF023EA00BA3E619CE7807A8411BA3361590CDE3C0CE2E7AB266D681285B6B43AC2174829A30066B66236B28540D53CC8D1E16AD5509ED950367AB7EB7B1B1859442E4293AFE48FE6F507DA3B7E69B94B531094680706C90D1722379B916822EAFECF6D723A9808D5B777C48C87A9BBB3D36EE07C94E735BE243CF45CD1A219FA9A2CDCD6BB2FF8282CC7FC62282C6893D90F09FCA59B7306B65CB172E55EC18D5A177577D2760621729037496C6CF4C940F3A40978F3D1D66BBE400AEC4AAF8B6D90C3EE227A00AE317405732062E96FEF1957680E3EC34A71B1E473700857C6415BEA59D05A2F98BED9A5A9B84FB314FA54F8D2764B9";
    char P_str[] = "00BADED22DFB1CE3404AAA58970F319B4B322C684431CE446621FDBA36C407220FBB5A01781938F15DBA6FD91D6ED852EB854C5F429BD271881AB1B2BE1ED98349BC6448080AF30C305DE3824BD6C28BD68D902255B6FDFD06F63B1944B91EE88662C41DED6482AE0111D7F91E91082A90C5FEF0D6110E337B42F5DA1D7BDFC313";
    char Q_str[] = "00B836A1CF0E2104953046311EFC96B5DFBF720996105DDED5D0232BB4B30F981433DE8F5FB5389BF0E2D4843B3CA86EE9CA7903FA2407F103182B415CA15F9D96CF6D902D84D2FCC508AD7888FA3AA501AC1C0C5F0077535BF4532346739E0E8B3FE25D1B76B91EBC5D178DD28BDC05F4E6D962C4AF331887D759C690D3C68CDF";
    char DP_str[] = "00816BC2EB3F8841478CAEAFCE672277283C268F4BF3A6A4AE8B8A1B86C47B2036FF0D9608E487107B569D12249559530C74F470E143E41AE7257DBE8A77F933394535BAC17F3BD74C442A465A59F1C141A62795F0015DA4F9465DA5E177C89548456D08411AA47AF898B01883F7F44CEE998238B0397DB95FFBFF23C1D9C21E6B";
    char DQ_str[] = "5A9D952EF3364EB8CC8C5AB17172F9785D064DACE1D42B3F662946E0968C7F34C0E72403E2733C525C6249C191EF10D034671F0BB719A8F6EAB6AC6A527F6457C125961CAE0FD38360E764398D4DB7C980CE334FB03D026663635A20D7D2D238B16E3A7ABE060616867C85A9225F5316CB4FF803E462F0183258D91278FA5C65";
    char QP_str[] = "00B4C7083FEF4018825CA815633C99003C63F6E1FD0B33AC4D3DE3721FD50A9AD5306E5B929F59E6610AA6D2BE18686F193649A6CA44510527186462F5475132019A92F9EACA087216AD9ABFE55F78F73777DFBCB2CB0C1AAEB8A096F3C9131E5FD71A0DB106DAF6138DAEEBD4A32909C48526ABB0FAD65D120BC867919338049A";

    mbedtls_rsa_init( &rsa, MBEDTLS_RSA_PKCS_V15, 0 );

    mbedtls_mpi_init( &N ); mbedtls_mpi_init( &P ); mbedtls_mpi_init( &Q );
    mbedtls_mpi_init( &D ); mbedtls_mpi_init( &E ); mbedtls_mpi_init( &DP );
    mbedtls_mpi_init( &DQ ); mbedtls_mpi_init( &QP );

    if( argc != 2 )
    {
        mbedtls_printf( "usage: rsa_sign <filename>\n" );

#if defined(_WIN32)
        mbedtls_printf( "\n" );
#endif

        goto exit;
    }

    if( ( ret = mbedtls_mpi_read_string( &E , 16, E_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &N , 16, N_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &D , 16, D_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &P , 16, P_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &Q , 16, Q_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &DP , 16, DP_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &DQ , 16, DQ_str ) ) != 0 ||
        ( ret = mbedtls_mpi_read_string( &QP , 16, QP_str ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_mpi_read_string returned %d\n\n", ret );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_import( &rsa, &N, &P, &Q, &D, &E ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_rsa_import returned %d\n\n",
                        ret );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_complete( &rsa ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_rsa_complete returned %d\n\n",
                        ret );
        goto exit;
    }

    mbedtls_printf( "\n  . Checking the private key" );
    fflush( stdout );
    if( ( ret = mbedtls_rsa_check_privkey( &rsa ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_rsa_check_privkey failed with -0x%0x\n", (unsigned int) -ret );
        goto exit;
    }

    /*
     * Compute the SHA-256 hash of the input file,
     * then calculate the RSA signature of the hash.
     */
    mbedtls_printf( "\n  . Generating the RSA/SHA-256 signature" );
    fflush( stdout );

    if( ( ret = mbedtls_md_file(
                    mbedtls_md_info_from_type( MBEDTLS_MD_SHA256 ),
                    argv[1], hash ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! Could not open or read %s\n\n", argv[1] );
        goto exit;
    }

    if( ( ret = mbedtls_rsa_pkcs1_sign( &rsa, NULL, NULL, MBEDTLS_RSA_PRIVATE, MBEDTLS_MD_SHA256,
                                20, hash, buf ) ) != 0 )
    {
        mbedtls_printf( " failed\n  ! mbedtls_rsa_pkcs1_sign returned -0x%0x\n\n", (unsigned int) -ret );
        goto exit;
    }

    /*
     * Write the signature into <filename>.sig
     */
    mbedtls_snprintf( filename, sizeof(filename), "%s.sig", argv[1] );

    if( ( f = fopen( filename, "wb+" ) ) == NULL )
    {
        mbedtls_printf( " failed\n  ! Could not create %s\n\n", argv[1] );
        goto exit;
    }

    for( i = 0; i < rsa.len; i++ )
        mbedtls_fprintf( f, "%02X%s", buf[i],
                 ( i + 1 ) % 16 == 0 ? "\r\n" : " " );

    fclose( f );

    mbedtls_printf( "\n  . Done (created \"%s\")\n\n", filename );

    exit_code = MBEDTLS_EXIT_SUCCESS;

exit:

    mbedtls_rsa_free( &rsa );
    mbedtls_mpi_free( &N ); mbedtls_mpi_free( &P ); mbedtls_mpi_free( &Q );
    mbedtls_mpi_free( &D ); mbedtls_mpi_free( &E ); mbedtls_mpi_free( &DP );
    mbedtls_mpi_free( &DQ ); mbedtls_mpi_free( &QP );

#if defined(_WIN32)
    mbedtls_printf( "  + Press Enter to exit this program.\n" );
    fflush( stdout ); getchar();
#endif

    mbedtls_exit( exit_code );
}
#endif /* MBEDTLS_BIGNUM_C && MBEDTLS_RSA_C && MBEDTLS_SHA256_C &&
          MBEDTLS_FS_IO */
