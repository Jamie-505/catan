package de.lmu.settlebattle.catanclient.devCardChips;

import static de.lmu.settlebattle.catanclient.utils.Constants.NEW;
import static de.lmu.settlebattle.catanclient.utils.Constants.INVENTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.KNIGHT;
import static de.lmu.settlebattle.catanclient.utils.Constants.MONOPOLE;
import static de.lmu.settlebattle.catanclient.utils.Constants.PLAYED;
import static de.lmu.settlebattle.catanclient.utils.Constants.RD_CONSTR;
import static de.lmu.settlebattle.catanclient.utils.Constants.VICTORY_PT;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class DevCardFragment extends Fragment {

  private static final String EXTRA = "data";

  @BindView(R.id.dev_card_pool)
  RecyclerView devCardPool;

  private DevCardHandler devCardHandler;
  private RecyclerView.Adapter adapter;
  private List<String> positions;
  private List devCardList;
  private int newCardCnt;

  /** replace here different data sets */
  private ItemsFactory itemsFactory = new ChipsFactory();

  @RestrictTo(RestrictTo.Scope.SUBCLASSES)
  public DevCardFragment() {
    // Required empty public constructor
  }

  public static DevCardFragment newInstance() {
    return new DevCardFragment();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof DevCardHandler) {
      devCardHandler = (DevCardHandler) context;
    }
  }
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dev_cards, container, false);
  }

  @SuppressWarnings("unchecked")
  private RecyclerView.Adapter createAdapter(Bundle savedInstanceState) {

    List<String> items = new ArrayList<>();
    if (savedInstanceState != null) {
      items = savedInstanceState.getStringArrayList(EXTRA);
    }

    adapter = itemsFactory.createAdapter(items, onRemoveListener, devCardHandler);
    this.devCardList = items;

    return adapter;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    ButterKnife.bind(this, view);

    adapter = createAdapter(savedInstanceState);

    ChipsLayoutManager spanLayoutManager = ChipsLayoutManager
        .newBuilder(getContext())
        .setOrientation(ChipsLayoutManager.VERTICAL)
        .setScrollingEnabled(true)
        .build();

    devCardPool.addItemDecoration(new SpacingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.item_space),
        getResources().getDimensionPixelOffset(R.dimen.item_space)));

    positions = new LinkedList<>();
    for (int i = 0; i< devCardList.size(); i++) {
      positions.add(String.valueOf(i));
    }

    devCardPool.setLayoutManager(spanLayoutManager);
    devCardPool.getRecycledViewPool().setMaxRecycledViews(0, 10);
    devCardPool.getRecycledViewPool().setMaxRecycledViews(1, 10);
    devCardPool.setAdapter(adapter);
  }

  private OnRemoveListener onRemoveListener = new OnRemoveListener() {
    @Override
    public void onItemRemoved(int position) {
      devCardList.remove(position);
      Log.i("activity", "delete at " + position);
      adapter.notifyItemRemoved(position);
    }
  };

  @Override
  @SuppressWarnings("unchecked")
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelableArrayList(EXTRA, new ArrayList<>(devCardList));
  }

  public void activateNewCards() {
    for (int i = 0; i < newCardCnt; i++) {
      ChipsEntity chip = (ChipsEntity) devCardList.get(i);
      devCardList.remove(i);
      adapter.notifyItemRemoved(i);
      applyDevCard(chip.getName(), i, "");
    }
    newCardCnt = 0;
  }

  public void invalidateCard(int position) {
    ChipsEntity chip = (ChipsEntity) devCardList.get(position);
    devCardList.remove(position);
    adapter.notifyItemRemoved(position);
    if (KNIGHT.equals(chip.getName())) {
      applyDevCard(chip.getName(), position, PLAYED);
    }
  }

  // Methoden für die einzelnen Karten
  // Ritterkarte, Straßenbaukarte, Erfinderkarte, Monopolkarte, Siegpunkt

  public void applyDevCard(String cardType, int position, String modifier) {
   if (NEW.equals(modifier)) {
     newCardCnt++;
   }
    switch (cardType) {
      case INVENTION:
        addInvention(position, modifier);
        break;
      case KNIGHT:
        addKnight(position, modifier);
        break;
      case MONOPOLE:
        addMonopole(position, modifier);
        break;
      case RD_CONSTR:
        addRoadCon(position, modifier);
        break;
      case VICTORY_PT:
        addVP(position, modifier);
        break;
    }
  }

  private void addKnight(int position, String modifier) {
    devCardList.add(position, itemsFactory.createRitter(modifier));
    adapter.notifyItemInserted(position);
  }

  private void addMonopole(int position, String modifier) {
    devCardList.add(position, itemsFactory.createMonopol(modifier));
    adapter.notifyItemInserted(position);
  }

  private void addVP(int position, String modifier) {
    devCardList.add(position, itemsFactory.createSiegpunkt(modifier));
    Log.i("activity", "Monopol inserted at " + position);
    adapter.notifyItemInserted(position);
  }

  private void addInvention(int position, String modifier) {
    devCardList.add(position, itemsFactory.createErfindung(modifier));
    Log.i("activity", "Erfinder inserted at " + position);
    adapter.notifyItemInserted(position);
  }

  private void addRoadCon(int position, String modifier) {
    devCardList.add(position, itemsFactory.createStrassenbau(modifier));
    Log.i("activity", "Strasse inserted at " + position);
    adapter.notifyItemInserted(position);
  }
}
