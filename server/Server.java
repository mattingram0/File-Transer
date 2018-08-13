/**
 * Created by matt on 22/01/2018.
 */

import java.io.IOException;
import java.net.ServerSocket;


public class Server {
    private ServerSocket listener;

    public static void main(String args[]){

        Server server = new Server();
        server.listenSocket();

    }

    public void listenSocket(){

        try{
            listener = new ServerSocket(9090);
            System.out.println("[+] Server listening on port 9090");
        } catch (IOException e) {
            System.out.println("[-] Server could not listen on port 9090");
            System.exit(-1);
        }

        while(true){
            ClientHelper clientHelper;

            try{
                clientHelper = new ClientHelper(listener.accept());
                Thread thread = new Thread(clientHelper);
                thread.start();
            } catch (IOException e) {
                System.out.println("[-] Server could not accept connection on port 9090");
                System.exit(-1);
            }
        }
    }

    protected void finalize(){
        try{
            listener.close();
        } catch (IOException e) {
            System.out.println("[-] Could not close socket");
            System.exit(-1);
        }
    }

}
