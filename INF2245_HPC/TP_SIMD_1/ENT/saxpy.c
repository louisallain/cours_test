#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#include <time.h>
#include <limits.h>

#include <x86intrin.h> // gcc specific
#include <cpuid.h>

const int N=1<<16;

/*************************************************************************
                           array utilities
**************************************************************************/

// randomly initialize a vector of short
void rnd_float_array(float A[]){
    for(int i=0; i<N; i++){
        A[i]= rand()/ (float) RAND_MAX;
    }
}

// compare two vector of short
int compare_float_array(float A1[],float A2[]){
    for(int i=0; i<N; i++){
        if (fabsf(A1[i]-A2[i])>1e-6){
            printf("*** error: A1[%i]=%e and A2[%i]=%e differ\n",i,A1[i],i,A2[i]);
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

// Y = Y + A * X
int scalar_saxpy(float ss[], float yy[], float a, float xx[]) {

    for (int i = 0; i < N; i++) {
        ss[i] = yy[i] + a * 
        
    }
    return 0;
}


/*************************************************************************
                           intrinsics function
**************************************************************************/

int simd_saxypyl(float ss[], float yy[], float a, float xx[]) {
    
    
    return 0;
}

/*************************************************************************
                           main function
**************************************************************************/

int main(int argc,char *argv[]){
    
    float vector_Y1[N] __attribute__((aligned(32))), 
          vector_Y2[N] __attribute__((aligned(32))), 
          vector_Y[N] __attribute__((aligned(32))),
          vector_X[N]__attribute__((aligned(32))) ;
    float SA;
    
    if (argc<2){
        printf("error: missing loop number on command line, usage ./saxpy <n>\n");
        return -1;
    }
    const int nb_loops= atoi(argv[1]);
    
    srand (time (0));
    SA= rand()/ (float) RAND_MAX;
    rnd_float_array(vector_X);
    rnd_float_array(vector_Y);
    
    for(int i=0; i<nb_loops; i++){
        start_timer();
        scalar_saxpy(vector_Y1,vector_Y,SA,vector_X);
        pause_timer();
    }
    display_stats("Scalar stats",nb_loops);
    reset_stats();
    
    return compare_float_array(vector_Y1,vector_Y2);
}   
