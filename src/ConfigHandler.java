import com.google.gson.Gson;
import models.Config;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {
  public ErrorHandler ErrorHandler;

  public ConfigHandler(ErrorHandler errorHandler) {
    this.ErrorHandler = errorHandler;
  }

  Config loadConfig() {
    try {
      String content = Files.readString(Path.of("config.json"));
      Gson gson = new Gson();
      return gson.fromJson(content, Config.class);
    } catch (Exception e) {
      // log error
      throw new RuntimeException(e);
    }
  }
}
