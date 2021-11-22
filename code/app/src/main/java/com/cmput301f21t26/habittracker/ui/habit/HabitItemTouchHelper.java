package com.cmput301f21t26.habittracker.ui.habit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.objects.UserController;

public class HabitItemTouchHelper extends androidx.recyclerview.widget.ItemTouchHelper.Callback {
    private final ItemTouchHelperAdapter itemTouchHelperAdapter;
    private boolean draggable = true;

    private HabitController habitController;

    public HabitItemTouchHelper(ItemTouchHelperAdapter itemTouchHelperAdapter) {
        this.itemTouchHelperAdapter = itemTouchHelperAdapter;

        habitController = HabitController.getInstance();
    }

    // Handle long press in habit adapter
    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    // Don't allow swipe
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    // When habit item is released into its new position, make the background color white again
    @Override
    public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        CardView habitContentCardView = viewHolder.itemView.findViewById(R.id.habitContentCardView);
        habitContentCardView.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.white));

        // Update habitPosition for each habit in database
        habitController.updateHabitPositions();

        super.clearView(recyclerView, viewHolder);
    }

    // When we select, change background color
    @Override
    public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            assert viewHolder != null;
            CardView habitContentCardView = viewHolder.itemView.findViewById(R.id.habitContentCardView);
            habitContentCardView.setCardBackgroundColor(ContextCompat.getColor(viewHolder.itemView.getContext(), R.color.light_gray));
        }
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        final int dragFlags = draggable ? ItemTouchHelper.UP | ItemTouchHelper.DOWN : 0;
        final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /**
     * Set whether or not the habit recycler view
     * can drag a habit item.
     * @param value
     *  The {@link boolean} that determines whether or not the item is draggable
     */
    public void setDraggable(boolean value) {
        draggable = value;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        itemTouchHelperAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }
}
