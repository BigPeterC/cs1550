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

pthread_mutex_t state;
pthread_mutexattr_t attr;

int** allocate_matrix(int n, int a){
  int** matrix = malloc(n * sizeof(int*)); 
  int i;
  for(i=0; i<n; i++){
    matrix[i] = malloc(a * sizeof(int)); 
  }
  return matrix;
}

void free_matrix(int** matrix){
  free(*matrix);
  free(matrix);
}

int rmain(int argc, char* argv[])
{
  
  if(argc < 4 && strcmp(argv[1],"-n") != 0 && strcmp(argv[3],"-a") != 0)
  {
	printf("common isn't correct, please input -n number -a numbers");
	exit(0);
  }
  int i, j;
  pthread_mutexattr_init(&attr);
  pthread_mutex_init(&state,&attr);
  
  n = atoi(argv[2]);
  a = argc - 4;
  c = n;
  
  
  t = (struct table *) malloc(sizeof(struct table));
  t->resource = (int *) malloc(a*sizeof(int));
  t->available = (int *) malloc(a*sizeof(int));
  t->max = allocate_matrix(n,a);
  t->allocated = allocate_matrix(n,a);
  t->need = allocate_matrix(n,a);

  
  for (j = 0; j < a; j++)
	t->resource[j] = atoi(argv[j+4]);
  
  
  for(i = 0; i < n; i++) {
    for(j = 0; j < a; j++) {
	   t->allocated[i][j] = 0;
	 }
  }
  for(i = 0; i < a; i++) {
    
    t->available[i] = t->resource[i];
  }

  
  srand ( time(NULL) );
  
  pthread_t *tid = malloc(n*sizeof(pthread_t));
  for (i = 0; i < n; i++)
    pthread_create(&tid[i], NULL, thread, (void *) (long) i);
  while(1)
  {
	//printf("c:%d",c);
    if(c==1)
	{
		printf("Only one process left,Succesfully finished!\n");
	
		free(tid);
		free(t->resource);
		free(t->available);
		free_matrix(t->max);
		free_matrix(t->allocated);
		free_matrix(t->need);
		free(t);
		pthread_exit(0);
		exit(0);
	}
  }
  return 0;

}
