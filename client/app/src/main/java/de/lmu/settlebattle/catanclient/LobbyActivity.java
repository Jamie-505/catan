package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.ADD_KI;
import static de.lmu.settlebattle.catanclient.utils.Constants.ALL_PLAYERS;
import static de.lmu.settlebattle.catanclient.utils.Constants.BLUE;
import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_READY;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_START;
import static de.lmu.settlebattle.catanclient.utils.Constants.GAME_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.ORANGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_UPDATE;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.RED;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHITE;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import java.lang.reflect.Type;

public class LobbyActivity extends BaseSocketActivity {

  private Player[] players;
  private Gson gson = new Gson();
  private LocalBroadcastManager localBroadcastManager;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(GAME_START)) {
        Intent startGame = new Intent(LobbyActivity.this, MainActivity.class);
        startGame.putExtra(BOARD, intent.getStringExtra(BOARD));
        startActivity(startGame);
        finish();
      } else if (intent.getAction().equals(PLAYER_UPDATE)) {
        updatePlayers(Storage.getAllPlayersAsJson());
      } else if (intent.getAction().equals(PLAYER_WAIT)) {
        updatePlayers(Storage.getAllPlayersAsJson());
      }
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_lobby);

    Intent intent = getIntent();
    updatePlayers(intent.getStringExtra(ALL_PLAYERS));
    localBroadcastManager = LocalBroadcastManager.getInstance(this);

    Button startGameBtn = findViewById(R.id.startGameBtn);
    startGameBtn.setOnClickListener(
        (View v) -> {
          mService.sendMessage(createJSONString(GAME_READY, null));
          startGameBtn.setText(R.string.wait_for_other_players);
        });
    Button addKiBtn = findViewById(R.id.kiBtn);
    addKiBtn.setOnClickListener((View v) -> {
      mService.sendMessage(createJSONString(ADD_KI, null));
    });
  }

  @Override
  protected void onResume(){
    super.onResume();
    setIntentFilter();
  }

  @Override
  protected void onPause() {
    super.onPause();
    localBroadcastManager.unregisterReceiver(broadcastReceiver);
  }

  private void setIntentFilter() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(GAME_START);
    filter.addAction(PLAYER_UPDATE);
    filter.addAction(PLAYER_WAIT);
    localBroadcastManager.registerReceiver(broadcastReceiver, filter);
  }

  private void updatePlayers(String allPlayers) {
    setPlayers(allPlayers);
    updatePlayers();
  }

  private void updatePlayers() {
    // player card
    TextView playerName = findViewById(R.id.playerName);
    TextView hello = findViewById(R.id.textView5);
    playerName.setText(players[0].name);
    hello.setText("Hi, "+players[0].name+"!");
    ImageButton playerColor = findViewById(R.id.playerStatus);

    int colorId = getResources().getIdentifier(players[0].color.toLowerCase()
        , "color", getPackageName());
    playerColor.setBackgroundColor(getResources().getColor(colorId));
    if (players[0].status.equals(GAME_WAIT)) {
      playerColor.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
    }

    // Update PlayerImage
    ImageView playerPic = findViewById(R.id.playerPic);
    Drawable color = null;
    switch (players[0].color) {
      case BLUE:
        color = getResources().getDrawable(R.drawable.pl_blau_armeen);
        break;
      case ORANGE:
        color = getResources().getDrawable(R.drawable.pl_orange_james);
        break;
      case RED:
        color = getResources().getDrawable(R.drawable.pl_rot_lisa);
        break;
      case WHITE:
        color = getResources().getDrawable(R.drawable.pl_weiss_danijel);
        break;
    }
    playerPic.setImageDrawable(color);
    // end player card

    // opponent cards
    for (int i = 1; i < players.length; i++) {
      try {
        if (players[i] != null) {
          ConstraintLayout opponentCard = findViewById(getResources()
                  .getIdentifier("player" + (i + 1), "id", getPackageName()));
          opponentCard.setVisibility(View.VISIBLE);

          TextView opponentName = findViewById(getResources().getIdentifier(
              "p" + (i + 1) + "Name", "id", getPackageName()));
          opponentName.setText(players[i].name);

          int oColorId = getResources().getIdentifier(players[i].color.toLowerCase()
              , "color", getPackageName());
          ImageButton opponentColor = findViewById(getResources().getIdentifier(
              "p" + (i + 1) + "Status","id", getPackageName()));
          ImageView opponentImg = findViewById(getResources().getIdentifier(
              "p" + (i + 1) + "Pic","id", getPackageName()));
          switch (players[i].color) {
            case BLUE:
              color = getResources().getDrawable(R.drawable.pl_blau_armeen);
              break;
            case ORANGE:
              color = getResources().getDrawable(R.drawable.pl_orange_james);
              break;
            case RED:
              color = getResources().getDrawable(R.drawable.pl_rot_lisa);
              break;
            case WHITE:
              color = getResources().getDrawable(R.drawable.pl_weiss_danijel);
              break;
          }
          opponentImg.setImageDrawable(color);
          opponentColor.setBackgroundColor(getResources().getColor(oColorId));
          if (players[i].status.equals(GAME_WAIT)) {
            opponentColor.setImageDrawable(getResources().getDrawable(R.drawable.ic_success));
          }
        }
      } catch (NullPointerException e) {
        ConstraintLayout opponentCard = findViewById(
            getResources().getIdentifier("player" + (i + 1), "id", getPackageName()));
        opponentCard.setVisibility(View.GONE);
      }
    }
  }

  private void setPlayers(String playersJSON) {
    Type type = new TypeToken<Player[]>(){}.getType();
    players = gson.fromJson(playersJSON, type);
  }
}
