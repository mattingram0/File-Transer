import java.io.*;
import java.net.Socket;
import java.lang.StringBuilder;

public class ClientHelper implements Runnable {

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private BufferedInputStream bis;
    private BufferedOutputStream bos;

    ClientHelper(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        String command;
        System.out.println("[+] Connection accepted from: " + socket.getLocalAddress().toString() + " on port: " + Integer.toString(socket.getLocalPort()) + "\n");

        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            bis = new BufferedInputStream(socket.getInputStream());
            bos = new BufferedOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("[-] Unable to create input and output streams");
            return;
        }

        while (true) {
            try {

                //Go into wait for command mode
                command = getCommand();

                switch (command) {
                    case "UPLD":
                        upload();
                        break;
                    case "LIST":
                        list();
                        break;
                    case "DWLD":
                        download();
                        break;
                    case "DELF":
                        delete();
                        break;
                    case "QUIT":
                        return;
                }

            } catch (InputException e) {
                System.out.println("[-] " + e.getMessage());
                return; // Exiting thread
            }
        }
    }

    public void download(){
        StringBuilder filename = new StringBuilder();
        File downloadFile;
        ByteArrayOutputStream byteOutputStream;
        BufferedInputStream bfis;
        byte[] buffer;
        int readBytes;

        int length;
        try {
            length = dis.readInt();

            for(int i = 0; i < length; i++){
                filename.append(dis.readChar());
            }

            //Check file exists
            if (new File(filename.toString()).isFile()) {
                downloadFile = new File(filename.toString());
                bfis = new BufferedInputStream(new FileInputStream(downloadFile));
                byteOutputStream = new ByteArrayOutputStream();

                buffer = new byte[1024];

                //Read file into buffer
                while ((readBytes = bfis.read(buffer)) > 0) {
                    byteOutputStream.write(buffer, 0, readBytes);
                }
            } else {
                dos.writeInt(-1);
                return;
            }

            //Send file size
            buffer = byteOutputStream.toByteArray();
            dos.writeInt(buffer.length);

            //Send file
            bos.write(buffer, 0 , buffer.length);
            bos.flush();
        } catch (IOException e){
            System.out.println("[-] Error whilst downloading file from Server to Client");
        }
    }

    public void delete() {
        int length;
        int confirmation;
        StringBuilder filename = new StringBuilder();
        File file;

        try {
            length = dis.readInt();

            for(int i = 0; i < length; i++){
                filename.append(dis.readChar());
            }

            if (new File(filename.toString()).isFile()) {
                dos.writeInt(0);
                file = new File(filename.toString());
            } else {
                dos.writeInt(-1);
                return;
            }

            confirmation = dis.readInt();

            if (confirmation == 0) {
                if (file.delete()) {
                    dos.writeInt(0);
                } else {
                    dos.writeInt(-1);
                }
            }
        } catch (IOException e) {
            System.out.println("[-] Error whilst deleting file from server");
        }
    }

    public void upload() {
        int filenameLength;
        int filesize;
        int readBytes;
        int totalBytes = 0;
        byte[] buffer = new byte[1024];
        StringBuilder filename = new StringBuilder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        FileOutputStream fileOutputStream;
        File outputFile;


        try {
            //Read filename size and filename
            filenameLength = dis.readInt();

            for(int i = 0; i < filenameLength; i++){
                filename.append(dis.readChar());
            }

            //Check if file exists, and if user wants to overwrite
            if(new File(filename.toString()).isFile()){
                dos.writeInt(-1);

                if(dis.readInt() != 0){
                    System.out.println("[-] User cancelled upload");
                    return;
                }
            } else {
                dos.writeInt(0);
            }

            //Send status ready to receive bytes
            dos.writeInt(0);
        } catch (IOException e) {
            System.out.println("[-] Unable to read filename or filesize");
            return;
        }

        try {

            //read filesize
            filesize = dis.readInt();

            //Receive and read bytes
            while (totalBytes < filesize) {
                readBytes = bis.read(buffer);
                outputStream.write(buffer, 0, readBytes);
                totalBytes += readBytes;
            }

            //Send number of bytes received
            buffer = outputStream.toByteArray();
            dos.writeInt(buffer.length);
        } catch (IOException e) {
            System.out.println("[-] Unable to read file ");
            return;
        }

        //Write buffer to file
        if (totalBytes == filesize) {
            try {
                outputFile = new File(filename.toString());
                outputFile.createNewFile();
                fileOutputStream = new FileOutputStream(outputFile);
                fileOutputStream.write(buffer);
            } catch (IOException e) {
                System.out.println("[-] Unable to save file");
            }
        } else {
            System.out.println("[-] Incorrect number of bytes received, file not saved");
        }

    }

    public void list() {
        StringBuilder fileList = new StringBuilder();
        StringBuilder directoryList = new StringBuilder();

        File folder = new File(".");
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                fileList.append(file.getName() + "\n");
            }
            if (file.isDirectory()) {
                directoryList.append("[D] " + file.getName() + "\n");
            }
        }

        try {
            dos.writeInt((directoryList.toString() + fileList.toString()).length());
            dos.writeChars(directoryList.toString() + fileList.toString());
        } catch (IOException e) {
            System.out.println("[-] Error whilst listing files on server");
        }
    }

    public String getCommand() throws InputException {

        StringBuilder command = new StringBuilder("");

        while (!command.toString().equals("LIST") && !command.toString().equals("DWLD") && !command.toString().equals("DELF") && !command.toString().equals("QUIT") && !command.toString().equals("UPLD")) {

            command = new StringBuilder("");

            try {
                for(int i = 0; i < 4; i++){
                    command.append(dis.readChar());
                }
            } catch (IOException e) {
                throw new InputException("Unable to read command, or connection closed by client");
            }

            if (command.toString().equals("ECHO")) {
                try {
                    dos.writeInt(0);
                } catch (IOException e) {
                    throw new InputException("Unable to send heartbeat response");
                }
            }
        }

        System.out.println("[+] " + command + " command received from " + socket.getLocalAddress().toString() + ":");
        return command.toString();
    }
}