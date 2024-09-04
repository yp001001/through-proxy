import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @program: proxy-test-demo
 * @description:
 * @author: yp
 * @create: 2024-09-03 20:15
 **/
public class Server {

    public static void main(String[] args) throws InterruptedException {
        startServer();
    }



    public static void startServer() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel channel) throws Exception {
                        channel.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {


                                super.channelActive(ctx);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                byte[] bytes = new byte[buf.readableBytes()];
                                buf.readBytes(bytes);
                                System.out.println("接收到外部发出来的消息："+new String(bytes));


                                ByteBuf buffer = ctx.alloc().buffer();
                                buffer.writeBytes("hello，我是内部服务发送来的消息".getBytes());
                                ctx.channel().writeAndFlush(buffer);

                                super.channelRead(ctx, msg);
                            }
                        });
                    }
                });

        serverBootstrap.bind(3306).sync()
                .addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                        if(channelFuture.isSuccess()){
                            System.out.println("绑定成功...");
                        }
                    }
                });

    }


}
