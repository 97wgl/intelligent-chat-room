package com.hust.netty.process;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hust.common.ClassOneData;
import com.hust.common.SessionResponseCounterPair;
import com.hust.entity.DialogLibrary;
import com.hust.service.DialogLibraryService;
import com.hust.service.WatsonService;
import com.hust.config.ClassFirstConfig;
import com.hust.netty.protocol.IMDecoder;
import com.hust.netty.protocol.IMEncoder;
import com.hust.netty.protocol.IMMessage;
import com.hust.netty.protocol.IMP;
import com.hust.util.comonent.SpringContextUtil;
import com.ibm.watson.assistant.v2.model.CreateSessionOptions;
import com.ibm.watson.assistant.v2.model.MessageResponse;
import com.ibm.watson.assistant.v2.model.RuntimeResponseGeneric;
import com.ibm.watson.assistant.v2.model.SessionResponse;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.swagger.models.auth.In;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息处理类
 */
@Slf4j
public class MsgProcessor {

    // 记录在线用户
    private static final ChannelGroup onlineUsers = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    // 每一个客户端对应一个Watson的Session
    // private static final ConcurrentHashMap<Channel, SessionResponse> clientSessionMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Channel, SessionResponseCounterPair> clientSession = new ConcurrentHashMap<>();
    // channel自定义属性
    private final AttributeKey<String> USERNAME = AttributeKey.valueOf("username");
    private final AttributeKey<String> HEAD_PIC = AttributeKey.valueOf("headPic");
    private final AttributeKey<String> IP_ADDR = AttributeKey.valueOf("ipAddr");
    private final AttributeKey<JSONObject> ATTRS = AttributeKey.valueOf("attrs");
    // 消息解码器
    private final IMDecoder decoder = new IMDecoder();
    // 消息编码器
    private final IMEncoder encoder = new IMEncoder();
    // 判断学生是否回复
    protected static volatile boolean studentReplied = false;
    // 虚拟教师对话角色
    private final static IMMessage teacherResponse = new IMMessage(IMP.CHAT.getName(), "虚拟教师", "https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207204353.png");

    public void process(Channel client, String msg) {
        // 将字符串解析为自定义格式
        IMMessage request = decoder.decode(msg);
        if (null == request) {
            return;
        }
        // 获取消息发送者
        String username = request.getSender();
        // 判断如果是登录动作，就往onlineUsers中加入一条数据
        if (IMP.LOGIN.getName().equals(request.getCmd())) {

            client.attr(IP_ADDR).getAndSet("");
            client.attr(USERNAME).getAndSet(request.getSender());
            client.attr(HEAD_PIC).getAndSet(request.getHeadPic());
            // 将当前用户添加进在线用户列表
            onlineUsers.add(client);
            SessionResponse watsonSession = null;
            // 创建watson助手的session
            while (watsonSession == null) {
                try {
                    CreateSessionOptions sessionOptions = new CreateSessionOptions.Builder(ClassFirstConfig.ASSISTANT_ID).build();
                    watsonSession = WatsonService.ASSISTANT.createSession(sessionOptions).execute().getResult();
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 将netty客户端和watson的session进行对应
            // clientSessionMap.put(client, watsonSession);
            clientSession.put(client, new SessionResponseCounterPair(watsonSession, 1, 0));
            // 调用IBM Watson
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
                channel.writeAndFlush(new TextWebSocketFrame(welcomeText));
            }
            new Thread(new AssistantReplyTask(client, 1, 1)).start();
        }
        // 登出动作
        else if (IMP.LOGOUT.getName().equals(request.getCmd())) {
            logout(client);
        }
        // 聊天动作
        else if (IMP.CHAT.getName().equals(request.getCmd())) {
            synchronized (MsgProcessor.class) {
                studentReplied = true;
                SessionResponseCounterPair pair = clientSession.get(client);
                // 调用IBM Watson
                teacherResponse.setTime(sysTime());
                MessageResponse messageResponse = WatsonService.requestOfText(request.getContent(), pair.getSessionResponse());
                // 没有识别到意图
                if (messageResponse.getOutput().getIntents().size() == 0 && messageResponse.getOutput().getEntities().size() == 0) {
                    pair.setRepeatResponseCounter(pair.getRepeatResponseCounter() + 1);
                    log.info("没有识别到意图，计数器加1, 当前计数为" + pair.getRepeatResponseCounter());
                    if (pair.getRepeatResponseCounter() > 3) {
                        // TODO 将正确意图回答给watson
//                        messageResponse = WatsonService.requestOfText(, pair.getSessionResponse());
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
                studentReplied = false;
                new Thread(new AssistantReplyTask(client, pair.getDialogCounter(), 1)).start();
            }
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

    private String buildResponseText(MessageResponse messageResponse) {
        StringBuilder res = new StringBuilder();
        for (RuntimeResponseGeneric s : messageResponse.getOutput().getGeneric()) {
            String responseType = s.responseType();
            switch (responseType) {
                case "text":
                    res.append(s.text());
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
     * @return 系统时间
     */
    protected static long sysTime() {
        return System.currentTimeMillis();
    }

}


/**
 * 学伴响应线程
 */
@Slf4j
class AssistantReplyTask implements Runnable {

    private static DialogLibraryService dialogLibraryService;

    static {
        //从 Spring 容器中获取service对象
        dialogLibraryService = SpringContextUtil.getBean(DialogLibraryService.class);
    }

    // 学伴等待响应时间：WAIT_TIME * 0.2（s）
    private static final int WAIT_TIME = 25;

    private final Channel session;

    private AtomicInteger time;

    private Integer classId;

    private Integer dialogCounter;

    public AssistantReplyTask(Channel session, Integer dialogCounter, Integer classId) {
        this.session = session;
        this.dialogCounter = dialogCounter;
        this.classId = classId;
        time = new AtomicInteger(0);
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            Thread.sleep(200);
            time.getAndIncrement();
            if (MsgProcessor.studentReplied) {
                break;
            }
            if (time.get() >= WAIT_TIME) {
                if (!MsgProcessor.studentReplied && ClassOneData.data.containsKey(dialogCounter)) {
                    IMMessage assistantResponse = new IMMessage(IMP.CHAT.getName(), MsgProcessor.sysTime(), "学伴");
                    // 调用学伴回答库进行响应
                    assistantResponse.setContent(dialogLibraryService.getOne(
                            new QueryWrapper<DialogLibrary>().eq("class_id", classId).eq("round_no", dialogCounter)).getAssistantReply());

                    assistantResponse.setHeadPic("https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201207200240.jpeg");
                    session.writeAndFlush(new TextWebSocketFrame(new IMEncoder().encode(assistantResponse)));

                }
                break;
            }
        }
    }
}