/**
 * @program: proxy-test-demo
 * @description:
 * @author: yp
 * @create: 2024-09-04 22:04
 **/
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

public class UdpClient {

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8089;

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<NioDatagramChannel>() {
                        @Override
                        protected void initChannel(NioDatagramChannel ch) throws Exception {
                            ch.pipeline().addLast(new UdpClientHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();

            // 发送消息到服务端
            String message = "Hello from client!";
            DatagramPacket packet = new DatagramPacket(
                    Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                    new InetSocketAddress(HOST, 9104));
            f.channel().writeAndFlush(packet).sync();

            f.channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }
}

class UdpClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        String message = packet.content().toString(CharsetUtil.UTF_8);
        System.out.println("Received response: " + message);
    }
}

