package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import java.lang.reflect.Type;

public class LobbyActivity extends BaseSocketActivity {

  private Player[] players = new Player[4];
  private Gson gson = new Gson();
  private Storage storage;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(GAME_START)) {
        Intent startGame = new Intent(LobbyActivity.this, MainActivity.class);
        startActivity(startGame);
      } else if (intent.getAction().equals(PLAYER_UPDATE)) {
        String allPlayers = storage.getAllPlayers();
        updatePlayers(allPlayers);
      } else if (intent.getAction().equals(PLAYER_WAIT)) {
        String allPlayers = storage.getAllPlayers();
        updatePlayers(allPlayers);
      }
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lobby);

    storage = new Storage(getApplicationContext());

    Intent intent = getIntent();
    updatePlayers(intent.getStringExtra(ALL_PLAYERS));
    setIntentFilter();

    Button startGameBtn = (Button) findViewById(R.id.startGameBtn);
    startGameBtn.setOnClickListener(
        (View v) -> {
          mService.sendMessage(createJSONString(GAME_READY, null));
          startGameBtn.setText(R.string.wait_for_other_players);
        });
  }

  private void setIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(GAME_START);
    filter.addAction(PLAYER_UPDATE);
    filter.addAction(PLAYER_WAIT);
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(broadcastReceiver, filter);
  }

  private void updatePlayers(String allPlayers) {
    setPlayers(allPlayers);
    // player card
    TextView playerName = (TextView) findViewById(R.id.playerName);
    playerName.setText(players[0].name);
    ImageButton playerColor = (ImageButton) findViewById(R.id.playerStatus);
    int colorId = getResources().getIdentifier(players[0].color.toLowerCase()
        , "color", getPackageName());
    playerColor.setBackgroundColor(getResources().getColor(colorId));
    if (players[0].status.equals(GAME_WAIT)) {
      playerColor.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
    }
    // end player card

    // opponent cards
    for (int i = 1; i < players.length; i++) {
      try {
        if (players[i] != null) {
          ConstraintLayout opponentCard =
              (ConstraintLayout) findViewById(getResources().getIdentifier(
              "player" + (i + 1), "id", getPackageName()));
          opponentCard.setVisibility(View.VISIBLE);

          TextView opponentName = (TextView) findViewById(getResources().getIdentifier(
              "p" + (i + 1) + "Name", "id", getPackageName()));
          opponentName.setText(players[i].name);

          int oColorId = getResources().getIdentifier(players[i].color.toLowerCase()
              , "color", getPackageName());
          ImageButton opponentColor = (ImageButton) findViewById(getResources().getIdentifier(
              "p" + (i + 1) + "Status","id", getPackageName()));
          opponentColor.setBackgroundColor(getResources().getColor(oColorId));
          if (players[i].status.equals(GAME_WAIT)) {
            opponentColor.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
          }
        }
      } catch (NullPointerException e) {
        ConstraintLayout opponentCard = (ConstraintLayout) findViewById(
            getResources().getIdentifier("player" + (i + 1), "id", getPackageName()));
        opponentCard.setVisibility(View.GONE);
      }
    }
  }

  private void setPlayers(String playersJSON) {
    Type type = new TypeToken<Player[]>(){}.getType();
    Player[] allPlayers = gson.fromJson(playersJSON, type);
    System.arraycopy(allPlayers, 0, players, 0, allPlayers.length);
  }
}