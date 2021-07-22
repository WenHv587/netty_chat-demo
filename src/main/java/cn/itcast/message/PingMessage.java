package cn.itcast.message;

/**
 * @author LWenH
 * @create 2021/7/22 - 20:55
 */
public class PingMessage extends Message {
    @Override
    public int getMessageType() {
        return PingMessage;
    }
}
