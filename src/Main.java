import com.global.api.entities.exceptions.ApiException;
import com.global.api.terminals.ingenico.responses.IngenicoTerminalResponse;
import com.google.gson.Gson;
import models.Config;
import models.EPOSMessage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
    IngenicoHandler ingenicoHandler = new IngenicoHandler(config, errorHandler);

    try {
      ServerSocket serverSocket = new ServerSocket(config.serverPort);
      while (true) {
        new ConnectionHandler(serverSocket.accept(), ingenicoHandler, errorHandler).start();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
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
        // TODO: Handle error
        errorHandler.logError("Error in ConnectionHandler");
      }
    }

    public void run() {
      System.out.println("Started listening to EPOS");
      while (true) {
        try {
          EPOSMessage msg = waitForMessage();
          System.out.println("Received message: " + msg.type);
          new TransactionHandler(ingenicoHandler, out, msg, errorHandler).start();
        } catch (IOException e) {
          // TODO: Handle error
          errorHandler.logError("Error in ConnectionHandler");
          //throw new RuntimeException(e);
        }
      }
    }

    public EPOSMessage waitForMessage() throws IOException {
      byte[] messageByte = new byte[1024];
      StringBuilder dataString = new StringBuilder(1024);
      EPOSMessage response;

      int lastChar = 0;
      do {
        int currentBytesRead = in.read(messageByte);
        if(currentBytesRead>0) {
          dataString.append(new String(messageByte, 0, currentBytesRead, StandardCharsets.UTF_8));
          lastChar = dataString.charAt(dataString.length() - 1);
        }
//        if(!dataString.isEmpty()){
//          lastChar = dataString.charAt(dataString.length() - 1);
//        }
      } while ((lastChar != 3) && (lastChar != 0));

      if(!dataString.isEmpty()){
        String outString = dataString.substring(0, dataString.length() - 1);
        response = gson.fromJson(outString, EPOSMessage.class);
      } else {
        response = new EPOSMessage(0, "termination", "","");
      }

      return response;
    }
  }

  private static class TransactionHandler extends Thread {
    private final IngenicoHandler ingenicoHandler;
    private final EPOSMessage msg;
    private final DataOutputStream out;
    private final ErrorHandler errorHandler;

    public TransactionHandler(IngenicoHandler ingenicoHandler, DataOutputStream out, EPOSMessage msg, ErrorHandler errorHandler) {
      this.ingenicoHandler = ingenicoHandler;
      this.out = out;
      this.msg = msg;
      this.errorHandler = errorHandler;
    }

    public void run() {
      System.out.println("Transaction handling started");
      ingenicoHandler.setupCallback(out);

      IngenicoTerminalResponse resp = null;
      try {
        if (Objects.equals(msg.type, "Sale")) {
          resp = ingenicoHandler.doSale(new BigDecimal(msg.value), msg.id);
        } else if (Objects.equals(msg.type, "Return")) {
          resp = ingenicoHandler.doRefund(new BigDecimal(msg.value), msg.id);
        } else if (Objects.equals(msg.type, "Cancel")) {
          resp = ingenicoHandler.cancelTransaction();
        }

        String json = gson.toJson(resp) + (char) 4;
        out.write(json.getBytes());
      } catch (ApiException | IOException e) {
        // TODO: Handle error
        errorHandler.logError("Error in TransactionHandler");
      }
    }
  }
}
