package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.grid.building.BuildingView.createTag;
import static de.lmu.settlebattle.catanclient.utils.Constants.BOARD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_CITY;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_SETTLEMENT;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_STREET;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_TRADE;
import static de.lmu.settlebattle.catanclient.utils.Constants.BUILD_VILLAGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.CARD_BUY;
import static de.lmu.settlebattle.catanclient.utils.Constants.CARD_KNIGHT;
import static de.lmu.settlebattle.catanclient.utils.Constants.CARD_RD_CON;
import static de.lmu.settlebattle.catanclient.utils.Constants.CHAT_IN;
import static de.lmu.settlebattle.catanclient.utils.Constants.COSTS;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_RESULT;
import static de.lmu.settlebattle.catanclient.utils.Constants.DICE_THROW;
import static de.lmu.settlebattle.catanclient.utils.Constants.DISPLAY_ERROR;
import static de.lmu.settlebattle.catanclient.utils.Constants.END_TURN;
import static de.lmu.settlebattle.catanclient.utils.Constants.ERROR_MSG;
import static de.lmu.settlebattle.catanclient.utils.Constants.HARVEST;
import static de.lmu.settlebattle.catanclient.utils.Constants.LOCATION;
import static de.lmu.settlebattle.catanclient.utils.Constants.NEW_CONSTRUCT;
import static de.lmu.settlebattle.catanclient.utils.Constants.OK;
import static de.lmu.settlebattle.catanclient.utils.Constants.OWNER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYER_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.RAW_MATERIALS;
import static de.lmu.settlebattle.catanclient.utils.Constants.RD_CON1;
import static de.lmu.settlebattle.catanclient.utils.Constants.RD_CON2;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROBBER_TO;
import static de.lmu.settlebattle.catanclient.utils.Constants.ROLL_DICE;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_UPD;
import static de.lmu.settlebattle.catanclient.utils.Constants.STATUS_WAIT;
import static de.lmu.settlebattle.catanclient.utils.Constants.TOSS_CARDS;
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
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
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
import android.widget.TextSwitcher;
import android.widget.TextView;
import com.google.gson.Gson;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;
import com.sdsmdg.harjot.vectormaster.models.PathModel;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import de.lmu.settlebattle.catanclient.MainActivityFragment.FragmentHandler;
import de.lmu.settlebattle.catanclient.chat.ChatFragment;
import de.lmu.settlebattle.catanclient.chat.ChatMessage;
import de.lmu.settlebattle.catanclient.devCards.InventionFragment;
import de.lmu.settlebattle.catanclient.devCards.MonopoleFragment;
import de.lmu.settlebattle.catanclient.dice.Dice;
import de.lmu.settlebattle.catanclient.dice.DiceFragment;
import de.lmu.settlebattle.catanclient.grid.Board;
import de.lmu.settlebattle.catanclient.grid.CircleImageView;
import de.lmu.settlebattle.catanclient.grid.Construction;
import de.lmu.settlebattle.catanclient.grid.Construction.ConstructionType;
import de.lmu.settlebattle.catanclient.grid.ConstructionsLayer;
import de.lmu.settlebattle.catanclient.grid.Cube;
import de.lmu.settlebattle.catanclient.grid.Grid;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.Location;
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
import de.lmu.settlebattle.catanclient.robber.Robber;
import de.lmu.settlebattle.catanclient.robber.RobberFragment;
import de.lmu.settlebattle.catanclient.robber.TossCardsFragment;
import de.lmu.settlebattle.catanclient.trade.DomTradeFragment;
import de.lmu.settlebattle.catanclient.trade.SeaTradeFragment;
import de.lmu.settlebattle.catanclient.trade.Trade;
import de.lmu.settlebattle.catanclient.trade.TradeOfferFragment;
import de.lmu.settlebattle.catanclient.utils.Harvest;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import de.lmu.settlebattle.catanclient.chipsUI.ItemsFragment;
import com.beloo.widget.chipslayoutmanager.BuildConfig;
import butterknife.BindView;
import butterknife.ButterKnife;



public class MainActivity extends BaseSocketActivity implements FragmentHandler {

  // LogCat tag
  private static final String TAG = MainActivity.class.getSimpleName();

