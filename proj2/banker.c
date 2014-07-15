#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <sys/time.h>
#include <pthread.h>
#include <math.h>
#include <stdbool.h>
#include <unistd.h>
#include "driver.h"
#include "banker.h"


struct table {
  int *resource;
  int *available;
  int **max;
  int **allocated;
  int **need;
};



int n, a, c; 
struct table *t = NULL;


extern pthread_mutex_t state;
extern pthread_mutexattr_t attr;


int compare(int *need, int *ava, int l){
  int r;
  for(r=0; r<l;r++)
    if(need[r]>ava[r]) return 0;
  return 1;
}


int is_safe(){
 
  int pid;
  int r;
  int *work;
  int *finish;
  int result = 0;
  work =  malloc(a*sizeof(int));
  memcpy(work, t->available, a*sizeof(int)); 
  finish =  malloc(n*sizeof(int));
  for(pid=0; pid<a; pid++) 
	finish[pid] = 0; 

  pid = banker(work,finish);
  
  while(pid != -1){ 
    finish[pid] = 1;
    for(r=0; r<a; r++)
      work[r] += t->allocated[pid][r];
    pid = banker(work,finish); 
  }

  for(pid=0; pid<n; pid++)
  {
    if(finish[pid] == 0) 
		result = -1;
	}
  free(finish);
  free(work);
  return result;
}


int banker(int *work, int *finish){
  int pid;
  int r;
  for(pid=0; pid<n; pid++){
    if(finish[pid] == 0 && compare(t->need[pid],work,a)==1) 
		return pid;
  }
  return -1;
}


int request_resources(int pid, int resources[])
{
  pthread_mutex_lock(&state);
  if(c == 1)
  {
  //printf("xxxxx1");
    pthread_exit(0);
	}
  int i;
  calcuate_need(pid);
  generate_request(pid, resources);

  printf("Request:\nPid:%d:\n",pid);
    printf("Now Available:");
  for(i=0; i<a;i++)
    printf(" %d ", t->available[i]);
  /*printf("\nMax:\n");
  int x;
  for (x = 0; x < n; x++){
	for(i=0; i<a;i++)
      printf(" %d ", t->max[x][i]);
	printf("\n");
  }*/
  printf("\nRequest Resources:");
  for(i=0; i<a;i++)
  printf(" %d ",resources[i]);
  printf("\n");
  int r;
  if(compare(resources,t->available,a) == 1)
  {
	
	for(r=0;r<a;r++){
      t->available[r] -= resources[r];
      t->allocated[pid][r] += resources[r];
      t->need[pid][r] -= resources[r];
	  
    }
    
    if(is_safe() == 0){
      
	  printf("Safe\nNow Available:");
	  int i;
	  for(i=0; i<a;i++)
	    printf(" %d ", t->available[i]);
	  printf("\n\n");
	  pthread_mutex_unlock(&state);
      return 0;
    }	  
  }
  for(r=0;r<a;r++){
	    t->available[r] += t->allocated[pid][r];
	    t->allocated[pid][r] = 0;
	    t->need[pid][r] = 0;
		t->max[pid][r] = 0;
	    }
  printf("Unsafe terminate this thread!\n\n");  
  c--;
  pthread_mutex_unlock(&state);
  pthread_exit(0);
  return -1;
}


void release_resources(int pid, int resources[])
{
  
  pthread_mutex_lock(&state);
  if(c == 1)
  {
   //printf("xxxxx3");
    pthread_exit(0);
	}
  generate_release(pid, resources);
  printf("Release:\nPid:%d\nRelease Resources:",pid);
  int i;
  for(i=0; i<a;i++)
	printf(" %d ",resources[i]);
  int r;
  for(r=0;r<a;r++){
    t->available[r] += resources[r];
    t->allocated[pid][r] -= resources[r];
    t->need[pid][r] += resources[r];
  }
  printf("\nNow Available:");
  for(i=0; i<a;i++)
  printf(" %d ", t->available[i]);
  printf("\n\n");

  pthread_mutex_unlock(&state);
}

void generate_max(int pid)
{
  int r, sum = 0;
  while (!sum) {
    for (r = 0;r < a; r++) {
      t->max[pid][r] = round((double)t->resource[r] * ((double)rand()) / (double)RAND_MAX);
      sum += t->max[pid][r];
    }
  }
}
void calcuate_need(int pid)
{
  int r;
  for (r = 0;r < a; r++)
  {
	t->need[pid][r] = t->max[pid][r]-t->allocated[pid][r];
	}
}
void generate_request(int pid, int resources[])
{
  int r, sum = 0;
  while (!sum) {
    for (r = 0;r < a; r++) {
      resources[r] = round((double)t->need[pid][r] * ((double)rand()) / (double)RAND_MAX);
      sum += resources[r];
    }
  }
}


void generate_release(int pid, int resources[])
{
  int r, sum = 0;
  while (!sum) {
    for (r = 0; r < a; r++) {
      resources[r] = round((double)t->allocated[pid][r]*((double)rand())/ (double)RAND_MAX);
      sum += resources[r];
    }
  }
}

int main(int argc, char* argv[])
{
  rmain(argc,argv);
}
