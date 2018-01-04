package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.MESSAGE;

import com.google.gson.annotations.SerializedName;

public class Message {

  class Error {
    @SerializedName(MESSAGE)
    String Message;
  }

}
