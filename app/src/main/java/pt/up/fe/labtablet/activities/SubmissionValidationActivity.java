package pt.up.fe.labtablet.activities;

import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Locale;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.api.SubmissionStepHandler;
import pt.up.fe.labtablet.fragments.SubmissionStep1;
import pt.up.fe.labtablet.fragments.SubmissionStep2;
import pt.up.fe.labtablet.fragments.SubmissionStep3;
import pt.up.fe.labtablet.fragments.SubmissionStep4;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.utils.Utils;

public class SubmissionValidationActivity extends Activity implements SubmissionStepHandler {

    private static String favoriteName;
    private static String projectName;
    private static String destUri;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    private ProgressBar pb_submission_state;

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

        pb_submission_state = (ProgressBar) findViewById(R.id.pb_submission);
        pb_submission_state.setProgress(0);

        favoriteName = getIntent().getStringExtra("favorite_name");

        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("SubmissionValidation" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, SubmissionValidationActivity.this);
        } else {
            mActionBar.setTitle(favoriteName);
            mActionBar.setSubtitle("Validating submission");
        }


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                ObjectAnimator animation;
                switch (position) {
                    case 0:
                        animation = ObjectAnimator.ofInt(pb_submission_state, "progress", 0);
                        animation.setDuration(250); // 0.5 second
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.start();
                        //pb_submission_state.setProgress(0);
                        break;
                    case 1:
                        animation = ObjectAnimator.ofInt(pb_submission_state, "progress", 33);
                        animation.setDuration(250); // 0.5 second
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.start();
                        break;
                    case 2:
                        animation = ObjectAnimator.ofInt(pb_submission_state, "progress", 66);
                        animation.setDuration(250); // 0.5 second
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.start();
                        break;
                    case 3:
                        animation = ObjectAnimator.ofInt(pb_submission_state, "progress", 100);
                        animation.setDuration(250); // 0.5 second
                        animation.setInterpolator(new DecelerateInterpolator());
                        animation.start();
                        break;
                    default:
                        pb_submission_state.setProgress(0);
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
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
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
                    return SubmissionStep3.newInstance(favoriteName, SubmissionValidationActivity.this);
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
