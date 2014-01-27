package weibo4j.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import weibo4j.http.HttpClient;
import weibo4j.test.PropertyUtil;

public class TokenManagement {

  static Logger logger = Logger.getLogger(TokenManagement.class.getName());

  /**
   * token计数
   */
  private static Map<String, Long> tokenCountMap = new ConcurrentHashMap<String, Long>();

  /**
   * app
   */
  private static int APP_ID = 0;
  private static int APP_TOKEN_ID = 0;
  // apps: app_id, app_secret, app_redirect
  private static String[][] apps = {};
  public static String[][] tokens = {};

  private static List<String[]> proxyList = new ArrayList<String[]>();

  /**
   * 最后更新token的小时
   */
  private static int lastHour = -1;

  /**
   * 获取token前需要sleep的时间
   */
  private static long sleepTime = -1;

  // 每次启动，从一个随机的Token开始
  public static int count = (int) (Math.random() * 2);

  public static int proxyIndex = 0;

  public static Set<Integer> errorCodeSet;

  /**
   * 是否使用本地IP
   */
  public static boolean useLocalIP = false;

  // public static int count = (int) (Math.random()*tokens.length);
  // public static int count = 2;

  static {
    init();
  }

  public static void init() {
    try {
      if (WeiboConfig.isAutoUpdateToken()) {
        int tokenListNum = Integer.valueOf(WeiboConfig.getValue("tokenListNum"));
        apps = new String[tokenListNum][];
        tokens = new String[tokenListNum][];
        for (int i = 1; i <= tokenListNum; i++) {
          apps[i - 1] = new String[] {"app" + i, "", "", ""};
          List<String> tokenList = new LinkedList<String>();
          String[] tokenParts = WeiboConfig.getValue("tokenList" + i).split(",");
          for (String tokenPart : tokenParts) {
            if (!tokenPart.trim().isEmpty()) {
              tokenList.add(tokenPart.trim());
            }
          }
          if (tokenList.isEmpty()) {
            throw new Exception("tokenList" + i + " is empty");
          }
          tokens[i - 1] = tokenList.toArray(new String[tokenList.size()]);
        }

        String localIpValue = WeiboConfig.getValue("useLocalIP", "true");
        if (localIpValue != null && "true".equalsIgnoreCase(localIpValue)) {
          proxyIndex = -1;
          useLocalIP = true;
        }

        String proxyValue = WeiboConfig.getValue("proxyList", null);
        if (proxyValue != null && !proxyValue.trim().isEmpty()) {
          addProxyList(proxyValue);
        }

        String errorCodeValue = WeiboConfig.getValue("errorCodeList", null);
        if (errorCodeValue != null && !errorCodeValue.trim().isEmpty()) {
          addErrorCodeList(errorCodeValue);
        }

        setupApp();
        logger.info("init token success");

      }
    } catch (Exception ex) {
      System.out.println("init token error");
      ex.printStackTrace();
      System.exit(1);
    }
  }

  public static boolean isAutoUpdateToken() {
    return WeiboConfig.isAutoUpdateToken();
  }

