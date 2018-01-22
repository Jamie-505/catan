package de.lmu.settlebattle.catanclient.chat;

import static de.lmu.settlebattle.catanclient.utils.Constants.CONTENT;
import static de.lmu.settlebattle.catanclient.utils.Constants.SENDER;

import com.google.gson.annotations.SerializedName;

public class ChatMessage {

  public ChatMessage(String content) {
    this.content = content;
  }
  @SerializedName(CONTENT)
  private String content;

  @SerializedName(SENDER)
  private Integer senderId;

  public String getContent() {
    return content;
  }
  public int getSenderId() {
    return senderId;
  }
}
