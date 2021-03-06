package connection.tasks;

import com.rabbitmq.client.QueueingConsumer;

import connection.Connector;

public abstract class Task implements Runnable {
	private QueueingConsumer queue = null;

	public QueueingConsumer getQueue() {
		return queue;
	}

	public Connector getConn() {
		return conn;
	}

	private Connector conn = null;

	public Task(QueueingConsumer queue) {
		this.queue = queue;
		this.conn = Connector.getInstance();
	}

}