  public static String getToken() {
    if (sleepTime > 0) {
      try {
        logger.info("sleep: " + sleepTime);
        Thread.sleep(sleepTime);
        sleepTime = 0;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return tokens[APP_ID][APP_TOKEN_ID];
  }

  public static void setupApp() {
    WeiboConfig.updateProperties("client_ID", apps[APP_ID][0]);
    WeiboConfig.updateProperties("client_SERCRET", apps[APP_ID][1]);
    WeiboConfig.updateProperties("redirect_URI", apps[APP_ID][2]);
    if (lastHour == -1) {
      Calendar cal = Calendar.getInstance();
      lastHour = cal.get(Calendar.HOUR_OF_DAY);
    }
  }

  public static void updateClient(HttpClient client, boolean updateToken) {
    if (updateToken) {
      TokenManagement.updateToken(client.getToken());
    }
    client.setToken(TokenManagement.getToken());
    client.setProxyHost(TokenManagement.getProxyHost());
    client.setProxyPort(TokenManagement.getProxyPort());
    client.setAutoManageMent(true);
  }

  public static boolean isUpdateToken(int errorCode) {
    return errorCodeSet.contains(errorCode);
  }

  public synchronized static void updateToken(String curToken) {
    if (!tokens[APP_ID][APP_TOKEN_ID].equalsIgnoreCase(curToken) || sleepTime > 0) {
      // 已经更新
      return;
    }

    long tokenCount = 0;
    if (tokenCountMap.containsKey(curToken)) {
      tokenCount = tokenCountMap.get(curToken);
    }

    logger.info("===== before updateToken [app: " + APP_ID + ", token:" + APP_TOKEN_ID
        + ", proxy: " + proxyIndex + ", hour: " + lastHour + ", count: " + tokenCount + "]");

    if (APP_TOKEN_ID < tokens[APP_ID].length - 1) {
      // 更新token
      APP_TOKEN_ID++;
    } else {
      APP_TOKEN_ID = 0;
      if (APP_ID < tokens.length - 1) {
        // 更新app
        APP_ID++;
      } else {
        APP_ID = 0;

        if (proxyList.size() > 0) {
          if (proxyIndex < proxyList.size() - 1) {
            proxyIndex++;
          } else {
            if (useLocalIP) {
              proxyIndex = -1;
            } else {
              proxyIndex = 0;
            }
            // 检查是否需要等待
            Calendar cal = Calendar.getInstance();
            int curHour = cal.get(Calendar.HOUR_OF_DAY);
            if (curHour == lastHour) {
              // 需要等待
              int curMinute = cal.get(Calendar.MINUTE);
              sleepTime = (60 - curMinute) * 60000;
              if (sleepTime > 300000) {
                sleepTime = 300000;
              }
            } else {
              lastHour = curHour;
            }
          }
        } else {
          // 检查是否需要等待
          Calendar cal = Calendar.getInstance();
          int curHour = cal.get(Calendar.HOUR_OF_DAY);
          if (curHour == lastHour) {
            // 需要等待
            int curMinute = cal.get(Calendar.MINUTE);
            sleepTime = (60 - curMinute) * 60000;
            if (sleepTime > 300000) {
              sleepTime = 300000;
            }
          } else {
            lastHour = curHour;
          }
        }
      }
      setupApp();
    }

    logger.info("===== after updateToken [app: " + APP_ID + ", token:" + APP_TOKEN_ID + ", proxy: "
        + proxyIndex + ", hour: " + lastHour + "]");

    if (sleepTime > 0) {
      logger.info("====== token usage =======");
      long totalCount = 0;
      for (Entry<String, Long> entry : tokenCountMap.entrySet()) {
        String token = entry.getKey();
        logger.info("token: " + token.substring(token.length() - 5) + ", count: "
            + entry.getValue());
        totalCount = totalCount + entry.getValue();
        tokenCountMap.put(token, 0l);
      }
      logger.info("====== total count: " + totalCount + " =======");
    }

  }

  public synchronized static void updateTokenCount(String curToken) {
    long count = 1;
    if (tokenCountMap.containsKey(curToken)) {
      count = tokenCountMap.get(curToken) + 1;
    }
    tokenCountMap.put(curToken, count);
  }

  // public static boolean isTokenAlive(String token) {
  // Account am = new Account();
  // am.client.setToken(token);
  // try {
  // RateLimitStatus json = am.getAccountRateLimitStatus();
  // if (json.getRemainingUserHits() != 0 && json.getRemainingIpHits() != 0) {
  // // if (json.getRemainingUserHits() != 0){
  // return true;
  // }
  // } catch (WeiboException e) {
  // // Log.logInfo("Token " + TokenManagement.count + " is dead");
  // }
  // return false;
  // }

  public static void addProxyList(String proxys) {
    proxyList = new LinkedList<String[]>();
    String[] proxysParts = proxys.split(",");
    for (String proxyInfo : proxysParts) {
      proxyInfo = proxyInfo.trim();
      String[] proxyParts = proxyInfo.split(":");
      if (proxyParts.length == 2) {
        proxyList.add(proxyParts);
      }
    }

  }

  public static void addErrorCodeList(String errorCodes) {
    errorCodeSet = new TreeSet<Integer>();
    String[] codeParts = errorCodes.split(",");
    for (String code : codeParts) {
      code = code.trim();
      if (code.isEmpty()) {
        continue;
      }
      errorCodeSet.add(Integer.valueOf(code));
    }

  }

  public static String getProxyHost() {
    if (proxyIndex == -1) {
      return null;
    } else {
      return proxyList.get(proxyIndex)[0];
    }
  }

  public static int getProxyPort() {
    if (proxyIndex == -1) {
      return -1;
    } else {
      return Integer.valueOf(proxyList.get(proxyIndex)[1]);
    }
  }

  public static Properties loadProperty(String filePath, String encode) throws Exception {
    Properties prop = null;
    InputStream propStream = null;
    InputStreamReader reader = null;
    try {
      File file = new File(filePath);
      if (file.exists()) {
        propStream = new FileInputStream(file);
      } else {
        propStream = PropertyUtil.class.getResourceAsStream(filePath);
      }

      reader = new InputStreamReader(propStream, encode); // "UTF-8"

      prop = new Properties();
      prop.load(reader);
    } catch (FileNotFoundException e) {
      logger.error("not found: " + filePath, e);
      throw new Exception(e.getMessage());
    } catch (UnsupportedEncodingException e) {
      logger.error("encoding error: " + filePath, e);
      throw new Exception(e.getMessage());
    } catch (IOException e) {
      logger.error("io error: " + filePath, e);
      throw new Exception(e.getMessage());
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException e) {}
      }
    }
    return prop;
  }

