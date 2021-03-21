package com.hust.netty.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.msgpack.MessagePack;

/**
 * 自定义IM协议的编码器
 */
public class IMEncoder extends MessageToByteEncoder<IMMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, IMMessage msg, ByteBuf out)
            throws Exception {
        out.writeBytes(new MessagePack().write(msg));
    }

    /**
     * 将消息实体转化为协议字符串
     * @param msg 消息实体
     * @return 协议字符串  [消息类型][时间戳][用户类别][用户名][用户头像] - 消息
     * 示例：
     * [CHAT][1607956475736][student/assistant/teacher][王桂林][https://wgl-picture.oss-cn-hangzhou.aliyuncs.com/img/20201124102557.png] - wang guilin
     */
    public String encode(IMMessage msg) {
        if (null == msg) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[").append(msg.getCmd()).append("][").append(msg.getTime()).append("]");
        if (IMP.LOGIN.getName().equals(msg.getCmd()) ||
                IMP.FLOWER.getName().equals(msg.getCmd())) {
            stringBuilder.append("[").append(msg.getSender()).append("]");
        } else if (IMP.CHAT.getName().equals(msg.getCmd())) {
            stringBuilder.append("[").append(msg.getSender()).append("][").append(msg.getHeadPic()).append("]");
        } else if (IMP.SYSTEM.getName().equals(msg.getCmd())) {
            stringBuilder.append("[").append(msg.getOnline()).append("]");
        }
        if (!(null == msg.getContent() || "".equals(msg.getContent()))) {
            stringBuilder.append(" - ").append(msg.getContent().replace("\n", "<br/>"));
        }
        
        return stringBuilder.toString();
        
    }

}