  private boolean enableRobber;
  private boolean isItTimeToBuild;
  private boolean viaKnightCard;
  private Player self;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      reactToIntent(intent);
    }
  };

  // Visual Elements
  private Button cancelRdConBtn;
  private Button domTradeBtn;
  private Button endTurnBtn;
  private Button seaTradeBtn;
  private ChatFragment chatFragment;
  private CircleImageView currentRobberTile;
  private ConstructionsLayer settlementLayer;
  private ConstructionsLayer streetLayer;
  private DrawerLayout drawer;
  private FragmentManager fragmentManager = getFragmentManager();
  private Gson gson = new Gson();
  private ImageButton drawerHandleBtn;
  private ImageButton diceBtn;
  private LinearLayout chatDrawer;
  private RelativeLayout gridLayout;
  private SeaTradeFragment seaTradeFragment = new SeaTradeFragment();
  private SlidingUpPanelLayout slidingPanel;
  private TextSwitcher infoBox;
  private TextSwitcher selfClayCnt;
  private TextSwitcher selfDevCardCnt;
  private TextSwitcher selfOreCnt;
  private TextSwitcher selfWheatCnt;
  private TextSwitcher selfWoodCnt;
  private TextSwitcher selfWoolCnt;
  private TextSwitcher[] textSwitchers;
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
    if (drawer.isDrawerOpen(chatDrawer)) {
      drawer.closeDrawer(Gravity.END);
    }
    super.onBackPressed();
  }
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    self = Storage.getSelf();
    setIntentFilters();


    //StartFragment for Cards
    ButterKnife.bind(this);
    getSupportFragmentManager().beginTransaction()
        .add(R.id.fragmentContainer2, ItemsFragment.newInstance())
        .commit();


    Intent startActivity = getIntent();
    // I know weird but I need to get the fields from the message somehow to init the board
    Board board = gson.fromJson(startActivity.getStringExtra(BOARD), Board.class);
    board = new Board(board.fields);

    cancelRdConBtn = findViewById(R.id.end_rd_con_btn);
    chatDrawer = findViewById(R.id.chatDrawer);
    diceBtn = findViewById(R.id.throwDiceBtn);
    domTradeBtn = findViewById(R.id.dom_trade_btn);
    drawer = findViewById(R.id.drawer_layout);
    drawerHandleBtn = findViewById(R.id.open_chat_btn);
    endTurnBtn = findViewById(R.id.end_turn_btn);
    gridLayout = findViewById(R.id.gridLayout);
    infoBox = findViewById(R.id.info_box);
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

    textSwitchers = new TextSwitcher[] { selfWoodCnt, selfClayCnt, selfWoolCnt,
            selfWheatCnt, selfOreCnt };

    for (TextSwitcher tS : textSwitchers) {
      tS.setInAnimation(this, android.R.anim.slide_in_left);
      tS.setOutAnimation(this, android.R.anim.slide_out_right);
    }
    selfDevCardCnt.setInAnimation(this, android.R.anim.slide_in_left);
    selfDevCardCnt.setOutAnimation(this, android.R.anim.slide_out_right);

    setOnClickListener();

    int radius = 3;

    Bundle extras = getIntent().getExtras();
    if (extras != null) {
      radius = extras.getInt("GRID_RADIUS", 3);
    }

    initializePlayerCards(Storage.getAllPlayers());

    initGridView(radius, board.fields);
    currentRobberTile = findViewByLoc(new Location(0, 0));
    disableClickLayers();

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
            disableClickLayers();
            enableConstructionLayer(streetLayer);
            break;
          case BUILD_SETTLEMENT:
          case BUILD_CITY:
            disableClickLayers();
            enableConstructionLayer(settlementLayer);
            break;
          case CARD_BUY:
            disableClickLayers();
            mService.sendMessage(createJSONString(CARD_BUY, null));
            break;
        }
        slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        displaySnackBar(selectedItem, null);
      } else {
        displaySnackBar("Du kannst gerade nicht bauen, evtl musst du erst Würfeln oder auf deinen Zug warten", null);
      }
    });

    LinearLayout dragPanel = findViewById(R.id.dragView);
    int colorId = getResources().getIdentifier(self.color.toLowerCase(),
        "color", getPackageName());
    dragPanel.setBackgroundColor(getResources().getColor(colorId));
    int colorIdDark = getResources().getIdentifier(self.color.toLowerCase() + "_dark",
        "color", getPackageName());
    infoBox.setBackgroundColor(getResources().getColor(colorIdDark));
    infoBox.setInAnimation(this, android.R.anim.slide_in_left);
    infoBox.setOutAnimation(this, android.R.anim.slide_out_right);
    slidingPanel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View panel, float slideOffset) {
        Log.i(TAG, "onPanelSlide, offset " + slideOffset);
      }

      @Override
      public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState,
          SlidingUpPanelLayout.PanelState newState) {}
    });
    slidingPanel.setFadeOnClickListener(
        view -> slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED));
    slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

    chatFragment = (ChatFragment) fragmentManager.findFragmentById(R.id.chat);
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

  private void disableClickLayers() {
    settlementLayer.setWithholdTouchEventsFromChildren(true);
    streetLayer.setWithholdTouchEventsFromChildren(true);
  }

  private void enableConstructionLayer(ConstructionsLayer layer) {
    if (layer == settlementLayer) {
      streetLayer.setWithholdTouchEventsFromChildren(true);
      settlementLayer.setWithholdTouchEventsFromChildren(false);
    } else if (layer == streetLayer) {
      settlementLayer.setWithholdTouchEventsFromChildren(true);
      streetLayer.setWithholdTouchEventsFromChildren(false);
    }
  }

  private void endTurn() {
    hideActiveElements();
    mService.sendMessage(createJSONString(END_TURN, null));
  }

  private CircleImageView findViewByLoc(Location l) {
   return gridLayout.findViewWithTag(createTag(l));
  }

  private void hideActiveElements() {
    seaTradeBtn.setVisibility(View.INVISIBLE);
    domTradeBtn.setVisibility(View.INVISIBLE);
    endTurnBtn.setVisibility(View.INVISIBLE);
    diceBtn.setVisibility(View.INVISIBLE);
  }

  private void hideFragment(Fragment fragment) {
    if (fragment != null) {
      FragmentTransaction ft = fragmentManager.beginTransaction();
      ft.hide(fragment);
      ft.commit();
    }
  }

  private void initGridView(int radius, Hex[] fields) {
    int scale = setGridDimensions(radius);

    //Init node elements
    setGridNodes(radius, scale, fields);
  }

  private void initializePlayerCards(Player[] players) {
    mCardAdapter = new CardPagerAdapter();
    mViewPager = findViewById(R.id.viewPager);

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

  private boolean moveRobber(Location newRobberLoc) {
    if (currentRobberTile.getHex().getLocation() == newRobberLoc) {
      displaySnackBar("Der Räuber braucht ein neues Zuhause!", null);
      return false;
    } else {
      currentRobberTile.showRobber(false);
      currentRobberTile = findViewByLoc(newRobberLoc);
      currentRobberTile.showRobber(true);
      return true;
    }
  }

  private void OnGridHexClick(CircleImageView cV) {
    if (enableRobber) {
      if(moveRobber(cV.getHex().getLocation())) {
        if (cV.getOwners().size() > 1) {
          Bundle robberBundle = new Bundle();
          robberBundle.putIntegerArrayList(OWNER, cV.getOwners());
          robberBundle.putString(LOCATION, gson.toJson(
              currentRobberTile.getHex().getLocation()));
          RobberFragment robberFragment = new RobberFragment();
          robberFragment.setArguments(robberBundle);
          enableRobber = false;
          showFragmentNoBackstack(robberFragment);
        } else if (cV.getOwners().size() == 1) {
          sendRobberMsg(cV.getOwners().get(0));
        } else {
          sendRobberMsg(null);
        }
      }
    }
  }

  /**
   * Plays device's default notification sound
   * */
  public void playBeep() {
    try {
      Uri notification = RingtoneManager
          .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
      Ringtone r = RingtoneManager.getRingtone(this,
          notification);
      r.play();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void playInventionCard() {
    hideActiveElements();
    setStatus("Monopolkarte gespielt: Wähle einen Rohstoff!");
    showFragmentNoBackstack(new InventionFragment());
  }

  private void playKnightCard() {
    viaKnightCard = true;
    enableRobber = true;
    hideActiveElements();
    setStatus("Ritterkarte gespielt: Versetze den Räuber!");
  }

  private void playMonoCard() {
    hideActiveElements();
    setStatus("Monopolkarte gespielt: Wähle einen Rohstoff!");
    showFragmentNoBackstack(new MonopoleFragment());
  }

  private void playRdConCard() {
    String msg = createJSONString(CARD_RD_CON, null);
    mService.sendMessage(msg);
  }

  private void reactToIntent(Intent intent) {
    String action = intent.getAction();
    if (action != null) {
      switch (action) {
        case BUILD_VILLAGE:
          mViewPager.setCurrentItem(0, true);
          mCardAdapter.getCardViewAt(0).findViewById(R.id.cardContain).setBackgroundColor(Color.WHITE);
          setStatus("Du kannst jetzt eine Siedlung bauen");
          enableConstructionLayer(settlementLayer);
          break;
        case BUILD_STREET:
          mViewPager.setCurrentItem(0, true);
          mCardAdapter.getCardViewAt(0).findViewById(R.id.cardContain).setBackgroundColor(Color.WHITE);
          setStatus("Du kannst jetzt eine Staße bauen");
          enableConstructionLayer(streetLayer);
          break;
        case BUILD_TRADE:
          updatePlayerViews();
          Player p = gson.fromJson(intent.getStringExtra(PLAYER), Player.class);
          if (Storage.isItMe(p.id)) {
            // needed because of double status updates from the server
            enableRobber = false;
            isItTimeToBuild = true;
            setStatus("Du kannst jetzt bauen und handeln!");
            cancelRdConBtn.setVisibility(View.GONE);
            diceBtn.setVisibility(View.INVISIBLE);
            domTradeBtn.setVisibility(View.VISIBLE);
            seaTradeBtn.setVisibility(View.VISIBLE);
            endTurnBtn.setVisibility(View.VISIBLE);
          }
          break;
        case CHAT_IN:
          ChatMessage cMsg = gson.fromJson(
              intent.getStringExtra(CHAT_IN), ChatMessage.class);
          chatFragment.appendMessage(cMsg);
          p = Storage.getPlayer(cMsg.getSenderId());
          if (!Storage.isItMe(cMsg.getSenderId())) {
            playBeep();
            String text = String.format(Locale.GERMAN,
               "Neue Chatnachricht von %s erhalten", p.name);
            displaySnackBar(text, cMsg.getSenderId());
          }
          break;
        case COSTS:
          Harvest costs = gson.fromJson(intent.getStringExtra(COSTS), Harvest.class);
          if (Storage.isItMe(costs.getPlayerId())) {
            String update = "Dir wurden " + costs.getRawMaterials() + " abgezogen!";
            displaySnackBar(update, costs.getPlayerId());
          }
          break;
        case DICE_RESULT:
          Dice dice = gson.fromJson(intent.getStringExtra(DICE_THROW), Dice.class);
          if (Storage.isItMe(dice.getPlayerId())) {
            Bundle diceBundle = new Bundle();
            diceBundle.putString(DICE_THROW, intent.getStringExtra(DICE_THROW));
            DiceFragment diceFragment = new DiceFragment();
            diceFragment.setArguments(diceBundle);
            showFragmentViaBackstack(diceFragment);
          } else {
            String name = Storage.getPlayer(dice.getPlayerId()).name;
            int sum = dice.getSum();
            String text = String.format(
                Locale.GERMAN, "%s hat eine %d gewürfelt", name, sum);
            displaySnackBar(text, dice.getPlayerId());
          }
          break;
        case DISPLAY_ERROR:
          displaySnackBar(intent.getStringExtra(ERROR_MSG), null);
          break;
        case HARVEST:
          Harvest harvest = gson.fromJson(intent.getStringExtra(HARVEST), Harvest.class);
          if (Storage.isItMe(harvest.getPlayerId())) {
            String update = "Du hast " + harvest.getRawMaterials() + " erhalten!";
            displaySnackBar(update, harvest.getPlayerId());
          }
          break;
        case NEW_CONSTRUCT:
          disableClickLayers();
          String conStr = intent.getStringExtra(NEW_CONSTRUCT);
          Construction construction = gson.fromJson(conStr, Construction.class);
          showConstruction(construction);
          break;
        case OK:
          hideFragment(seaTradeFragment);
          break;
        case PLAYER_WAIT:
          setStatus("Ein anderer Spieler ist am Zug");
          hideActiveElements();
          updatePlayerViews();
          break;
        case RD_CON1:
          hideActiveElements();
          setStatus("Baue deine 1. Strasse");
          showView(cancelRdConBtn);
          enableConstructionLayer(streetLayer);
          break;
        case RD_CON2:
          setStatus("Baue deine 2. Strasse");
          enableConstructionLayer(streetLayer);
          cancelRdConBtn.setText(R.string.end_rd_con);
          showView(cancelRdConBtn);
          break;
        case ROBBER:
          Robber robber = gson.fromJson(intent.getStringExtra(ROBBER), Robber.class);
          moveRobber(robber.location);
          break;
        case ROBBER_TO:
          enableRobber = true;
          setStatus("Setze den Räuber an eine neue Stelle");
          break;
        case ROLL_DICE:
          setStatus("Lass die Würfel rollen!");
          showView(diceBtn);
          mViewPager.setCurrentItem(0, true);
          mCardAdapter.getCardViewAt(0).findViewById(R.id.cardContain).setBackgroundColor(Color.WHITE);
        case STATUS_UPD:
          updatePlayerViews();
          break;
        case TOSS_CARDS:
          Bundle tossCardBundle = new Bundle();
          String rawMaterials = gson.toJson(self.rawMaterials);
          tossCardBundle.putString(RAW_MATERIALS, rawMaterials);
          TossCardsFragment f = new TossCardsFragment();
          f.setArguments(tossCardBundle);
          showFragmentNoBackstack(f);
          break;
        case TRD_FIN:
          if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
          }
          Trade t = gson.fromJson(intent.getStringExtra(TRADE), Trade.class);
          showTradeSummary(t);
          break;
        case TRD_OFFER:
          Bundle tradeBundle = new Bundle();
          tradeBundle.putString(TRADE, intent.getStringExtra(TRADE));
          tradeOfferFragment.setArguments(tradeBundle);
          showFragmentViaBackstack(tradeOfferFragment);
          break;
      }
      updatePlayerViews();
    } else {
      Log.e(TAG, "Received intend had no action");
    }
  }

  public void sendRobberMsg(Integer id) {
    Robber r = new Robber(currentRobberTile.getHex().getLocation(), id);
    if (viaKnightCard) {
      mService.sendMessage(createJSONString(CARD_KNIGHT, r));
    } else {
      mService.sendMessage(createJSONString(ROBBER_TO, r));
    }
    viaKnightCard = false;
  }

  private void setOnClickListener() {
    cancelRdConBtn.setOnClickListener((View v) -> {
      String msg = createJSONString(BUILD, null);
      mService.sendMessage(msg);
    });
    diceBtn.setOnClickListener((View v) -> {
      diceBtn.setVisibility(View.INVISIBLE);
      String diceMsg = createJSONString(ROLL_DICE, new Object());
      mService.sendMessage(diceMsg);
    });
    drawerHandleBtn.setOnClickListener((View v) -> drawer.openDrawer(chatDrawer));
    domTradeBtn.setOnClickListener((View v) -> {
      Bundle tradeBundle = new Bundle();
      String rawMaterials = gson.toJson(self.rawMaterials);
      tradeBundle.putString(RAW_MATERIALS, rawMaterials);
      DomTradeFragment f = new DomTradeFragment();
      f.setArguments(tradeBundle);
      showFragmentNoBackstack(f);
    });
    endTurnBtn.setOnClickListener((View v) -> {
      disableClickLayers();
      isItTimeToBuild = false;
      infoBox.setText("Ein anderer Spieler ist am Zug");
      endTurn();
    });
    seaTradeBtn.setOnClickListener((View v) -> showFragmentViaBackstack(seaTradeFragment));
  }

  private void setIntentFilters() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(BUILD_VILLAGE);
    filter.addAction(BUILD_STREET);
    filter.addAction(BUILD_TRADE);
    filter.addAction(CHAT_IN);
    filter.addAction(COSTS);
    filter.addAction(DICE_RESULT);
    filter.addAction(DISPLAY_ERROR);
    filter.addAction(HARVEST);
    filter.addAction(NEW_CONSTRUCT);
    filter.addAction(OK);
    filter.addAction(PLAYER_WAIT);
    filter.addAction(RD_CON1);
    filter.addAction(RD_CON2);
    filter.addAction(ROBBER);
    filter.addAction(ROBBER_TO);
    filter.addAction(ROLL_DICE);
    filter.addAction(STATUS_UPD);
    filter.addAction(TOSS_CARDS);
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
      View.OnTouchListener gridNodeTouchListener = (v, event) -> {
//        v.performClick();
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            float xPoint = event.getX();
            float yPoint = event.getY();
            boolean isPointOutOfCircle = (grid.centerOffsetX -xPoint)*(grid.centerOffsetX -xPoint)
                + (grid.centerOffsetY -yPoint)*(grid.centerOffsetY -yPoint) > grid.width * grid.width / 4;

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
            OnGridHexClick((CircleImageView) v);
            break;
        }
        return true;
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
            view.setImageResource(R.drawable.desert_field);
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
      displaySnackBar("Sorry, there was a problem initializing the application.", null);
      e.printStackTrace();
    }

    return null;
  }

  private void setStatus(String status) {
    infoBox.setText(status);
  }

  private void showConstruction(Construction construction) {
    Player p;
    p = Storage.getPlayer(construction.owner);
    String viewId = construction.viewId;
    if (viewId == null) {
      viewId = createTag(construction.locations);
    }
    switch (construction.type) {
      case CITY:
        BuildingView bV = settlementLayer.findViewWithTag(viewId);
        bV.setClickable(false);
      case SETTLEMENT:
        bV = settlementLayer.findViewWithTag(viewId);
        Drawable buildingImg = createColoredBuilding(p.color, construction.type);
        bV.setImageDrawable(buildingImg);
        bV.setType(ConstructionType.CITY);
        // only need opponents as owners
        if (!Storage.isItMe(construction.owner)){
          updateHexOwners(viewId, construction.owner);
        }
        break;
      case STREET:
        StreetView s = streetLayer.findViewWithTag(viewId);
        Drawable streetImg = createColoredStreet(p.color, s.getOrientation());
        s.setImageDrawable(streetImg);
        break;
    }
  }

  private void showFragmentNoBackstack(MainActivityFragment f) {
    FragmentTransaction ft = fragmentManager.beginTransaction();
    ft.add(R.id.fragmentContainer, f, f.tag());
    ft.show(f);
    ft.commit();
  }

  private void showFragmentViaBackstack(MainActivityFragment f) {
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
    Player p1 = Storage.getPlayer(t.player);
    Player p2 = Storage.getPlayer(t.opponent);
    String msg = String.format("Handel zwischen %s und %s wurde abgeschlossen",
        p1.name, p2.name);
    displaySnackBar(msg, null);
  }

  private void showView(View v) {
    v.setVisibility(View.VISIBLE);
  }

  private void updateHexOwners(String viewId, int id) {
    Matcher m = Pattern.compile("-?\\d-?\\d").matcher(viewId);
    while (m.find()) {
      CircleImageView v = gridLayout.findViewWithTag(m.group());
      if (v != null) v.addOwner(id);
    }
  }

  private void updatePlayerCards() {
    Player[] allPlayers = Storage.getAllPlayers();
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
        mViewPager.setCurrentItem(i, true);
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
    updatePlayerCards();
    updateSlidePanel();
  }

  private void updateSlidePanel() {
    self = Storage.getSelf();
    RawMaterialOverview res = self.rawMaterials;
    int[] qnts = res.getQnts();
    for (int i = 0; i < textSwitchers.length; i++) {
      TextView textView = (TextView) textSwitchers[i].getCurrentView();
      String current = textView.getText().toString();
      String update = String.valueOf(qnts[i]);
      if (!current.equals(update)) {
        textSwitchers[i].setText(update);
      }
    }
    TextView devCard = (TextView) selfDevCardCnt.getCurrentView();
    String current = devCard.getText().toString();
    String update = String.valueOf(self.devCards.getTotalAmnt());
    if (!current.equals(update)) {
      selfDevCardCnt.setText(update);
    }
  }

// --------------------- interface methods ---------------------------------
  @Override
  public void closeFragment(MainActivityFragment f) {
    hideFragment(f);
  }
  @Override
  public void displayFragMsg(String msg) {
    displaySnackBar(msg, self.id);
  }

  @Override
  public void popBackstack(MainActivityFragment f) {
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    }
  }

  @Override
  public void sendMsgToServer(String msg) {
    mService.sendMessage(msg);
  }

  @Override
  public void sendRobberMsgToServer(Integer id) { sendRobberMsg(id); }
}
