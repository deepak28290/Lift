package indwin.c3.liftapp.pojos;

import indwin.c3.liftapp.MessageDetails;

import java.util.ArrayList;

public class MessageListE {
	String message;
	String type;
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	ArrayList<MessageDetails> messageList;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ArrayList<MessageDetails> getMessageList() {
		return messageList;
	}

	public void setMessageList(ArrayList<MessageDetails> messageList) {
		this.messageList = messageList;
	}

}
