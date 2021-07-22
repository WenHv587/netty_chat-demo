package cn.itcast.server.handler;

import cn.itcast.message.GroupCreateRequestMessage;
import cn.itcast.message.GroupCreateResponseMessage;
import cn.itcast.server.session.Group;
import cn.itcast.server.session.GroupSession;
import cn.itcast.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @author LWenH
 * @create 2021/7/22 - 16:08
 */
@ChannelHandler.Sharable
public class GroupCreateRequestHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {

    /**
     * 处理创建聊天组请求
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Set<String> members = msg.getMembers();
        GroupSession groupSession = GroupSessionFactory.getGroupSession();
        Group group = groupSession.createGroup(groupName, members);
        if (group == null) {
            // 成功建群
            ctx.writeAndFlush(new GroupCreateResponseMessage(true,"创建群聊成功！"));
            // 向被拉入群聊的人发送消息
            List<Channel> membersChannel = groupSession.getMembersChannel(groupName);
            for (Channel channel : membersChannel) {
                channel.writeAndFlush(channel.writeAndFlush(new GroupCreateResponseMessage(true,"您已被拉入[" + groupName + "]群聊")));
            }
        } else {
            // 群聊已经存在，建群失败
            ctx.writeAndFlush(new GroupCreateResponseMessage(false, "群聊已存在，建群失败"));
        }
    }
}
