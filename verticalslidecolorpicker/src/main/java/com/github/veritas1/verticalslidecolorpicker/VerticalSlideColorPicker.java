package com.github.veritas1.verticalslidecolorpicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Mark on 11/08/2016.
 */

public class VerticalSlideColorPicker extends View {

	private Paint paint;
	private Paint strokePaint;
	private Paint selectorStrokePaint;
	private Path path;
	private Bitmap bitmap;
	private int viewWidth;
	private int viewHeight;
	private float centerX;
	private float colorPickerRadius;
	private OnColorChangeListener onColorChangeListener;
	private RectF colorPickerBody;
	private float selectorYPos;
	private int borderColor;
	private float borderWidth;
	private int[] colors;
	private int defaultColor = Color.TRANSPARENT;
	private int selectedColor = defaultColor;
	private boolean selectorYPosNeedsUpdate;
	private boolean cacheBitmap = true;

	public VerticalSlideColorPicker(Context context) {
		super(context);
		init();
	}

	public VerticalSlideColorPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(
			attrs,
			R.styleable.VerticalSlideColorPicker,
			0, 0);

		try {
			borderColor = a.getColor(R.styleable.VerticalSlideColorPicker_vBorderColor, Color.WHITE);
			borderWidth = a.getDimension(R.styleable.VerticalSlideColorPicker_vBorderWidth, 5f);
			int colorsResourceId = a.getResourceId(R.styleable.VerticalSlideColorPicker_vColors, R.array.vscp_default_colors);
			colors = a.getResources().getIntArray(colorsResourceId);
			defaultColor = a.getColor(R.styleable.VerticalSlideColorPicker_vDefaultColor, defaultColor);
		} finally {
			a.recycle();
		}
		init();
	}

	public VerticalSlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public VerticalSlideColorPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init();
	}

	private void init() {
		setWillNotDraw(false);
		paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);

		path = new Path();

		strokePaint = new Paint();
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setColor(borderColor);
		strokePaint.setAntiAlias(true);
		strokePaint.setStrokeWidth(borderWidth);

		selectorStrokePaint = new Paint(strokePaint);

		setDrawingCacheEnabled(true);

		setColor(defaultColor);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawPath(path, paint);
		canvas.drawPath(path, strokePaint);

		if (cacheBitmap) {
			bitmap = getDrawingCache();
			cacheBitmap = false;
			if(selectorYPosNeedsUpdate) {
				updateSelectedY();
			} else {
				invalidate();
			}
		} else {
			canvas.drawLine(colorPickerBody.left + borderWidth/2, selectorYPos, colorPickerBody.right - borderWidth/2, selectorYPos, selectorStrokePaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		float yPos = Math.min(event.getY(), colorPickerBody.bottom);
		yPos = Math.max(colorPickerBody.top, yPos);

		selectorYPos = yPos;
		selectedColor = bitmap.getPixel(viewWidth/2, (int) selectorYPos);

		updateSelectorAlpha();

		if (onColorChangeListener != null) {
			onColorChangeListener.onColorChange(selectedColor);
		}

		invalidate();

		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		viewWidth = w;
		viewHeight = h;

		centerX = viewWidth / 2;

		colorPickerRadius = (viewWidth/2) - borderWidth;

		colorPickerBody = new RectF(centerX - colorPickerRadius, borderWidth + colorPickerRadius,
				       centerX + colorPickerRadius, viewHeight - (borderWidth + colorPickerRadius));

		updatePath();
		updatePaint();

		resetToDefault();
	}

	private void updatePaint() {
		// some old versions of AS preview only supports 2 colors linear gradients
		LinearGradient gradient = new LinearGradient(
				0, colorPickerBody.top,
				0, colorPickerBody.bottom,
				colors, null, Shader.TileMode.CLAMP);

		paint.setShader(gradient);
	}

	private void updatePath() {
		float centerTopY = borderWidth + colorPickerRadius;
		float centerBottomY = viewHeight - (borderWidth + colorPickerRadius);

		path.reset();
		path.moveTo(centerX-colorPickerRadius, centerBottomY);
		path.lineTo(centerX-colorPickerRadius, centerTopY);
		path.arcTo(new RectF(centerX-colorPickerRadius, centerTopY-colorPickerRadius,
				centerX+colorPickerRadius, centerTopY+colorPickerRadius),
				180, 180, false);
		path.lineTo(centerX + colorPickerRadius, centerBottomY);
		path.arcTo(new RectF(centerX-colorPickerRadius, centerBottomY-colorPickerRadius,
				centerX+colorPickerRadius, centerBottomY+colorPickerRadius),
				0, 180, false);
		path.close();
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
		invalidate();
	}

	public void setBorderWidth(float borderWidth) {
		this.borderWidth = borderWidth;
		invalidate();
	}

	public void setColors(int[] colors) {
		this.colors = colors;
		cacheBitmap = true;
		invalidate();
	}

	// from android.support.v4.graphics.ColorUtils#calculateLuminance
	private static double calculateLuminance(int color) {
		double red = Color.red(color) / 255d;
		red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);

		double green = Color.green(color) / 255d;
		green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);

		double blue = Color.blue(color) / 255d;
		blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);

		return (0.2126 * red) + (0.7152 * green) + (0.0722 * blue);
	}

	public void resetToDefault() {
		selectedColor = defaultColor;

		updateSelectedY();

		if (onColorChangeListener != null) {
			onColorChangeListener.onColorChange(defaultColor);
		}

		invalidate();
	}

	private void updateSelectedY() {
		if(bitmap == null) {
			selectorYPosNeedsUpdate = true;
			return;
		}

		selectorYPosNeedsUpdate = false;
		int i;
		for (i = 0; i < colors.length; i++) {
			if(colors[i] == selectedColor) {
				break;
			}
		}

		if(i < colors.length) {
			selectorYPos = colorPickerBody.top + (colorPickerBody.bottom - colorPickerBody.top) / (colors.length - 1) * i;
		} else {
            selectorYPos = colorPickerBody.top;
        }
	}

	private void updateSelectorAlpha() {
		int scaleValue = 255 - (int) (calculateLuminance(selectedColor) * 255);
		selectorStrokePaint.setColor(Color.rgb(scaleValue, scaleValue, scaleValue));
	}

	private void setColor(int color) {
		if(selectedColor != color) {
			selectedColor = color;
			updateSelectedY();
			updateSelectorAlpha();
		}
	}

	public void setOnColorChangeListener(OnColorChangeListener onColorChangeListener) {
		this.onColorChangeListener = onColorChangeListener;
	}

	public interface OnColorChangeListener {

		void onColorChange(int selectedColor);
	}
}
