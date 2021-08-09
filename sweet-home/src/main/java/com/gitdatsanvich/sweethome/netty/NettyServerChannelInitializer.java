package com.gitdatsanvich.sweethome.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;


/**
 * @author TangChen
 * @date 2021/8/6 10:41
 */
public class NettyServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) {
        channel.pipeline().
                addLast(new HttpServerCodec()).
                addLast(new HttpObjectAggregator(65535)).
                addLast(new ChunkedWriteHandler()).
                addLast(new WebSocketServerHandler());
    }
}