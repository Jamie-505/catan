package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import de.lmu.settlebattle.catanclient.dice.DiceFragment;
import de.lmu.settlebattle.catanclient.grid.Cube;
import de.lmu.settlebattle.catanclient.grid.Board;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Grid;
import de.lmu.settlebattle.catanclient.grid.StorageMap;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.trade.DomTradeFragment;
import de.lmu.settlebattle.catanclient.trade.SeaTradeFragment;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.trade.TradeOfferFragment;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;

import com.otaliastudios.zoom.ZoomImageView;
import com.otaliastudios.zoom.ZoomLayout;
import com.otaliastudios.zoom.ZoomLogger;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseSocketActivity {

  // LogCat tag
  private static final String TAG = MainActivity.class.getSimpleName();

  public Storage storage ;
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      reactToIntent(intent);
    }
  };

  // Visual Elements
  private Button bauenBtn;
  private Button domTradeBtn;
  private Button seaTradeBtn;
  private DomTradeFragment domTradeFragment = new DomTradeFragment();
  private DiceFragment diceFragment = new DiceFragment();
  private FragmentManager fragmentManager = getFragmentManager();
  private Gson gson = new Gson();
  private ImageButton diceBtn;
  private RelativeLayout mRelativeLayout;
  private SeaTradeFragment seaTradeFragment = new SeaTradeFragment();
  private TradeOfferFragment tradeOfferFragment = new TradeOfferFragment();
  private Board board;

  @Override
  public void onBackPressed() {
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    }
    if (mLayout != null &&
        (mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED || mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
      mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    super.onBackPressed();
  }

  // Main Panel zum Sliden
  private SlidingUpPanelLayout mLayout;
  //Beginn on Create
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    storage = new Storage(this);
    setIntentFilters();

    Intent startActivity = getIntent();
    board = gson.fromJson(startActivity.getStringExtra(BOARD), Board.class);

    board = new Board(board.fields);

    bauenBtn = (Button) findViewById(R.id.bauen_button);
    diceBtn = (ImageButton) findViewById(R.id.throwDiceBtn);
    domTradeBtn = (Button) findViewById(R.id.domTradeBtn);
    seaTradeBtn = (Button) findViewById(R.id.handeln_button);
    mRelativeLayout = (RelativeLayout) findViewById(R.id.gridLayout);

    setClickListener();

    int radius = 3;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      radius = extras.getInt("GRID_RADIUS", 3);
    }

    initGridView(radius, board.fields);

    setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
    // Liste & Baukostenkarte
    ListView list;
    String[] itemname ={
        "Straße bauen",
        "Siedlung bauen",
        "Stadt bauen",
        "Entwicklungskarte"

    };

    Integer[] imgid={
        R.drawable.cost_street,
        R.drawable.cost_settlment,
        R.drawable.cost_city,
        R.drawable.cost_devcard
    };

      CustomListAdapter adapter=new CustomListAdapter(this, itemname, imgid);
      list=(ListView)findViewById(R.id.list);
      list.setAdapter(adapter);

      list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                int position, long id) {
          // TODO Auto-generated method stub
          String Slecteditem= itemname[+position];
          Toast.makeText(getApplicationContext(), Slecteditem, Toast.LENGTH_SHORT).show();

        }
      });
    /*

    ListView lv = (ListView) findViewById(R.id.list);
    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(MainActivity.this, "onItemClick", Toast.LENGTH_SHORT).show();
      }
    });

    List<String> your_array_list = Arrays.asList(
        "Straße bauen",
        "Siedlung bauen",
        "Stadt bauen",
        "Entwicklungskarte"
    );

    // This is the array adapter, it takes the context of the activity as a
    // first parameter, the type of list view as a second parameter and your
    // array as a third parameter.
    /* old list view
    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
        this,
        android.R.layout.simple_list_item_1,
        your_array_list );

    lv.setAdapter(arrayAdapter);

    */

    mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
    mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View panel, float slideOffset) {
        Log.i(TAG, "onPanelSlide, offset " + slideOffset);

      }


      @Override
      public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        TextView wood = (TextView) findViewById(R.id.woodtxt);
        wood.setText("4");
        TextView brick = (TextView) findViewById(R.id.bricktxt);
        // Platz um die Anzahl der Ressourcen zu Parsen brick.setText(Html.fromHtml(getString(R.string.brick_count)));
        TextView iron = (TextView) findViewById(R.id.irontxt);
        //i.setText(Html.fromHtml(getString(R.string.iron_count)));
        TextView sheep = (TextView) findViewById(R.id.sheeptxt);
        //sheep.setText(Html.fromHtml(getString(R.string.sheep_count)));
        TextView card = (TextView) findViewById(R.id.cardtxt);
        //card.setText(Html.fromHtml(getString(R.string.cards_count)));
        TextView wheat = (TextView) findViewById(R.id.wheattxt);
        //wheat.setText(Html.fromHtml(getString(R.string.wheat_count)));

         /*
        TextView y = (TextView) findViewById(R.id.rsclong);
        t.setText(Html.fromHtml(getString(R.string.rsc_big)));

        if(mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED||mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
        {
          t.setVisibility(View.INVISIBLE);
          y.setVisibility(View.VISIBLE);
          Log.i(TAG, "Großer Text sollte sichbar sein ");
        }
        else {
          t.setVisibility(View.VISIBLE);
          y.setVisibility(View.INVISIBLE);
          Log.i(TAG, "Kurzer Text sollte sichbar sein ");*/



      }
    });
    mLayout.setFadeOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

      }
    });

    mLayout.setAnchorPoint(0.6f);
    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

