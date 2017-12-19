package de.lmu.settlebattle.catanclient.utils;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.Map;

public class Message {

  class Error {
    @SerializedName(MESSAGE)
    String Message;
  }

}
