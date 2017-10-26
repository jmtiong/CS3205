package sg.edu.nus.cs3205.subsystem3.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

public class MyLogger{

  public static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
  static private SimpleFormatter formatterTxt;
  static {
    try{
      Handler fileHandler = new FileHandler("/home/jim/temp/cs3205s3.log");
      formatterTxt = new SimpleFormatter();
      fileHandler.setFormatter(formatterTxt);
      LOGGER.addHandler(fileHandler);
    } catch(Exception e){
      e.printStackTrace();
    }
  }
}
