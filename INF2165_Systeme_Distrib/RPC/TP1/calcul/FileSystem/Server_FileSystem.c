#include <stdio.h>
#include <rpc/rpc.h>
#include"FileReadWrite.h"
#include <string.h>

// WRITE
void * write_2(writeargs *a) {

    FILE *fptr;

    if((fptr = fopen(a.filename, "w")) == NULL) {
        
        fprintf(stderr, "Can't open file\n");
        exit(1);
    }
    
    fseek(fptr, a.position, SEEK_SET); // positionning
    fwrite(a.data.buffer , 1, a.data.length, fptr); // writing
    fclose(fptr);
}

// READ
Data * read_2(readargs * a) {

    static Data result; /* must be static */

    FILE *fptr;

    if((fptr = fopen(a.filename, "r")) == NULL) {
        
        fprintf(stderr, "Can't open file\n");
        exit(1);
    }

    fseek(fptr, a.position, SEEK_SET); // positionning
    result.length = fread(result.buffer, a.length, 1, fptr); // reading
    fclose(fptr);

    return &result;
}

// DELETE
void * delete_2(writeargs *a) {

    int ret;

    if((ret = remove(filename)) == 0) {
        printf("File deleted successfully");
    } else {
        printf("Error: unable to delete the file");
    }
}