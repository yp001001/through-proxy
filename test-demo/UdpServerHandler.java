import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;

public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String message = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println("Received message: " + message);

        // 回复客户端的消息
        String responseMessage = "Message received: " + message;
        DatagramPacket responsePacket = new DatagramPacket(
                Unpooled.copiedBuffer(responseMessage, CharsetUtil.UTF_8),
                packet.sender());
        ctx.writeAndFlush(responsePacket);
    }
}
