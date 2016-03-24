package com.example.android.weatherbeprepared;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * Define integer arrays to hold the button and the corresponding layout id's
     */
    private Integer[] buttonIds = { R.id.butt_where, R.id.butt_term, R.id.butt_plan,
                                    R.id.butt_source, R.id.butt_real };
    private Integer[] layoutIds = { R.id.layout_where, R.id.layout_term,
                                    R.id.layout_plan,  R.id.layout_source,
                                    R.id.layout_real };
    // Array below identifies the method to call when the visibility on the layout changes, it is
    // trigger in the onClick for the button.
    private String[] method2Call ={"", "", "", "", "playRealVideo"};

    // Needed to dynamically add checkbox view to planLinearLayout object
    LinearLayout planLinearLayout;

    // The videoview object, managed in the playSourceVideo method
    VideoView mRealVideoView;

    // We'll get the checkbox data associated with the plan layout by inspecting the string
    // resources... that way it's completely data driven, just add string resources (of a particular
    // naming convention) and the app will add them.
    private static int viewsCount = 0;
    private static int planButtonsChecked = 0;
    private List<View> planCheckBoxViews = new ArrayList<View>();
    private List<Integer> planCheckBoxIds = new ArrayList<Integer>();
    private List<String> planCheckBoxText = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the object for the plan linear layout
        planLinearLayout = (LinearLayout) findViewById(R.id.layout_plan);

        // Get all the resources defined as string, we want the one's named planCB* they
        //   are the plan checkboxes
        Field[] idFlds =  R.string.class.getFields();
        for (int i = 0; i < idFlds.length; i++) {
            String theName = idFlds[i].getName();
            if ( theName.length() > 6 ) {
                if (theName.substring(0, 6).equalsIgnoreCase("planCB")) {
                    // Get id
                    int strId = getResources().getIdentifier(theName, "string", getPackageName());

                    // Add id and name to array lists
                    planCheckBoxIds.add(strId);
                    planCheckBoxText.add(theName);
                }
            }
        }

        for (int i = 0; i < planCheckBoxIds.size(); i++) {
            createCheckBox(planLinearLayout, planCheckBoxIds.get(i), ++viewsCount);
        }
    }

    /**
     * This handles the click for the main button's in the application (the ones for each section)
     * When it is clicked it'll toggle the associated view as visible or gone, it'll also invoke
     * any method that should be called when the visibility changes.
     *
     * @param v The view associated with the button click
     */
    public void buttonClicked(View v) {
        // Get button id of the one selected
        Integer currButton = v.getId();

        // Define variables/object needed in the loop
        int currVisibility, newVisibility;
        LinearLayout ll;

        // Variables for the method call... we call the method associated with a view when it's
        // visibility changes.  We pass in a boolean identifying it's visibility
        String theMethod2Call = "";
        Class noparms[] = {};  // Don't need but left here to see how you'd define no parameters
        Class oneParm[] = new Class[1];
        oneParm[0] = boolean.class;

        for (int i = 0; i < buttonIds.length; i++) {
            // Get Linear layout object associated with the button
            ll = (LinearLayout) findViewById(layoutIds[i]);
            currVisibility = ll.getVisibility();  // Get it's current visibility
            theMethod2Call = method2Call[i];      // The method to call
            if ( buttonIds[i].compareTo(currButton) == 0 ) {
              // It's the button pushed, toggle the new visibility based on prior value
              newVisibility = (currVisibility == View.GONE ? View.VISIBLE: View.GONE);
            } else {
              newVisibility = View.GONE;
            }
            // If visibility has changed then reset it and call the method associated with it.
            if (currVisibility != newVisibility) {
                ll.setVisibility(newVisibility);
                if (theMethod2Call.length() > 0) {
                    try {
                        // Get the method to be invoked
                        Method meth = this.getClass().getDeclaredMethod(theMethod2Call,oneParm);

                        // Set visibility indicator
                        boolean isVisible = (newVisibility == View.VISIBLE ? true : false);

                        // Invoke the method in 'this' object, pass visibility arg
                        meth.invoke(this, isVisible);

                        Log.i("buttonClicked ",theMethod2Call + isVisible);
                    }
                    catch (Exception exc) {
                        Log.i("onClick", " " + exc.getMessage()); }
                }
            }
        }
    }

    /**
     * Add a checkbox to the layout passed in, for the checkbox id also passed in
     *
     * @param ll identifies the layout to add the checkbox to
     * @param cbStringId is the checkbox string id
     * @param newId is the id of the new checkbox create
     */
    public void createCheckBox(LinearLayout ll, int cbStringId, int newId) {
        // Get the text associated with the checkbox id
        String cbStringText = getString(cbStringId);
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setId(newId);
        checkBox.setText(cbStringText);
        checkBox.setTextSize(10);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              // Any code to handle the click event... may need down the road
              CheckBox checkBox = (CheckBox)v;
              if (checkBox.isChecked()){
                planButtonsChecked++;
              }
              else { planButtonsChecked--; }

              int redColor = (int) ((255.0 / viewsCount) * (viewsCount - planButtonsChecked));
              int greenColor = (int) ((255.0 / viewsCount) * planButtonsChecked);
              int rgbColor = Color.rgb(redColor, greenColor, 0);
              TextView tv = (TextView) findViewById(R.id.textview_plan);
              tv.setBackgroundColor(rgbColor);

            }
        });
        planCheckBoxViews.add(checkBox);
        ll.addView(checkBox);
    }

    /**
     * This method handles the video associated with the 'source' layout.  It will play (or stop)
     * the video based on the visibility of the layout.
     * @param isVisible is true if layout is visible and false otherwise
     */
    private void playRealVideo(boolean isVisible) {
        // If object exists then check if it's playing, if so stop it
        if (mRealVideoView != null) {
            if (mRealVideoView.isPlaying()) {
                mRealVideoView.stopPlayback();
            }
        }
        else {
            // Get object reference, set it's path (using shared resource location)
            mRealVideoView = (VideoView) findViewById(R.id.video_real);
            mRealVideoView.setVideoPath("/mnt/shared/SharedResources/goofyweather.3gp");
            android.widget.MediaController controller = new android.widget.MediaController(this);
            controller.setAnchorView(mRealVideoView);
            mRealVideoView.setMediaController(controller);
        }
        if (isVisible) {
            // Visible, request focus and start video.
            mRealVideoView.requestFocus();
            mRealVideoView.start();
        }
        else {
            // Stop playback and release reference
            mRealVideoView.stopPlayback();
            mRealVideoView = null;
        }
    }
}
