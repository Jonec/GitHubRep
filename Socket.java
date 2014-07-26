package channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Socket {
	/**
	 * 服务器
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		ServerSocketChannel serverChannel;
		try {
			serverChannel = ServerSocketChannel.open();
			ServerSocket server = serverChannel.socket();
			serverChannel.configureBlocking(false);
			server.bind(new InetSocketAddress("localhost", 8080));

			Selector selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);

			while (true) {
				int n = selector.select();
				if (n == 0)
					continue;
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					
					if (key.isAcceptable()) {
						ServerSocketChannel serChannel = (ServerSocketChannel) key
								.channel();
						SocketChannel channel = serChannel.accept();
						channel.configureBlocking(false);
						System.out.println("获得请求:"
								+ channel.socket().getInetAddress());
						channel.register(selector, SelectionKey.OP_READ);
					} else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer bff = ByteBuffer.allocate(1024);
						int len = 0;
						StringBuilder sb = new StringBuilder();
						while ((len = channel.read(bff)) > 0) {
							bff.flip();
							sb.append(new String(bff.array(),0,len));
							bff.clear();
						}
						System.out.println("请求内容:" + sb.toString());
						channel.register(selector, SelectionKey.OP_WRITE);
					} else if (key.isWritable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer bff = ByteBuffer
								.wrap("Hello!this is nio server".getBytes());
						channel.write(bff);
					    channel.close();
					    key.cancel();
					}
					it.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
