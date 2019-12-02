#include <stdio.h>
#include <rpc/rpc.h>
#include "FileReadWrite .h"

main(int argc, char ** argv) {

    CLIENT *clientHandle;
    char *serverName = "localhost"; // modify for remote server obv
    readargs rdArgs;
    writeargs wrArgs;
    deleteargs dlArgs;
    Data *data;

    clientHandle = clnt_create(serverName, FILEREADWRITE, VERSION, "udp"); /* creates socket and a client handle*/

    if (clientHandle == NULL){
        clnt_pcreateerror(serverName); /* unable to contact server */
        exit(1);
    }

    // call remote write
    data.length = 14;
    data.buffer = "Ecrit 13 car."
    wrArgs.filename = "testfile.txt";
    wrArgs.position = 0;
    wrArgs.data = &data; // TODO : a voir pour "&data" ou juste "data"
    write_2(&wrArgs, clientHandle)
    
    // call remote read
    rdArgs.filename = "testfile.txt";
    rdArgs.position = 0;
    rdArgs.length = 10;
    data = read_2(&rdArgs, clientHandle); // handle result data

    // call remote delete
    dlArgs.filename = "delete_testfile.txt";
    delete_2(&dlArgs, clientHandle);

    clnt_destroy(clientHandle); /* closes socket */
}