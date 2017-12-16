package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.content.BroadcastReceiver;

import de.lmu.settlebattle.catanclient.utils.JSONUtils;
import de.lmu.settlebattle.catanclient.utils.Storage;
import java.util.HashMap;

public class SelectPlayerActivity extends BaseSocketActivity {

  private Storage storage;
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(DISPLAY_ERROR)) {
        displayError(intent.getStringExtra(ERROR_MSG));
      } else if (intent.getAction().equals(NEXT_ACTIVITY)) {
        Intent enterLobby = new Intent(SelectPlayerActivity.this,
            LobbyActivity.class);
        String allPlayers = storage.getAllPlayers();
        enterLobby.putExtra(ALL_PLAYERS, allPlayers);
        startActivity(enterLobby);
      }
    }
  };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_select_player);

    storage = new Storage(getApplicationContext());

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
		EditText pNameField = findViewById(R.id.input_player_name);

    EditText.OnEditorActionListener enterListener = new EditText.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView inputField, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE
            && event.getAction() == KeyEvent.ACTION_DOWN) {
          String pColor = spinner.getSelectedItem().toString();
          sendPlayerInfo(inputField.getText().toString(), pColor);
        }
        return true;
      }
    };

    confirmBtn.setOnClickListener((View v) -> {
      String pColor = spinner.getSelectedItem().toString();
      String pName = pNameField.getText().toString();
      if (pName.length() > 0) {
	      sendPlayerInfo(pName, pColor);
      }

    });
  }

  public void displayError(String eMsg){
    View layout = findViewById(R.id.constraint);
	  Snackbar snackbar = Snackbar.make(layout, eMsg, Snackbar.LENGTH_LONG);
	  snackbar.show();
	}

  private void sendPlayerInfo(String name, String color) {
    HashMap<String, Object> playerInfo = new HashMap<>();
    playerInfo.put(PLAYER_NAME, name);
    playerInfo.put(PLAYER_COLOR, color);
    playerInfo.put(PLAYER_ID, storage.getSessionId());
    String pInfo = JSONUtils.createJSONString(PLAYER, playerInfo);
    mService.sendMessage(pInfo);
  }
}
