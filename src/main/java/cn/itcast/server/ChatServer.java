package cn.itcast.server;

import cn.itcast.message.GroupChatRequestMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import cn.itcast.server.handler.ChatRequestMessageHandler;
import cn.itcast.server.handler.GroupChatRequestMessageHandler;
import cn.itcast.server.handler.GroupCreateRequestHandler;
import cn.itcast.server.handler.GroupJoinRequestHandler;
import cn.itcast.server.handler.GroupMemberRequestHandler;
import cn.itcast.server.handler.GroupQuitRequestHandler;
import cn.itcast.server.handler.LoginRequestMessageHandler;
import cn.itcast.server.handler.QuitHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatServer {
    public static void main(String[] args) {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        LoginRequestMessageHandler loginRequestMessageHandler = new LoginRequestMessageHandler();
        ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler();
        GroupCreateRequestHandler groupCreateRequestHandler = new GroupCreateRequestHandler();
        GroupChatRequestMessageHandler groupChatRequestMessage = new GroupChatRequestMessageHandler();
        GroupJoinRequestHandler groupJoinRequestHandler = new GroupJoinRequestHandler();
        GroupMemberRequestHandler groupMemberRequestHandler = new GroupMemberRequestHandler();
        GroupQuitRequestHandler groupQuitRequestHandler = new GroupQuitRequestHandler();
        QuitHandler quitHandler = new QuitHandler();


        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.group(boss, worker);
            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ProcotolFrameDecoder());
                    pipeline.addLast(LOGGING_HANDLER);
                    pipeline.addLast(MESSAGE_CODEC);
                    /*
                        IdleStateHandler 用来判断 读空闲时间过长，或 写空闲时间过长
                        ChannelDuplexHandler：可同时当做入站和出站处理器
                     */
                    // 5s没有收到数据，就会触发一个 IdleState#READER_IDLE 事件
                    pipeline.addLast(new IdleStateHandler(5, 0, 0));
                    pipeline.addLast(new ChannelDuplexHandler() {
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                            if (idleStateEvent.state().equals(IdleState.READER_IDLE)) {
                                log.debug("5s没有收到客户端的数据");
                                // 关闭channel
                                ctx.close();
                            }
                        }
                    });
                    pipeline.addLast(loginRequestMessageHandler);
                    pipeline.addLast(chatRequestMessageHandler);
                    pipeline.addLast(groupCreateRequestHandler);
                    pipeline.addLast(groupChatRequestMessage);
                    pipeline.addLast(groupJoinRequestHandler);
                    pipeline.addLast(groupMemberRequestHandler);
                    pipeline.addLast(groupQuitRequestHandler);
                    pipeline.addLast(quitHandler);
                }
            });
            Channel channel = serverBootstrap.bind(8080).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
