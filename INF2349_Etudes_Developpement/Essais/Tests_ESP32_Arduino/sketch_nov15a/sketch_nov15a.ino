#include "mbedtls/config.h"
#include "mbedtls/platform.h"
#include "mbedtls/rsa.h"
#include "mbedtls/md.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int ret = 1;
unsigned c;
int exit_code = MBEDTLS_EXIT_FAILURE;
size_t i;
mbedtls_rsa_context rsa;
unsigned char hash[32];
unsigned char buf[MBEDTLS_MPI_MAX_SIZE];
const char pub_key_modulus[] = "65444e4463586b764e6a4534524774754f465a575a7a644b636a42484f565634596c4e4a616e5a764b334d7763574930556d4a5152586f774e31464d656e46425757644562306b786330396f55544a6e596c63356256686154316c71656d56435747395663556f31516a637859565252525549784e6c56524b306c504b3351314e3055785a334a6f63475a7757455a76646c6c344b7a564e546c6376533056315631524254554933626e64544f586c765a6d597852454e4e544570324e54424e6555524257465977576a4e784b7a467465574e355a31526b526b5a5653475230595746474f47354c547a4e74523367776548427a4c3038314d334e59516b4e4b515756364e6c6733526a4645516b3550525442555756464b5258704e656d4e535330564463565172597a6b3455555a69656b744e6344424b566b493263566c744c30597762585279526b7051636d4a34565746565a5774704d304e4b546e46515747593153464e6c5a4664534c3356615155783155466c4c556a67725a586c565a575656563364464c303145623152525a564245556b39494e56465964475a45656b684254455930526e64564f4852335233565754466b78636d74314d32644657556c53646c52525054303d";
const char pub_key_exponent[] = "5156464251673d3d";

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

void setup() {
  Serial.begin(115200);

  

}

void loop() {
  mbedtls_rsa_init( &rsa, MBEDTLS_RSA_PKCS_V15, 0 );
  if( ( ret = mbedtls_mpi_read_string( &rsa.N, 16, pub_key_modulus ) ) != 0 ||
      ( ret = mbedtls_mpi_read_string( &rsa.E, 16, pub_key_exponent ) ) != 0 )
  {
    Serial.printf( " failed\n  ! mbedtls_mpi_read_file returned -0x%0x\n\n", ret );
  }
  rsa.len = ( mbedtls_mpi_bitlen( &rsa.N ) + 7 ) >> 3;

  const char* hexSignature = "636d356e55556435576a684a5247395254304a6f4f4735314d6d7870626e49764e446b785a444a5851575a54656e4a4655444634537a4e7751316472516a526b646a5636596a64524e6d6f72616939686257313464453834556939524b335a4f63314978593164714e6b7377646b3578566d7333633164486243395a4d565a33636a41774f576c4e625446314e6b646a54576c6a4c7a4a7559314643616a4e6b4d475a515647343163576779566b4e76513031764e6d7475515764774b304a73564777314d485a4d564752786143393361554a3662444e305a555a4f6248597754326458525670784d573972516c4d354e554a76516d557656304e545133457a556c5a714f55316e59307778656e6c69525464484e586c4d4d6e70534b336b32646a56695153394d56486c78627a527953455257566e5a7a617a4e454e55396e5933706f63554a4864456779626c5a5757564e6b654731485a577330636b6c6a4b7a686b637a4652556b56325257356b59793943556a557a57456f7a537a565856577074536b704c4d47566f616c6f324e6e6f33626c45725231425457474e6b5a6c4e485a485a7656303168636e4254566b6c5a61455657616d31526332745052476449536b4e6e5054303d";
  unsigned char * parsedSignature = hexstr_to_char(hexSignature); // converted hex string to byte array.
  const unsigned char SHA256HashedMessage[32] = "5f16795c54ab7de419edf8e9c6da6065f7dd448f122fcbc9815c67daa566ba8e";
  unsigned char * parsedHash = hexstr_to_char(SHA256HashedMessage);
  
  if( ( ret = mbedtls_rsa_pkcs1_verify( &rsa, NULL, NULL, MBEDTLS_RSA_PUBLIC,
                                  MBEDTLS_MD_SHA256, 20, SHA256HashedMessage, parsedSignature ) ) != 0 )
  {
    Serial.printf( " failed\n  ! mbedtls_rsa_pkcs1_verify returned -0x%0x\n\n", (unsigned int) -ret );
  }
  
  delay(2000);

}
