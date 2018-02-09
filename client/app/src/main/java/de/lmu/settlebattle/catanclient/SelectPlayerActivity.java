package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.content.BroadcastReceiver;

import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.utils.Constants;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

import java.util.HashMap;

public class SelectPlayerActivity extends BaseSocketActivity {

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(DISPLAY_ERROR)) {
        displayError(intent.getStringExtra(ERROR_MSG));
      } else if (intent.getAction().equals(NEXT_ACTIVITY)) {
        Intent enterLobby = new Intent(SelectPlayerActivity.this,
            LobbyActivity.class);
        String allPlayers = Storage.getAllPlayersAsJson();
        enterLobby.putExtra(ALL_PLAYERS, allPlayers);
        startActivity(enterLobby);
        finish();
      }
    }
  };
  private String pColor;

  @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_player);

    IntentFilter filter = new IntentFilter();
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(NEXT_ACTIVITY);
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(broadcastReceiver, filter);

		Spinner spinner = findViewById(R.id.farben);
    // Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
				R.array.farben_array, android.R.layout.simple_spinner_item);
    // Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    // Apply the adapter to the spinner
		spinner.setAdapter(adapter);

    Button confirmBtn = findViewById(R.id.enterLobbyBtn);
    ImageButton orangeBtn = findViewById(R.id.BtnOrange);
    ImageButton rotBtn = findViewById(R.id.BtnRot);
    ImageButton weissBtn = findViewById(R.id.BtnWeiss);
    ImageButton blauBtn = findViewById(R.id.BtnBlau);
		EditText pNameField = findViewById(R.id.input_player_name);

    EditText.OnEditorActionListener enterListener = (inputField, actionId, event) -> {
      if (actionId == EditorInfo.IME_ACTION_DONE
          && event.getAction() == KeyEvent.ACTION_DOWN) {
        String pColor = spinner.getSelectedItem().toString();
        sendPlayerInfo(inputField.getText().toString(), pColor);
      }
      return true;
    };


    orangeBtn.setOnClickListener((View v) -> {
      pColor = ORANGE;
      orangeBtn.setImageAlpha(255);
      rotBtn.setImageAlpha(128);
      weissBtn.setImageAlpha(128);
      blauBtn.setImageAlpha(128);
    });

    rotBtn.setOnClickListener((View v) -> {
      pColor = RED;
      rotBtn.setImageAlpha(255);
      orangeBtn.setImageAlpha(128);
      weissBtn.setImageAlpha(128);
      blauBtn.setImageAlpha(128);
    });

    weissBtn.setOnClickListener((View v) -> {
      pColor = WHITE;
      weissBtn.setImageAlpha(255);
      orangeBtn.setImageAlpha(128);
      rotBtn.setImageAlpha(128);
      blauBtn.setImageAlpha(128);
    });

    blauBtn.setOnClickListener((View v) -> {
      pColor = BLUE;
      blauBtn.setImageAlpha(255);
      orangeBtn.setImageAlpha(128);
      weissBtn.setImageAlpha(128);
      rotBtn.setImageAlpha(128);
    });

    pNameField.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

        if(s.toString().trim().length()!=0){
          confirmBtn.setEnabled(true);
          confirmBtn.setAlpha(1f);
          confirmBtn.setClickable(true);
        } else {
          confirmBtn.setEnabled(false);
          confirmBtn.setAlpha(0.8f);
          confirmBtn.setClickable(false);
        }


      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count,
                                    int after) {
        // TODO Auto-generated method stub

      }

      @Override
      public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub

      }
    });


    confirmBtn.setOnClickListener((View v) -> {
      String pName = pNameField.getText().toString();
      if (pName.length() > 0) {
	      sendPlayerInfo(pName, pColor);
      }

    });
  }




  public void displayError(String eMsg){
    View layout = findViewById(R.id.contain);
	  Snackbar snackbar = Snackbar.make(layout, eMsg, Snackbar.LENGTH_LONG);
	  snackbar.show();
	}

  private void sendPlayerInfo(String name, String color) {
    HashMap<String, Object> playerInfo = new HashMap<>();
    playerInfo.put(PLAYER_NAME, name);
    playerInfo.put(PLAYER_COLOR, color);
    playerInfo.put(PLAYER_ID, Storage.getSessionId());
    String pInfo = JSONUtils.createJSONString(PLAYER, playerInfo);
    mService.sendMessage(pInfo);
  }
}
