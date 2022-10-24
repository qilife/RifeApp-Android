package com.zappkit.zappid.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.util.AttributeSet;
import android.widget.EditText;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.FontUtils;


public class CustomFontEditText extends EditText {

    public CustomFontEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TextFont, 0, 0);
        fontName = a.getString(R.styleable.TextFont_fontText);
        init();
        try {
            a.recycle();
        } catch (Exception ex) {
        }
    }


    String fontName = null;

    private void init() {
        if (fontName != null) {
            try {
                setTypeface(FontUtils.getTypeface(getContext(), this.fontName));
            } catch (Exception e) {
            }
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            super.setText(Html.fromHtml(text != null ? text.toString() : "", Html.FROM_HTML_MODE_LEGACY), type);
        } else {
            super.setText(Html.fromHtml(text != null ? text.toString() : ""), type);
        }
    }
}