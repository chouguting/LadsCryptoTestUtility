#include <stddef.h>
#include <stdint.h>
#include "params.h"
#include "kem.h"
#include "indcpa.h"
#include "verify.h"
#include "symmetric.h"
#include "randombytes.h"

#include <stdio.h>

/*************************************************
* Name:        crypto_kem_keypair
*
* Description: Generates public and private key
*              for CCA-secure Kyber key encapsulation mechanism
*
* Arguments:   - uint8_t *pk: pointer to output public key
*                (an already allocated array of KYBER_PUBLICKEYBYTES bytes)
*              - uint8_t *sk: pointer to output private key
*                (an already allocated array of KYBER_SECRETKEYBYTES bytes)
*
* Returns 0 (success)
**************************************************/
int crypto_kem_keypair(uint8_t *pk,
                       uint8_t *sk)
{
  size_t i;
  indcpa_keypair(pk, sk);
  // Cloud_Add
  //debugPrint((uint8_t *)(pk), (CRYPTO_PUBLICKEYBYTES * 1), 1, "pk1 :");
  //debugPrint((uint8_t *)(sk), (CRYPTO_SECRETKEYBYTES * 1), 1, "sk1 :");
  // Cloud_End
  for(i=0;i<KYBER_INDCPA_PUBLICKEYBYTES;i++)
    sk[i+KYBER_INDCPA_SECRETKEYBYTES] = pk[i];
  // Cloud_Add
  //debugPrint((uint8_t *)(pk), (CRYPTO_PUBLICKEYBYTES * 1), 1, "pk2 :");
  //debugPrint((uint8_t *)(sk), (CRYPTO_SECRETKEYBYTES * 1), 1, "sk2 :");
  // Cloud_End
  hash_h(sk+KYBER_SECRETKEYBYTES-2*KYBER_SYMBYTES, pk, KYBER_PUBLICKEYBYTES);   // fips202.c --> sha3_256
  // Cloud_Add
  //debugPrint((uint8_t *)(pk), (CRYPTO_PUBLICKEYBYTES * 1), 1, "pk3 :");
  //debugPrint((uint8_t *)(sk), (CRYPTO_SECRETKEYBYTES * 1), 1, "sk3 :");
  // Cloud_End
  /* Value z for pseudo-random output on reject */
  randombytes(sk+KYBER_SECRETKEYBYTES-KYBER_SYMBYTES, KYBER_SYMBYTES);
  return 0;
}

/*************************************************
* Name:        crypto_kem_enc
*
* Description: Generates cipher text and shared
*              secret for given public key
*
* Arguments:   - uint8_t *ct: pointer to output cipher text
*                (an already allocated array of KYBER_CIPHERTEXTBYTES bytes)
*              - uint8_t *ss: pointer to output shared secret
*                (an already allocated array of KYBER_SSBYTES bytes)
*              - const uint8_t *pk: pointer to input public key
*                (an already allocated array of KYBER_PUBLICKEYBYTES bytes)
*
* Returns 0 (success)
**************************************************/
int crypto_kem_enc(uint8_t *ct,
                   uint8_t *ss,
                   const uint8_t *buf,
                   const uint8_t *pk)
{
  //uint8_t buf[2*KYBER_SYMBYTES];
  /* Will contain key, coins */
  uint8_t kr[2*KYBER_SYMBYTES];

  //randombytes(buf, KYBER_SYMBYTES);
  /* Don't release system RNG output */
  //hash_h(buf, buf, KYBER_SYMBYTES);   // fips202.c --> sha3_256

  /* Multitarget countermeasure for coins + contributory KEM */
  hash_h((uint8_t *)buf+KYBER_SYMBYTES, pk, KYBER_PUBLICKEYBYTES);   // fips202.c --> sha3_256
  hash_g(kr, buf, 2*KYBER_SYMBYTES);   // fips202.c --> sha3_512

  /* coins are in kr+KYBER_SYMBYTES */
  indcpa_enc(ct, buf, pk, kr+KYBER_SYMBYTES);

  /* overwrite coins in kr with H(c) */
  hash_h(kr+KYBER_SYMBYTES, ct, KYBER_CIPHERTEXTBYTES);   // fips202.c --> sha3_256
  /* hash concatenation of pre-k and H(c) to k */
  kdf(ss, kr, 2*KYBER_SYMBYTES);   // fips202.c --> shake256
  return 0;
}

/*************************************************
* Name:        crypto_kem_dec
*
* Description: Generates shared secret for given
*              cipher text and private key
*
* Arguments:   - uint8_t *ss: pointer to output shared secret
*                (an already allocated array of KYBER_SSBYTES bytes)
*              - const uint8_t *ct: pointer to input cipher text
*                (an already allocated array of KYBER_CIPHERTEXTBYTES bytes)
*              - const uint8_t *sk: pointer to input private key
*                (an already allocated array of KYBER_SECRETKEYBYTES bytes)
*
* Returns 0.
*
* On failure, ss will contain a pseudo-random value.
**************************************************/
int crypto_kem_dec(uint8_t *ss,
                   const uint8_t *ct,
                   const uint8_t *sk)
{
  size_t i;
  int fail;
  uint8_t buf[2*KYBER_SYMBYTES];
  /* Will contain key, coins */
  uint8_t kr[2*KYBER_SYMBYTES];
  uint8_t cmp[KYBER_CIPHERTEXTBYTES];
  const uint8_t *pk = sk+KYBER_INDCPA_SECRETKEYBYTES;

  indcpa_dec(buf, ct, sk);

  /* Multitarget countermeasure for coins + contributory KEM */
  for(i=0;i<KYBER_SYMBYTES;i++)
    buf[KYBER_SYMBYTES+i] = sk[KYBER_SECRETKEYBYTES-2*KYBER_SYMBYTES+i];
  hash_g(kr, buf, 2*KYBER_SYMBYTES);   // fips202.c --> sha3_512

  /* coins are in kr+KYBER_SYMBYTES */
  indcpa_enc(cmp, buf, pk, kr+KYBER_SYMBYTES);

  fail = verify(ct, cmp, KYBER_CIPHERTEXTBYTES);

  /* overwrite coins in kr with H(c) */
  hash_h(kr+KYBER_SYMBYTES, ct, KYBER_CIPHERTEXTBYTES);   // fips202.c --> sha3_256

  /* Overwrite pre-k with z on re-encryption failure */
  cmov(kr, sk+KYBER_SECRETKEYBYTES-KYBER_SYMBYTES, KYBER_SYMBYTES, fail);

  /* hash concatenation of pre-k and H(c) to k */
  kdf(ss, kr, 2*KYBER_SYMBYTES);   // fips202.c --> shake256
  return 0;
}
/*
void debugPrint(uint8_t *showBuff, uint32_t showBuffLen, uint8_t showByteLen, char *showTitle)
{
	uint32_t	u32tIndex;
	uint8_t		u8tByteCount;

	printf("%s", showTitle);
	for(u32tIndex = 0 ; u32tIndex < showBuffLen ;)
	{
		printf(" %02X", showBuff[u32tIndex++]);
		for(u8tByteCount = 1 ; (u8tByteCount < showByteLen) && (u32tIndex < showBuffLen) ; u8tByteCount++) {
			printf("%02X", showBuff[u32tIndex++]);
		}
	}
	printf("\n");
}
*/
