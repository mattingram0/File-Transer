# File Transfer System
A simple client and server file transfer system that uses Java sockets

## Prerequisites
To run the distributed system locally on your machine, the latest version of **Java** needs to be installed.

## Installation
Click 'Clone or download' above and then 'Download ZIP', or alternatively run the following command from the command line:

```
git clone https://github.com/mattingram0/File-Transfer.git
```

To recompile client program, navigate to /[PATH]/client/ and run:
```
javac Client.java InputException.java TransferException.java
```

To recompile server program, navigate to /[PATH]/server/ and run:

```
javac Server.java InputException.java TransferException.java ClientHelper.java
```

## Running
/client/ contains the client source files and programs, /server/ contains the server source files and programs. 

* Please note the following:
  * Socket connection uses port 9090
  * [PATH] is path to /File-Transfer/ directory 

To run the client program, navigate to /[PATH]/client/ and run:
```
java Client
```

To run the server program, navigate to /[PATH]/server/ and run:
```
java Server
```
