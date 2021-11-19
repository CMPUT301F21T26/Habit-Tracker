package com.cmput301f21t26.habittracker.ui.habitevent;

import android.content.DialogInterface;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentEditHabitEventBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditHabitEventFragment extends Fragment {

    private final String TAG = "EditHabitEventFragment";

    private FragmentEditHabitEventBinding binding;
    private NavController navController;

    private Button delBtn;
    private Button editConfirmBtn;
    private TextInputEditText commentET;
    private TextView habitEventDateFormatTV;
    private TextView habitEventTitleTV;
    private TextView habitEventLocationTV;
    private EditText habitEventCommentET;

    private ImageView habitEventImage;
    private Button habitEventChooseImageBtn;
    private ImageButton habitEventCameraBtn;

    private Habit habit;
    private HabitEvent hEvent;

    public EditHabitEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        habit = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabit();
        hEvent = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabitEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentEditHabitEventBinding.inflate(inflater, container, false);

        delBtn = binding.deleteHabitEventButton;
        editConfirmBtn = binding.confirmHabitEventButton;
        commentET = binding.habitEventCommentET;
        habitEventDateFormatTV = binding.habitEventDateFormatTV;
        habitEventTitleTV = binding.editHabitEventTitleTV;
        habitEventCommentET = binding.habitEventCommentET;
        habitEventLocationTV = binding.habitEventLocationTV;
        habitEventChooseImageBtn = binding.chooseImageButton;
        habitEventImage = binding.habitEventImage;
        habitEventCameraBtn = binding.cameraButton;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        editConfirmBtn.setOnClickListener(editConfirmOnClickListener);
        delBtn.setOnClickListener(deleteOnClickListener);
        habitEventCameraBtn.setOnClickListener(cameraBtnOnClickListener);
        habitEventImage.setOnClickListener(chooseImageOnClickListener);
        setEditHabitEventFields();
    }

    /**
     * Sets the fields of the edit habit event fragment views
     * to the proper values given by the habit event object
     */
    private void setEditHabitEventFields() {
        // Get date and set it to TextView
        String datePattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(datePattern, Locale.ROOT);
        String habitEventDateFormat = format.format(hEvent.getHabitEventDate());
        habitEventDateFormatTV.setText(habitEventDateFormat);

        // Temporary...don't know if title should be this.
        habitEventTitleTV.setText(hEvent.getTitle());

        habitEventCommentET.setText(hEvent.getComment());

        if (hEvent.getLocation() != null) {
            habitEventLocationTV.setText(hEvent.getLocation().toString());
        }

        if (hEvent.getPhotoUrl() != null) {
            // TODO set image view to the image given by habit event
        }
    }

    /**
     * Hides menu items in edit habit event fragment
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity.hideMenuItems(menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton), getActivity().findViewById(R.id.extendBottomNav));
    }

    private View.OnClickListener editConfirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String comment = commentET.getText().toString();
            // TODO get location, photograph from the user

            hEvent.setComment(comment);

            UserController.updateHabitEventInDb(hEvent, user -> {
                NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                navController.navigate(action);
            });
        }
    };

    private View.OnClickListener deleteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            // prompt user to approve
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Habit Event")
                    .setMessage("Are you sure you want to delete this habit event? The data will be lost forever.")
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // update DB
                            UserController.removeHabitEventFromDb(hEvent, cbUser -> {
                                NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                                navController.navigate(action);
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    };

    private View.OnClickListener cameraBtnOnClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        }
    };

    private View.OnClickListener chooseImageOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mGetContent.launch("image/*");  // launch file explorer

        }
    };
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    // Make sure uri not null; uri null can occur when we click to go to file explorer and press the back button without choosing an image
                    if (uri != null) {
                        //UPDATE THIS
                    }
                }
            });
}