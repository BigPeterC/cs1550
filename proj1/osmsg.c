#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <pwd.h>
#include <errno.h>
#include <unistd.h>


struct message {
       uid_t user;   //This is “to” when sending and “from” when receiving
       char msg[140];
};
uid_t trans_name_uid(char *name)
{
		struct passwd pwd;
		struct passwd *result;
		char *buf;
		size_t bufsize;
		int s;

		bufsize = sysconf(_SC_GETPW_R_SIZE_MAX);
		if (bufsize == -1)          
			bufsize = 16384;        

		buf = malloc(bufsize);
		if (buf == NULL) {
			perror("malloc");
			exit(EXIT_FAILURE);
		}

		s = getpwnam_r(name, &pwd, buf, bufsize, &result);
		if (result == NULL) {
			if (s == 0)
				printf("Not found\n");
			else {
				errno = s;
				perror("getpwnam_r");
				}
			exit(EXIT_FAILURE);
		}
		return pwd.pw_uid;
}
char* trans_uid_name(uid_t uid)
{
		struct passwd pwd;
		struct passwd *result;
		char *buf;
		size_t bufsize;
		int s;

		bufsize = sysconf(_SC_GETPW_R_SIZE_MAX);
		if (bufsize == -1)          
			bufsize = 16384;        

		buf = malloc(bufsize);
		if (buf == NULL) {
			perror("malloc");
			exit(EXIT_FAILURE);
		}

		s = getpwuid_r(uid, &pwd, buf, bufsize, &result);
		if (result == NULL) {
			if (s == 0)
				printf("Not found\n");
			else {
				errno = s;
				perror("getpwnam_r");
				}
			exit(EXIT_FAILURE);
		}
		return pwd.pw_name;
}
int main(int argc, char ** argv)
{
	char command[2];
	char recipient[20];
	int status;
	struct message *message;
	struct passwd *my_info;

	strncpy(command,argv[1],2);
	


	if((strncmp(command,"-s",2) != 0) && (strncmp(command,"-r",2) != 0))
	{
		printf("please use -s (send) or -r (read) \n");
		return 0;
	}


	if(strncmp(command,"-s",2)==0)
	{	
		message = (struct message*) malloc(sizeof(struct message));
		
		strncpy(recipient,argv[2],20);
		//my_info= getpwnam(recipient);
		
		
		//message->user =my_info->pw_uid;
		//printf("%s %ld",recipient,trans_name_uid(recipient));
		
		message->user = trans_name_uid(recipient);
		
		strncpy(message->msg,argv[3],140);
		//printf("osmsg:%ld  + %s",message->user,message->msg);
		status = syscall(325,message);
		
		if(status == 0)
		{
			printf("successful\n");
		}
		else
		{
			printf("error sending message\n");
		}
		free(message);
	}
	else 
	{
		message = (struct message*) malloc(sizeof(struct message));
		printf("reading messages...\n");
		
		do
		{
		
			status = syscall(326,message);
			//my_info = getpwuid(message->user);
			if(status < 0)
			{
			printf("Not your message !\n");
			break;
			}
			
			printf("%s said: %s \n",  trans_uid_name(message->user), message->msg);
				
		} while(status == 1);

		free(message);
	}
	return 0;
}
