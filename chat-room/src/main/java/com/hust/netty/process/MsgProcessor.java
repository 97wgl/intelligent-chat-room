package com.hust.netty.process;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hust.common.ClassOneData;
import com.hust.common.SessionResponseCounterPair;
import com.hust.config.AssistantConfig;
import com.hust.entity.DialogLibrary;
import com.hust.service.DialogLibraryService;
import com.hust.service.WatsonAssistantService;
import com.hust.service.WatsonService;
import com.hust.config.ClassFirstConfig;
import com.hust.netty.protocol.IMDecoder;
import com.hust.netty.protocol.IMEncoder;
import com.hust.netty.protocol.IMMessage;
import com.hust.netty.protocol.IMP;
import com.hust.util.SpringContextUtil;
import com.hust.util.StringUtil;
import com.ibm.cloud.sdk.core.security.IamAuthenticator;
import com.ibm.watson.assistant.v2.Assistant;
import com.ibm.watson.assistant.v2.model.*;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.ObjectUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import static com.hust.netty.process.MsgProcessor.buildResponseText;
import static com.hust.netty.process.MsgProcessor.sysTime;

/**
 * 消息处理类
 */
@Slf4j
public class MsgProcessor {

    // 记录在线用户
    private static final ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 每一个客户端对应一个Watson的Session
    // private static final ConcurrentHashMap<Channel, SessionResponse> clientSessionMap = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<Channel, SessionResponseCounterPair> clientSession = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<Channel, SessionResponseCounterPair> clientSessionForAssistant = new ConcurrentHashMap<>();
    // channel自定义属性
    private final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");
    private final AttributeKey<String> HEAD_PIC = AttributeKey.valueOf("headPic");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    // 消息解码器
    private final IMDecoder decoder = new IMDecoder();
    // 消息编码器
    private final IMEncoder encoder = new IMEncoder();
    // 虚拟教师对话角色
    private final static IMMessage teacherResponse = new IMMessage(IMP.CHAT.getName(), "虚拟教师", "https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207204353.png");
    private final static IMMessage assisResponse = new IMMessage(IMP.CHAT.getName(), "学伴", "https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207200240.jpeg");
    // 对话API
    private static final DialogLibraryService dialogLibraryService;
    // 学伴等待时长 25s
    private final Integer WAIT_TIME = 20 * 1000;
    private final Integer WELCOME_WAIT_TIME = 5 * 1000;

    static {
        // 通过自定义工具类获取SpringBoot的service对象
        dialogLibraryService = SpringContextUtil.getBean(DialogLibraryService.class);
    }

