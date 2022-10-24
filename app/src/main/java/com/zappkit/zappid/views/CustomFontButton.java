package com.zappkit.zappid.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;

import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.tools.FontUtils;


public class CustomFontButton extends Button {
	public CustomFontButton(Context context) {
		super(context);
	}

	public CustomFontButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CustomFontButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.TextFont, defStyle, 0);
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

}