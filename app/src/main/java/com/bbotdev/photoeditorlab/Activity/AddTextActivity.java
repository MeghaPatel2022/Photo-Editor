package com.bbotdev.photoeditorlab.Activity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bbotdev.photoeditorlab.Adapter.FontsAdpater;
import com.bbotdev.photoeditorlab.R;
import com.bbotdev.photoeditorlab.Utils.Const;
import com.bumptech.glide.Glide;
import com.github.veritas1.HorizontalSlideColorPicker;
import com.xw.repo.BubbleSeekBar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddTextActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.main)
    RelativeLayout main;
    @BindView(R.id.et_text)
    TextView et_text;
    @BindView(R.id.rl_textView)
    RelativeLayout rl_textView;
    @BindView(R.id.textviewLayout)
    FrameLayout textviewLayout;

    @BindView(R.id.img_color)
    ImageView img_color;
    @BindView(R.id.img_text)
    ImageView img_text;
    @BindView(R.id.img_bold)
    ImageView img_bold;
    @BindView(R.id.img_italic)
    ImageView img_italic;
    @BindView(R.id.img_underline)
    ImageView img_underline;
    @BindView(R.id.img_left)
    ImageView img_left;
    @BindView(R.id.img_center)
    ImageView img_center;
    @BindView(R.id.img_right)
    ImageView img_right;
    @BindView(R.id.img_save)
    ImageView img_save;
    @BindView(R.id.img_back)
    ImageView img_back;

    @BindView(R.id.rl_colorPicker)
    RelativeLayout rl_colorPicker;
    @BindView(R.id.ll_color)
    LinearLayout ll_color;
    @BindView(R.id.ll_text_style)
    LinearLayout ll_text_style;
    @BindView(R.id.ll_bold)
    LinearLayout ll_bold;
    @BindView(R.id.ll_italic)
    LinearLayout ll_italic;
    @BindView(R.id.ll_underline)
    LinearLayout ll_underline;
    @BindView(R.id.ll_left)
    LinearLayout ll_left;
    @BindView(R.id.ll_center)
    LinearLayout ll_center;
    @BindView(R.id.ll_right)
    LinearLayout ll_right;

    @BindView(R.id.textSizeSeekbar)
    BubbleSeekBar textSizeSeekbar;
    @BindView(R.id.textOpacitySeekBar)
    BubbleSeekBar textOpacitySeekBar;
    @BindView(R.id.tv_size_opacity)
    TextView tv_size_opacity;
    @BindView(R.id.rl_size_opacity)
    RelativeLayout rl_size_opacity;
    @BindView(R.id.tv_size)
    TextView tv_size;
    @BindView(R.id.tv_opcity)
    TextView tv_opcity;

    @BindView(R.id.ll_fontView)
    LinearLayout ll_fontView;
    @BindView(R.id.rv_font)
    RecyclerView rv_font;

    @BindView(R.id.color_hpicker)
    HorizontalSlideColorPicker color_hpicker;

    @BindView(R.id.imgMain)
    ImageView imgMain;


    private FontsAdpater mFontsAdpater;
    ArrayList<String> font_data = new ArrayList();

    private String text = "";

    private Typeface fontFace;

    private int int1 = 30;
    private final int int2 = 0;
    private final int int3 = 0;

    private String string1;
    private String string2;
    private String string3;
    private int xDelta;
    private int yDelta;

    private boolean isBold = false;
    private boolean isItalic = false;
    private boolean isUnderline = false;

    private File file;

    private String option = "Size";
    private Bitmap originalBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_text);

        ButterKnife.bind(AddTextActivity.this);

        tv_size_opacity.setOnClickListener(this);
        tv_size.setOnClickListener(this);
        tv_opcity.setOnClickListener(this);
        ll_color.setOnClickListener(this);
        ll_text_style.setOnClickListener(this);
        ll_bold.setOnClickListener(this);
        ll_italic.setOnClickListener(this);
        ll_underline.setOnClickListener(this);
        ll_left.setOnClickListener(this);
        ll_center.setOnClickListener(this);
        ll_right.setOnClickListener(this);
        img_save.setOnClickListener(this);
        img_back.setOnClickListener(this);

        text = getIntent().getStringExtra("StringText");
        et_text.setText(text);

        et_text.setOnTouchListener(onTouchListener());

        color_hpicker.setBorderColor(getResources().getColor(R.color.white));
        color_hpicker.setSliderTrackerColor(getResources().getColor(R.color.white));

        textSizeSeekbar.setProgress(et_text.getTextSize());

        setAdapter();
        setBitmap();
        setTextColor();
        seekerListener();
    }

    private void seekerListener(){
        textOpacitySeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                try {
                    et_text.setAlpha(((float) progress) / 100.0f);
                    return;
                } catch (Exception e) {
                    Log.e("LLLLL_Error: ",e.getLocalizedMessage());
                    return;
                }
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });

        textSizeSeekbar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
                int1 = progress;
                et_text.setTextSize(progressFloat);
                return;
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

            }
        });
    }

    private void setAdapter() {
        font_data.clear();
        Collections.addAll(font_data, getResources().getStringArray(R.array.FontFamily));
        rv_font.setLayoutManager(new LinearLayoutManager(AddTextActivity.this, RecyclerView.HORIZONTAL, false));
        mFontsAdpater = new FontsAdpater(AddTextActivity.this, font_data);
        rv_font.setAdapter(mFontsAdpater);
    }

    private void setBitmap() {
        System.gc();
//        byte[] byteArray = getIntent().getByteArrayExtra("BitmapImage");
//        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
//        imgMain.setImageBitmap(EditActivity.transform_bitmap);

        Glide
                .with(AddTextActivity.this)
                .asBitmap()
                .load(EditActivity.transform_bitmap)
                .into(imgMain);

        originalBitmap = EditActivity.transform_bitmap;
        EditActivity.bitmapList.add(originalBitmap);

        mFontsAdpater.setClickListener((view, position) -> {
            fontFace = Typeface.createFromAsset(getAssets(), font_data.get(position));
            et_text.setTypeface(fontFace);
            setTypeFaceText();
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_size_opacity:
                if (rl_size_opacity.getVisibility() == View.VISIBLE) {
                    Const.collapse(rl_size_opacity);
                } else {
                    Const.expand(rl_size_opacity);
                }
                break;
            case R.id.tv_size:
                Const.collapse(rl_size_opacity);
                option = "Size";
                tv_size_opacity.setText(option);
                textSizeSeekbar.setVisibility(View.VISIBLE);
                textOpacitySeekBar.setVisibility(View.GONE);
                break;
            case R.id.tv_opcity:
                Const.collapse(rl_size_opacity);
                option = "Opacity";
                tv_size_opacity.setText(option);
                textOpacitySeekBar.setVisibility(View.VISIBLE);
                textSizeSeekbar.setVisibility(View.GONE);
                break;
            case R.id.ll_color:
                if (ll_fontView.getVisibility() == View.VISIBLE) {
                    ll_fontView.setVisibility(View.GONE);
                    img_text.setImageDrawable(getResources().getDrawable(R.drawable.text_unselect));
                }
                if (rl_colorPicker.getVisibility() == View.VISIBLE) {
                    rl_colorPicker.setVisibility(View.GONE);
                    img_color.setImageDrawable(getResources().getDrawable(R.drawable.color));
                } else {
                    rl_colorPicker.setVisibility(View.VISIBLE);
                    img_color.setImageDrawable(getResources().getDrawable(R.drawable.color_select));
                }
                break;
            case R.id.ll_text_style:
                if (rl_colorPicker.getVisibility() == View.VISIBLE) {
                    rl_colorPicker.setVisibility(View.GONE);
                    img_color.setImageDrawable(getResources().getDrawable(R.drawable.color));
                }
                if (ll_fontView.getVisibility() == View.VISIBLE) {
                    ll_fontView.setVisibility(View.GONE);
                    img_text.setImageDrawable(getResources().getDrawable(R.drawable.text_unselect));
                } else {
                    ll_fontView.setVisibility(View.VISIBLE);
                    img_text.setImageDrawable(getResources().getDrawable(R.drawable.text_select));
                }
                break;
            case R.id.ll_bold:
                if (isBold) {
                    img_bold.setImageDrawable(getResources().getDrawable(R.drawable.bold));
                    isBold = false;
                } else {
                    img_bold.setImageDrawable(getResources().getDrawable(R.drawable.bold_select));
                    isBold = true;
                }
                setTypeFaceText();
                break;
            case R.id.ll_italic:
                if (isItalic) {
                    img_italic.setImageDrawable(getResources().getDrawable(R.drawable.italic));
                    isItalic = false;
                } else {
                    img_italic.setImageDrawable(getResources().getDrawable(R.drawable.italic_select));
                    isItalic = true;
                }
                setTypeFaceText();
                break;
            case R.id.ll_underline:
                if (isUnderline) {
                    img_underline.setImageDrawable(getResources().getDrawable(R.drawable.underline));
                    isUnderline = false;
                } else {
                    img_underline.setImageDrawable(getResources().getDrawable(R.drawable.underline_select));
                    isUnderline = true;
                }
                setTypeFaceText();
                break;
            case R.id.ll_left:
                if (et_text.getGravity() != Gravity.LEFT) {
                    img_left.setImageDrawable(getResources().getDrawable(R.drawable.left_select));
                    img_right.setImageDrawable(getResources().getDrawable(R.drawable.right));
                    img_center.setImageDrawable(getResources().getDrawable(R.drawable.center));
                    et_text.setGravity(Gravity.LEFT | Gravity.CENTER);
                } else {
                    img_left.setImageDrawable(getResources().getDrawable(R.drawable.left));
                    et_text.setGravity(Gravity.CENTER);
                }
                break;
            case R.id.ll_center:
                if (et_text.getGravity() != Gravity.CENTER) {
                    img_center.setImageDrawable(getResources().getDrawable(R.drawable.center_select));
                    img_left.setImageDrawable(getResources().getDrawable(R.drawable.left));
                    img_right.setImageDrawable(getResources().getDrawable(R.drawable.right));
                    et_text.setGravity(Gravity.CENTER);
                } else {
                    img_center.setImageDrawable(getResources().getDrawable(R.drawable.center));
                }
                break;
            case R.id.ll_right:
                if (et_text.getGravity() != Gravity.RIGHT) {
                    et_text.setGravity(Gravity.RIGHT | Gravity.CENTER);
                    img_right.setImageDrawable(getResources().getDrawable(R.drawable.right_select));
                    img_left.setImageDrawable(getResources().getDrawable(R.drawable.left));
                    img_center.setImageDrawable(getResources().getDrawable(R.drawable.center));
                } else {
                    et_text.setGravity(Gravity.CENTER);
                    img_right.setImageDrawable(getResources().getDrawable(R.drawable.right));
                }
                break;
            case R.id.img_save:
                file = new File(getFilesDir(), "Text_1.png");

                //Convert to byte array
                ByteArrayOutputStream stream1 = new ByteArrayOutputStream();
                Bitmap bitmap1 = getTextBitmap();
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, stream1);
                byte[] byteArray1 = stream1.toByteArray();

                Intent intent = new Intent(AddTextActivity.this, EditActivity.class);
                intent.putExtra("filePath", Const.FilePath);
                intent.putExtra("TextBitmap", byteArray1);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.stay);

                break;
            case R.id.img_back:
                onBackPressed();
                break;
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private Bitmap getTextBitmap() {
        et_text.requestLayout();
        et_text.setDrawingCacheEnabled(true);
        et_text.layout(0, 0, et_text.getMeasuredWidth(), et_text.getMeasuredHeight());
        try {
            et_text.getDrawingCache(true).compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            Log.e("LLLL_Error: ",e.getLocalizedMessage());
        }
        System.out.println("Sagartext_path:" + this.file.getAbsolutePath());

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        return bitmap;
    }

    private void setTypeFaceText() {
        SpannableString content = new SpannableString(et_text.getText().toString().trim());
        if (isUnderline) {
            content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        } else {
            content.setSpan(new UnderlineSpan(), 0, 0, 0);
        }
        et_text.setText(content);
        if (isBold)
            et_text.setTypeface(et_text.getTypeface(), Typeface.BOLD);
        if (isBold && isItalic)
            et_text.setTypeface(et_text.getTypeface(), Typeface.BOLD_ITALIC);
        if (isItalic)
            et_text.setTypeface(et_text.getTypeface(), Typeface.ITALIC);
        if (!isBold && !isItalic && !isUnderline)
            et_text.setTypeface(et_text.getTypeface(), Typeface.NORMAL);
        et_text.invalidate();
    }

    public void setTextColor() {
        try {
            color_hpicker.setOnColorChangedListener(color -> {
                et_text.getPaint().setShader(new LinearGradient(0.0f, (float) (int1 * 1), 0.0f, (float) (int1 * 2), new int[]{Color.parseColor(color), Color.parseColor(color)}, new float[]{0.0f, 1.0f}, Shader.TileMode.CLAMP));
                et_text.getPaint().setStrokeWidth(5.0f);
                et_text.invalidate();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private View.OnTouchListener onTouchListener() {
        return (view, event) -> {

            final int x = (int) event.getRawX();
            final int y = (int) event.getRawY();

            switch (event.getAction() & MotionEvent.ACTION_MASK) {

                case MotionEvent.ACTION_DOWN:
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams)
                            view.getLayoutParams();

                    xDelta = x - lParams.leftMargin;
                    yDelta = y - lParams.topMargin;
                    break;

                case MotionEvent.ACTION_UP:

                    break;

                case MotionEvent.ACTION_MOVE:
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view
                            .getLayoutParams();
                    layoutParams.leftMargin = x - xDelta;
                    layoutParams.topMargin = y - yDelta;
                    layoutParams.rightMargin = 0;
                    layoutParams.bottomMargin = 0;
                    view.setLayoutParams(layoutParams);
                    break;
            }
            main.invalidate();
            return true;
        };
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.stay, R.anim.fade_out);
    }
}