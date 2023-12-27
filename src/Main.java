import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.google.gson.Gson;
import models.Config;
import models.EPOSMessage;
import models.ErrorType;

import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main {
  private static final Gson gson = new Gson();

  public static void main(String[] args) {
    ErrorHandler errorHandler = new ErrorHandler();
    Config config = new ConfigHandler(errorHandler).loadConfig();
    IngenicoHandler ingenicoHandler = new IngenicoHandler(config, errorHandler).connectToDevice();

    try {
      ServerSocket serverSocket = new ServerSocket(config.serverPort);
      while (true) {
        waitForConnection(serverSocket, ingenicoHandler, errorHandler);
      }
    } catch (IOException e) {
      // Crash out of app - Likely failed to bind to port
      errorHandler.error(ErrorType.socketError, e, "Likely failed to bind to port");
      System.exit(1);
    }
  }

  public static void waitForConnection(ServerSocket serverSocket, IngenicoHandler ingenicoHandler, ErrorHandler errorHandler) {
    if (ingenicoHandler.device == null) ingenicoHandler.connectToDevice();

    try {
      new ConnectionHandler(serverSocket.accept(), ingenicoHandler, errorHandler).start();
    }
    catch (IOException e) {
      // Logs error locally if the socket dies.
      errorHandler.error(ErrorType.socketError, e, "Socket died while connecting to EPOS");
    }
  }

  private static class ConnectionHandler extends Thread {
    private final IngenicoHandler ingenicoHandler;
    private final ErrorHandler errorHandler;
    DataOutputStream out;
    DataInputStream in;

    public ConnectionHandler(Socket socket, IngenicoHandler ingenicoHandler, ErrorHandler errorHandler) {
      this.ingenicoHandler = ingenicoHandler;
      this.errorHandler = errorHandler;

      try {
        out = new DataOutputStream(socket.getOutputStream());
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
      } catch (IOException e) {
        // Logs error locally if the socket dies.
        errorHandler.error(ErrorType.socketError, e, "Socket died while connecting to EPOS");
      }
    }

    public void run() {
      System.out.println("Started listening to EPOS");
      while (true) {
        try {
          EPOSMessage msg = waitForMessage();
          if (msg == null || Objects.equals(msg.type, "closeConnection")) break; //When we receive this message from 4D, break from loop.

          new MessageHandler(ingenicoHandler, out, msg, errorHandler).start();
        } catch (IOException e) {
          // Logs error locally if the socket dies.
          errorHandler.error(ErrorType.socketError, e, "Socket died while receiving message from EPOS");
        }
      }
    }

    public EPOSMessage waitForMessage() throws IOException {
      byte[] messageByte = new byte[1024];
      StringBuilder dataString = new StringBuilder(1024);

      int lastChar = 0;
      do {
        int currentBytesRead = in.read(messageByte);
        if(currentBytesRead>0) {
          dataString.append(new String(messageByte, 0, currentBytesRead, StandardCharsets.UTF_8));
          lastChar = dataString.charAt(dataString.length() - 1);
        }
      } while ((lastChar != 3) && (lastChar != 0));

      if (dataString.isEmpty()) return null;

      String outString = dataString.substring(0, dataString.length() - 1);
      return gson.fromJson(outString, EPOSMessage.class);
    }
  }

  private static class MessageHandler extends Thread {
    private final IngenicoHandler ingenicoHandler;
    private final EPOSMessage msg;
    private final DataOutputStream out;
    private final ErrorHandler errorHandler;

    public MessageHandler(IngenicoHandler ingenicoHandler, DataOutputStream out, EPOSMessage msg, ErrorHandler errorHandler) {
      this.ingenicoHandler = ingenicoHandler;
      this.out = out;
      this.msg = msg;
      this.errorHandler = errorHandler;
    }

    public void run() {
      ingenicoHandler.setupCallback(out);

      IngenicoTerminalResponse resp = null;
      try {

        if (this.ingenicoHandler.device == null) {
          postToEPOS(errorHandler.buildErrorObject(ErrorType.ingenicoDeviceNotConnected));
          return;
        }

        if (Objects.equals(msg.type, "Sale")) {
          resp = ingenicoHandler.doSale(new BigDecimal(msg.value), msg.id);
        } else if (Objects.equals(msg.type, "Return")) {
          resp = ingenicoHandler.doRefund(new BigDecimal(msg.value), msg.id);
        } else if (Objects.equals(msg.type, "Cancel")) {
          resp = ingenicoHandler.cancelTransaction();
        } else if (Objects.equals(msg.type, "Status")) {
          resp = ingenicoHandler.getStatus();
        }

        String json = gson.toJson(resp) + (char) 4;
        postToEPOS(json.getBytes());
      } catch (ApiException e) {
        // Logs ingenico error locally and to the EPOS socket
        errorHandler.error(ErrorType.ingenicoGenericError, e);
        postToEPOS(errorHandler.buildErrorObject(ErrorType.ingenicoGenericError, e.getMessage()));
      }
    }

    public void postToEPOS(byte[] data) {
      try {
        out.write(data);
      } catch (IOException e) {
        errorHandler.error(ErrorType.socketError, e); // Logs error locally if the socket dies.
      }
    }
  }
}
