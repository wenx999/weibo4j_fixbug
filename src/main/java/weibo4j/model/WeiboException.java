/*
 * Copyright (c) 2007-2009, Yusuke Yamamoto All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. Redistributions in
 * binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution. Neither
 * the name of the Yusuke Yamamoto nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package weibo4j.model;

import java.util.HashMap;
import java.util.Map;

import weibo4j.org.json.JSONException;
import weibo4j.org.json.JSONObject;

/**
 * An exception class that will be thrown when WeiboAPI calls are failed.<br>
 * In case the Weibo server returned HTTP error code, you can get the HTTP
 * status code using getStatusCode() method.
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class WeiboException extends Exception {
  private int statusCode = -1;
  private int errorCode = -1;
  private String request;
  private String error;
  private static final long serialVersionUID = -2623309261327598087L;

  public static Map<Integer, String> errorCodeMap = new HashMap<Integer, String>();
  static {
    errorCodeMap.put(10001, "系统错误");
    errorCodeMap.put(10002, "服务暂停");
    errorCodeMap.put(10003, "远程服务错误");
    errorCodeMap.put(10004, "IP限制不能请求该资源");
    errorCodeMap.put(10005, "该资源需要appkey拥有授权");
    errorCodeMap.put(10006, "缺少source (appkey) 参数");
    errorCodeMap.put(10007, "不支持的MediaType (%s)");
    errorCodeMap.put(10008, "参数错误，请参考API文档");
    errorCodeMap.put(10009, "任务过多，系统繁忙");
    errorCodeMap.put(10010, "任务超时");
    errorCodeMap.put(10011, "RPC错误");
    errorCodeMap.put(10012, "非法请求");
    errorCodeMap.put(10013, "不合法的微博用户");
    errorCodeMap.put(10014, "应用的接口访问权限受限");
    errorCodeMap.put(10016, "缺失必选参数 (%s)，请参考API文档");
    errorCodeMap.put(10017, "参数值非法，需为 (%s)，实际为 (%s)，请参考API文档");
    errorCodeMap.put(10018, "请求长度超过限制");
    errorCodeMap.put(10020, "接口不存在");
    errorCodeMap.put(10021, "请求的HTTP METHOD不支持，请检查是否选择了正确的POST/GET方式");
    errorCodeMap.put(10022, "IP请求频次超过上限");
    errorCodeMap.put(10023, "用户请求频次超过上限");
    errorCodeMap.put(10024, "用户请求特殊接口 (%s) 频次超过上限");
    errorCodeMap.put(20001, "IDs参数为空");
    errorCodeMap.put(20002, "Uid参数为空");
    errorCodeMap.put(20003, "用户不存在");
    errorCodeMap.put(20005, "不支持的图片类型，仅仅支持JPG、GIF、PNG");
    errorCodeMap.put(20006, "图片太大");
    errorCodeMap.put(20007, "请确保使用multpart上传图片");
    errorCodeMap.put(20008, "内容为空");
    errorCodeMap.put(20009, "IDs参数太长了");
    errorCodeMap.put(20012, "输入文字太长，请确认不超过140个字符");
    errorCodeMap.put(20013, "输入文字太长，请确认不超过300个字符");
    errorCodeMap.put(20014, "安全检查参数有误，请再调用一次");
    errorCodeMap.put(20015, "账号、IP或应用非法，暂时无法完成此操作");
    errorCodeMap.put(20016, "发布内容过于频繁");
    errorCodeMap.put(20017, "提交相似的信息");
    errorCodeMap.put(20018, "包含非法网址");
    errorCodeMap.put(20019, "提交相同的信息");
    errorCodeMap.put(20020, "包含广告信息");
    errorCodeMap.put(20021, "包含非法内容");
    errorCodeMap.put(20022, "此IP地址上的行为异常");
    errorCodeMap.put(20031, "需要验证码");
    errorCodeMap.put(20032, "发布成功，目前服务器可能会有延迟，请耐心等待1-2分钟");
    errorCodeMap.put(20101, "不存在的微博");
    errorCodeMap.put(20102, "不是你发布的微博");
    errorCodeMap.put(20103, "不能转发自己的微博");
    errorCodeMap.put(20104, "不合法的微博");
    errorCodeMap.put(20109, "微博ID为空");
    errorCodeMap.put(20111, "不能发布相同的微博");
    errorCodeMap.put(20201, "不存在的微博评论");
    errorCodeMap.put(20202, "不合法的评论");
    errorCodeMap.put(20203, "不是你发布的评论");
    errorCodeMap.put(20204, "评论ID为空");
    errorCodeMap.put(20301, "不能给不是你粉丝的人发私信");
    errorCodeMap.put(20302, "不合法的私信");
    errorCodeMap.put(20303, "不是属于你的私信");
    errorCodeMap.put(20305, "不存在的私信");
    errorCodeMap.put(20306, "不能发布相同的私信");
    errorCodeMap.put(20307, "非法的私信ID");
    errorCodeMap.put(20401, "域名不存在");
    errorCodeMap.put(20402, "Verifier错误");
    errorCodeMap.put(20501, "参数source_user或者target_user的用户不存在");
    errorCodeMap.put(20502, "必须输入目标用户id或者screen_name");
    errorCodeMap.put(20503, "参数user_id必须是你关注的用户");
    errorCodeMap.put(20504, "你不能关注自己");
    errorCodeMap.put(20505, "加关注请求超过上限");
    errorCodeMap.put(20506, "已经关注此用户");
    errorCodeMap.put(20507, "需要输入验证码");
    errorCodeMap.put(20508, "根据对方的设置，你不能进行此操作");
    errorCodeMap.put(20509, "悄悄关注个数到达上限");
    errorCodeMap.put(20510, "不是悄悄关注人");
    errorCodeMap.put(20511, "已经悄悄关注此用户");
    errorCodeMap.put(20512, "你已经把此用户加入黑名单，加关注前请先解除");
    errorCodeMap.put(20513, "你的关注人数已达上限");
    errorCodeMap.put(20521, "hi 超人，你今天已经关注很多喽，接下来的时间想想如何让大家都来关注你吧！如有问题，请联系新浪客服：400 690 0000");
    errorCodeMap.put(20522, "还未关注此用户");
    errorCodeMap.put(20523, "还不是粉丝");
    errorCodeMap.put(20524, "hi 超人，你今天已经取消关注很多喽，接下来的时间想想如何让大家都来关注你吧！如有问题，请联系新浪客服：400 690 0000");
    errorCodeMap.put(20601, "列表名太长，请确保输入的文本不超过10个字符");
    errorCodeMap.put(20602, "列表描叙太长，请确保输入的文本不超过70个字符");
    errorCodeMap.put(20603, "列表不存在");
    errorCodeMap.put(20604, "不是列表的所属者");
    errorCodeMap.put(20605, "列表名或描叙不合法");
    errorCodeMap.put(20606, "记录已存在");
    errorCodeMap.put(20607, "数据库错误，请联系系统管理员");
    errorCodeMap.put(20608, "列表名冲突");
    errorCodeMap.put(20610, "目前不支持私有分组");
    errorCodeMap.put(20611, "创建列表失败");
    errorCodeMap.put(20612, "目前只支持私有分组");
    errorCodeMap.put(20613, "订阅列表达到上限");
    errorCodeMap.put(20614, "创建列表达到上限，请参考API文档");
    errorCodeMap.put(20615, "列表成员上限，请参考API文档");
    errorCodeMap.put(20701, "不能提交相同的收藏标签");
    errorCodeMap.put(20702, "最多两个收藏标签");
    errorCodeMap.put(20703, "收藏标签名不合法");
    errorCodeMap.put(20801, "参数trend_name是空值");
    errorCodeMap.put(20802, "参数trend_id是空值");
    errorCodeMap.put(20901, "错误:已经添加了黑名单");
    errorCodeMap.put(20902, "错误:已达到黑名单上限");
    errorCodeMap.put(20903, "错误:不能添加系统管理员为黑名单");
    errorCodeMap.put(20904, "错误:不能添加自己为黑名单");
    errorCodeMap.put(20905, "错误:不在黑名单中");
    errorCodeMap.put(21001, "标签参数为空");
    errorCodeMap.put(21002, "标签名太长，请确保每个标签名不超过14个字符");
    errorCodeMap.put(21101, "参数domain错误");
    errorCodeMap.put(21102, "该手机号已经被使用");
    errorCodeMap.put(21103, "该用户已经绑定手机");
    errorCodeMap.put(21104, "Verifier错误");
    errorCodeMap.put(21301, "认证失败");
    errorCodeMap.put(21302, "用户名或密码不正确");
    errorCodeMap.put(21303, "用户名密码认证超过请求限制");
    errorCodeMap.put(21304, "版本号错误");
    errorCodeMap.put(21305, "缺少必要的参数");
    errorCodeMap.put(21306, "OAuth参数被拒绝");
    errorCodeMap.put(21307, "时间戳不正确");
    errorCodeMap.put(21308, "参数nonce已经被使用");
    errorCodeMap.put(21309, "签名算法不支持");
    errorCodeMap.put(21310, "签名值不合法");
    errorCodeMap.put(21311, "参数consumer_key不存在");
    errorCodeMap.put(21312, "参数consumer_key不合法");
    errorCodeMap.put(21313, "参数consumer_key缺失");
    errorCodeMap.put(21314, "Token已经被使用");
    errorCodeMap.put(21315, "Token已经过期");
    errorCodeMap.put(21316, "Token不合法");
    errorCodeMap.put(21317, "Token不合法");
    errorCodeMap.put(21318, "Pin码认证失败");
    errorCodeMap.put(21319, "授权关系已经被解除");
    errorCodeMap.put(21320, "使用OAuth2必须使用https");
    errorCodeMap.put(21321, "未审核的应用使用人数超过限制");
    errorCodeMap.put(21327, "token过期");
    errorCodeMap.put(21335, "uid参数仅允许传入当前授权用户uid");
    errorCodeMap.put(21501, "参数urls是空的");
    errorCodeMap.put(21502, "参数urls太多了");
    errorCodeMap.put(21503, "IP是空值");
    errorCodeMap.put(21504, "参数url是空值");
    errorCodeMap.put(21601, "需要系统管理员的权限");
    errorCodeMap.put(21602, "含有敏感词");
    errorCodeMap.put(21603, "通知发送达到限制");
    errorCodeMap.put(21701, "提醒失败，需要权限");
    errorCodeMap.put(21702, "无效分类");
    errorCodeMap.put(21703, "无效状态码");
    errorCodeMap.put(21901, "地理信息输入错误");
  }

  public WeiboException(String msg) {
    super(msg);
  }

  public WeiboException(Exception cause) {
    super(cause);
  }

  public WeiboException(String msg, int statusCode) throws JSONException {
    super(msg);
    this.statusCode = statusCode;
  }

  public WeiboException(String msg, JSONObject json, int statusCode) throws JSONException {
    super(msg + "\n error:" + json.getString("error") + " error_code:" + json.getInt("error_code")
        + getCodeMsg(json.getInt("error_code")) + json.getString("request"));
    this.statusCode = statusCode;
    this.errorCode = json.getInt("error_code");
    this.error = getCodeMsg(errorCode) + json.getString("error");
    this.request = json.getString("request");

  }

  public WeiboException(String msg, Exception cause) {
    super(msg, cause);
  }

  public WeiboException(String msg, Exception cause, int statusCode) {
    super(msg, cause);
    this.statusCode = statusCode;

  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public int getErrorCode() {
    return errorCode;
  }

  public String getRequest() {
    return request;
  }

  public String getError() {
    return error;
  }

  public static String getCodeMsg(int code) {
    if (errorCodeMap.containsKey(code)) {
      return "reason: " + errorCodeMap.get(code) + ", ";
    } else {
      return "";
    }
  }

}
