package com.cmput301f21t26.habittracker.ui.habitevent;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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

import androidx.activity.result.ActivityResult;
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

import com.bumptech.glide.Glide;
import com.cmput301f21t26.habittracker.objects.HabitEventController;
import com.cmput301f21t26.habittracker.ui.MainActivity;
import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.databinding.FragmentEditHabitEventBinding;
import com.cmput301f21t26.habittracker.objects.Habit;
import com.cmput301f21t26.habittracker.objects.HabitEvent;
import com.cmput301f21t26.habittracker.objects.UserController;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EditHabitEventFragment extends Fragment {

    private final String TAG = "EditHabitEventFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private Uri uri;
    private String picturePath;
    private String dbImageUrl;

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

    private HabitEventController habitEventController;

    public EditHabitEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        habit = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabit();
        hEvent = EditHabitEventFragmentArgs.fromBundle(getArguments()).getHabitEvent();

        habitEventController = HabitEventController.getInstance();
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
        habitEventCameraBtn = (ImageButton) binding.cameraButton;
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        editConfirmBtn.setOnClickListener(editConfirmOnClickListener);
        delBtn.setOnClickListener(deleteOnClickListener);
        habitEventCameraBtn.setOnClickListener(cameraBtnOnClickListener);
        habitEventChooseImageBtn.setOnClickListener(chooseImageOnClickListener);
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
            if (getActivity() != null) {
                Glide.with(getActivity())
                        .load(hEvent.getPhotoUrl())
                        .placeholder(R.drawable.default_image)
                        .into(habitEventImage);
            }
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

    /**
     * listener to handle clicks on edit button in edit habit event, takes current information that is available inside the
     * edit habit event fragment and updates it in the database
     */
    private View.OnClickListener editConfirmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String comment = commentET.getText().toString();
            // TODO get location, photograph from the user

            hEvent.setComment(comment);

            habitEventController.updateHabitEventInDb(hEvent, user -> {
                NavDirections action = MobileNavigationDirections.actionGlobalNavigationTimeline(null);
                navController.navigate(action);
            });
        }
    };

    /**
     * listener to handle clicks on delete button in edit habit event, creates alert dialog that prompts the user to either continue
     * and delete the habit event, or to cancel their deletion
     */
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
                            habitEventController.removeHabitEventFromDb(hEvent, cbUser -> {
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

    /**
     * responds to clicks on camera button by launching camera to capture a photo
     *
     */
    private View.OnClickListener cameraBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mStartForResult.launch(takePictureIntent);
        }
    };


    /**
     * sets the photo from the camera to the image view of the habit event, and
     * updates the photoUrl parameter of the respective habit event
     *
     */
    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent takePictureIntent = result.getData();
                    // Handle the intent
                    Bitmap captureImage = (Bitmap) takePictureIntent.getExtras().get("data");
                    habitEventImage.setImageBitmap(captureImage);

                    uri = getImageUri(getContext(), captureImage);
                    picturePath = "eventPictures/" + uri.hashCode() + ".jpeg";
                    habitEventController.updateHabitEventImageInDb(hEvent, picturePath, uri, user -> {

                    });
                }
            }
        });


        public Uri getImageUri(Context inContext, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);

            return Uri.parse(path);
        }

    /** responds to clicks on the choose image button by launching the users file explorer to select an image
     *
     */
    private View.OnClickListener chooseImageOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mGetContent.launch("image/*");  // launch file explorer
            }
        };
    /**
     * sets the image chosen from the users library to the image view of the habit event, and updates the
     * PhotoUrl parameter of the respective habit event
     */
    ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if (uri != null) {
                            picturePath = "eventPictures/" + uri.hashCode() + ".jpeg";
                            habitEventController.updateHabitEventImageInDb(hEvent, picturePath, uri, user -> {
                                // TODO refactor: set image right after the user chooses the image
                                if (getActivity() != null) {
                                    Glide.with(getActivity())
                                            .load(hEvent.getPhotoUrl())
                                            .placeholder(R.drawable.default_image)
                                            .into(habitEventImage);
                                }
                            });

                        }
                    }
                });
}

