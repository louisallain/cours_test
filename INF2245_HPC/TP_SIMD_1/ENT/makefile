#CFLAGS=-O3 -mavx2 -mfma -fopt-info-vec-all 
CFLAGS=-std=gnu99 -mavx2 -mfma

output= csize \
	condaddmul \
	saxpy \
	sol-saxpy

all: $(output)

csize: csize.c
	gcc $(CFLAGS) -o csize csize.c
	
condaddmul: condaddmul.c
	gcc $(CFLAGS) -o condaddmul condaddmul.c
	
saxpy: saxpy.c
	gcc $(CFLAGS) -o saxpy saxpy.c
	
sol-saxpy: sol-saxpy.c
	gcc $(CFLAGS) -o sol-saxpy sol-saxpy.c
	
clean:
	rm -f $(output)

	