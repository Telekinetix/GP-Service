import models.ErrorType;

public class ErrorHandler {
  void error(ErrorType type, Exception e, String message) {
    System.err.println("An error has occurred: " + type.name());
    System.err.println(message);
    e.printStackTrace();
  }

  void error(ErrorType type, Exception e) {
    System.err.println("An error has occurred: " + type.name());
    e.printStackTrace();
  }

  byte[] buildErrorObject(ErrorType type) {
    return buildErrorObject(type, "");
  }

  byte[] buildErrorObject(ErrorType type, String message) {
    return ("{\"err\": \"" + type.name() + "\""+ (!message.isEmpty() ? "\"message\": \"" + message + "\"" : "")+ "}"
            + (char) 4).getBytes();
  }
}