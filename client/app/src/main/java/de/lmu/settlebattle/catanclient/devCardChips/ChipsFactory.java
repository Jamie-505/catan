package de.lmu.settlebattle.catanclient.devCardChips;

import static de.lmu.settlebattle.catanclient.utils.Constants.INVENTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.KNIGHT;
import static de.lmu.settlebattle.catanclient.utils.Constants.MONOPOLE;
import static de.lmu.settlebattle.catanclient.utils.Constants.RD_CONSTR;
import static de.lmu.settlebattle.catanclient.utils.Constants.VICTORY_PT;

import android.support.v7.widget.RecyclerView;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter;
import de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler;
import java.util.List;

class ChipsFactory implements ItemsFactory<ChipsEntity> {

  public ChipsEntity createRitter(String modifier) {
    return ChipsEntity.newBuilder()
        .name(KNIGHT)
        .description(modifier)
        .drawableResId(R.drawable.rittermacht)
        .build();
  }

  public ChipsEntity createMonopol (String modifier) {
    return ChipsEntity.newBuilder()
        .name(MONOPOLE)
        .description(modifier)
        .drawableResId(R.drawable.ek_monopol)
        .build();
  }
  public ChipsEntity createSiegpunkt (String modifier) {
    return ChipsEntity.newBuilder()
        .name(VICTORY_PT)
        .description(modifier)
        .drawableResId(R.drawable.ek_siegpunkt)
        .build();
  }

  public ChipsEntity createErfindung(String modifier) {
    return ChipsEntity.newBuilder()
        .name(INVENTION)
        .description(modifier)
        .drawableResId(R.drawable.ek_erfinder)
        .build();
  }

  public ChipsEntity createStrassenbau(String modifier) {
    return ChipsEntity.newBuilder()
        .name(RD_CONSTR)
        .description(modifier)
        .drawableResId(R.drawable.handelsmacht)
        .build();
  }

  @Override
  public RecyclerView.Adapter<? extends RecyclerView.ViewHolder> createAdapter(
      List<ChipsEntity> chipsEntities, OnRemoveListener onRemoveListener, DevCardHandler devCardHandler) {
    return new ChipsAdapter(chipsEntities, onRemoveListener, devCardHandler);
  }
}
