package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.*;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.lmu.settlebattle.catanclient.dice.DiceResultFragment;
import de.lmu.settlebattle.catanclient.grid.Cube;
import de.lmu.settlebattle.catanclient.grid.DemoObjects;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Grid;
import de.lmu.settlebattle.catanclient.grid.StorageMap;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.trade.SeaTradeFragment;
import de.lmu.settlebattle.catanclient.utils.JSONUtils;

public class MainActivity extends BaseSocketActivity {

  // LogCat tag
  private static final String TAG = MainActivity.class.getSimpleName();

  private Storage storage ;
  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      reactToIntent(intent);
    }
  };

  // Visual Elements
  private Button bauenBtn;
  private Button handelnBtn;
  private DiceResultFragment diceFragment = new DiceResultFragment();
  private FragmentManager fragmentManager = getFragmentManager();
  private ImageButton diceBtn;
  private RelativeLayout mRelativeLayout;
  private SeaTradeFragment seaTradeFragment = new SeaTradeFragment();

  @Override
  public void onBackPressed() {
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      super.onBackPressed();
    }
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    storage = new Storage(this);
    setIntentFilters();

    handelnBtn = (Button) findViewById(R.id.handeln_button);
    bauenBtn = (Button) findViewById(R.id.bauen_button);
    mRelativeLayout = (RelativeLayout) findViewById(R.id.gridLayout);
    diceBtn = (ImageButton) findViewById(R.id.throwDiceBtn);

    setClickListener();

    int radius = 3;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      radius = extras.getInt("GRID_RADIUS", 3);
    }

    initGridView(radius);
  }

  private void addViewToLayout(View view, Hex hex, Grid grid) {
    //Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(grid.width, grid.height);
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerLayout);
    params.addRule(RelativeLayout.BELOW, R.id.centerLayout);
    mRelativeLayout.addView(view, params);

    //Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x;
    params.topMargin = -grid.centerOffsetY + p.y;
  }

  public void displayError(String eMsg){
    View layout = findViewById(R.id.contain);
    Snackbar snackbar = Snackbar.make(layout, eMsg, Snackbar.LENGTH_LONG);
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

  private void initGridView(int radius) {
    int scale = setGridDimensions(radius);

    //Init node elements
    Grid grid = setGridNodes(radius, scale);
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
          displayError(intent.getStringExtra(ERROR_MSG));
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
    handelnBtn.setOnClickListener((View v) -> {
      showFragment(seaTradeFragment);
    });

    diceBtn.setOnClickListener((View v) -> {
      String diceMsg = JSONUtils.createJSONString(ROLL_DICE, new Object());
      mService.sendMessage(diceMsg);
    });
  }

  private void setIntentFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(OK);
    filter.addAction(PLAYER_UPDATE);
    filter.addAction(PLAYER_WAIT);
    filter.addAction(ROLL_DICE);
    filter.addAction(DICE_RESULT);
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

  private Grid setGridNodes(int radius, int scale) {
    try {
      StorageMap storageMap = new StorageMap(radius, DemoObjects.squareMap);
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
        Integer pic = (Integer) storageMap.getObjectByCoordinate(hex.getQ(), hex.getR());
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

  private void showView(View v) {
    v.setVisibility(View.VISIBLE);
  }
}
