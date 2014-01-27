package weibo4j.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class WeiboConfig {
  public WeiboConfig() {}

  private static Properties props = new Properties();

  public static boolean isAutoUpdateToken = false;

  static {
    try {
      props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(
          "config.properties"));
      isAutoUpdateToken = "true".equalsIgnoreCase(props.getProperty("autoUpdateToken"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static String getValue(String key) {
    return props.getProperty(key);
  }

  public static String getValue(String key, String defaultValue) {
    return props.getProperty(key, defaultValue);
  }

  public static void updateProperties(String key, String value) {
    props.setProperty(key, value);
  }

  public static boolean isAutoUpdateToken() {
    return isAutoUpdateToken;
  }
}
