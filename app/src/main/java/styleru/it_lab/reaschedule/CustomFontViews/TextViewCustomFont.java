package styleru.it_lab.reaschedule.CustomFontViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import styleru.it_lab.reaschedule.R;

public class TextViewCustomFont extends TextView{

    private static final String DEBUG_TAG = "TextViewCustom_DEBUG";
    private static final String[] FONTS = new String[]{
            "fonts/HelveticaNeueCyr-Light.otf",
            "fonts/HelveticaNeueCyr-Medium.otf",
            "fonts/HelveticaNeueCyr-Roman.otf",
            "fonts/HelveticaNeueCyr-Bold.otf"
    };

    public TextViewCustomFont(Context context) {
        super(context);
    }

    public TextViewCustomFont(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public TextViewCustomFont(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.AttrCustomFont);
        int customFontNum = a.getInteger(R.styleable.AttrCustomFont_customFont, 0);

        String customFont = FONTS[customFontNum];
        setCustomFont(ctx, customFont);
        a.recycle();
    }

    public boolean setCustomFont(Context ctx, String asset) {
        Typeface typeface = null;

        try {
            typeface = Typeface.createFromAsset(ctx.getAssets(), asset);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Unable to load typeface: " + e.getMessage());
            return false;
        }

        setTypeface(typeface);
        return true;
    }

}
