package com.gitdatsanvich.sweethome.netty;

import com.gitdatsanvich.common.constants.CommonConstants;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author TangChen
 * @date 2021/8/6 12:08
 */
@Slf4j
@Component
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handShaker;
    /**
     * websocket存储器
     */
    private static final ConcurrentHashMap<ChannelId, ChannelHandlerContext> CHANNEL_MAP = new ConcurrentHashMap<>();

    /**
     * 接收到消息
     *
     * @param ctx ctx
     * @param msg msg
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        //首次请求之后先进行握手，通过http请求来实现
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof CloseWebSocketFrame) {
            /*关闭socket*/
            closeSocket(ctx);
            return;
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketRequest(ctx, (WebSocketFrame) msg);
        }
        /*存储 Channel*/
        ChannelId channelId = ctx.channel().id();
        if (CHANNEL_MAP.containsKey(channelId)) {
            log.info("客户端【" + channelId + "】是连接状态，连接通道数量: " + CHANNEL_MAP.size());
        } else {
            CHANNEL_MAP.put(channelId, ctx);
            InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIp = inSocket.getAddress().getHostAddress();
            int clientPort = inSocket.getPort();
            log.info("客户端【" + channelId + "】连接netty[IP:" + clientIp + "--->PORT:" + clientPort + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }

    private void closeSocket(ChannelHandlerContext ctx) {
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = inSocket.getAddress().getHostAddress();
        ChannelId channelId = ctx.channel().id();
        //包含此客户端才去删除
        if (CHANNEL_MAP.containsKey(channelId)) {
            //删除连接
            CHANNEL_MAP.remove(channelId);
            log.info("客户端【" + channelId + "】退出netty服务器[IP:" + clientIp + "--->PORT:" + inSocket.getPort() + "]");
            log.info("连接通道数量: " + CHANNEL_MAP.size());
        }
    }

    /**
     * 处理握手
     *
     * @param ctx     ctx
     * @param request request
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        //解码http失败返回
        if (!request.decoderResult().isSuccess()) {
            sendResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.BAD_REQUEST, ctx.alloc().buffer()));
            return;
        }
        if (!HttpMethod.GET.equals(request.method())) {
            sendResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.FORBIDDEN, ctx.alloc().buffer()));
            return;
        }
        //参数分别是ws地址，子协议，是否扩展，最大frame长度
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true, 5 * 1024 * 1024);
        handShaker = factory.newHandshaker(request);
        if (handShaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handShaker.handshake(ctx.channel(), request);
        }
    }

    /**
     * SSL支持采用wss:
     *
     * @param request request
     * @return String
     */
    private String getWebSocketLocation(FullHttpRequest request) {
        String location = request.headers().get(HttpHeaderNames.HOST) + "/websocket";
        return "ws://" + location;
    }

    private void handleWebSocketRequest(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //关闭
        if (frame instanceof CloseWebSocketFrame) {
            handShaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            //包含此客户端才去删除
            closeSocket(ctx);
        }
        //握手 PING/PONG
        if (frame instanceof PingWebSocketFrame) {
            ctx.write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        //文本接收和发送
        if (frame instanceof TextWebSocketFrame) {
            String rev = ((TextWebSocketFrame) frame).text();
            /*手动心跳*/
            if (CommonConstants.PING.equals(rev)) {
                ctx.write(new TextWebSocketFrame(CommonConstants.PONG));
            }
            ctx.flush();
            return;
        }
        /*暂时不做处理直接返回*/
        if (frame instanceof BinaryWebSocketFrame) {
            ctx.write(frame.retain());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void sendResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse resp) {
        HttpResponseStatus status = resp.status();
        if (status != HttpResponseStatus.OK) {
            ByteBufUtil.writeUtf8(resp.content(), status.toString());
            HttpUtil.setContentLength(req, resp.content().readableBytes());
        }
        boolean keepAlive = HttpUtil.isKeepAlive(req) && status == HttpResponseStatus.OK;
        HttpUtil.setKeepAlive(req, keepAlive);
        ChannelFuture future = ctx.write(resp);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void publishAll(String message) {
        CHANNEL_MAP.forEachValue(CHANNEL_MAP.size(), channelHandlerContext -> {
            channelHandlerContext.write(new TextWebSocketFrame(message));
            channelHandlerContext.flush();
        });
    }
}