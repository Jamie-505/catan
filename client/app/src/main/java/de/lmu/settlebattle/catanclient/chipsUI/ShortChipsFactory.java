package de.lmu.settlebattle.catanclient.chipsUI;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.chipsUI.adapter.ChipsAdapter;
import de.lmu.settlebattle.catanclient.chipsLogic.ChipsEntity;

class ShortChipsFactory implements IItemsFactory<ChipsEntity> {

    @Override
    public List<ChipsEntity> getFewItems() {
        List<ChipsEntity> chipsList = new ArrayList<>();
        chipsList.add(ChipsEntity.newBuilder()
                .drawableResId(R.drawable.ek_erfinder)
                .name("Erfinder")
                .build());

        return chipsList;
    }

    @Override
    public List<ChipsEntity> getItems() {
        List<ChipsEntity> chipsEntities = getFewItems();

        List<ChipsEntity> secondPortion = getFewItems();
        Collections.reverse(secondPortion);
        chipsEntities.addAll(secondPortion);
        chipsEntities.addAll(getFewItems());
        chipsEntities.addAll(getFewItems());

        for (int i=0; i< chipsEntities.size(); i++) {
            ChipsEntity chipsEntity = chipsEntities.get(i);
            chipsEntity.setName(chipsEntity.getName() + " " + i);
        }

        return chipsEntities;
    }

    @Override
    public List<ChipsEntity> getDoubleItems() {
        List<ChipsEntity> chipsEntities = getFewItems();

        List<ChipsEntity> secondPortion = getFewItems();
        Collections.reverse(secondPortion);
        chipsEntities.addAll(secondPortion);
        return chipsEntities;
    }

    @Override
    public List<ChipsEntity> getALotOfItems() {
        List<ChipsEntity> entities = new LinkedList<>();
        for (int i=0; i < 5; i++){
            entities.addAll(getItems());
        }
        return entities;
    }

    @Override
    public List<ChipsEntity> getALotOfRandomItems() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public ChipsEntity createOneItemForPosition(int position) {
        return ChipsEntity.newBuilder()
                .name("Newbie " + position)
                .drawableResId(R.drawable.ek_monopol)
                .build();
    }

    public ChipsEntity createRitterkarteOffen (int position) {
        return ChipsEntity.newBuilder()
            .name("Ritterkarte")
            .drawableResId(R.drawable.ek_rittermacht)
            .description("(ausgespielt)")
            .build();
    }

  public ChipsEntity createRitterkarte (int position) {
    return ChipsEntity.newBuilder()
        .name("Ritterkarte")
        .drawableResId(R.drawable.ek_rittermacht)
        .build();
  }

    public ChipsEntity createMonopol (int position) {
        return ChipsEntity.newBuilder()
            .name("Monopolkarte")
            .drawableResId(R.drawable.ek_monopol)
            .build();
    }
  public ChipsEntity createSiegpunkt (int position) {
    return ChipsEntity.newBuilder()
        .name("Siegpunkt")
        .drawableResId(R.drawable.ek_siegpunkt)
        .build();
  }
  public ChipsEntity createErfinder (int position) {
    return ChipsEntity.newBuilder()
        .name("Erfinder")
        .drawableResId(R.drawable.ek_erfinder)
        .build();
  }
  public ChipsEntity createStrassenkarte (int position) {
    return ChipsEntity.newBuilder()
        .name("Strassenkarte")
        .drawableResId(R.drawable.ek_handelsmacht)
        .build();
  }



    @Override
    public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(List<ChipsEntity> chipsEntities, OnRemoveListener onRemoveListener) {
        return new ChipsAdapter(chipsEntities, onRemoveListener);
    }
}
