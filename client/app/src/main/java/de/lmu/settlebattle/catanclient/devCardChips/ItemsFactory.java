package de.lmu.settlebattle.catanclient.devCardChips;

import android.support.v7.widget.RecyclerView;
import de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler;
import java.util.List;

public interface ItemsFactory<Item> {

    Item createRitter(String modifier);
    Item createMonopol(String modifier);
    Item createSiegpunkt(String modifier);
    Item createErfindung(String modifier);
    Item createStrassenbau(String modifier);
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(List<Item> items, OnRemoveListener onRemoveListener, DevCardHandler devCardHandler);
}
