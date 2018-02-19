package maynoothuniversity.bcd.corkparkingbikes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.FolderOverlay;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

public class OSMActivity extends AppCompatActivity {

    public MapView map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();

        // this is required to not get banned from osm servers apparently
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        setContentView(R.layout.activity_osm);
        map = findViewById(R.id.map);

        map.setTileSource(TileSourceFactory.OpenTopo);

        // Zoom buttons
        map.setBuiltInZoomControls(false);
        // Pinch control
        map.setMultiTouchControls(true);

        //new DisplayParkKML().execute(map);

        setupGeoJsonPark();
        setupGeoJsonBike();
    }

    private void setupGeoJsonPark() {
        String gJson = null;
        try {
            gJson = getGeoStringPark();
        } catch (IOException e) {
            e.printStackTrace();
        }

        KmlDocument mKmlGeoJson = new KmlDocument();
        mKmlGeoJson.parseGeoJSON(gJson);

        Drawable defaultMarker = getResources().getDrawable(R.drawable.circle_sprite_park);
        Bitmap defaultBitmap = ((BitmapDrawable)defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);

        FolderOverlay myOverLay = (FolderOverlay) mKmlGeoJson.mKmlRoot.
                buildOverlay(map, defaultStyle, null, mKmlGeoJson);
        map.getOverlays().add(myOverLay);
        map.getController().setZoom(16);
        map.setMinZoomLevel(12);
        map.getController().setCenter(new GeoPoint(51.8982899, -8.4765593));
        map.invalidate();
    }
    private void setupGeoJsonBike() {
        String gJson = null;
        try {
            gJson = getGeoStringBike();
        } catch (IOException e) {
            e.printStackTrace();
        }

        KmlDocument mKmlGeoJson = new KmlDocument();
        mKmlGeoJson.parseGeoJSON(gJson);

        Drawable defaultMarker = getResources().getDrawable(R.drawable.circle_sprite_bike);
        Bitmap defaultBitmap = ((BitmapDrawable)defaultMarker).getBitmap();
        Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);

//        FolderOverlay myOverLay = (FolderOverlay) mKmlGeoJson.mKmlRoot.
//                buildOverlay(map, defaultStyle, null, mKmlGeoJson);

        RadiusMarkerClusterer bikeCluster = new RadiusMarkerClusterer(this);
        Drawable bikeDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.circle_sprite_bike, null);
        if ((bikeDrawable) != null) {
            Bitmap bikeClusterIcon = ((BitmapDrawable) bikeDrawable).getBitmap();

            bikeCluster.setIcon(bikeClusterIcon);
            bikeCluster.getTextPaint().setTextSize(12 * getResources().getDisplayMetrics().density);
            bikeCluster.mAnchorV = Marker.ANCHOR_BOTTOM;
            bikeCluster.mTextAnchorU = 0.70f;
            bikeCluster.mTextAnchorV = 0.27f;

            map.getOverlays().add(bikeCluster);
        }

        map.getController().setZoom(16);
        map.setMinZoomLevel(12);
        map.getController().setCenter(new GeoPoint(51.8982899, -8.4765593));
        map.invalidate();
    }

    // Load GeoJSON files
    private String getGeoStringPark() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.parking);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        return jsonString;
    }
    private String getGeoStringBike() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.bike);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        String jsonString = writer.toString();
        return jsonString;
    }


//    private class DisplayParkKML extends AsyncTask<MapView, MapView, FolderOverlay> {
//        KmlDocument parkingKML = new KmlDocument();
//        KmlDocument bikeKML = new KmlDocument();
//
//        @Override
//        protected FolderOverlay doInBackground(MapView... params) {
//
//            // https://www.dropbox.com/s/bnvcsmfnykqjkps/bike.kml
//            // https://www.dropbox.com/s/ntzcfitqf7kbaok/park.kml
//
//            parkingKML.parseKMLUrl("https://drive.google.com/open?id=1TqBrLLQraguVX9faXNbcjXiR5GOAhmoL");
//
//            Drawable defaultMarker = getResources().getDrawable(R.drawable.marker_kml_point);
//            Bitmap defaultBitmap = ((BitmapDrawable)defaultMarker).getBitmap();
//            Style defaultStyle = new Style(defaultBitmap, 0x901010AA, 3.0f, 0x20AA1010);
//
//            FolderOverlay parkingOverlay = (FolderOverlay) parkingKML.mKmlRoot.buildOverlay(map, defaultStyle, null, parkingKML);
//
//            return parkingOverlay;
//        }
//
//        @Override
//        protected void onPostExecute(FolderOverlay folderOverlay) {
//            super.onPostExecute(folderOverlay);
//            Toast.makeText(getApplicationContext(), "Post execute", Toast.LENGTH_LONG).show();
//
//            map.getOverlays().add(folderOverlay);
//            map.invalidate();
//
//        }
//    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
    }
}
