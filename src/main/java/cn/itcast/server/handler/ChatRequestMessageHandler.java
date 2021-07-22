package cn.itcast.server.handler;

import cn.itcast.message.ChatRequestMessage;
import cn.itcast.message.ChatResponseMessage;
import cn.itcast.server.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author LWenH
 * @create 2021/7/22 - 15:29
 */
@ChannelHandler.Sharable
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage msg) throws Exception {
        // 发送目标
        String to = msg.getTo();
        // 获取目标用户对应的channel
        Channel channel = SessionFactory.getSession().getChannel(to);
        if (channel != null) {
            // 如果在线 发送给目标用户
            channel.writeAndFlush(new ChatResponseMessage(msg.getFrom(), msg.getContent()));
        } else {
            // 不在线将失败消息返回发送消息的用户
            ctx.writeAndFlush(new ChatResponseMessage(false,"用户不在线"));
//            ctx.writeAndFlush(ctx.channel().alloc().buffer().writeBytes(new byte[]{1,2 ,3}));
        }
    }
}
