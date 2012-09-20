/*	Author: Jinwu Li
*	Student Number: D10120110
*	The skeletion of the function socketInit() and socketClose()
*	was originally came from http://msdn.microsoft.com/en-us/library/ms899596.aspx
*	since most of the socket application do pretty much the same things
*	I adopt the MSDN sample code to meet my requirements
*/


#pragma comment(lib, "ws2_32.lib") 

#include <stdio.h>
#include <Windows.h>

#define PORT 12345
#define IP_ADDRESS "localhost"

extern "C" int sendData(SOCKET client_socket, double d[], int len);
extern "C" SOCKET socketInit();
extern "C" void socketClose(SOCKET client_socket);

SOCKET socketInit()
{
	WSADATA Ws;
	SOCKET ClientSocket;
	struct sockaddr_in ServerAddr;
	struct hostent *he;
	int Ret = 0;
	int AddrLen = 0;
	HANDLE hThread = NULL;
	char SendBuffer[MAX_PATH];

	printf("before create socket");

	if(WSAStartup(MAKEWORD(2,2),&Ws)!=0)
	{
		printf("Init Socket Failed: %u\n",GetLastError());
		return -1;
	}

	ClientSocket = socket(AF_INET,SOCK_STREAM,IPPROTO_TCP);
	if(ClientSocket == INVALID_SOCKET)
	{
		printf("Create Socket Failed: %u\n", GetLastError());
		return -1;
	}

	he = gethostbyname(IP_ADDRESS);
	ServerAddr.sin_family = AF_INET;
	 memcpy(&ServerAddr.sin_addr, he->h_addr_list[0], sizeof(he->h_addr_list[0]));
	ServerAddr.sin_port = htons(PORT);
	memset(ServerAddr.sin_zero,0x00,8);

	printf("before connect\n");

	Ret = connect(ClientSocket,(struct sockaddr*)&ServerAddr,sizeof(ServerAddr));
	if(Ret == SOCKET_ERROR)
	{
		printf("Connect Error: %u\n", GetLastError());
		return -1;
	}
	else
	{
		printf("Connect Success!\n");
	}
	return ClientSocket;
}

int sendData(SOCKET client_socket, double d[], int LEN)
{
	int i;
	int Ret;
	int string_len=0;
	printf("before send\n");

	char c[150];

	for(i=0;i<LEN;++i)
	{
		sprintf(c+string_len,"%g",d[i]);
		string_len = strlen(c);
		c[string_len] = ' ';
		string_len++;
		printf("%d %lf ",i, d[i]);
	}
	c[string_len-1]='\n';
	c[string_len] = '\0';
	printf("\n%s\n",c);
	//cin.getline(SendBuffer, sizeof(SendBuffer));
	//int len = strlen(SendBuffer);
	//SendBuffer[len] = '\n';
	//SendBuffer[len+1] = '\0';
	//Ret = send(ClientSocket,SendBuffer,(int)strlen(SendBuffer),0);

	Ret = send(client_socket,c,(int)strlen(c),0);

	printf("%d\n", Ret);
		
	if(Ret == SOCKET_ERROR)
	{
		printf("\nend Error: %u\n", GetLastError());
	}

	printf("end send\n");

	return 0;
}

void socketClose(SOCKET client_socket)
{
	closesocket(client_socket);
	WSACleanup();
}