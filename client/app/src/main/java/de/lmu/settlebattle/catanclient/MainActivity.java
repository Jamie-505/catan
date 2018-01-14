package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_CITY;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_SETTLEMENT;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_VILLAGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_STREET;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.CARD_BUY;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_RESULT;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;
import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.END_TURN;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR_MSG;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEW_CONSTRUCT;
import static de.lmu.settlebattle.catanclient.utils.Constants.OK;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROLL_DICE;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_UPD;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_ABORTED;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_FIN;
import static de.lmu.settlebattle.catanclient.utils.Constants.TRD_OFFER;
import static de.lmu.settlebattle.catanclient.utils.JSONUtils.createJSONString;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import de.lmu.settlebattle.catanclient.dice.DiceFragment;
import de.lmu.settlebattle.catanclient.grid.Board;
import de.lmu.settlebattle.catanclient.grid.Construction;
import de.lmu.settlebattle.catanclient.grid.Construction.ConstructionType;
import de.lmu.settlebattle.catanclient.grid.ConstructionsLayer;
import de.lmu.settlebattle.catanclient.grid.Cube;
import de.lmu.settlebattle.catanclient.grid.Grid;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.StorageMap;
import de.lmu.settlebattle.catanclient.grid.building.BuildingView;
import de.lmu.settlebattle.catanclient.grid.building.BuildingViewN;
import de.lmu.settlebattle.catanclient.grid.building.BuildingViewNW;
import de.lmu.settlebattle.catanclient.grid.street.StreetView;
import de.lmu.settlebattle.catanclient.grid.street.StreetView.Orientation;
import de.lmu.settlebattle.catanclient.grid.street.StreetViewNE;
import de.lmu.settlebattle.catanclient.grid.street.StreetViewNW;
import de.lmu.settlebattle.catanclient.grid.street.StreetViewW;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import de.lmu.settlebattle.catanclient.player.Player;
import de.lmu.settlebattle.catanclient.player.RawMaterialOverview;
import de.lmu.settlebattle.catanclient.player.Storage;
import de.lmu.settlebattle.catanclient.playerCards.CardItem;
import de.lmu.settlebattle.catanclient.playerCards.CardPagerAdapter;
import de.lmu.settlebattle.catanclient.playerCards.ShadowTransformer;
import de.lmu.settlebattle.catanclient.trade.DomTradeFragment;
import de.lmu.settlebattle.catanclient.trade.SeaTradeFragment;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.trade.TradeOfferFragment;

public class MainActivity extends BaseSocketActivity {

  // LogCat tag
  private static final String TAG = MainActivity.class.getSimpleName();

