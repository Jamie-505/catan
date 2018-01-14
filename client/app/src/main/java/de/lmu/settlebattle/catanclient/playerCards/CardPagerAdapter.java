package de.lmu.settlebattle.catanclient.playerCards;

import static de.lmu.settlebattle.catanclient.utils.Constants.BLUE;
import static de.lmu.settlebattle.catanclient.utils.Constants.ORANGE;
import static de.lmu.settlebattle.catanclient.utils.Constants.RED;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHEAT;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHITE;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.lmu.settlebattle.catanclient.R;
import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {

    private List<CardView> mViews;
    private List<CardItem> mData;
    private float mBaseElevation;

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(CardItem item) {
        mViews.add(null);
        mData.add(item);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
    }
    // hier passiert die magie
    private void bind(CardItem item, View view) {

        ImageView colorBar = (ImageView) view.findViewById(R.id.color_bar);
        Drawable color = null;
        switch (item.getColor()) {
          case BLUE:
            color = view.getContext().getResources().getDrawable(R.drawable.rec_blue);
            break;
          case ORANGE:
            color = view.getContext().getResources().getDrawable(R.drawable.rec_orange);
            break;
          case RED:
            color = view.getContext().getResources().getDrawable(R.drawable.rec_red);
            break;
          case WHITE:
            color = view.getContext().getResources().getDrawable(R.drawable.rec_white);
            break;
        }
        colorBar.setImageDrawable(color);
        // TextViews ersetzen
        TextView titleTextView = (TextView) view.findViewById(R.id.pName); // Name
        TextView siegpunkteView = (TextView) view.findViewById(R.id.siegpunkte);
        TextView rscKartenView = (TextView) view.findViewById(R.id.rsckarten);
        TextView ekView = (TextView) view.findViewById(R.id.entwicklungskarten);

        titleTextView.setText(item.getName());
        siegpunkteView.setText((Integer.toString(item.getSP())));
        rscKartenView.setText((Integer.toString(item.getRK())));
        ekView.setText((Integer.toString((item.getEK()))));

        // Siegermacht und Handelsmacht aktivieren
        ImageView ritterView = (ImageView) view.findViewById(R.id.rittericon);
        ImageView strassenView = (ImageView) view.findViewById(R.id.strasseicon);

        if (item.getRM()) {
          ritterView.setVisibility(View.VISIBLE);
        } else {
          ritterView.setVisibility(View.INVISIBLE);
        }

      if (item.getHS()) {
        strassenView.setVisibility(View.VISIBLE);
      } else {
        strassenView.setVisibility(View.INVISIBLE);
      }

      // Spieler am Zug aktivieren
        FrameLayout karte = (FrameLayout) view.findViewById(R.id.cardContain);

        if (item.getZug()) {
        karte.setBackgroundColor(Color.WHITE);}
        else {
            karte.setBackgroundColor(Color.parseColor("#CCCCCC"));
        }
        // Farbe zuweisen
    }

}
