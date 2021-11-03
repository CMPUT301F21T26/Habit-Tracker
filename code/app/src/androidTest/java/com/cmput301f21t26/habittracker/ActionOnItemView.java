package com.cmput301f21t26.habittracker;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static org.hamcrest.Matchers.allOf;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;

import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;

import java.util.ArrayList;
import java.util.List;

public class ActionOnItemView {

    /**
     * Performs an action on a view within the recycler view item.
     * In our case, for today's habits, if given the id of the checkbox
     * and the action click(), it will click on the checkbox.
     * Example:
     *  ViewAction itemViewAction = ActionOnItemView.actionOnItemView(withId(R.id.habitCheckbox), click());
     *         onView(withId(R.id.todayHabitRV))
     *                 .perform(RecyclerViewActions.actionOnItemAtPosition(0, itemViewAction));
     * @param matcher
     *  The matcher that matches with the given view. Type {@link Matcher<View>}
     * @param action
     *  The action to be performed on the view. Type {@link ViewAction}
     * @return
     *  Returns a ViewAction to be used with RecyclerViewActions. Type {@link ViewAction}
     */
    // Ref: https://stackoverflow.com/questions/61013026/performing-click-on-a-button-of-a-child-in-recycler-view-using-espresso
    public static ViewAction actionOnItemView(Matcher<View> matcher, ViewAction action) {

        return new ViewAction() {

            @Override public String getDescription() {
                return String.format("performing ViewAction: %s on item matching: %s", action.getDescription(), StringDescription.asString(matcher));
            }

            @Override public Matcher<View> getConstraints() {
                return allOf(withParent(isAssignableFrom(RecyclerView.class)), isDisplayed());
            }

            @Override public void perform(UiController uiController, View view) {
                List<View> results = new ArrayList<>();
                for (View v : TreeIterables.breadthFirstViewTraversal(view)) {
                    if (matcher.matches(v)) results.add(v);
                }
                if (results.isEmpty()) {
                    throw new RuntimeException(String.format("No view found %s", StringDescription.asString(matcher)));
                } else if (results.size() > 1) {
                    throw new RuntimeException(String.format("Ambiguous views found %s", StringDescription.asString(matcher)));
                }
                action.perform(uiController, results.get(0));
            }
        };
    }
}
