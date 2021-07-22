package cn.itcast.protocol;

import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author wddv587
 *
 * 按照协议定义好属性的LengthFieldBasedFrameDecoder
 */
public class ProcotolFrameDecoder extends LengthFieldBasedFrameDecoder {

    public ProcotolFrameDecoder() {
        this(1024, 12, 4, 0, 0);
    }

    public ProcotolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
}