/*
    final private Runnable runnable = new Runnable() {
      public void run() {
        LayerDrawable myDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.all_layers);
        Drawable layer =  myDrawable.findDrawableByLayerId(R.id.interesting_layer);
        if (layer.isVisible()==true)
        {
          layer.setVisible(false, false);
        }
        else
        {
          layer.setVisible(true, false);
        }
        TextView txt = (TextView) findViewById(R.id.txtTest);
        if (txt.getVisibility()==0)
        {
          txt.setVisibility(4);
        }
        else
        {
          txt.setVisibility(0);
        }
        Handler handler;
        handler.postDelayed(this, 5000);
      }
    };*/

  /*
  private void showTile (position,type,[building, corner, color]) {

    LayerDrawable layers = (LayerDrawable) findViewById(R.id.layer_list);

    layers.findDrawableByLayerId(R.id.compoundBackgroundItem).setAlpha(
            0);

    layers.findDrawableByLayerId(R.id.moreIndicatorItem).setAlpha(0);

    layers.findDrawableByLayerId(R.id.bluetoothItem).setAlpha(0);

    layers.findDrawableByLayerId(R.id.handsetItem).setAlpha(0);

    layers.findDrawableByLayerId(R.id.speakerphoneOnItem).setAlpha(255);

    layers.findDrawableByLayerId(R.id.speakerphoneOffItem).setAlpha(0);
}

   */
