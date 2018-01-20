package de.lmu.settlebattle.catanclient;

import android.app.Fragment;
import android.content.Context;

public abstract class MainActivityFragment extends Fragment {

  protected FragmentHandler fragHandler;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof FragmentHandler) {
      fragHandler = (FragmentHandler) context;
    } else {
      throw new RuntimeException(
          context.toString() + " must implement correct Interface");
    }
  }

  public interface FragmentHandler {
    void closeFragment(MainActivityFragment f);
    void displayFragMsg(String msg);
    void popBackstack(MainActivityFragment f);
    void sendMsgToServer(String msg);
  }

  public String tag() {
    return this.getClass().getSimpleName();
  }

}
