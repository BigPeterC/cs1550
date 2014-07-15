#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <pthread.h>
#include <math.h>
#include <stdbool.h>
#include <unistd.h>
#include "banker.h"
#include "process.h"

struct table {
  int *resource;
  int *available;
  int **max;
  int **allocated;
  int **need;
};

extern int n, a, c; 
extern struct table *t;

void *thread(void *param)
{
  
  int pid = (int) (long) param;
  generate_max(pid);
  
  int *resource = malloc(a*sizeof(int));
  while (1) {
    //printf("FFFFFFFFFFFFFFFFFff%d:",pid);
	
    
   
   
    if(request_resources(pid,resource)==0)
	{
    sleep((rand()%1000+1)/1000);
	//pthread_yield();
    
    
    release_resources(pid, resource);
    
    sleep((rand()%1000+1)/1000);
	}
  }
  free(resource);
  return NULL;
}
