package cn.itcast.server.handler;

import cn.itcast.message.LoginRequestMessage;
import cn.itcast.message.LoginResponseMessage;
import cn.itcast.server.ChatServer;
import cn.itcast.server.service.UserServiceFactory;
import cn.itcast.server.session.SessionFactory;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author LWenH
 * @create 2021/7/22 - 15:26
 */
@Slf4j
@ChannelHandler.Sharable
public class LoginRequestMessageHandler extends SimpleChannelInboundHandler<LoginRequestMessage> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, LoginRequestMessage msg) throws Exception {
        log.debug("LoginRequestMessage:{}", msg);
        String username = msg.getUsername();
        String password = msg.getPassword();
        LoginResponseMessage loginResponseMessage;
        boolean login = UserServiceFactory.getUserService().login(username, password);
        if (login) {
            // 如果成功登录
            loginResponseMessage = new LoginResponseMessage(true, "登录成功");
            SessionFactory.getSession().bind(ctx.channel(), username);
        } else {
            loginResponseMessage = new LoginResponseMessage(false, "登录失败");
        }
        ctx.writeAndFlush(loginResponseMessage);
    }
}
