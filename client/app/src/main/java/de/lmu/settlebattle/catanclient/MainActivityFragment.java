package de.lmu.settlebattle.catanclient;

import android.app.Fragment;

public abstract class MainActivityFragment extends Fragment {

  public interface FragmentCloser {
    void closeFragment(MainActivityFragment f);
  }

  public interface FragmentMessageDeliverer {
    void sendMsgToServer(String msg);
  }

  public interface FragmentMessageDisplayer {
    void displayMessageFromFragment(String msg);
  }

  public String tag() {
    return this.getClass().getSimpleName();
  }

}
