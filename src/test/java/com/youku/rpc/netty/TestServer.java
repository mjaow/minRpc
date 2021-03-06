package com.youku.rpc.netty;

import com.youku.rpc.model.User;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

public class TestServer {

	public static void main(String[] args) throws InterruptedException {
		// boss线程组接受客户端请求信息，并将接收到的信息交给work线程组处理。所以boss group数量设为1即可
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		// work group是实际处理用户请求信息的工作线程组，建议配置N，1<=N<=CPU core
		EventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline()//
								.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2))//
								.addLast(new RpcDecoder())//
								.addLast(new LengthFieldPrepender(2))//
								.addLast(new RpcEncoder())//
								.addLast(new ServerHandler());

					}
				}).option(ChannelOption.SO_BACKLOG, 128).childOption(ChannelOption.SO_KEEPALIVE, true);

		b.bind("localhost", 8080).sync();
	}

	private static class ServerHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			User user = (User) msg;
			System.out.println("----------------resp:" + user);

			ctx.writeAndFlush(user);
		}

	}
}
