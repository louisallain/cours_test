#include <stdio.h>
#include <stdlib.h>

#include <time.h>
#include <limits.h>

#include <x86intrin.h> // gcc specific

#include <cpuid.h>

// vector size
#define N 1<<16

/*************************************************************************
                           array utilities
**************************************************************************/

// randomly initialize a vector of short
void rnd_short_array(short A[]){
    for(int i=0; i<N; i++){
        A[i]= (short) rand();
    }
}

// compare two vector of short
int compare_short_array(short A1[],short A2[]){
    for(int i=0; i<N; i++){
        if (A1[i]!=A2[i]){
            printf("A1[%i]=%d and A2[%i]=%d differ\n",i,A1[i],i,A2[i]);
            return 1;
        }
    }
    return 0;
}

/*************************************************************************
                           timing utilities
**************************************************************************/

static long time1, time_diff, sum_diff= 0, max=0;

static inline void reset_stats(){
    sum_diff= 0;
    max=0;
}

static inline void start_timer(){
    time1= __rdtsc();
}

static inline void pause_timer(){
    time_diff= __rdtsc()-time1;
    sum_diff += time_diff;
    if (time_diff > max) max= time_diff;
}

static inline void display_stats(char *msg,int nb_loops){
    static long old_avg;
    long avg= (float) (sum_diff-max)/(nb_loops-1);
    printf("%s: average= %12li\n",msg,avg);
    if (old_avg == 0){ // first call
        old_avg= avg;
    }else{ // next call
        float avg_speedup= (float) old_avg/avg;
        printf("Speedup: average= %5.2f\n",avg_speedup);
        old_avg= avg;
    };
}


/*************************************************************************
                           scalar function
**************************************************************************/

// conditional aa = bb > 0 ? cc+2 : bb*cc
int scalar_condaddmul(short aa[], short bb[], short cc[]) {

    for (int i = 0; i < N; i++) {
        aa[i] = (bb[i] > 0) ? (cc[i] + 2) : (bb[i] * cc[i]);
    }
    return 0;
}

/*************************************************************************
                           intrinsics function
**************************************************************************/

// Branch/loop function vectorized:
int simd_condaddmul(short int aa[], short int bb[], short int cc[]) {
    
    __m128i zero = _mm_set1_epi16(0);// Make a vector of (0,0,0,0,0,0,0,0)
    __m128i two = _mm_set1_epi16(2);// Make a vector of (2,2,2,2,2,2,2,2)
    
    for (int i = 0; i < N; i += 8) {// Roll out loop by eight to fit the eight-element vectors:
        
        __m128i b = _mm_load_si128((__m128i *) (bb+i));// Load eight consecutive elements from bb into vector b:
        __m128i c = _mm_load_si128((__m128i *) (cc+i));// Load eight consecutive elements from cc into vector c:
        
        __m128i c2 = _mm_add_epi16(c, two);// Add 2 to each element in vector c
        __m128i bc = _mm_mullo_epi16(b, c);// Multiply b and c
        __m128i mask = _mm_cmpgt_epi16(b, zero);// Compare each element in b to 0 and generate a bit-mask:
        c2 = _mm_and_si128(c2, mask);// AND each element in vector c2 with the bit-mask:
        bc = _mm_andnot_si128(mask, bc);// AND each element in vector bc with the inverted bit-mask:
        __m128i a = _mm_or_si128(c2, bc);// OR the results of the two AND operations:
        _mm_store_si128((__m128i *)(aa+i),a);
    }
    return 0;
}

// Branch/loop function vectorized:
int simd_condaddmul_avx2(short int aa[], short int bb[], short int cc[]) {
    
    __m256i zero = _mm256_set1_epi16(0);// Make a vector of (0,0,0,0,0,0,0,0)
    __m256i two = _mm256_set1_epi16(2);// Make a vector of (2,2,2,2,2,2,2,2)
    
    for (int i = 0; i < N; i += 16) {// Roll out loop by eight to fit the eight-element vectors:
        
        __m256i b = _mm256_load_si256((__m256i *) (bb+i));// Load eight consecutive elements from bb into vector b:
        __m256i c = _mm256_load_si256((__m256i *) (cc+i));// Load eight consecutive elements from cc into vector c:
        
        __m256i c2 = _mm256_add_epi16(c, two);// Add 2 to each element in vector c
        __m256i bc = _mm256_mullo_epi16(b, c);// Multiply b and c
        __m256i mask = _mm256_cmpgt_epi16(b, zero);// Compare each element in b to 0 and generate a bit-mask:
        c2 = _mm256_and_si256(c2, mask);// AND each element in vector c2 with the bit-mask:
        bc = _mm256_andnot_si256(mask, bc);// AND each element in vector bc with the inverted bit-mask:
        __m256i a = _mm256_or_si256(c2, bc);// OR the results of the two AND operations:
        _mm256_store_si256((__m256i *)(aa+i),a);
    }
    return 0;
}

/*************************************************************************
                           main function
**************************************************************************/
    short vector_A1[N] __attribute__((aligned(32))), 
          vector_A2[N] __attribute__((aligned(32))),
          vector_B[N] __attribute__((aligned(32))), 
          vector_C[N] __attribute__((aligned(32)));


int main(int argc,char *argv[]){
    
    if (argc<2){
        printf("error: missing loop number on command line, usage ./condaddmul <n>\n");
        return -1;
    }
    const int nb_loops= atoi(argv[1]);
    
    srand (time (0));
    rnd_short_array(vector_B);
    rnd_short_array(vector_C);
    
    for(int i=0; i<nb_loops; i++){
        start_timer();
        scalar_condaddmul(vector_A1,vector_B,vector_C);
        pause_timer();
    }
    display_stats("Scalar stats",nb_loops);
    reset_stats();
    for(int i=0; i<nb_loops; i++){
        start_timer();
        simd_condaddmul_avx2(vector_A2,vector_B,vector_C);
        pause_timer();
    }
    display_stats("SIMD stats",nb_loops);
    return compare_short_array(vector_A1,vector_A2);
}   
