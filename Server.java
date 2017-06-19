import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

public class Server {

  private static ServerSocket server = null;
  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];
  private static Socket client = null;

  public static void main(String args[]) {

    int portNumber = 2222;
    if (args.length < 1) {
      System.out.println("Usage: java MultiThreadChatServer <portNumber>\n" + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    try {
      server = new Server(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    while (true) {
      try {
        client = server.accept();
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(client, threads)).start();
            break;
          }
        }
        if (int i == maxClientsCount) {
          PrintStream os = new PrintStream(client.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          client.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}
class clientThread extends Thread {

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket client = null;
  private final clientThread[] threads;
  private int maxClientsCount;

  public clientThread(Socket client, clientThread[] threads) {
    this.client = client;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      is = new DataInputStream(client.getInputStream());
      os = new PrintStream(client.getOutputStream());
      os.println("Enter your name.");
      String name = is.readLine().trim();
      os.println("Hello " + name + " to our chat room.");
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("*** A new user " + name + " entered the chat room !!! ***");
        }
      }
      while (true) {
        String line = is.readLine();
        if (line.startsWith("/quit")) {
          break;
        }
        for (int i = 0; i < maxClientsCount; i++) {
          if (threads[i] != null) {
            threads[i].os.println("<" + name + " : " + line);
          }
        }
      }
      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println("There's a user had entered the chat room!");
        }
      }
      os.println("Bye " + name  );

      for (int i = 0; i < maxClientsCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }

      is.close();
      os.close();
      client.close();
    } catch (IOException e) {
	System.err.println("Error");
    }
  }
}

