package cn.itcast.server.session;

import io.netty.channel.Channel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wddv587
 *
 * 会话实现类
 */
public class SessionMemoryImpl implements Session {
    /**
     * 基于内存的会话管理数据库 存储用户和channel 以及属性的相关信息
     */
    private final Map<String, Channel> usernameChannelMap = new ConcurrentHashMap<>();
    private final Map<Channel, String> channelUsernameMap = new ConcurrentHashMap<>();
    private final Map<Channel, Map<String, Object>> channelAttributesMap = new ConcurrentHashMap<>();

    /**
     * 绑定用户与channel
     *
     * @param channel  哪个 channel 要绑定会话
     * @param username 会话绑定用户
     */
    @Override
    public void bind(Channel channel, String username) {
        usernameChannelMap.put(username, channel);
        channelUsernameMap.put(channel, username);
        channelAttributesMap.put(channel, new ConcurrentHashMap<>());
    }

    /**
     * 解绑
     * @param channel 哪个 channel 要解绑会话
     */
    @Override
    public void unbind(Channel channel) {
        String username = channelUsernameMap.remove(channel);
        usernameChannelMap.remove(username);
        channelAttributesMap.remove(channel);
    }

    @Override
    public Object getAttribute(Channel channel, String name) {
        return channelAttributesMap.get(channel).get(name);
    }

    @Override
    public void setAttribute(Channel channel, String name, Object value) {
        channelAttributesMap.get(channel).put(name, value);
    }

    @Override
    public Channel getChannel(String username) {
        return usernameChannelMap.get(username);
    }

    @Override
    public String toString() {
        return usernameChannelMap.toString();
    }
}
