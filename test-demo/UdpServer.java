/**
 * @program: proxy-test-demo
 * @description:
 * @author: yp
 * @create: 2024-09-04 22:03
 **/
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

public class UdpServer {

    private static final int PORT = 8082;

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
                            ch.pipeline().addLast(new UdpServerHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();
            System.out.println("UDP Server started on port " + PORT);
            f.channel().closeFuture().await();
        } finally {
            group.shutdownGracefully();
        }
    }
}