    public void process(Channel client, String msg) throws InterruptedException {
        // 将字符串解析为自定义格式
        IMMessage request = decoder.decode(msg);
        if (null == request) {
            return;
        }
        IMMessage assisRequest = decoder.decode(msg);
        // 获取消息发送者
        String username = request.getSender();
        // 判断如果是登录动作，就往onlineUsers中加入一条数据
        if (IMP.LOGIN.getName().equals(request.getCmd())) {

            client.attr(IP_ADDR).getAndSet("");
            client.attr(USERNAME).getAndSet(request.getSender());
            client.attr(HEAD_PIC).getAndSet(request.getHeadPic());
            // 将当前用户添加进在线用户列表
            onlineUsers.add(client);
            IamAuthenticator authenticator = new IamAuthenticator(ClassFirstConfig.API_KEY);
            Assistant assistant = new Assistant("2020-11-30", authenticator);
            assistant.setServiceUrl(ClassFirstConfig.SERVICE_URL);
            SessionResponse watsonSession = null;
            // 创建watson助手的session
            while (watsonSession == null) {
                try {
                    CreateSessionOptions sessionOptions = new CreateSessionOptions.Builder(ClassFirstConfig.ASSISTANT_ID).build();
                    watsonSession = assistant.createSession(sessionOptions).execute().getResult();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    // e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error("watson会话建立失败-教师，原因：{}",e.getMessage());
                    // e.printStackTrace();
                }
            }

            IamAuthenticator authenticatorV2 = new IamAuthenticator(AssistantConfig.API_KEY);
            Assistant assistantV2 = new Assistant("2020-11-30", authenticatorV2);
            assistant.setServiceUrl(AssistantConfig.SERVICE_URL);
            SessionResponse watsonSessionV2 = null;
            // 创建watson助手的session
            while (watsonSessionV2 == null) {
                try {
                    CreateSessionOptions sessionOptions = new CreateSessionOptions.Builder(AssistantConfig.ASSISTANT_ID).build();
                    watsonSessionV2 = assistantV2.createSession(sessionOptions).execute().getResult();
                } catch (Exception e) {
                    log.error("watson会话建立失败-学伴，原因：{}",e.getMessage());
                    // e.printStackTrace();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    // e.printStackTrace();
                }
            }
            // 将netty客户端和watson的session进行对应
            // clientSessionMap.put(client, watsonSession);
            clientSession.put(client, new SessionResponseCounterPair(watsonSession, 1, 0));
            clientSessionForAssistant.put(client, new SessionResponseCounterPair(watsonSessionV2, 1, 0));

            // 学伴定时
           // assisRequest.setContent("hello，kevin，my name is wangguilin");
            // new Timer().schedule(new WatsonAssistantReplyTimerTask(assisRequest, onlineUsers, watsonSessionV2), WELCOME_WAIT_TIME);
            // 老师打招呼
            teacherResponse.setTime(sysTime());
            teacherResponse.setContent(buildResponseText(WatsonService.requestOfText(request.getContent(), watsonSession)));
            if (onlineUsers.size() > 1) {
                teacherResponse.setContent(teacherResponse.getContent() + " @" + request.getSender());
            }
            String welcomeText = encoder.encode(teacherResponse);
            // 向所有用户发送系统消息
            for (Channel channel : onlineUsers) {
                if (channel != client) {
                    // 自定义系统消息格式 [system][时间戳][用户数量][消息内容]
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), username + " 加入课堂！");
                }
                // 向自己发送消息
                else {
                    request = new IMMessage(IMP.SYSTEM.getName(), sysTime(), onlineUsers.size(), username + " 欢迎进入课堂！");
                }
                //发送消息
                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
                client.writeAndFlush(new TextWebSocketFrame(welcomeText));
            }
           //  client.writeAndFlush(new TextWebSocketFrame(assisWelcomeText));
            // new Thread(new AssistantReplyTask(client, 1, 1)).start();
        }
        // 登出动作
        else if (IMP.LOGOUT.getName().equals(request.getCmd())) {
            logout(client);
        }
        // 聊天动作
        else if (IMP.CHAT.getName().equals(request.getCmd())) {
            SessionResponseCounterPair pair = clientSession.get(client);
            SessionResponseCounterPair pairForAssistant = clientSessionForAssistant.get(client);
            pair.getStuRepliedMap().put(pair.getDialogCounter() - 1, false);
            pair.getStuRepliedMap().put(pair.getDialogCounter(), true);
            // 调用IBM Watson
            teacherResponse.setTime(sysTime());
            MessageResponse messageResponse = WatsonService.requestOfText(request.getContent(), pair.getSessionResponse());
            // 没有识别到意图
            if (messageResponse.getOutput().getIntents().size() == 0 && messageResponse.getOutput().getEntities().size() == 0) {
                pair.setRepeatResponseCounter(pair.getRepeatResponseCounter() + 1);
                log.info("没有识别到意图，计数器加1, 当前计数为" + pair.getRepeatResponseCounter());
                if (pair.getRepeatResponseCounter() > 3) {
                    messageResponse = WatsonService.requestOfText(dialogLibraryService.getOne(
                            new QueryWrapper<DialogLibrary>().eq("class_id", 1).eq("round_no", pair.getDialogCounter())).getAssistantReply(), pair.getSessionResponse());
                    log.info("计数器大于3，跳转到下一轮对话");
                    pair.setDialogCounter(pair.getDialogCounter() + 1);
                    pair.setRepeatResponseCounter(0);
                }
            } else {
                pair.setDialogCounter(pair.getDialogCounter() + 1);
            }
            teacherResponse.setContent(buildResponseText(messageResponse));
            // 如果当前在线用户数大于1，则在消息后面加一个@符号
            if (onlineUsers.size() > 1) {
                teacherResponse.setContent(teacherResponse.getContent() + " @" + request.getSender());
            }
            new Timer().schedule(new WatsonAssistantReplyTimerTask(assisRequest, onlineUsers, pairForAssistant.getSessionResponse()), WAIT_TIME);
            String sysText = encoder.encode(teacherResponse);
            // System.out.println(sysText);
            for (Channel channel : onlineUsers) {
                // 向其他人发送消息
                if (channel != client) {
                    request.setSender(username);
                }
                // 向自己发送消息
                else {
                    request.setSender("MY_SELF");
                }
                // 发送消息
                String text = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(text));
                channel.writeAndFlush(new TextWebSocketFrame(sysText));
            }
            // new Thread(new AssistantReplyTask(client, pair.getDialogCounter(), 1)).start();

        }
        // 鲜花动作
        else if (IMP.FLOWER.getName().equals(request.getCmd())) {
            JSONObject attrs = getAttrs(client);
            long currTime = sysTime();
            if (null != attrs) {
                long lastTime = attrs.getLongValue("lastFlowerTime");
                // 60秒之内不允许重复刷鲜花
                int seconds = 10;
                long sub = currTime - lastTime;
                if (sub < 1000 * seconds) {
                    request.setSender("MY_SELF");
                    request.setCmd(IMP.SYSTEM.getName());
                    request.setContent("您送鲜花太频繁," + (seconds - sub / 1000) + "秒后再试");
                    String content = encoder.encode(request);
                    client.writeAndFlush(new TextWebSocketFrame(content));
                    return;
                }
            }
            // 正常送花
            for (Channel channel : onlineUsers) {
                if (channel == client) {
                    request.setSender("MY_SELF");
                    request.setContent("你给大家送了一波鲜花雨");
                    setAttrs(client, "lastFlowerTime", currTime);
                } else {
                    request.setSender(getNickName(client));
                    request.setContent(getNickName(client) + "送来一波鲜花雨");
                }
                request.setTime(sysTime());

                String content = encoder.encode(request);
                channel.writeAndFlush(new TextWebSocketFrame(content));
            }
        }
    }

    public static String buildResponseText(MessageResponse messageResponse) {
        StringBuilder res = new StringBuilder();
        for (RuntimeResponseGeneric s : messageResponse.getOutput().getGeneric()) {
            String responseType = s.responseType();
            switch (responseType) {
                case "text":
                    res.append(StringUtil.removeBracket(s.text()));
                    break;
                case "image":
                    res.append("<img src='").append(s.source()).append("'>");
                    break;
                default:
                    res.append("未知的返回类型");
            }
            res.append("\n");
        }
        return res.toString();
    }


    /**
     * 获取用户昵称
     *
     * @param client 客户端
     * @return 用户昵称
     */
    public String getNickName(Channel client) {
        return client.attr(USERNAME).get();
    }

    /**
     * 获取用户远程IP地址
     *
     * @param client 客户端
     * @return 用户远程IP地址
     */
    public String getAddress(Channel client) {
        return client.remoteAddress().toString().replaceFirst("/", "");
    }

    /**
     * 获取扩展属性
     *
     * @param client 客户端
     * @return 扩展属性
     */
    public JSONObject getAttrs(Channel client) {
        try {
            return client.attr(ATTRS).get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取扩展属性
     *
     * @param client 客户端
     */
    private void setAttrs(Channel client, String key, Object value) {
        try {
            JSONObject json = client.attr(ATTRS).get();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        } catch (Exception e) {
            JSONObject json = new JSONObject();
            json.put(key, value);
            client.attr(ATTRS).set(json);
        }
    }

    /**
     * 登出通知
     *
     * @param client 客户端
     */
    public void logout(Channel client) {
        IMMessage request = new IMMessage();
        request.setSender(client.attr(USERNAME).get());
        request.setCmd(IMP.SYSTEM.getName());
        request.setOnline(onlineUsers.size());
        request.setContent(request.getSender() + " 退出课堂！");
        //向所有用户发送系统消息
        for (Channel channel : onlineUsers) {
            // 向其他人发送消息
            if (channel != client) {
                // 自定义IM协议解码
                String text = encoder.encode(request);
                // 发送消息
                channel.writeAndFlush(new TextWebSocketFrame(text));
            }
        }

        onlineUsers.remove(client);
    }

    /**
     * 获取当前系统时间
     *
     * @return 系统时间
     */
    protected static long sysTime() {
        return System.currentTimeMillis();
    }

}


@Slf4j
class AssistantReplyTimerTask extends TimerTask {
    private static final DialogLibraryService dialogLibraryService;

    static {
        // 通过SpringBoot的上下文中获取service对象
        dialogLibraryService = SpringContextUtil.getBean(DialogLibraryService.class);
    }

    private final Channel session;

    private final Integer classId;

    private final Integer dialogCounter;

    public AssistantReplyTimerTask(Channel session, Integer dialogCounter, Integer classId) {
        this.session = session;
        this.dialogCounter = dialogCounter;
        this.classId = classId;
    }

    @SneakyThrows
    @Override
    public void run() {
        SessionResponseCounterPair sessionResponseCounterPair = MsgProcessor.clientSession.get(session);
        // 对话逻辑中学伴应该响应 && 学生未响应 && 学伴在此轮对话中未响应
        if (ClassOneData.data.containsKey(dialogCounter)
                && !sessionResponseCounterPair.getStuRepliedMap().containsKey(dialogCounter)
                && !sessionResponseCounterPair.getAssistantReplySet().contains(dialogCounter)) {
            IMMessage assistantResponse = new IMMessage(IMP.CHAT.getName(), sysTime(), "学伴");
            // 调用学伴回答库进行响应
            assistantResponse.setContent(dialogLibraryService.getOne(
                    new QueryWrapper<DialogLibrary>().eq("class_id", classId).eq("round_no", dialogCounter)).getAssistantReply());

            assistantResponse.setHeadPic("https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207200240.jpeg");
            session.writeAndFlush(new TextWebSocketFrame(new IMEncoder().encode(assistantResponse)));
            sessionResponseCounterPair.getAssistantReplySet().add(dialogCounter);
        }
    }
}

@Slf4j
class WatsonAssistantReplyTimerTask extends TimerTask {

    private final IMEncoder encoder = new IMEncoder();

    private final ChannelGroup onlineUsers;

    private final SessionResponse watsonSessionV2;

    private final IMMessage request;

    // 虚拟学伴对话角色
    private final static IMMessage assisResponse = new IMMessage(IMP.CHAT.getName(), "学伴", "https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207200240.jpeg");


    public WatsonAssistantReplyTimerTask(IMMessage request,
                                         ChannelGroup onlineUsers, SessionResponse sessionResponse) {
        this.request = request;
        this.onlineUsers = onlineUsers;
        this.watsonSessionV2 = sessionResponse;
    }

    @SneakyThrows
    @Override
    public void run() {
        // 学伴打招呼
        assisResponse.setTime(sysTime());
        assisResponse.setContent(buildResponseText(WatsonAssistantService.requestOfText(request.getContent(), watsonSessionV2)).replaceAll("-", " "));
        if (onlineUsers.size() > 1) {
            assisResponse.setContent(assisResponse.getContent() + " @" + request.getSender());
        }
        String assisWelcomeText = encoder.encode(assisResponse);
        // 向所有用户发送系统消息
        for (Channel channel : onlineUsers) {
            channel.writeAndFlush(new TextWebSocketFrame(assisWelcomeText));
        }
    }
}

