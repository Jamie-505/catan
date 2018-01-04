package de.lmu.settlebattle.catanclient;

import android.app.Fragment;

public abstract class MainActivityFragment extends Fragment {

  public String tag() {
    return this.getClass().getSimpleName();
  }

}
