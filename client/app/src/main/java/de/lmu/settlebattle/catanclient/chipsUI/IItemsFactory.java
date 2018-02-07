package de.lmu.settlebattle.catanclient.chipsUI;

import android.support.v7.widget.RecyclerView;

import java.util.List;

public interface IItemsFactory<Item> {

    List<Item> getFewItems();

    List<Item> getItems();

    List<Item> getDoubleItems();

    List<Item> getALotOfItems();

    List<Item> getALotOfRandomItems();

    Item createOneItemForPosition(int position);
    Item createRitterkarte(int position);
    Item createRitterkarteOffen(int position);
    Item createMonopol(int position);
    Item createSiegpunkt(int position);
    Item createErfinder(int position);
    Item createStrassenkarte(int position);
    RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(List<Item> items, OnRemoveListener onRemoveListener);
}
