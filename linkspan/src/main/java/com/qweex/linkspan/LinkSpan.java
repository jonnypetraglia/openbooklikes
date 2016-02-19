package com.qweex.linkspan;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.klinker.android.link_builder.LinkConsumableTextView;
import com.klinker.android.link_builder.TouchableBaseSpan;

public class LinkSpan extends TouchableBaseSpan {

    private OnLinkClickListener onClickListener;
    private OnLinkLongClickListener onLongClickListener;
    private LinkSpanStyle linkStyle;
    private String linkTarget;

    public LinkSpan(String urlS, int color) {
        this(urlS, new LinkSpanStyle(color));
    }

    public LinkSpan(String urlS, LinkSpanStyle style) {
        linkTarget = urlS;
        linkStyle = style;
    }

    public LinkSpan(URLSpan url, int color) {
        this(url, new LinkSpanStyle(color));
    }

    public LinkSpan(URLSpan url, LinkSpanStyle style) {
        this(url.getURL(), style);
    }

    public LinkSpanStyle getStyle() {
        return linkStyle;
    }

    @Override
    public void onClick(View widget) {
        super.onClick(widget);

        SpannableString widgetContent = (SpannableString)((LinkConsumableTextView) widget).getText();
        String label = widgetContent.subSequence(widgetContent.getSpanStart(this), widgetContent.getSpanEnd(this)).toString();

        if(onClickListener!=null)
            onClickListener.onClick(this, label, linkTarget, (LinkConsumableTextView) widget);
    }

    @Override
    public void onLongClick(View widget) {
        super.onLongClick(widget);

        SpannableString widgetContent = (SpannableString)((LinkConsumableTextView) widget).getText();
        String label = widgetContent.subSequence(widgetContent.getSpanStart(this), widgetContent.getSpanEnd(this)).toString();

        if(onLongClickListener!=null)
            onLongClickListener.onLongClick(this, label, linkTarget, (LinkConsumableTextView) widget);
    }

    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);

        ds.setUnderlineText(linkStyle.underline);
        ds.setFakeBoldText(linkStyle.bold);
        ds.setColor(linkStyle.textColor);
        ds.bgColor = touched ? adjustAlpha(linkStyle.textColor, linkStyle.highlightAlpha) : Color.TRANSPARENT;
        if (linkStyle.typeface != null)
            ds.setTypeface(linkStyle.typeface);
    }



    public interface OnLinkClickListener {
        void onClick(LinkSpan linkSpan, String label, String link, LinkConsumableTextView textView);
    }

    public interface OnLinkLongClickListener {
        void onLongClick(LinkSpan linkSpan, String label, String link, LinkConsumableTextView textView);
    }

    public void setOnClickListener(OnLinkClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(OnLinkLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }



    public static class LinkSpanStyle {
        public int textColor;
        public boolean underline = true, bold = false;
        public Typeface typeface = null;
        public float highlightAlpha = 0.2f;

        public LinkSpanStyle(int color) {this.textColor = color;}

        public LinkSpanStyle(int color, boolean underline, boolean bold, Typeface typeface, float highlightAlpha) {
            this.textColor = color;
            this.underline = underline;
            this.bold = bold;
            this.typeface = typeface;
        }
    }

    public static void replaceURLSpans(TextView view, OnLinkClickListener shortClick, OnLinkLongClickListener longCLick) {
        SpannableString muhText = ((SpannableString)view.getText());
        URLSpan[] urls = muhText.getSpans(0, view.getText().length(), URLSpan.class);
        for(URLSpan url : urls) {
            int start = muhText.getSpanStart(url),
                    end = muhText.getSpanEnd(url),
                    flags = muhText.getSpanFlags(url);
            muhText.removeSpan(url);


            LinkSpanStyle style = new LinkSpanStyle(view.getTextColors().getDefaultColor());
            style.textColor = view.getLinkTextColors().getDefaultColor(); // or maybe #2593de
            LinkSpan muhSpan = new LinkSpan(url, style);


            muhSpan.setOnClickListener(shortClick);
            muhSpan.setOnLongClickListener(longCLick);
            muhText.setSpan(muhSpan, start, end, flags);
        }
    }
}
