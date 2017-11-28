package de.lmu.settlebattle.catanclient;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import de.lmu.settlebattle.catanclient.network.WebSocketService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class StartActivityTest {


  @Rule
  public IntentsTestRule<StartActivity> intentsTestRule =
    new IntentsTestRule<>(StartActivity.class);


  @Test
  public void launchMainActivity() {

    // Type text and then press the button.
    onView(withId(R.id.btnJoin)).perform(click());

    intended(hasComponent(WebSocketService.class.getName()));


    // Check that the text was changed.
    onView(withId(R.id.btnJoin))
        .check(matches(withText(R.string.btn_join)));

    onView(withId(R.id.btnJoin)).perform(click());

    intended(hasComponent(MainActivity.class.getName()));
  }
}
