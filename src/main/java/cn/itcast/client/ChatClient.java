package cn.itcast.client;

import cn.itcast.message.ChatRequestMessage;
import cn.itcast.message.GroupChatRequestMessage;
import cn.itcast.message.GroupCreateRequestMessage;
import cn.itcast.message.GroupJoinRequestMessage;
import cn.itcast.message.GroupMembersRequestMessage;
import cn.itcast.message.GroupQuitRequestMessage;
import cn.itcast.message.LoginRequestMessage;
import cn.itcast.message.LoginResponseMessage;
import cn.itcast.message.PingMessage;
import cn.itcast.protocol.MessageCodecSharable;
import cn.itcast.protocol.ProcotolFrameDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {
    public static void main(String[] args) {
        ExecutorService executorService = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10), new DefaultThreadFactory("login"));
        CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
        AtomicBoolean LOGIN = new AtomicBoolean(false);
        AtomicBoolean EXIT = new AtomicBoolean(false);
        NioEventLoopGroup group = new NioEventLoopGroup();
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);
        MessageCodecSharable MESSAGE_CODEC = new MessageCodecSharable();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.group(group);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(LOGGING_HANDLER);
                    ch.pipeline().addLast(new ProcotolFrameDecoder());
                    ch.pipeline().addLast(MESSAGE_CODEC);
                    // 3s ?????????????????????????????????????????????????????? IdleState#WRITER_IDLE ??????
                    ch.pipeline().addLast(new IdleStateHandler(0, 3, 0));
                    ch.pipeline().addLast(new ChannelDuplexHandler() {
                        @Override
                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                            // ????????????????????????
                            if (idleStateEvent.state() == IdleState.WRITER_IDLE) {
//                                log.debug("???????????????");
                                ctx.writeAndFlush(new PingMessage());
                            }
                        }
                    });
                    ch.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {

                        // ??????????????????
                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg= {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                if (loginResponseMessage.isSuccess()) {
                                    // ??????????????????
                                    LOGIN.set(true);
                                }
                                // ???????????????????????????
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        // ?????????????????? active ??????
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                            // ???????????????????????????????????????????????????????????????????????????????????????
                            executorService.execute(() -> {
                                Scanner scanner = new Scanner(System.in);
                                System.out.println("??????????????????:");
                                String username = scanner.nextLine();
                                if(EXIT.get()){
                                    return;
                                }
                                System.out.println("???????????????:");
                                String password = scanner.nextLine();
                                if(EXIT.get()){
                                    return;
                                }
                                // ??????????????????
                                LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                                // ????????????
                                ctx.writeAndFlush(loginRequestMessage);
                                try {
                                    WAIT_FOR_LOGIN.await();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (!LOGIN.get()) {
                                    // ??????????????????
                                    ctx.channel().close();
                                } else {
                                    log.debug("???????????????");
                                    while (true) {
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");
                                        String command;
                                        try {
                                            command = scanner.nextLine();
                                        } catch (Exception e) {
                                            break;
                                        }
                                        if(EXIT.get()){
                                            return;
                                        }
                                        String[] s = command.split(" ");
                                        switch (s[0]){
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gsend":
                                                ctx.writeAndFlush(new GroupChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gcreate":
                                                Set<String> set = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                // ????????????
                                                set.add(username);
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], set));
                                                break;
                                            case "gmembers":
                                                ctx.writeAndFlush(new GroupMembersRequestMessage(s[1]));
                                                break;
                                            case "gjoin":
                                                ctx.writeAndFlush(new GroupJoinRequestMessage(username, s[1]));
                                                break;
                                            case "gquit":
                                                ctx.writeAndFlush(new GroupQuitRequestMessage(username, s[1]));
                                                break;
                                            case "quit":
                                                ctx.channel().close();
                                                return;
                                            default:
                                                System.out.println("??????????????????????????????");
                                        }
                                    }
                                }
                            });
                        }

                        // ?????????????????????
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("???????????????");
                            EXIT.set(true);
                        }

                        // ?????????????????????
                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("?????????????????? ???????????????{}", cause.getMessage());
                            EXIT.set(true);
                        }
                    });
                }
            });
            Channel channel = bootstrap.connect("localhost", 8080).sync().channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            log.error("client error", e);
        } finally {
            executorService.shutdown();
            group.shutdownGracefully();
        }
    }
}