/*
    Button f = (Button) findViewById(R.id.follow);
    f.setText(Html.fromHtml(getString(R.string.follow)));
    f.setMovementMethod(LinkMovementMethod.getInstance());
    f.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://www.twitter.com/umanoapp"));
        startActivity(i);
      }
    });*/


  }
   /*
  final Button baubtn = (Button) findViewById(R.id.bauen_button);
 baubtn.setOnClickListener(new View.OnClickListener() {
    public void onClick(View v) {
      // Code here executes on main thread after user presses button
    }
  mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
*/

  private void addViewToLayout(View view, Hex hex, Grid grid) {
    //Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        grid.width, grid.height
    );
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerLayout);
    params.addRule(RelativeLayout.BELOW, R.id.centerLayout);
    mRelativeLayout.addView(view, params);

    //Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x;
    params.topMargin = -grid.centerOffsetY - p.y;
  }

  public void displayMessage(String msg){
    View layout = findViewById(R.id.contain);
    Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

  private void hideActiveElements() {
    diceBtn.setVisibility(View.INVISIBLE);
  }

  private void hideFragment(Fragment fragment) {
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.hide(fragment);
    ft.commit();
  }

  private void initGridView(int radius, Hex[] fields) {
    int scale = setGridDimensions(radius);

    //Init node elements
    setGridNodes(radius, scale, fields);
  }

  private void OnGridHexClick(Hex hex) {
    Toast.makeText(MainActivity.this, "Es wurde: " + hex + "gedrückt.", Toast.LENGTH_SHORT).show();
  }

  private void reactToIntent(Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case DICE_RESULT:
          Bundle diceBundle = new Bundle();
          diceBundle.putString(DICE_THROW, intent.getStringExtra(DICE_THROW));
          diceFragment.setArguments(diceBundle);
          showFragment(diceFragment);
          break;
        case DISPLAY_ERROR:
          displayMessage(intent.getStringExtra(ERROR_MSG));
          break;
        case OK:
          hideFragment(seaTradeFragment);
          break;
        case PLAYER_UPDATE:
          Log.d(TAG, storage.getAllPlayers());
          // TODO update the player views
          break;
        case PLAYER_WAIT:
          hideActiveElements();
        case ROLL_DICE:
          showView(diceBtn);
          break;
        case TRD_FIN:
          onBackPressed();
          Trade t = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
          showTradeSummary(t);
          break;
        case TRD_OFFER:
          Bundle tradeBundle = new Bundle();
          tradeBundle.putString(TRADE, intent.getStringExtra(TRADE));
          tradeOfferFragment.setArguments(tradeBundle);
          showFragment(tradeOfferFragment);
          break;
      }
    } else {
      Log.e(TAG, "Received intend had no action");
    }
  }

  public Bitmap rotateBitmap(Bitmap original, float degrees) {
    int width = original.getWidth();
    int height = original.getHeight();

    Matrix matrix = new Matrix();
    matrix.preRotate(degrees);

    Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
    Canvas canvas = new Canvas(rotatedBitmap);
    canvas.drawBitmap(original, 5.0f, 0.0f, null);

    return rotatedBitmap;
  }

  private void setClickListener() {
    diceBtn.setOnClickListener((View v) -> {
      String diceMsg = JSONUtils.createJSONString(ROLL_DICE, new Object());
      mService.sendMessage(diceMsg);
    });

    domTradeBtn.setOnClickListener((View v) -> showFragment(domTradeFragment));
    seaTradeBtn.setOnClickListener((View v) -> showFragment(seaTradeFragment));
  }

  private void setIntentFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(DICE_RESULT);
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(OK);
    filter.addAction(PLAYER_UPDATE);
    filter.addAction(PLAYER_WAIT);
    filter.addAction(ROLL_DICE);
    filter.addAction(TRD_ABORTED);
    filter.addAction(TRD_FIN);
    filter.addAction(TRD_OFFER);
    LocalBroadcastManager.getInstance(this)
        .registerReceiver(broadcastReceiver, filter);
  }

  private int setGridDimensions(int radius) {
    // Gets the layout params that will allow to resize the layout
    ViewGroup.LayoutParams params = mRelativeLayout.getLayoutParams();

    //Get display metrics
    Display display = getWindowManager().getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int displayWidth = size.x;
    int displayHeight = size.y;

    //If in landscape mode, keep the width small as in portrait mode
    if(displayWidth > displayHeight) displayWidth = displayHeight;

    int horizontalPadding = (int) getResources().getDimension(R.dimen.activity_horizontal_margin);
    //int horizontalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, horizontalPaddingInDp, getResources().getDisplayMetrics());
    displayWidth -= 2 * horizontalPadding;

    // Calculate the scale: the radius of single node.
    int scale = (int) (displayWidth / ((2*radius + 1) * (Math.sqrt(3))));

    // Changes the height and width of the grid to the specified *pixels*
    params.width = Grid.getGridWidth(radius, scale);
    params.height = Grid.getGridHeight(radius, scale);

    return scale;
  }

  private Grid setGridNodes(int radius, int scale, Hex[] fields) {
    try {
      Board board = new Board(fields);

      StorageMap storageMap = new StorageMap(radius, board.hexMap);
      final Grid grid = new Grid(radius, scale);

      //Grid node listener restricted to the node's circular area.
      View.OnTouchListener gridNodeTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(final View v, MotionEvent event) {
          switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
              float xPoint = event.getX();
              float yPoint = event.getY();
              //Hex hex = grid.pixelToHex(event.getX(), event.getY()); //This can work on the RelativeLayout grid area
              boolean isPointOutOfCircle = (grid.centerOffsetX -xPoint)*(grid.centerOffsetX -xPoint) + (grid.centerOffsetY -yPoint)*(grid.centerOffsetY -yPoint) > grid.width * grid.width / 4;

              if (isPointOutOfCircle) return false;
              else v.setSelected(true);
              break;
            case MotionEvent.ACTION_OUTSIDE:
              break;
            case MotionEvent.ACTION_CANCEL:
              break;
            case MotionEvent.ACTION_MOVE:
              break;
            case MotionEvent.ACTION_SCROLL:
              break;
            case MotionEvent.ACTION_UP:
              v.setSelected(false);
              CircleImageView view = (CircleImageView) v;
              OnGridHexClick(view.getHex());
              break;
          }
          return true;
        }
      };

      for(Cube cube : grid.nodes) {
        Hex hex = cube.toHex();

        CircleImageView view = new CircleImageView(this);
        Integer pic = (Integer) storageMap.getObjectByCoordinate(hex.getX(), hex.getY());
        if(pic == null) {
          view.setHex(hex);
          view.setOnTouchListener(gridNodeTouchListener);
//                    view.setBackgroundResource(R.drawable.ring);
          view.setImageResource(R.drawable.wheat_highlighted);
        } else {
          view = new CircleImageView(this);
          //view.setBackgroundResource(R.drawable.hexagon);
          view.setOnTouchListener(gridNodeTouchListener);
          view.setHex(hex);
          if(pic != 0) {
            view.setImageResource(pic);
          } else {
            view.setImageResource((R.drawable.harbor));
          }
        }
        addViewToLayout(view, hex, grid);
      }

      return grid;
    } catch (Exception e) {
      Toast.makeText(MainActivity.this, "Sorry, there was a problem initializing the application.", Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }

    return null;
  }

  private void showFragment(MainActivityFragment f) {
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.addToBackStack(f.tag());
    if (fragmentManager.findFragmentByTag(f.tag()) == null) {
      ft.add(R.id.fragmentContainer, f, f.tag());
    } else {
      ft.show(f);
    }
    ft.commit();
  }

  private void showTradeSummary(Trade t) {
    Player p1 = storage.getOpponent(t.player);
    Player p2 = storage.getOpponent(t.opponent);
    if (p1 == null) {
      p1 = storage.getPlayer();
    } else if (p2 == null) {
      p2 = storage.getPlayer();
    }
    String msg = String.format("Handel zwischen %s und %s wurde abgeschlossen",
        p1.name, p2.name);
    displayMessage(msg);
  }

  private void showView(View v) {
    v.setVisibility(View.VISIBLE);
  }
}
