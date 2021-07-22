package cn.itcast.server.handler;

import cn.itcast.message.GroupJoinRequestMessage;
import cn.itcast.message.GroupJoinResponseMessage;
import cn.itcast.server.session.Group;
import cn.itcast.server.session.GroupSession;
import cn.itcast.server.session.GroupSessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author LWenH
 * @create 2021/7/22 - 17:00
 */
@ChannelHandler.Sharable
public class GroupJoinRequestHandler extends SimpleChannelInboundHandler<GroupJoinRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupJoinRequestMessage msg) throws Exception {
        String groupName = msg.getGroupName();
        Group group = GroupSessionFactory.getGroupSession().joinMember(groupName, msg.getUsername());
        if (group != null) {
            ctx.writeAndFlush(new GroupJoinResponseMessage(true, "加入群聊成功！"));
        } else {
            ctx.writeAndFlush(new GroupJoinResponseMessage(false, groupName + "不存在"));
        }

    }
}
