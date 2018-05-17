package maynoothuniversity.bcd.corkparkingbikes;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        TextView attributionTxt = findViewById(R.id.attrib_body);
        attributionTxt.setMovementMethod(LinkMovementMethod.getInstance());

//        Element versionElement = new Element();
//        versionElement.setTitle("Version 1.0");
//
//        Element controlElement = new Element();
//        controlElement.setTitle(getString(R.string.controls));
//
//        Element attributionElement = new Element();
//        attributionElement.setTitle("© Mapbox, © OpenStreetMap contributors");
//
//        View aboutPage = new AboutPage(this)
//                .isRTL(false)
//                .setDescription(getString(R.string.about_body_text))
//                .addItem(versionElement)
//                .addItem(attributionElement)
//                .addGroup("Controls")
//                .addItem(controlElement)
//                .addGroup("Want to leave feedback?")
//                .addEmail("dashboards@mu.ie")
//                .addTwitter("dashbuild")
//                .create();
//
//        setContentView(aboutPage);

        // back arrow

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to previous activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

//    Intent intent = new Intent(Intent.ACTION_SENDTO);
//    intent.setData(Uri.parse("mailto:dashboards@mu.ie"));
//    startActivity(Intent.createChooser(intent, "Send feedback"));

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.website:
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW);
                websiteIntent.setData(Uri.parse("http://corkdashboard.ie/pages/index"));
                startActivity(websiteIntent);
                break;
            case R.id.email:
                Intent emailIntent = new Intent(android.content.Intent.ACTION_VIEW);
                emailIntent.setData(Uri.parse("mailto:dashboards@mu.ie"));
                startActivity(emailIntent);
                break;
            case R.id.twitter:
                Intent twitterIntent = new Intent(android.content.Intent.ACTION_VIEW);
                twitterIntent.setData(Uri.parse("https://twitter.com/dashbuild"));
                startActivity(twitterIntent);
                break;
        }
    }
}
