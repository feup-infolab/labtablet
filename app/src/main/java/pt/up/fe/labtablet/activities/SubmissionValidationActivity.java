package pt.up.fe.labtablet.activities;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.util.Locale;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.SubmissionStepHandler;
import pt.up.fe.labtablet.fragments.SubmissionStep1;
import pt.up.fe.labtablet.fragments.SubmissionStep2;
import pt.up.fe.labtablet.fragments.SubmissionStep3;
import pt.up.fe.labtablet.fragments.SubmissionStep4;

/**
 * Holds the four steps to upload a favorite to the repository and handles
 * their transition
 */
public class SubmissionValidationActivity extends AppCompatActivity implements SubmissionStepHandler {

    private static String favoriteName;
    private static String projectName;
    private static String destUri;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private FloatingActionButton pb_submission_state;

    public static String getProjectName() {
        return projectName;
    }

    public static void setProjectName(String inProjectName) {
        projectName = inProjectName;
    }

    public static String getDestUri() {
        return destUri;
    }

    public static void setDestUri(String uri) {
        destUri = uri;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submission_validation);

        if (getIntent().getExtras().containsKey("favoriteName")) {
            Toast.makeText(this, "Could not find favorite name. Exiting...", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        destUri = "";
        projectName = "";

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        pb_submission_state = (FloatingActionButton) findViewById(R.id.pb_submission);
        pb_submission_state.setProgress(0, true);

        favoriteName = getIntent().getStringExtra("favorite_name");

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ObjectAnimator animation;
                switch (position) {
                    case 0:
                        pb_submission_state.setProgress(0, true);
                        break;
                    case 1:pb_submission_state.setProgress(33, true);
                        break;
                    case 2:pb_submission_state.setProgress(66, true);
                        break;
                    case 3:pb_submission_state.setProgress(100, true);
                        break;
                    default:
                        pb_submission_state.setProgress(0, true);
                        break;
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.submission_validation, menu);
        return true;
    }

    @Override
    public void nextStep(int stage) {
        mViewPager.setCurrentItem(stage);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.cancel_submission)
                .setMessage(R.string.really_cancel_upload)
                .setPositiveButton(R.string.form_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return SubmissionStep1.newInstance(favoriteName, SubmissionValidationActivity.this);
                case 1:
                    return SubmissionStep2.newInstance(favoriteName, SubmissionValidationActivity.this);
                case 2:
                    return SubmissionStep3.newInstance(SubmissionValidationActivity.this);
                case 3:
                    return SubmissionStep4.newInstance(favoriteName, projectName, destUri);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
            }
            return null;
        }
    }

}
