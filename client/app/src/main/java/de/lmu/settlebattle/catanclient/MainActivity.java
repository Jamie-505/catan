package de.lmu.settlebattle.catanclient;

//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.support.constraint.ConstraintLayout;
//import android.support.design.widget.Snackbar;
//import android.support.v4.content.LocalBroadcastManager;
//import android.view.View;
//import android.widget.Button;
//import de.lmu.settlebattle.catanclient.network.WebSocketService;
//
//public class MainActivity extends BaseSocketActivity {
//
//  // LogCat tag
////  private static final String TAG = MainActivity.class.getSimpleName();
//
////  private Storage storage;
//
//  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
//    @Override
//    public void onReceive(Context context, Intent intent) {
//    }
//  };
//
//  @Override
//  protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//
//    setContentView(R.layout.activity_main);
//
//    //final ConstraintLayout contain= (ConstraintLayout) findViewById(R.id.contain);
//    Button handelnButton = (Button) findViewById(R.id.handeln_button);
//    Button bauenButton = (Button) findViewById(R.id.bauen_button);
//	  System.out.println("hello_world onCreate");
//// TODO: Funktioniert atm nicht, unbekannte gründe
//    handelnButton.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View view) {
//	      System.out.println("hello_world button");
//       // Snackbar.make(contain, "Handeln button wurde geklickt", Snackbar.LENGTH_SHORT).show();
//           }
//    });
//  }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
//            new IntentFilter(WebSocketService.ACTION_MSG_RECEIVED));
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//    }
//
//    @Override
//    protected void onStop() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//        super.onStop();
//    }
//}

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.lmu.settlebattle.catanclient.grid.Cube;
import de.lmu.settlebattle.catanclient.grid.DemoObjects;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Grid;
import de.lmu.settlebattle.catanclient.grid.StorageMap;

import com.otaliastudios.zoom.ZoomImageView;
import com.otaliastudios.zoom.ZoomLayout;
import com.otaliastudios.zoom.ZoomLogger;

public class MainActivity extends BaseSocketActivity {

  // LogCat tag
  private static final String TAG = MainActivity.class.getSimpleName();

  // TODO: need to use zoom panview

  private RelativeLayout mRelativeLayout;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getActionBar().hide();

    mRelativeLayout = findViewById(R.id.gridLayout);

    int radius = 3;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      radius = extras.getInt("GRID_RADIUS", 3);
    }

    initGridView(radius);
  }

  private void initGridView(int radius) {
    int scale = setGridDimensions(radius);

    //Init node elements
    Grid grid = setGridNodes(radius, scale);

    //Init zoom buttons
//    setGridButtons(grid);
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



//  private void setGridButtons(final Grid grid) {
//    int scale = Grid.getGridWidth(grid.radius, grid.scale, grid.shape) / 16;
//
//    View zoomOutButton = findViewById(R.id.zoomOutButton);
//    ViewGroup.LayoutParams params = zoomOutButton.getLayoutParams();
//    params.width = scale;
//    params.height = scale;
//
//    View zoomInButton = findViewById(R.id.zoomInButton);
//    params = zoomInButton.getLayoutParams();
//    params.width = scale;
//    params.height = scale;
//
//    zoomOutButton.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        int newRadius = grid.radius+1;
//        if(newRadius > 12) return;
//
//        //Restart the activity with the new parameters
//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.putExtra("GRID_RADIUS", newRadius);
//        intent.putExtra("GRID_SHAPE", grid.shape.name());
//        startActivity(intent);
//        finish();
//
//        //Remove all the elements from the view except the side buttons
////                final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.container_layout);
////                viewGroup.removeAllViews();
////                mRelativeLayout = (RelativeLayout) View.inflate(MainActivity.this, R.layout.hex_grid_layout, null);
////                viewGroup.addView(mRelativeLayout);
////                viewGroup.invalidate();
//      }
//    });

//    zoomInButton.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        int newRadius = grid.radius-1;
//        if(newRadius < 0) return;
//
//        //Restart the activity with the new parameters
//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        intent.putExtra("GRID_RADIUS", newRadius);
//        intent.putExtra("GRID_SHAPE", grid.shape.name());
//        startActivity(intent);
//        finish();
//
//        //Remove all the elements from the view except the side buttons
////                mRelativeLayout.removeAllViews();
////                initGridView(newRadius, grid.shape);
////                mRelativeLayout.invalidate();
//      }
//    });
//  }

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

  private void OnGridHexClick(Hex hex) {
    Toast.makeText(MainActivity.this, "Es wurde: " + hex + "gedrückt.", Toast.LENGTH_SHORT).show();
  }
}
