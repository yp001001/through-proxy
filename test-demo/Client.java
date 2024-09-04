
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

/**
 * @program: proxy-test-demo
 * @description:
 * @author: yp
 * @create: 2024-09-03 20:22
 **/
public class Client {

    public static void main(String[] args) throws InterruptedException {
        startClient();
    }
    
    
    public static void startClient() throws InterruptedException {

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("发送消息");
                                ByteBuf buffer = ctx.alloc().buffer();
                                buffer.writeBytes("hello，我是外部信息".getBytes());
                                ctx.channel().writeAndFlush(buffer);
                                super.channelActive(ctx);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;

                                byte[] bytes = new byte[buf.readableBytes()];
                                buf.readBytes(bytes);

                                System.out.println("接收到内部服务器发送来的消息："+ new String(bytes));

                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                });
        InetSocketAddress address = new InetSocketAddress("localhost", 9102);
        bootstrap.connect(address).sync()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            System.out.println("连接代理服务器成功");
                        }else{
                            System.out.println("连接代理服务器失败");
                        }
                    }
                });

    }
    
}
