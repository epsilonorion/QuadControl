/**RosImageView.java*************************************************************
 *       Author : Joshua Weaver
 * Last Revised : August 26, 2012
 *      Purpose : RosImageView borrowed from rosjava descriptions.  Simple
 *      		: node that mimics ImageView however, sets image based on ROS
 *      		: node.
 *    Call Path : MainActivity->MediaFragment->RosImageView
 * 			XML : 
 * Dependencies : MediaFragment, ROSJava, Android-Core
 ****************************************************************************/
package com.qinetiq.quadcontrol;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import org.ros.android.MessageCallable;
import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.node.topic.Subscriber;

public class RosImageView<T> extends ImageView implements NodeMain {

	int count = 0;

	private String topicName;
	private String messageType;
	private MessageCallable<Bitmap, T> callable;

	public RosImageView(Context context) {
		super(context);
	}

	public RosImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RosImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public void setMessageToBitmapCallable(MessageCallable<Bitmap, T> callable) {
		this.callable = callable;
	}

	@Override
	public GraphName getDefaultNodeName() {
		return GraphName.of("ros_image_view");
	}

	@Override
	public void onStart(ConnectedNode connectedNode) {
		Subscriber<T> subscriber = connectedNode.newSubscriber(topicName,
				messageType);
		subscriber.addMessageListener(new MessageListener<T>() {
			@Override
			public void onNewMessage(final T message) {
				post(new Runnable() {
					@Override
					public void run() {
						count++;
						// TODO: HACK to allow smooth display. Must remove count
						// value that is used to handle memory leak
						if (count == 10) {
							setImageBitmap(callable.call(message));
							count = 0;
						}
					}
				});
					postInvalidate();
			}
		});
	}

	@Override
	public void onShutdown(Node node) {
	}

	@Override
	public void onShutdownComplete(Node node) {
	}

	@Override
	public void onError(Node node, Throwable throwable) {
	}
}
