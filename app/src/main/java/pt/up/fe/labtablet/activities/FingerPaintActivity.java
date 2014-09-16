package pt.up.fe.labtablet.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.api.AsyncBitmapExporter;
import pt.up.fe.labtablet.api.AsyncTaskHandler;
import pt.up.fe.labtablet.api.ChangelogManager;
import pt.up.fe.labtablet.models.ChangelogItem;
import pt.up.fe.labtablet.utils.ColorPickerDialog;
import pt.up.fe.labtablet.utils.Utils;

public class FingerPaintActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {

    MyView mDrawingView;
    private String folderName;
    private Paint mPaint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        folderName = getIntent().getStringExtra("folderName");

        ActionBar mActionBar = getActionBar();
        if (mActionBar == null) {
            ChangelogItem item = new ChangelogItem();
            item.setMessage("ValidateMetadata" + "Couldn't get actionbar. Compatibility mode layout");
            item.setTitle(getResources().getString(R.string.developer_error));
            item.setDate(Utils.getDate());
            ChangelogManager.addLog(item, FingerPaintActivity.this);
        } else {
            getActionBar().setTitle(folderName);
            getActionBar().setSubtitle(getResources().getString(R.string.title_activity_finger_paint));
        }

        mDrawingView = new MyView(this);
        mDrawingView.setDrawingCacheEnabled(true);
        mDrawingView.setBackgroundResource(R.drawable.card);//set the back ground if you wish to
        setContentView(mDrawingView);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(15);
    }

    @Override
    public void colorChanged(int color) {
        mPaint.setColor(color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.finger_paint, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //private static final int COLOR_MENU_ID = Menu.FIRST;
    //private static final int EMBOSS_MENU_ID = Menu.FIRST + 1;
    //private static final int ERASE_MENU_ID = Menu.FIRST + 2;
    //private static final int Save = Menu.FIRST + 3;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xFF);

        switch (item.getItemId()) {
            case R.id.sketch_pick_color:
                new ColorPickerDialog(this, this, mPaint.getColor()).show();
                //new ColorPicker(this, this, "KEY", mPaint.getColor(), mPaint.getColor()).show();
                return true;
            case R.id.sketch_save:
                final String name = "" + System.currentTimeMillis() + ".png";
                Bitmap bitmap = mDrawingView.getDrawingCache();

                final String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + getResources().getString(R.string.app_name) +
                        "/" + folderName + "/meta/" + name;

                new AsyncBitmapExporter(new AsyncTaskHandler<Void>() {
                    @Override
                    public void onSuccess(Void result) {

                        mDrawingView.invalidate();
                        mDrawingView.setDrawingCacheEnabled(false);
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result", path);
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception error) {

                        Toast.makeText(FingerPaintActivity.this, ".", Toast.LENGTH_SHORT).show();
                        ChangelogItem item = new ChangelogItem();
                        item.setMessage("Sketch: " + error.toString() + "When exporting the sketch to a bitmap. Possible causes involve permissions and lack of storage space.");
                        item.setTitle(getResources().getString(R.string.developer_error));
                        item.setDate(Utils.getDate());
                        ChangelogManager.addLog(item, FingerPaintActivity.this);
                    }

                    @Override
                    public void onProgressUpdate(int value) {
                    }
                }).execute(path, bitmap, getApplication());

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyView extends View {

        private static final float TOUCH_TOLERANCE = 4;
        Context context;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint mBitmapPaint;
        private float mX, mY;

        public MyView(Context c) {
            super(c);
            context = c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);

        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);


            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

            canvas.drawPath(mPath, mPaint);
        }

        private void touch_start(float x, float y) {
//showDialog();
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;

        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
// commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);
// kill this so we don't double draw
            mPath.reset();
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
//mPaint.setMaskFilter(null);
        }

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:

                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}