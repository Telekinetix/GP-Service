import com.google.gson.Gson;
import models.Config;
import models.ErrorType;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigHandler {

  Config loadConfig() {
    try {
      String content = Files.readString(Path.of("config.json"));
      Gson gson = new Gson();
      return gson.fromJson(content, Config.class);
    } catch (Exception e) {
      // Crash out of app - Failed to load config file
      ErrorHandler.error(ErrorType.configError, e, "Failed to load config.");
      System.exit(1);
      return null;
    }
  }
}
