#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/resource.h>
#include <sys/wait.h>
int main (int argc,char **argv) {

    unsigned long user,nice,syst,idle;
    unsigned long user2,nice2,syst2,idle2;
    int pid;
    int chargeWanted = atoi(argv[1]);

    while(1) {

        FILE * stat;
        stat = fopen("/proc/stat", "rw");
        fscanf(stat,"cpu %lu%lu%lu%lu",&user,&nice,&syst,&idle);
        fclose(stat);
        usleep(200000);
        stat = fopen("/proc/stat","rw");
        fscanf(stat,"cpu %lu%lu%lu%lu",&user2,&nice2,&syst2,&idle2);
        fclose(stat);

        int cpuload = ((user2-user)+(nice2-nice)+(syst2-syst))*100/((user2-user)+(nice2-nice)+(syst2-syst)+(idle2-idle));
        printf("CPU:%d\%\n", cpuload);

        if(cpuload < chargeWanted-3) {

            if ((pid = fork()) == 0) {

                int val;

                for(int j=0; j<100;j++) {
                    for(int k=0; k<1000000; k++) {
                        val=1.0/j;
                        usleep(1);
                    }
                    
                }
            }
        }
        else if(cpuload > chargeWanted+5){

        }
        usleep(200000);
    }
}