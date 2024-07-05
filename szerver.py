from socket import *
from threading import *

# Laszlo Botond (lbim2260) - 522/2

def getAllUsers():
    # for the list
    global allNames
    ans = "#allUsers:"
    for thisName in allNames:
        ans = ans + thisName + ":"
    ans = ans[0:len(ans)-1] + "\n"
    return ans

def getTextUsers():
    # for chat popup
    global allNames
    ans = "#activeUsers#Active users: "
    for thisName in allNames:
        ans = ans + thisName + ", "
    ans = ans[0:len(ans)-2] + "\n"
    print(ans)
    return ans

def answer_request(connectionSocket, _):
    global allSockets
    global allNames
    nameConfirmed = False
    print(len(allSockets))
    while True:
        try:
            request = connectionSocket.recv(2048).decode()
            print('Request received: ' + request)

            #process request
            isName = False
            isPriv = False
            if request[0:6] == "#name:":
                myName = request[6:len(request) - 3] # excludes newline
                if myName in allNames:
                    connectionSocket.send("#errorName\n".encode())
                    continue
                allNames.append(myName)
                nameConfirmed = True
                isName = True
            elif request[0:8] == "#private":
                isPriv = True
                target = request.split("#")[2]
                msg = "<PRIVATE from:" + myName + ">" + "#".join(request.split("#")[3:])
            elif request[0:6] == "#users":
                connectionSocket.send(getTextUsers().encode())
                continue
            else:
                isName = False
            
            # build answer
            targetFound = False
            i = 0
            for thisSocket in allSockets:
                if (isName):
                    thisSocket.send(getAllUsers().encode())
                elif (isPriv):
                    if (allNames[i] == target):
                        thisSocket.send(msg.encode())
                        targetFound = True
                else:
                    thisSocket.send((request + "\n").encode())
                i += 1
            if isPriv:
                if targetFound:
                    selfMsg = "<PRIVATE to:" + target + "> " + "#".join(request.split("#")[3:])
                else:
                    selfMsg = "<ERROR> User not active!\n"
                connectionSocket.send(selfMsg.encode())
            print('Answer sent!')

        except Exception:
            print('Connection closed')
            # remove socket and name
            allSockets.remove(connectionSocket)
            if (myName in allNames) and nameConfirmed:
                allNames.remove(myName)
            # update names for everyone else
            for thisSocket in allSockets:
                thisSocket.send(getAllUsers().encode())
            break

allSockets = []
allNames = []
serverPort = 22600
serverSocket = socket(AF_INET,SOCK_STREAM)
serverSocket.bind(("localhost",serverPort))
serverSocket.listen(1)
print("The server is ready to receive")
while True:
    connectionSocket, addr = serverSocket.accept()
    allSockets.append(connectionSocket)
    print("Connection created! ")
    x = Thread(target=answer_request, args=(connectionSocket, 0))
    x.start()