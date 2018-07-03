package maynoothuniversity.bcd.corkparkingbikes;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.services.commons.geojson.Feature;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.mapbox.mapboxsdk.style.layers.Filter.all;
import static com.mapbox.mapboxsdk.style.layers.Filter.gte;
import static com.mapbox.mapboxsdk.style.layers.Filter.lt;
import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.circleRadius;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

//import android.support.design.widget.FloatingActionButton;

// DISCLAIMER: this was my very first full scale app, so it is very rough in many places and could be improved in a lot of ways!!

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    // Defines a bounding box region to confine the camera to this region.
    // The first set of coordinates is the top left corner, the second set is the bottom right corner.
    private static final LatLngBounds CORK_CITY = new LatLngBounds.Builder()
            .include(new LatLng(51.917283, -8.557001))
            .include(new LatLng(51.851449, -8.366767))
            .build();

    // Defines our MapView, MapboxMap, and CheckBox objects
    private MapView mapView;
    private MapboxMap mapboxMap;
    private CheckBox dontShow;

    // Defines the floating action button we'll use for the info page
    android.support.design.widget.FloatingActionButton floatingActionButton;

    // Defines the floating action menu and buttons for the menu we'll use to perform some functions
    // This particular menu came from https://github.com/Clans/FloatingActionButton
    // However you may be able to find a more modern and well-supported library
    FloatingActionMenu floatingActionMenu;
    FloatingActionButton floatingActionButton1, floatingActionButton2, floatingActionButton3;

    // Defines the string array to hold the free_spaces data from the dataset
    // and also define the string that holds the data for the entire column
    public static String[] splitFreeSpaces = new String [8];
    public static String freeSpaces;

    // Defines a string to hold the free_spaces data for each car park
    private static String saint_finbarr;
    private static String merchant_quay;
    private static String grand_parade;
    private static String carroll_quay;
    private static String city_hall;
    private static String black_ash;
    private static String north_main;
    private static String paul_street;

    // Defines an array to hold each value needed for the bicycle stations
    // The first index is the number of bikes, and the second index is the number of stands,
    // and so on in that pattern - there are 31 stations, so 62 slots needed
    private static int dataArray[] = new int[62];

    // Keeps track of the connection status of the data URLs
    private static int connectionResultCarPark;
    private static int connectionResultBike;

    // Used to store the current date
    public String date;

    // This function is called when the activity starts
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Required to use Mapbox maps.
        // You can either put your Mapbox API key in directly, or put it in your strings.xml and reference it like below
        Mapbox.getInstance(this, getString(R.string.access_token_mapbox));

        // Sets this activity's layout as the "activity_main" layout
        setContentView(R.layout.activity_main);

        // Assign our MapView with the MapView in the layout (with id mapView)
        mapView = findViewById(R.id.mapView);

        // Assign our floating action menus and buttons to the ones in the layout file
        floatingActionMenu = findViewById(R.id.material_design_android_floating_action_menu);
        floatingActionButton1 = findViewById(R.id.material_design_floating_action_menu_item1);
        floatingActionButton2 = findViewById(R.id.material_design_floating_action_menu_item2);
        floatingActionButton3 = findViewById(R.id.material_design_floating_action_menu_item3);

        // Disables the menu's icon animating when clicked
        floatingActionMenu.setIconAnimated(false);

        // Changes the value of the menu button's colour
        floatingActionMenu.setMenuButtonColorNormal(Color.parseColor("#d84e52"));

        // Assign the non-menu floating action button to the info button in the layout file
        floatingActionButton = findViewById(R.id.info_fab);

        // Get the time at the creation of the activity in the pattern hour:minutes am/pm
        DateFormat df = new SimpleDateFormat("h:mma", Locale.ENGLISH);
        date = df.format(Calendar.getInstance().getTime());

        // This handles the dialog which acts as a first time mini-tutorial using SharedPreferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // if the "FirstTime" value hasn't been set..
        if(!prefs.contains("FirstTime")) {
            // Then create a dialog builder with the first_time_popup layout
            AlertDialog.Builder firstTime = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.first_time_popup, null);

            // Assign the CheckBox to the one in the layout file
            dontShow = view.findViewById(R.id.dont_show_again);

            firstTime.setView(view);
            firstTime.setTitle("How To Use This Map");

            // Creates an "OK" button which closes the dialog after executing the code within
            firstTime.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    // If the CheckBox was checked (which read "Don't show this again")...
                    if(dontShow.isChecked()) {
                        // Edit our SharedPreferences to set the FirstTime value to true
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("FirstTime",true);
                        editor.apply();
                    }
                }
            });

            // Prevent being able to close the dialog by clicking outside of it
            // And show the dialog
            firstTime.setCancelable(false);
            firstTime.show();
        }

        // Here we'll retrieve data for the app in separate threads
        new GetCarParkData().execute("http://data.corkcity.ie/datastore/dump/6cc1028e-7388-4bc5-95b7-667a59aa76dc");
        new GetBikeData().execute();

        // Create the map and then wait for the callback which tells us the map is ready
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    // This function is called when the map is created
    // and sets up map preferences, markers, and listeners
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        MainActivity.this.mapboxMap = mapboxMap;

        // Set the bounds of the camera to the bounding box we defined before
        // We can also set the maximum amount we can zoom in, and the minimum amount we can zoom out
        mapboxMap.setLatLngBoundsForCameraTarget(CORK_CITY);
        mapboxMap.setMaxZoomPreference(18);
        mapboxMap.setMinZoomPreference(10);

        // Calls the function which will add markers to the map and cluster them
        addClusteredGeoJsonSource();

        // Adds a click listener to register taps on the map
        mapboxMap.addOnMapClickListener(this);

        // Set click listeners to each of the floating menu button's FABs to perform their functions
        // Buttons 1 & 2 toggle the parking and bicycle layers while Button 3 restarts the activity
        floatingActionButton1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleParkingLayer();
            }
        });
        floatingActionButton2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                toggleBikeLayer();
            }
        });
        // Restart/refresh
        floatingActionButton3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                startActivity(intent);
            }
        });

        // This click listener starts the intent to open the info page
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });
    }

    // These functions handle layer toggling - these layers come from the addClusteredGeoJsonSource() function
    // Toggling is performed by setting visibility to NONE or to VISIBLE as needed
    // Toggle the bike markers layer -> involves toggling off the cluster circles, the number text, and the unclustered markers
    private void toggleBikeLayer() {
        for(int i = 0; i <= 3; i++) {
            // Toggle circles
            Layer layer = mapboxMap.getLayer("clusterBike-"+i);
            if(layer != null) {
                if (VISIBLE.equals(layer.getVisibility().getValue())) {
                    layer.setProperties(visibility(NONE));
                } else {
                    layer.setProperties(visibility(VISIBLE));
                }
            }
        }
        // Toggle the individual markers
        Layer layer = mapboxMap.getLayer("unclustered-points-bike");
        if(layer != null) {
            if (VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
            } else {
                layer.setProperties(visibility(VISIBLE));
            }
        }
        // Toggle the circle numbers
        Layer layer_nums = mapboxMap.getLayer("countBike");
        if(layer_nums != null) {
            if (VISIBLE.equals(layer_nums.getVisibility().getValue())) {
                layer_nums.setProperties(visibility(NONE));
            } else {
                layer_nums.setProperties(visibility(VISIBLE));
            }
        }
    }
    private void toggleParkingLayer() {
        // Toggle the individual markers
        Layer layer = mapboxMap.getLayer("unclustered-points-park");
        if(layer != null) {
            if (VISIBLE.equals(layer.getVisibility().getValue())) {
                layer.setProperties(visibility(NONE));
            } else {
                layer.setProperties(visibility(VISIBLE));
            }
        }
    }

    // This function handles tapping the map and bringing up dialogs that display information
    @Override
    public void onMapClick(@NonNull LatLng point) {

        // Get the point of the screen that was clicked
        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);

        // Query the map's features to see if the point lines up with one of the given layer IDs
        // These layer IDs denote different types of markers & icons on the map
        // If it's one of the "unclustered-points" layers, add the feature to the relevant "features" list
        // If it's the "clusterBike" layer, add the feature to the "cluster" list
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-park");
        List<Feature> bike_features = mapboxMap.queryRenderedFeatures(pixel, "unclustered-points-bike");
        List<Feature> cluster = mapboxMap.queryRenderedFeatures(pixel, "clusterBike-" + 1, "clusterBike-" + 2);

        // Define variables for the screen orientation, current zoom, and half the map size
        Display screenOrientation = getWindowManager().getDefaultDisplay();
        double currentZoom = mapboxMap.getCameraPosition().zoom;
        int half = (int)mapboxMap.getHeight()/2;

        // If a cluster is tapped
        if(cluster.size() > 0) {
            // Update the camera to zoom in 2 units more from the current zoom
            CameraPosition position = new CameraPosition.Builder()
                    .target(point)
                    .zoom(currentZoom+2)
                    .build();
            mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
        }

        // If a parking marker is tapped
        else if (features.size() > 0) {
            // If a connection was made
            if (connectionResultCarPark == 200) {
                // Retrieve the information contained within the selected feature
                Feature feature = features.get(0);

                // Centre the camera on the selected feature
                if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
                    // The orientation is "Square", centre as normal
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                } else {
                    if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                        // The orientation is "Portrait", centre as normal
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    } else {
                        // The orientation is "Landscape", add padding (using the half variable)
                        // This ensures our popup does not cover the marker we are moving to
                        mapboxMap.setPadding(0, half, 0, 0);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }

                // Create an alert dialog builder to act as our information popup - quick and dirty fix for SymbolLayers
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                // We'll use a custom layout for the dialog
                // Inflating means to render the object in memory
                View mView = getLayoutInflater().inflate(R.layout.dialog_info, null);

                // In my car parks popup layout, I have 4 blank TextViews which have relevant IDs
                // Here, we can define them and give them variable names
                TextView txtName = mView.findViewById(R.id.name);
                TextView txtFree = mView.findViewById(R.id.freeSpaces);
                TextView txtPrice = mView.findViewById(R.id.price);
                TextView txtOpening = mView.findViewById(R.id.openingTimes);

                // In my marker features, I have a number of properties
                // I can assign them to their relevant TextView by getting the String property
                // Note: the key for the property is case-sensitive
                String park_name = feature.getStringProperty("name");
                String price = feature.getStringProperty("price");
                String opening_time = feature.getStringProperty("opening_times");

                txtName.setText(park_name);
                txtPrice.setText(price);
                txtOpening.setText(R.string.open);
                txtOpening.append(opening_time);

                // Used by getTextPercentageColour() to colour car park text
                double percentage;

                // Assign free_spaces values to relevant dialog box
                // First we check if the name of the feature we tapped matches one of the car park's names
                // Then, using Html.fromHtml for markup, we can input the value for free_spaces and the total number of spaces
                //<editor-fold desc="Parking Data Assignment">
                if (park_name.equals("Saint Finbarr's")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", saint_finbarr, feature.getStringProperty("spaces"))));
                    // The percentage colour is gotten from converting these two values to doubles, dividing them and multiplying by 100
                    // The value is passed through the getTextPercentageColour() function which returns a string that is parsed into a colour and set to the text colour.
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour((Double.parseDouble(saint_finbarr) / Double.parseDouble(feature.getStringProperty("spaces")))*100)));
                }
                if (park_name.equals("Merchant's Quay")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", merchant_quay, feature.getStringProperty("spaces"))));
                    // You can also do this using the "percentage" variable for more readability
                    percentage = (Double.parseDouble(merchant_quay) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("Grand Parade")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", grand_parade, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(grand_parade) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("Carrolls Quay")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", carroll_quay, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(carroll_quay) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("City Hall - Eglington Street")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", city_hall, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(city_hall) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("Black Ash Park & Ride")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", black_ash, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(black_ash) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("North Main Street")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", north_main, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(north_main) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                if (park_name.equals("Paul Street")) {
                    txtFree.setText(Html.fromHtml(String.format("Currently <b>%s</b> free spaces out of %s", paul_street, feature.getStringProperty("spaces"))));
                    percentage = (Double.parseDouble(paul_street) / Double.parseDouble(feature.getStringProperty("spaces"))) * 100;
                    txtFree.setTextColor(Color.parseColor(getTextPercentageColour(percentage)));
                }
                //</editor-fold>

                // Append the current time to the information
                txtFree.append("\n (" + date + ")");

                // Set the inflated view to the popup and create the dialog
                builder.setView(mView);
                AlertDialog dialog = builder.create();

                // Before enabling the dialog, we can remove the default dim that a dialog makes
                Window window = dialog.getWindow();
                if (window != null) {
                    window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim
                }

                // We can also reposition the dialog by setting the Gravity to TOP
                WindowManager.LayoutParams wlp;
                if (window != null) {
                    wlp = window.getAttributes();
                    wlp.gravity = Gravity.TOP;
                    window.setAttributes(wlp);
                }

                // Finally, show the dialog popup
                dialog.show();
            } else {
                // We did not make a successful connection, so make a toast message to tell the user that an error occurred
                Toast.makeText(getApplicationContext(), "Data could not be loaded. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }

        // For bike markers -----------------------------------------------------------------------
        else if (bike_features.size() > 0) {
            if(connectionResultBike == 200) {

                Feature feature = bike_features.get(0);

                // Centre the camera on the selected feature
                if (screenOrientation.getWidth() == screenOrientation.getHeight()) {
                    // The orientation is "Square", centre as normal
                    CameraPosition position = new CameraPosition.Builder()
                            .target(point)
                            .build();
                    mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                } else {
                    if (screenOrientation.getWidth() < screenOrientation.getHeight()) {
                        // The orientation is "Portrait", centre as normal
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    } else {
                        // The orientation is "Landscape", add padding (using the half variable)
                        // This ensures our popup does not cover the marker we are moving to
                        mapboxMap.setPadding(0, half, 0, 0);
                        CameraPosition position = new CameraPosition.Builder()
                                .target(point)
                                .build();
                        mapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(position));
                    }
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_info_bike, null);

                TextView txtName = mView.findViewById(R.id.name);
                TextView txtBikes = mView.findViewById(R.id.bikes);

                String bike_station_name = feature.getStringProperty("data__name");

                txtName.setText(bike_station_name);

                // Assign bikesAvailable and docksAvailable values to relevant dialog box
                // Again, check if the name equals one of the stations below
                // If so, input the relevant values from the relevant indexes of the dataArray[]
                //<editor-fold desc="Bike Data Assignment">
                if (bike_station_name.equals("Gaol Walk")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[0], dataArray[1])));
                }
                if (bike_station_name.equals("Fitzgerald's Park")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[2], dataArray[3])));
                }
                if (bike_station_name.equals("Bandfield")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[4], dataArray[5])));
                }
                if (bike_station_name.equals("Dyke Parade")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[6], dataArray[7])));
                }
                if (bike_station_name.equals("Mercy Hospital")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[8], dataArray[9])));
                }
                if (bike_station_name.equals("St. Fin Barre's Bridge")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[10], dataArray[11])));
                }
                if (bike_station_name.equals("Pope's Quay")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[12], dataArray[13])));
                }
                if (bike_station_name.equals("North Main St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[14], dataArray[15])));
                }
                if (bike_station_name.equals("Grattan St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[16], dataArray[17])));
                }
                if (bike_station_name.equals("Wandesford Quay")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[18], dataArray[19])));
                }
                if (bike_station_name.equals("Bishop St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[20], dataArray[21])));
                }
                if (bike_station_name.equals("Camden Quay")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[22], dataArray[23])));
                }
                if (bike_station_name.equals("Corn Market St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[24], dataArray[25])));
                }
                if (bike_station_name.equals("Lapp's Quay")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[26], dataArray[27])));
                }
                if (bike_station_name.equals("St. Patricks St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[28], dataArray[29])));
                }
                if (bike_station_name.equals("South Main St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[30], dataArray[31])));
                }
                if (bike_station_name.equals("Grand Parade")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[32], dataArray[33])));
                }
                if (bike_station_name.equals("Peace Park")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[34], dataArray[35])));
                }
                if (bike_station_name.equals("South Gate Bridge")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[36], dataArray[37])));
                }
                if (bike_station_name.equals("Coburg St.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[38], dataArray[39])));
                }
                if (bike_station_name.equals("Emmet Place")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[40], dataArray[41])));
                }
                if (bike_station_name.equals("South Mall")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[42], dataArray[43])));
                }
                if (bike_station_name.equals("College of Commerce")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[44], dataArray[45])));
                }
                if (bike_station_name.equals("Father Mathew Statue")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[46], dataArray[47])));
                }
                if (bike_station_name.equals("Cork School of Music")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[48], dataArray[49])));
                }
                if (bike_station_name.equals("Brian Boru Bridge")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[50], dataArray[51])));
                }
                if (bike_station_name.equals("Bus Station")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[52], dataArray[53])));
                }
                if (bike_station_name.equals("Cork City Hall")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[54], dataArray[55])));
                }
                if (bike_station_name.equals("Lower Glanmire Rd.")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[56], dataArray[57])));
                }
                if (bike_station_name.equals("Clontarf Street")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[58], dataArray[59])));
                }
                if (bike_station_name.equals("Kent Station")) {
                    txtBikes.setText(Html.fromHtml(String.format(Locale.ENGLISH, "Currently <b>%d</b> bikes and <b>%d</b> stands available", dataArray[60], dataArray[61])));
                }
                //</editor-fold>
                txtBikes.append("\n (" + date + ")");

                builder.setView(mView);
                AlertDialog dialog = builder.create();

                Window window = dialog.getWindow();
                if (window != null) {
                    window.setDimAmount(0.0f); // 0 = no dim, 1 = full dim
                }

                // We can also reposition the dialog by setting the Gravity to TOP
                WindowManager.LayoutParams wlp;
                if (window != null) {
                    wlp = window.getAttributes();
                    wlp.gravity = Gravity.TOP;
                    window.setAttributes(wlp);
                }

                // Finally, show the dialog popup
                dialog.show();
            } else {
                // We did not make a successful connection, so make a toast message to tell the user that an error occurred
                Toast.makeText(getApplicationContext(), "Data could not be loaded. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // This class handles getting the car park data from an online URL
    // To avoid issues with network calls causing crashing if run on the main UIThread, we'll use AsyncTask
    // This creates a new thread for this process to run in the background of the application
    private static class GetCarParkData extends AsyncTask<String, String, String> {

        // Defines variables to hold the data from the table
        String dataParsed;
        String[] data_csv;

        // This method performs its task in the background of the app
        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // The URL is the URL supplied in the thread execute on line 202/203
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int responseCode = connection.getResponseCode();

                // If the response from the webpage is 200/OK, then we connected
                if (responseCode == 200) {
                    // Save this code to check it later
                    connectionResultCarPark = responseCode;
                    InputStream stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));

                    String line;
                    int i = 0;

                    // Loop through all the lines of the data until the end
                    while ((line = reader.readLine()) != null) {
                        // the data is separated by commas, so split each piece into its own index in the String array
                        data_csv = line.split(",");
                        try {
                            // Skip adding the column header by checking past i = 0 (first row)
                            if (i > 0) {
                                Log.d("Data ", "" + data_csv[4]);

                                // We want the free_spaces information which is the 5th column (starting from 0, so 4th index)
                                // Also, we want to return a single String, so combine the results of this column together
                                dataParsed = dataParsed + data_csv[4] + ",";
                                i++;
                            } else {
                                i++;
                            }
                        } catch (Exception e) {
                            Log.d("Problem: ", e.toString());
                        }
                    }
                    return dataParsed;
                } else {
                    // We did not connect, so disconnect and return nothing
                    connection.disconnect();
                    Log.d("Car Park data error: ", ""+responseCode);
                    return "";
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        return "";
        }

        // This method runs after the background process has completed
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // If the result of doInBackground() is not empty
            if(result.length() > 0) {
                // We can set our public String variables equal to our results so we can reference them in other places in the code
                freeSpaces = result;

                // Split the result by comma again
                splitFreeSpaces = freeSpaces.split(",");

                // For logging the results
                for (String splitFreeSpace : splitFreeSpaces) {
                    Log.d("Split array output ", "" + splitFreeSpace);
                }

                // Give each car park their respective free_spaces value
                // The order is related to the order of the car parks in the dataset
                saint_finbarr = splitFreeSpaces[0].substring(4); // removes the null appended to the first no.
                merchant_quay = splitFreeSpaces[1];
                grand_parade = splitFreeSpaces[2];
                carroll_quay = splitFreeSpaces[3];
                city_hall = splitFreeSpaces[4];
                black_ash = splitFreeSpaces[5];
                north_main = splitFreeSpaces[6];
                paul_street = splitFreeSpaces[7];
            }
        }
    }

    // This class handles getting the car park data from an online URL
    // Again using AsyncTask
    private static class GetBikeData extends AsyncTask<String, Void, String> {
        @Override

        protected String doInBackground(String... params) {
            try {
                // We can also supply the URL in this method
                URL url = new URL("https://data.bikeshare.ie/dataapi/resources/station/data/list");

                // For this URL, we're required to use a POST request and specify some parameters.
                // To see the original code for this section, see this tutorial:
                // https://www.studytutorial.in/android-httpurlconnection-post-and-get-request-tutorial
                JSONObject postDataParams = new JSONObject();
                try {
                    postDataParams.put("key", "a5e70f27ae91405f9c21d023f4fb72400f24888687e26d6e75dc47b208c4aa97"); // The key for the API
                    postDataParams.put("schemeId", "2"); // The area for Cork city
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Time is in milliseconds
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);

                // Need this to make this a POST method
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream outputStream = connection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                bufferedWriter.write(getPostDataString(postDataParams));

                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                int responseCode = connection.getResponseCode();
                // HTTP_OK is the same as 200
                if(responseCode == HttpURLConnection.HTTP_OK) {
                    connectionResultBike = responseCode;
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                        break;
                    }
                    bufferedReader.close();
                    return stringBuffer.toString();
                }
                else {
                    connection.disconnect();
//                    Log.d("Bike data error: ", ""+responseCode);
                    return "";
                }
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result.length() > 0) {
                JSONObject object;
                try {
                    object = new JSONObject(result);
                    // The name of the array the result was stored in was called "data"
                    JSONArray jsonArray = object.getJSONArray("data");

                    // There are two pieces of data we want from this URL: the number of bikes, and of stands/docks
                    // i tracks each entry in the JSON object, while k stores this data in the public String array
                    // In our dataArray, the first position will be the bikes no., the next position the stands no.
                    // Then k jumps forward 2, so the next positions can be filled accordingly, and so on.
                    int k = 0;
                    for (int i = 0; i < jsonArray.length(); i++, k += 2) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        dataArray[k] = jsonObject.getInt("bikesAvailable");
                        dataArray[k + 1] = jsonObject.getInt("docksAvailable");
                    }
//                    for (int j = 0; j < dataArray.length; j++) {
//                        Timber.tag("Data Array").d(j + ") " + dataArray[j]);
//                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // This function is used by GetBikeData to append parameters to URLS and encodes them
    // Refer again to this tutorial: https://www.studytutorial.in/android-httpurlconnection-post-and-get-request-tutorial
    @NonNull
    public static String getPostDataString(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> iterator = params.keys();

        while(iterator.hasNext()) {
            String key = iterator.next();
            Object value = params.get(key);

            if(first) {
                first = false;
            }
            else {
                result.append("&");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    // This function is called from onMapReady() to add markers and cluster them
    // Original code can be found on the Mapbox demo GitHub repo: https://bit.ly/2K5VbVQ
    private void addClusteredGeoJsonSource() {
        // Bike Json from 24-05-2017 -> https://api.myjson.com/bins/dlp89
        // Static Car Park Json -> https://api.myjson.com/bins/x52zl
        // These URLs contain GeoJSON used to populate markers onto the map
        try {
            mapboxMap.addSource(
                    new GeoJsonSource("cork-parking",
                            new URL("https://api.myjson.com/bins/x52zl"),
                            new GeoJsonOptions()
                                    .withCluster(false)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                    )
            );
            mapboxMap.addSource(
                    new GeoJsonSource("cork-bike",
                            new URL("https://api.myjson.com/bins/dlp89"),
                            new GeoJsonOptions()
                                    .withCluster(true)
                                    .withClusterMaxZoom(14)
                                    .withClusterRadius(50)
                    )
            );
        } catch (MalformedURLException malformedUrlException) {
            Timber.e("Check the URL %s", malformedUrlException.getMessage());
        }

        // Defines the different steps of colour depending on the number of objects within a cluster
        // The number indicates how much is needed in order for the colour to change
        // i.e. blue < 20, green > 20, etc.
        int[][] layers = new int[][]{
                new int[]{150, Color.parseColor("#dd1c77")},
                new int[]{20, Color.parseColor("#addd8e")},
                new int[]{0, Color.parseColor("#2b8cbe")}
        };

        // These are the base markers that are not clustered
        // Both types are given a separate layer ID (park vs bike)
        // Both types also reference their respective source ID (the ID of the GeoJsonSource)
        SymbolLayer unclusteredPark = new SymbolLayer("unclustered-points-park", "cork-parking");
        unclusteredPark.withProperties(
                // iconImage comes from the icons in the MapBox map that is used in the layout file
                // iconIgnorePlacement set to true means icons will be visible regardless of zoom or overlap with other icons
                iconImage("parking-15-colour"),
                iconIgnorePlacement(true),
                iconSize(1.5f),
                visibility(VISIBLE)
        );
        SymbolLayer unclusteredBike = new SymbolLayer("unclustered-points-bike", "cork-bike");
        unclusteredBike.withProperties(
                iconImage("bicycle-share-15-colour-alt"),
                iconSize(1.5f),
                visibility(VISIBLE)
        );

        // Add these different markers to the map
        mapboxMap.addLayer(unclusteredPark);
        mapboxMap.addLayer(unclusteredBike);

        // For each of the cluster types (their colour/size)
        for (int i = 0; i < layers.length; i++) {
            //Add clusters' circles
            CircleLayer circlesPark = new CircleLayer("clusterPark-" + i, "cork-parking");
            circlesPark.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f)
            );
            CircleLayer circlesBike = new CircleLayer("clusterBike-" + i, "cork-bike");
            circlesBike.setProperties(
                    circleColor(layers[i][1]),
                    circleRadius(18f),
                    visibility(VISIBLE)
            );
            // Add a filter to the cluster layer that hides the circles based on "point_count"
            circlesPark.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            circlesBike.setFilter(
                    i == 0
                            ? gte("point_count", layers[i][0]) :
                            all(gte("point_count", layers[i][0]), lt("point_count", layers[i - 1][0]))
            );
            mapboxMap.addLayer(circlesPark);
            mapboxMap.addLayer(circlesBike);
        }

        // Create the text which counts how many features are in each cluster
        SymbolLayer countPark = new SymbolLayer("countPark", "cork-parking");
        countPark.setProperties(
                textField("{point_count}"),
                textSize(12f),
                textColor(Color.WHITE)
        );
        SymbolLayer countBike = new SymbolLayer("countBike", "cork-bike");
        countBike.setProperties(
                textField("{point_count}"),
                textSize(12f),
                textColor(Color.WHITE)
        );
        mapboxMap.addLayer(countPark);
        mapboxMap.addLayer(countBike);
    }

    // This function returns blue, yellow, or red based on given percentage
    // to give colour to text based on fullness of the car park
    public String getTextPercentageColour(double percentage) {
        if(percentage <= 20) {
            return "#f03b20"; // red
        }
        if(percentage > 20 && percentage <= 50) {
            return "#feb24c"; // orange

        } else {
            return "#43a2ca"; // blue
        }
    }

    // Lifecycle Methods required for MapBox
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        // refresh data on resume?
        // this appears to happen if the app is minimized/closed anyways
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

