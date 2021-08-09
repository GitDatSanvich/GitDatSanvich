package com.gitdatsanvich.sweethome.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangChen
 * @date 2021/8/6 10:48
 */

@Slf4j
@Component
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private WebSocketServerHandshaker handShaker;
    /**
     * 管理一个全局map，保存连接进服务端的通道数量
     */
    private static final ConcurrentHashMap<ChannelId, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 客户端连接
     *
     * @param channelHandlerContext channelHandlerContext
     */
    @Override
    public void channelActive(ChannelHandlerContext channelHandlerContext) {

        InetSocketAddress inSocket = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();

        String clientIp = inSocket.getAddress().getHostAddress();
        int clientPort = inSocket.getPort();
        //获取连接通道唯一标识
        ChannelId channelId = channelHandlerContext.channel().id();
        //如果map中不包含此连接，就保存连接
        if (CHANNEL_MAP.containsKey(channelId)) {
            log.info("客户端【" + channelId + "】是连接状态，连接通道数量: " + CHANNEL_MAP.size());
        } else {
            //保存连接
            CHANNEL_MAP.put(channelId, channelHandlerContext);
            log.info("客户端【" + channelId + "】连接netty服务器[IP:" + clientIp + "--->PORT:" + clientPort + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
            /*发送握手包*/
        }

    }

    /**
     * 有客户端终止连接
     *
     * @param channelHandlerContext channelHandlerContext
     */
    @Override
    public void channelInactive(ChannelHandlerContext channelHandlerContext) {
        InetSocketAddress inSocket = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        ChannelId channelId = channelHandlerContext.channel().id();
        //包含此客户端才去删除
        if (CHANNEL_MAP.containsKey(channelId)) {
            //删除连接
            CHANNEL_MAP.remove(channelId);
            log.info("客户端【" + channelId + "】退出netty服务器[IP:" + clientIp + "--->PORT:" + inSocket.getPort() + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }

    /**
     * @param channelHandlerContext channelHandlerContext
     * @param msg                   msg
     */
    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
        log.info("【" + channelHandlerContext.channel().id() + "】" + " :" + msg);
        /*暂时无用*/
    }

    /**
     * @param msg       需要发送的消息内容
     * @param channelId 连接通道唯一id
     */
    public void channelWrite(ChannelId channelId, Object msg) {
        ChannelHandlerContext ctx = CHANNEL_MAP.get(channelId);
        if (ctx == null) {
            log.info("通道【" + channelId + "】不存在");
            return;
        }
        if (msg == null || msg == "") {
            log.info("服务端响应空的消息");
            return;
        }
        //将客户端的信息直接返回写入ctx
        ctx.write(msg);
        //刷新缓存区
        ctx.flush();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        String socketString = ctx.channel().remoteAddress().toString();
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state().name()) {
                case "READER_IDLE":
                    log.info("Client: " + socketString + " READER_IDLE 读超时");
                    ctx.disconnect();
                    break;
                case "WRITER_IDLE":
                    log.info("Client: " + socketString + " WRITER_IDLE 写超时");
                    ctx.disconnect();
                    break;
                case "ALL_IDLE":
                    log.info("Client: " + socketString + " ALL_IDLE 总超时");
                    ctx.disconnect();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * @param ctx ctx
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
        log.info(ctx.channel().id() + " 发生了错误,此连接被关闭" + "此时连通数量: " + CHANNEL_MAP.size(), cause);
    }

    /**
     * 向全部连接客户端发送数据
     *
     * @param line line
     */
    public void channelPublishAll(String line) {
        CHANNEL_MAP.forEachValue(CHANNEL_MAP.size(), channelHandlerContext -> {
            channelHandlerContext.write(line);
            channelHandlerContext.flush();
        });
    }
}