  private boolean isItTimeToBuild;
  public Storage storage;
  private Player self;
  private Player[] allPlayers;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      reactToIntent(intent);
    }
  };

  // Visual Elements
  private Button domTradeBtn;
  private Button endTurnBtn;
  private Button seaTradeBtn;
  private ConstructionsLayer settlementLayer;
  private ConstructionsLayer streetLayer;
  private DomTradeFragment domTradeFragment = new DomTradeFragment();
  private FragmentManager fragmentManager = getFragmentManager();
  private Gson gson = new Gson();
  private ImageButton diceBtn;
  private RelativeLayout gridLayout;
  private SeaTradeFragment seaTradeFragment = new SeaTradeFragment();
  private SlidingUpPanelLayout slidingPanel;
  private TextView selfClayCnt;
  private TextView selfDevCardCnt;
  private TextView selfOreCnt;
  private TextView selfWheatCnt;
  private TextView selfWoodCnt;
  private TextView selfWoolCnt;
  private TradeOfferFragment tradeOfferFragment = new TradeOfferFragment();

  // Card Viewer
  private ViewPager mViewPager;
  private CardPagerAdapter mCardAdapter;
  private ShadowTransformer mCardShadowTransformer;

  @Override
  public void onBackPressed() {
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    }
    if (slidingPanel != null &&
        slidingPanel.getPanelState() != SlidingUpPanelLayout.PanelState.COLLAPSED) {
      slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    super.onBackPressed();
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    storage = new Storage(this);
    self = storage.getPlayer();
    allPlayers = storage.getAllPlayers();
    setIntentFilters();

    Intent startActivity = getIntent();
    // I know weird but I need to get the fields from the message somehow to init the board
    Board board = gson.fromJson(startActivity.getStringExtra(BOARD), Board.class);
    board = new Board(board.fields);

    diceBtn = findViewById(R.id.throwDiceBtn);
    domTradeBtn = findViewById(R.id.dom_trade_btn);
    endTurnBtn = findViewById(R.id.end_turn_btn);
    gridLayout = findViewById(R.id.gridLayout);
    seaTradeBtn = findViewById(R.id.seatrade_btn);
    selfClayCnt = findViewById(R.id.slidePnlClayCnt);
    selfDevCardCnt = findViewById(R.id.slidePnlDevCardCnt);
    selfOreCnt = findViewById(R.id.slidePnlOreCnt);
    selfWheatCnt = findViewById(R.id.slidePnlWheatCnt);
    selfWoodCnt = findViewById(R.id.slidePnlWoodCnt);
    selfWoolCnt = findViewById(R.id.slidePnlWoolCnt);
    settlementLayer = findViewById(R.id.layerSettlements);
    slidingPanel = findViewById(R.id.sliding_layout);
    streetLayer = findViewById(R.id.layerStreets);

    setClickListener();

    int radius = 3;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      radius = extras.getInt("GRID_RADIUS", 3);
    }

    initializePlayerCards(allPlayers);

    initGridView(radius, board.fields);
    streetLayer.setWithholdTouchEventsFromChildren(true);
    settlementLayer.setWithholdTouchEventsFromChildren(true);

    setSupportActionBar(findViewById(R.id.main_toolbar));

    // Liste & Baukostenkarte
    ListView list;
    String[] actionEntries ={
        BUILD_STREET,
        BUILD_SETTLEMENT,
        BUILD_CITY,
        CARD_BUY,
    };

    Integer[] imgid={
        R.drawable.cost_street,
        R.drawable.cost_settlment,
        R.drawable.cost_city,
        R.drawable.cost_devcard
    };

    CustomListAdapter adapter = new CustomListAdapter(this, actionEntries, imgid);
    list = findViewById(R.id.list);
    list.setAdapter(adapter);

    list.setOnItemClickListener((parent, view, position, id) -> {
      if (isItTimeToBuild) {
        String selectedItem= actionEntries[+position];
        switch (selectedItem) {
          case BUILD_STREET:
            streetLayer.setWithholdTouchEventsFromChildren(false);
            break;
          case BUILD_SETTLEMENT:
          case BUILD_CITY:
            settlementLayer.setWithholdTouchEventsFromChildren(false);
            break;
        }
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        Toast.makeText(getApplicationContext(), selectedItem, Toast.LENGTH_SHORT).show();
      }
    });

    LinearLayout dragPanel = findViewById(R.id.dragView);
    int colorId = getResources().getIdentifier(self.color.toLowerCase(),
        "color", getPackageName());
    dragPanel.setBackgroundColor(getResources().getColor(colorId));
    slidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View panel, float slideOffset) {
        Log.i(TAG, "onPanelSlide, offset " + slideOffset);
      }

      @Override
      public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
          SlidingUpPanelLayout.PanelState newState) {

         /*

        if(slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED||slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
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
    slidingPanel.setFadeOnClickListener(
        view -> slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
    slidingPanel.setAnchorPoint(0.6f);
    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
  }

  @Override
  public void onResume() {
    WebSocketService.mainActivityActive = true;
    super.onResume();
  }

  @Override
  public void onStop() {
    WebSocketService.mainActivityActive = false;
    super.onStop();
  }

  private void addViewToLayout(View view, Hex hex, Grid grid) {
    //Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        grid.width, grid.height
    );
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerLayout);
    params.addRule(RelativeLayout.BELOW, R.id.centerLayout);
    gridLayout.addView(view, params);

    //Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x;
    params.topMargin = -grid.centerOffsetY - p.y;
  }

  private void addBuildingTopLeft(Hex hex, Grid grid) {
    BuildingViewNW building = new BuildingViewNW(this, hex);

    int width = 48, height = 48;
    //Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        width, height
    );
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerSettlements);
    params.addRule(RelativeLayout.BELOW, R.id.centerSettlements);
    settlementLayer.addView(building, params);

    building.setOnClickListener((View v) -> {
      BuildingView b = (BuildingView) v;
      Construction c = new Construction(self.id, b.type, b.location, b.getTag().toString());
      mService.sendMessage(createJSONString(BUILD, c));
    });

    //Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x - width/2;
    params.topMargin = -grid.centerOffsetY - p.y + 20;
  }

  private void addBuildingTop(Hex hex, Grid grid) {

    BuildingViewN building = new BuildingViewN(this, hex);

    int width = 48, height = 48;
    //Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        width, height
    );
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerSettlements);
    params.addRule(RelativeLayout.BELOW, R.id.centerSettlements);
    settlementLayer.addView(building, params);

    building.setOnClickListener((View v) -> {
      BuildingView b = (BuildingView) v;
      Construction c = new Construction(self.id, b.type, b.location, b.getTag().toString());
      mService.sendMessage(createJSONString(BUILD, c));
    });

    //Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x - width/2 + grid.width/2;
    params.topMargin = -grid.centerOffsetY - p.y - height/2;
  }

  private void addStreetLeft(Hex hex, Grid grid) {

    StreetViewW street = new StreetViewW(this, hex);

    int width = 18, height = grid.scale;
    // Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        width, height
    );
    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerStreets);
    params.addRule(RelativeLayout.BELOW, R.id.centerStreets);
    streetLayer.addView(street, params);

    street.setOnClickListener((View v) -> {
      StreetView s = (StreetView) v;
      Construction c = new Construction(self.id, s.type, s.location, s.getTag().toString());
      mService.sendMessage(createJSONString(BUILD, c));
    });

    // Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x - width/2;
    params.topMargin = -grid.centerOffsetY - p.y + grid.height/4;
  }

  private void addStreetTopLeft(Hex hex, Grid grid) {

    StreetViewNW street = new StreetViewNW(this, hex);

    int width = grid.width/2, height = 50;
    // Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        width, height
    );

    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerStreets);
    params.addRule(RelativeLayout.BELOW, R.id.centerStreets);
    streetLayer.addView(street, params);

    street.setOnClickListener((View v) -> {
      StreetView s = (StreetView) v;
      Construction c = new Construction(self.id, s.type, s.location, s.getTag().toString());
      mService.sendMessage(createJSONString(BUILD, c));
    });

    // Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x + 72 - grid.width/2;
    params.topMargin = -grid.centerOffsetY - p.y - 5;
  }

  private void addStreetTopRight(Hex hex, Grid grid) {

    StreetViewNE street = new StreetViewNE(this, hex);
    int width = grid.width/2, height = 50;
    // Add to view
    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
        width, height
    );

    params.addRule(RelativeLayout.RIGHT_OF, R.id.centerStreets);
    params.addRule(RelativeLayout.BELOW, R.id.centerStreets);
    streetLayer.addView(street, params);

    street.setOnClickListener((View v) -> {
      StreetView s = (StreetView) v;
      Construction c = new Construction(self.id, s.type, s.location, s.getTag().toString());
      mService.sendMessage(createJSONString(BUILD, c));
    });

    // Set coordinates
    Point p = grid.hexToPixel(hex);
    params.leftMargin = -grid.centerOffsetX + p.x + 72;
    params.topMargin = -grid.centerOffsetY - p.y - 5;
  }

  private LayerDrawable createColoredBuilding(String farbe, ConstructionType type) {
    Drawable[] layers = new Drawable[2];
    int colorId = getResources().getIdentifier(farbe.toLowerCase() , "color", getPackageName());
    int color = getColor(colorId);
    int colorDarkId = getResources().getIdentifier(farbe.toLowerCase() + "_dark" , "color", getPackageName());
    int colorDark = getColor(colorDarkId);
    VectorMasterDrawable container = new VectorMasterDrawable(this, R.drawable.ic_bulding_container);
    PathModel pathModel = container.getPathModelByName("background");
    pathModel.setFillColor(color);
    pathModel.setStrokeColor(colorDark);
    layers[0] = container;
    switch (type) {
      case CITY:
        layers[1] = this.getResources().getDrawable(R.drawable.ic_city);
        break;
      case SETTLEMENT:
        layers[1] = this.getResources().getDrawable(R.drawable.ic_settlement);
        break;
    }
    LayerDrawable building = new LayerDrawable(layers);
    building.setLayerGravity(0, Gravity.CENTER);
    building.setLayerGravity(1, Gravity.CENTER);
    return building;
  }

  private Drawable createColoredStreet(String farbe, Orientation orientation) {
    int colorId = getResources().getIdentifier(farbe.toLowerCase() , "color", getPackageName());
    int color = getColor(colorId);
    int colorDarkId = getResources().getIdentifier(farbe.toLowerCase() + "_dark" , "color", getPackageName());
    int colorDark = getColor(colorDarkId);
    VectorMasterDrawable street;
    switch (orientation) {
      case TILTED:
        street = new VectorMasterDrawable(this, R.drawable.ic_street_tilted);
        break;
      default:
        street = new VectorMasterDrawable(this, R.drawable.ic_street_straight);
        break;
    }
    PathModel pathModel = street.getPathModelByName("color");
    pathModel.setFillColor(color);
    pathModel.setStrokeColor(colorDark);
    return street;
  }

  private void deactivateBuildLayers() {
    settlementLayer.setWithholdTouchEventsFromChildren(true);
    streetLayer.setWithholdTouchEventsFromChildren(true);
  }

  public void displayMessage(String msg){
    View layout = findViewById(R.id.contain);
    Snackbar snackbar = Snackbar.make(layout, msg, Snackbar.LENGTH_LONG);
    snackbar.show();
  }

  private void endTurn() {
    hideActiveElements();
    mService.sendMessage(createJSONString(END_TURN, null));
  }

  private void hideActiveElements() {
    seaTradeBtn.setVisibility(View.INVISIBLE);
    domTradeBtn.setVisibility(View.INVISIBLE);
    endTurnBtn.setVisibility(View.INVISIBLE);
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

  private void initializePlayerCards(Player[] players) {
    mCardAdapter = new CardPagerAdapter();
    mViewPager = (ViewPager) findViewById(R.id.viewPager);

    for (Player p : players) {
      CardItem card = new CardItem(false, p.color, p.name, p.victoryPts, p.devCards.getTotalAmnt(),
          p.rawMaterials.getTotalAmnt(), p.biggestArmy, p.longestRd);
      mCardAdapter.addCardItem(card);
    }

    //Aktiviere Views statt Fragments
    mViewPager.setAdapter(mCardAdapter);
    mViewPager.setPageTransformer(false, mCardShadowTransformer);

    mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

    mViewPager.setAdapter(mCardAdapter);
    mViewPager.setPageTransformer(false, mCardShadowTransformer);
    mViewPager.setOffscreenPageLimit(3);
  }

  private void OnGridHexClick(Hex hex) {
    Toast.makeText(MainActivity.this, "Es wurde: " + hex + "gedrückt.", Toast.LENGTH_SHORT).show();
  }

  private void reactToIntent(Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case BUILD_VILLAGE:
          Toast.makeText(MainActivity.this, "Du kannst jetzt eine Siedlung bauen", Toast.LENGTH_LONG).show();
          settlementLayer.setWithholdTouchEventsFromChildren(false);
          break;
        case BUILD_STREET:
          Toast.makeText(MainActivity.this, "Du kannst jetzt eine Straße bauen", Toast.LENGTH_LONG).show();
          streetLayer.setWithholdTouchEventsFromChildren(false);
          break;
        case BUILD_TRADE:
          Player p = gson.fromJson(intent.getStringExtra(PLAYER), Player.class);
          if (storage.isItMe(p.id)) {
            isItTimeToBuild = true;
            diceBtn.setVisibility(View.INVISIBLE);
            domTradeBtn.setVisibility(View.VISIBLE);
            seaTradeBtn.setVisibility(View.VISIBLE);
            endTurnBtn.setVisibility(View.VISIBLE);
          }
          break;
        case DICE_RESULT:
          Bundle diceBundle = new Bundle();
          diceBundle.putString(DICE_THROW, intent.getStringExtra(DICE_THROW));
          MainActivityFragment diceFragment = new DiceFragment();
          diceFragment.setArguments(diceBundle);
          showFragment(diceFragment);
          break;
        case DISPLAY_ERROR:
          displayMessage(intent.getStringExtra(ERROR_MSG));
          break;
        case NEW_CONSTRUCT:
          settlementLayer.setWithholdTouchEventsFromChildren(true);
          streetLayer.setWithholdTouchEventsFromChildren(true);
          String conStr = intent.getStringExtra(NEW_CONSTRUCT);
          Construction construction = gson.fromJson(conStr, Construction.class);
          showConstruction(construction);
        case OK:
          hideFragment(seaTradeFragment);
          break;
        case PLAYER_WAIT:
          hideActiveElements();
        case ROLL_DICE:
          showView(diceBtn);
          break;
        case STATUS_UPD:
          updatePlayerViews();
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

  private void setClickListener() {
    diceBtn.setOnClickListener((View v) -> {
      String diceMsg = createJSONString(ROLL_DICE, new Object());
      mService.sendMessage(diceMsg);
    });
    domTradeBtn.setOnClickListener((View v) -> showFragment(domTradeFragment));
    endTurnBtn.setOnClickListener((View v) -> {
      deactivateBuildLayers();
      isItTimeToBuild = false;
      endTurn();
    });
    seaTradeBtn.setOnClickListener((View v) -> showFragment(seaTradeFragment));
  }

  private void setIntentFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(BUILD_VILLAGE);
    filter.addAction(BUILD_STREET);
    filter.addAction(BUILD_TRADE);
    filter.addAction(DICE_RESULT);
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(NEW_CONSTRUCT);
    filter.addAction(OK);
    filter.addAction(PLAYER_WAIT);
    filter.addAction(ROLL_DICE);
    filter.addAction(STATUS_UPD);
    filter.addAction(TRD_ABORTED);
    filter.addAction(TRD_FIN);
    filter.addAction(TRD_OFFER);
    LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, filter);
  }

  private int setGridDimensions(int radius) {
    // Gets the layout params that will allow to resize the layout
    ViewGroup.LayoutParams params = gridLayout.getLayoutParams();

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
        view.setHex(hex);
        if (storageMap.getObjectByCoordinate(hex.getX(), hex.getY()) instanceof Hex) {
          Hex pic = (Hex) storageMap.getObjectByCoordinate(hex.getX(), hex.getY());
          view = new CircleImageView(this, pic.type, pic.number);
          view.setHex(hex);
          view.setOnTouchListener(gridNodeTouchListener);
        } else {
          Integer pic = (Integer) storageMap.getObjectByCoordinate(hex.getX(), hex.getY());
          if(pic == null) {
            view.setImageResource(R.drawable.wheat_field);
          } else {
            view = new CircleImageView(this);
            if(pic != 0) {
              view.setImageResource(pic);
            } else {
              view.setImageResource(R.drawable.hafen_0_3);
            }
          }
        }
        addViewToLayout(view, hex, grid);
        // we dont need buildings in the ocean
        if (hex.getX() != -3 && hex.getY() != 3) {
          if (hex.getX() + hex.getY() == -3) {
            addStreetTopRight(hex, grid);
            addBuildingTop(hex, grid);
          } else if (hex.getX() == 3 && hex.getY() == -3) {
            addStreetTopLeft(hex, grid);
            addBuildingTopLeft(hex, grid);
            addBuildingTop(hex, grid);
          } else if (hex.getX() + hex.getY() == 3) {
            addBuildingTopLeft(hex, grid);
            addStreetLeft(hex, grid);
          } else if (hex.getY() == -3) {
            addStreetTopLeft(hex, grid);
            addStreetTopRight(hex, grid);
            addBuildingTopLeft(hex, grid);
            addBuildingTop(hex, grid);
          } else if (hex.getX() == 3) {
            addStreetLeft(hex, grid);
            addStreetTopLeft(hex, grid);
            addBuildingTopLeft(hex, grid);
            addBuildingTop(hex, grid);
          } else {
            addStreetLeft(hex, grid);
            addStreetTopLeft(hex, grid);
            addStreetTopRight(hex, grid);
            addBuildingTopLeft(hex, grid);
            addBuildingTop(hex, grid);
          }
        }
      }

      return grid;
    } catch (Exception e) {
      Toast.makeText(MainActivity.this, "Sorry, there was a problem initializing the application.", Toast.LENGTH_LONG).show();
      e.printStackTrace();
    }

    return null;
  }

  private void showConstruction(Construction construction) {
    Player p;
    if (storage.isItMe(construction.owner)) {
      p = self;
    } else {
      p = storage.getOpponent(construction.owner);
    }
    String viewId = construction.viewId;
    if (viewId == null) {
      viewId = BuildingView.createTag(construction.locations);
    }
    switch (construction.type) {
      case SETTLEMENT:
      case CITY:
        BuildingView v = settlementLayer.findViewWithTag(viewId);
        Drawable settleImg = createColoredBuilding(p.color, construction.type); //
        v.setImageDrawable(settleImg);
        break;
      case STREET:
        StreetView s = streetLayer.findViewWithTag(viewId);
        Drawable streetImg = createColoredStreet(p.color, s.getOrientation());
        s.setImageDrawable(streetImg);
        break;
    }
  }

  private void showFragment(MainActivityFragment f) {
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.addToBackStack(f.tag());
    if (fragmentManager.findFragmentByTag(f.tag()) == null) {
      ft.add(R.id.fragmentContainer, f, f.tag());
      ft.show(f);
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

  private void updatePlayerCards() {
    for (int i = 0; i < allPlayers.length; i++) {
      Player p = allPlayers[i];
      CardView cardView = mCardAdapter.getCardViewAt(i);
      TextView devCards = cardView.findViewById(R.id.entwicklungskarten);
      devCards.setText(String.valueOf(p.devCards.getTotalAmnt()));
      TextView resCards = cardView.findViewById(R.id.rsckarten);
      resCards.setText(String.valueOf(p.rawMaterials.getTotalAmnt()));
      TextView vpPoints = cardView.findViewById(R.id.siegpunkte);
      vpPoints.setText(String.valueOf(p.victoryPts));
      if (!p.status.equals(STATUS_WAIT)) {
        cardView.findViewById(R.id.cardContain).setBackgroundColor(Color.WHITE);
      } else {
        cardView.findViewById(R.id.cardContain)
            .setBackgroundColor(Color.parseColor("#CCCCCC"));
      }
      if (p.biggestArmy) {
        cardView.findViewById(R.id.rittericon).setVisibility(View.VISIBLE);
      } else {
        cardView.findViewById(R.id.rittericon).setVisibility(View.INVISIBLE);
      }
      if (p.longestRd) {
        cardView.findViewById(R.id.strasseicon).setVisibility(View.VISIBLE);
      } else {
        cardView.findViewById(R.id.strasseicon).setVisibility(View.INVISIBLE);
      }
    }
  }

  private void updatePlayerViews() {
    self = storage.getPlayer();
    allPlayers = storage.getAllPlayers();
    updatePlayerCards();
    updateSlidePanel();
  }

  private void updateSlidePanel() {
    RawMaterialOverview res = self.rawMaterials;
    selfClayCnt.setText(String.valueOf(res.getClayCount()));
    selfOreCnt.setText(String.valueOf(res.getOreCount()));
    selfWheatCnt.setText(String.valueOf(res.getWheatCount()));
    selfWoodCnt.setText(String.valueOf(res.getWoodCount()));
    selfWoolCnt.setText(String.valueOf(res.getWoolCount()));
    selfDevCardCnt.setText(String.valueOf(self.devCards.getTotalAmnt()));
  }
}

