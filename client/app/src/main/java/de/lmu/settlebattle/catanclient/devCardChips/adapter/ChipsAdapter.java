package de.lmu.settlebattle.catanclient.devCardChips.adapter;

import static de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler.DevCardType.ERFINDUNG;
import static de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler.DevCardType.MONOPOL;
import static de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler.DevCardType.RITTER;
import static de.lmu.settlebattle.catanclient.devCardChips.adapter.ChipsAdapter.DevCardHandler.DevCardType.STRASSENBAU;
import static de.lmu.settlebattle.catanclient.utils.Constants.INVENTION;
import static de.lmu.settlebattle.catanclient.utils.Constants.KNIGHT;
import static de.lmu.settlebattle.catanclient.utils.Constants.MONOPOLE;
import static de.lmu.settlebattle.catanclient.utils.Constants.RD_CONSTR;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import de.lmu.settlebattle.catanclient.CircleTransform;
import de.lmu.settlebattle.catanclient.R;
import de.lmu.settlebattle.catanclient.devCardChips.ChipsEntity;
import de.lmu.settlebattle.catanclient.devCardChips.OnRemoveListener;
import java.util.List;

public class ChipsAdapter extends  RecyclerView.Adapter<ChipsAdapter.ViewHolder> {

  private List<ChipsEntity> chipsEntities;
  private OnRemoveListener onRemoveListener;
  private DevCardHandler devCardHandler;

  public ChipsAdapter(List<ChipsEntity> chipsEntities, OnRemoveListener onRemoveListener, DevCardHandler devCardHandler) {
    this.chipsEntities = chipsEntities;
    this.onRemoveListener = onRemoveListener;
    this.devCardHandler = devCardHandler;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chip, parent, false);
    return new ViewHolder(itemView);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.bindItem(chipsEntities.get(position));
  }

  @Override
  public int getItemCount() {
    return chipsEntities.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder {

    private TextView tvDescription;
    private ImageView ivPhoto;
    private TextView tvName;

    ViewHolder(View itemView) {
      super(itemView);
      tvDescription = itemView.findViewById(R.id.tvDescription);
      ivPhoto = itemView.findViewById(R.id.ivPhoto);
      tvName = itemView.findViewById(R.id.tvName);
    }

    void bindItem(ChipsEntity entity) {

      itemView.setTag(entity.getName());
      if (TextUtils.isEmpty(entity.getDescription())) {
        tvDescription.setVisibility(View.GONE);
      } else {
        tvDescription.setVisibility(View.VISIBLE);
        tvDescription.setText(entity.getDescription());
      }

      if (entity.getDrawableResId() != 0) {
        ivPhoto.setVisibility(View.VISIBLE);
        Glide.with(ivPhoto.getContext()).load(entity.getDrawableResId())
            .transform(new CircleTransform(ivPhoto.getContext())).into(ivPhoto);
      } else {
        ivPhoto.setVisibility(View.GONE);
      }

      tvName.setText(entity.getName());

      tvName.setOnClickListener(v -> {
        if (entity.getDescription().isEmpty()) {
          int p = getAdapterPosition();
          switch (entity.getName()) {
            case INVENTION:
              devCardHandler.playDevCard(ERFINDUNG, p);
              break;
            case KNIGHT:
              devCardHandler.playDevCard(RITTER, p);
              break;
            case MONOPOLE:
              devCardHandler.playDevCard(MONOPOL, p);
              break;
            case RD_CONSTR:
              devCardHandler.playDevCard(STRASSENBAU, p);
              break;
          }
        }
//        if (onRemoveListener != null && getAdapterPosition() != -1) {
//          onRemoveListener.onItemRemoved(getAdapterPosition());
//        }
      });
    }
  }

  public interface DevCardHandler {
    enum DevCardType {
      RITTER, ERFINDUNG, MONOPOL, STRASSENBAU
    }
    void playDevCard(DevCardType type, int position);
  }

}
