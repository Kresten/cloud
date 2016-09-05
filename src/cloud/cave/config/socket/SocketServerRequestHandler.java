package cloud.cave.config.socket;

import java.io.*;
import java.net.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.slf4j.*;

import cloud.cave.broker.*;
import cloud.cave.config.ObjectManager;
import cloud.cave.server.common.ServerConfiguration;

/**
 * The server request handler (SRH) that binds to the OS and forwards any
 * incoming requests on a server socket to the given server invoker.
 * <p>
 * The connection style is similar to HTTP/1.0 in that a separate connection is
 * made for every request. This is very simple and easy to implement but gives a
 * lot of connection overhead.
 * <p>
 * As this SRH is meant as a case study, it is abnormally talkative for a server
 * and prints a lot on the console instead of logging stuff.
 * 
 * @author Henrik Baerbak Christensen, Aarhus University.
 * 
 */
public class SocketServerRequestHandler implements ServerRequestHandler {
  private int portNumber;
  private Logger logger;
  private JSONParser parser;

  private ServerSocket serverSocket = null;
  private ObjectManager objectManager;

  @Override
  public void initialize(ObjectManager objectManager, ServerConfiguration config) {
    portNumber = config.get(0).getPortNumber();
    this.objectManager = objectManager;
    parser = new JSONParser();
    logger = LoggerFactory.getLogger(SocketServerRequestHandler.class);
  }

  @Override
  public void run() {
    openServerSocket();

    System.out.println("*** Server socket established ***");
    
    boolean isStopped = false;
    while (!isStopped) {

      Socket clientSocket = null;
      try {
        clientSocket = serverSocket.accept();
      } catch(IOException e) {
        if(isStopped) {
          System.out.println("Server Stopped.") ;
          return;
        }
        throw new RuntimeException(
            "Error accepting client connection", e);
      }

      try {
        readMessageAndDispatch(clientSocket);
      } catch (IOException e) {
        logger.error("IOException during readMessageAndDispatch", e);
        System.out.println("ERROR: IOException encountered, review log");
      }
    }
    System.out.println("Server Stopped.");
  }

  /** On purpose, the socket read and dispatch is very talkative for
   * teaching purposes - it makes it easy to see 'what is going on'
   * while understanding the SkyCave code. However, the StandardInvoker
   * will actually log every request and reply in the Inspector log
   * under the IPC topic, so the same information can be extracted
   * there.
   * 
   * @param clientSocket the socket to read raw messages from
   * @throws IOException in case of any socket expections
   */
  private void readMessageAndDispatch(Socket clientSocket) throws IOException {
    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
    BufferedReader in = new BufferedReader(new InputStreamReader(
        clientSocket.getInputStream()));

    String inputLine;
    JSONObject requestJson = null, reply = null;

    inputLine = in.readLine();
    System.out.println("--> [REQUEST] " + inputLine);
    
    if (inputLine == null) {
     reply = Marshaling.createInvalidReplyWithExplanation(StatusCode.SERVER_UNKNOWN_METHOD_FAILURE, 
         "Server read a null string from the socket???"); 
    } else {
      try {
        requestJson = (JSONObject) parser.parse(inputLine);
        reply = objectManager.getInvoker().handleRequest(requestJson);
        System.out.println("--< [REPLY] " + reply);
        
      } catch (ParseException e) {
        String errorMsg = "JSON Parse error on input = " + inputLine;
        logger.error(errorMsg, e);
        reply = Marshaling.createInvalidReplyWithExplanation(
            StatusCode.SERVER_FAILURE, errorMsg);
        System.out.println("--< [REPLY FAIL]: "+reply);
      }
    }
    out.println(reply.toString());

    in.close();
    out.close();
  }

  private void openServerSocket() {
    try {
      this.serverSocket = new ServerSocket(this.portNumber);
      System.out.println("Socket accepting on port: "+portNumber);
    } catch (IOException e) {
      logger.error("Cannot open port "+portNumber, e);
      System.out.println("Failed to open server socket at port "+portNumber);
      System.exit(-1);
    } 
  }
  
  public String toString() {
    return "SocketServerRequestHandler. Assigned to port: "+portNumber;
  }

}