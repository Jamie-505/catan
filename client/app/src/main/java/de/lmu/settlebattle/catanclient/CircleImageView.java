package de.lmu.settlebattle.catanclient;

import static de.lmu.settlebattle.catanclient.utils.Constants.CLAY;
import static de.lmu.settlebattle.catanclient.utils.Constants.ORE;
import static de.lmu.settlebattle.catanclient.utils.Constants.WHEAT;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOD;
import static de.lmu.settlebattle.catanclient.utils.Constants.WOOL;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import android.view.Gravity;
import de.lmu.settlebattle.catanclient.grid.Hex;
import de.lmu.settlebattle.catanclient.grid.building.BuildingView;
import java.util.ArrayList;

public class CircleImageView extends AppCompatImageView {

  private static final ScaleType SCALE_TYPE = ScaleType.CENTER; //WICHTIG DAS CENTER

  private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;
  private static final int COLORDRAWABLE_DIMENSION = 1;

  private static final int DEFAULT_BORDER_WIDTH = 0;
  private static final int SELECTED_BORDER_WIDTH = 0;
  private static final int DEFAULT_BORDER_COLOR = Color.TRANSPARENT;
  private static final int SELECTED_BORDER_COLOR = Color.TRANSPARENT;

  private final RectF mDrawableRect = new RectF();
  private final RectF mBorderRect = new RectF();

  private final Matrix mShaderMatrix = new Matrix();
  private final Paint mBitmapPaint = new Paint();
  private final Paint mBorderPaint = new Paint();

  private int mBorderColor = DEFAULT_BORDER_COLOR;
  private int mBorderWidth = DEFAULT_BORDER_WIDTH;

  private Bitmap mBitmap;
  private BitmapShader mBitmapShader;
  private int mBitmapWidth;
  private int mBitmapHeight;

  private float mDrawableRadius;
  private float mBorderRadius;

  private ColorFilter mColorFilter;

  private boolean mReady;
  private boolean mSetupPending;

  private Hex mHex; //Hold the node coordinates on the grid
  private Drawable[] layers = new Drawable[3];
  private ArrayList<Integer> owners = new ArrayList<>();

  public CircleImageView(Context context) {
    super(context);

    init();
  }

  public CircleImageView(Context context, String type, int number) {
    super(context);
    setLayers(type, number);
    init();
  }