  // public static String getToken() throws WeiboException{
  // int j = 0;
  // while(true){
  // int num_tokens = tokens.length; //如果为加油场景，仅使用第一个Token
  // int num = count % num_tokens; //8为tokens中设置的8个Tokens
  //
  // String temptoken = tokens[num];
  // if (APP_TOKEN_ID >= 0) temptoken = app_tokens[APP_TOKEN_ID];
  // //printTokenStatus();
  // if (isTokenAlive(temptoken)){
  // return temptoken;
  // }else{
  // Log.logInfo("Token "+num+" is out!!!!!!!!!!!!!!!!!!!change token!!!!!!!!!!!!!!!!");
  // count++;
  // j++;
  // }
  //
  // if (j> num_tokens){
  // //break;
  // waitToken();
  // j=0;
  // }
  // }
  // //throw new WeiboException("no alive token");
  // }

  // public static void waitToken() throws WeiboException {
  // long current = System.currentTimeMillis();
  // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  // Calendar cal = Calendar.getInstance();
  // Date date = cal.getTime();
  // Log.logInfo("当前时间:" + df.format(date));
  // cal.add(Calendar.HOUR, 0);
  // cal.add(Calendar.MINUTE, 32);
  // date = cal.getTime();
  // long nextTime = date.getTime();
  //
  // printTokenStatus();
  //
  // Log.logInfo("下次启动时间" + df.format(date));
  // try {
  // Thread.sleep((nextTime - current));
  // } catch (InterruptedException e) {
  // e.printStackTrace();
  // }
  // Log.logInfo("程序启动.....");
  // }

  // public static void printTokenStatus() throws WeiboException {
  // Account am = new Account();
  // for (int i = 0; i < tokens.length; i++) {
  // am.client.setToken(tokens[i]);
  // try {
  // RateLimitStatus json = am.getAccountRateLimitStatus();
  // Log.logInfo("Token" + i + "状态: ip:" + json.getRemainingIpHits()
  // + ";user:" + json.getRemainingUserHits() + ";time:"
  // + json.getResetTime());
  // } catch (WeiboException e) {
  // // e.printStackTrace();
  // }
  // }
  // }

}
