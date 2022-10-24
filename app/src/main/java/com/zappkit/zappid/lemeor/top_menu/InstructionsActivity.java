package com.zappkit.zappid.lemeor.top_menu;

import android.util.Log;
import android.view.View;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.shockwave.pdfium.PdfDocument;
import com.zappkit.zappid.R;
import com.zappkit.zappid.lemeor.base.BaseActivity;

import java.util.List;

public class InstructionsActivity extends BaseActivity implements OnPageChangeListener, OnLoadCompleteListener {

    private static final String TAG = InstructionsActivity.class.getSimpleName();
    public static final String SAMPLE_FILE = "qfa_user_guide.pdf";
    private PDFView mPdfView;
    Integer pageNumber = 0;
    String pdfFileName;

    @Override
    protected int initLayout() {
        return R.layout.activity_instructions;
    }

    @Override
    protected void initComponents() {
        setTitle(getString(R.string.txt_instructions));
        hiddenNavRight();
        mPdfView = findViewById(R.id.pdfView);
        displayFromAsset(SAMPLE_FILE);
    }

    private void displayFromAsset(String assetFileName) {
        pdfFileName = assetFileName;

        mPdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .load();
    }

    @Override
    protected void addListener() {
        showNavLeft(R.drawable.ic_back, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = mPdfView.getDocumentMeta();
        printBookmarksTree(mPdfView.getTableOfContents(), "-");
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
    }
}