  public CircleImageView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyle, 0);

    mBorderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
    mBorderColor = a.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);

    a.recycle();

    init();
  }

  private void init() {
    super.setScaleType(SCALE_TYPE);
    mReady = true;

    if (mSetupPending) {
      setup();
      mSetupPending = false;
    }
  }

  @Override
  public ScaleType getScaleType() {
    return SCALE_TYPE;
  }

  @Override
  public void setScaleType(ScaleType scaleType) {
    if (scaleType != SCALE_TYPE) {
      throw new IllegalArgumentException(String.format("ScaleType %s not supported.", scaleType));
    }
  }

  @Override
  public void setAdjustViewBounds(boolean adjustViewBounds) {
    if (adjustViewBounds) {
      throw new IllegalArgumentException("adjustViewBounds not supported.");
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (getDrawable() == null) {
      return;
    }

    canvas.drawCircle(getWidth() / 2, getHeight() / 2, mDrawableRadius, mBitmapPaint);
    if (mBorderWidth != 0) {
      canvas.drawCircle(getWidth() / 2, getHeight() / 2, mBorderRadius, mBorderPaint);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    setup();
  }

  public void addOwner(int id) {
    if (!owners.contains(id)){
      owners.add(id);
    }
  }

  public ArrayList<Integer> getOwners() {
    return owners;
  }

  public Hex getHex() {
    return mHex;
  }

  public void setHex(Hex hex) {
    mHex = hex;
    this.setTag(BuildingView.createTag(mHex.getLocation()));
  }

  public int getBorderColor() {
    return mBorderColor;
  }

  public void setBorderColor(int borderColor) {
    if (borderColor == mBorderColor) {
      return;
    }

    mBorderColor = borderColor;
    mBorderPaint.setColor(mBorderColor);
    invalidate();
  }

  private void setLayers(String type, int number) {
    switch (type) {
      case CLAY:
        layers[0] = this.getResources().getDrawable(R.drawable.clay_field);
        break;
      case ORE:
        layers[0] = this.getResources().getDrawable(R.drawable.ore_field);
        break;
      case WHEAT:
        layers[0] = this.getResources().getDrawable(R.drawable.wheat_field);
        break;
      case WOOD:
        layers[0] = this.getResources().getDrawable(R.drawable.woods_field);
        break;
      case WOOL:
        layers[0] = this.getResources().getDrawable(R.drawable.sheep_field);
        break;
      default:
        layers[0] = this.getResources().getDrawable(R.drawable.desert_field);
        layers[1] = this.getResources().getDrawable(R.drawable.desert_field);
        break;
    }
    switch (number) {
      case 2:
        layers[1] = this.getResources().getDrawable(R.drawable.no_2);
        break;
      case 3:
        layers[1] = this.getResources().getDrawable(R.drawable.no_3);
        break;
      case 4:
        layers[1] = this.getResources().getDrawable(R.drawable.no_4);
        break;
      case 5:
        layers[1] = this.getResources().getDrawable(R.drawable.no_5);
        break;
      case 6:
        layers[1] = this.getResources().getDrawable(R.drawable.no_6);
        break;
      // no seven because robber
      case 8:
        layers[1] = this.getResources().getDrawable(R.drawable.no_8);
        break;
      case 9:
        layers[1] = this.getResources().getDrawable(R.drawable.no_9);
        break;
      case 10:
        layers[1] = this.getResources().getDrawable(R.drawable.no_10);
        break;
      case 11:
        layers[1] = this.getResources().getDrawable(R.drawable.no_11);
        break;
      case 12:
        layers[1] = this.getResources().getDrawable(R.drawable.no_12);
        break;
    }
    layers[2] = this.getResources().getDrawable(R.drawable.robber).mutate();
    // set robber visible on desert in beginning
    if (!type.equals("DESERT")) {
      layers[2].setAlpha(0);
    }
    LayerDrawable layerImg = new LayerDrawable(layers);
    layerImg.setLayerGravity(1, Gravity.CENTER);
    layerImg.setLayerGravity(2, Gravity.CENTER);
    this.setImageDrawable(layerImg);
  }

  public void showRobber(boolean show) {
    if (show) {
      layers[2] = getResources().getDrawable(R.drawable.robber).mutate();
    } else {
      layers[2] = getResources().getDrawable(R.drawable.robber).mutate();
      layers[2].setAlpha(0);
    }
    LayerDrawable layerImg = new LayerDrawable(layers);
    layerImg.setLayerGravity(1, Gravity.CENTER);
    layerImg.setLayerGravity(2, Gravity.CENTER);
    this.setImageDrawable(layerImg);
  }

  public void setSelected(boolean selected) {
    //super.setSelected(selected);

    if(selected) {
      mBorderColor = SELECTED_BORDER_COLOR;
      setBorderWidth(SELECTED_BORDER_WIDTH);
    } else {
      mBorderColor = DEFAULT_BORDER_COLOR;
      setBorderWidth(DEFAULT_BORDER_WIDTH);
    }
  }

  public int getBorderWidth() {
    return mBorderWidth;
  }

  public void setBorderWidth(int borderWidth) {
    if (borderWidth == mBorderWidth) {
      return;
    }

    mBorderWidth = borderWidth;
    setup();
  }

  @Override
  public void setImageBitmap(Bitmap bm) {
    super.setImageBitmap(bm);
    mBitmap = bm;
    setup();
  }

  @Override
  public void setImageDrawable(Drawable drawable) {
    super.setImageDrawable(drawable);
    mBitmap = getBitmapFromDrawable(drawable);
    setup();
  }

  @Override
  public void setImageResource(int resId) {
    super.setImageResource(resId);
    mBitmap = getBitmapFromDrawable(getDrawable());
    setup();
  }

  @Override
  public void setImageURI(Uri uri) {
    super.setImageURI(uri);
    mBitmap = getBitmapFromDrawable(getDrawable());
    setup();
  }

  @Override
  public void setColorFilter(ColorFilter cf) {
    if (cf == mColorFilter) {
      return;
    }

    mColorFilter = cf;
    mBitmapPaint.setColorFilter(mColorFilter);
    invalidate();
  }

  private Bitmap getBitmapFromDrawable(Drawable drawable) {
    if (drawable == null) {
      return null;
    }

    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    try {
      Bitmap bitmap;

      if (drawable instanceof ColorDrawable) {
        bitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
      } else {
        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
      }

      Canvas canvas = new Canvas(bitmap);
      drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
      drawable.draw(canvas);
      return bitmap;
    } catch (OutOfMemoryError e) {
      return null;
    }
  }

  private void setup() {
    if (!mReady) {
      mSetupPending = true;
      return;
    }

    if (mBitmap == null) {
      return;
    }

    mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

    mBitmapPaint.setAntiAlias(true);
    mBitmapPaint.setShader(mBitmapShader);

    mBorderPaint.setStyle(Paint.Style.STROKE);
    mBorderPaint.setAntiAlias(true);
    mBorderPaint.setColor(mBorderColor);
    mBorderPaint.setStrokeWidth(mBorderWidth);

    mBitmapHeight = mBitmap.getHeight();
    mBitmapWidth = mBitmap.getWidth();

    mBorderRect.set(0, 0, getWidth(), getHeight());
    mBorderRadius = Math.min((mBorderRect.height() - mBorderWidth) / 2, (mBorderRect.width() - mBorderWidth) / 2);

    mDrawableRect.set(mBorderWidth, mBorderWidth, mBorderRect.width() - mBorderWidth, mBorderRect.height() - mBorderWidth);
    mDrawableRadius = Math.max(mDrawableRect.height() / 2, mDrawableRect.width() / 2);

    updateShaderMatrix();
    invalidate();
  }

  private void updateShaderMatrix() {
    float scale;
    float dx = 0;
    float dy = 0;

    mShaderMatrix.set(null);

    if (mBitmapWidth * mDrawableRect.height() > mDrawableRect.width() * mBitmapHeight) {
      scale = mDrawableRect.height() / (float) mBitmapHeight;
      dx = (mDrawableRect.width() - mBitmapWidth * scale) * 0.5f;
    } else {
      scale = mDrawableRect.width() / (float) mBitmapWidth;
      dy = (mDrawableRect.height() - mBitmapHeight * scale) * 0.5f;
    }

    mShaderMatrix.setScale(scale, scale);
    mShaderMatrix.postTranslate((int) (dx + 0.5f) + mBorderWidth, (int) (dy + 0.5f) + mBorderWidth);

    mBitmapShader.setLocalMatrix(mShaderMatrix);
  }

}