package pt.up.fe.labtablet.async;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import pt.up.fe.labtablet.R;
import pt.up.fe.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.labtablet.models.DataItem;
import pt.up.fe.labtablet.models.Descriptor;
import pt.up.fe.labtablet.models.FavoriteItem;
import pt.up.fe.labtablet.models.Form;
import pt.up.fe.labtablet.models.FormQuestion;
import pt.up.fe.labtablet.utils.FileMgr;
import pt.up.fe.labtablet.utils.Utils;

/**
 * Transforms the received data to a pdf file and saves it to
 * the intended path
 */
public class AsyncFormPDFGenerator extends AsyncTask<Object, Integer, String> {

    private Exception error;
    private final AsyncTaskHandler<String> mHandler;
    private static final Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18, Font.BOLD);
    private static final Font redFont = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.NORMAL, BaseColor.RED);
    private static final Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16, Font.BOLD);
    private static final Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD);

    public AsyncFormPDFGenerator(AsyncTaskHandler<String> mHandler) {
        this.mHandler = mHandler;
    }

    @Override
    protected String doInBackground(Object... params) {

        //Form, Favorite Name, Context
        if (! (params[0] instanceof Form
                || params[1] instanceof String
                || params[2] instanceof Context)) {
            Log.e("PDF", "Wrong object types received!");
            return null;
        }

        Context mContext = (Context) params[2];
        String favoriteName = (String) params[1];
        Form form = (Form) params[0];
        form.setParent(form.getFormName());
        form.setFormName(form.getFormName() + "_" + new Date().getTime());


        String path = Environment.getExternalStorageDirectory()
                + "/" + mContext.getString(R.string.app_name)
                + "/" + favoriteName + "/"
                + form.getFormName() + ".pdf" ;

        form.setLinkedResourcePath(path);

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            addMetaData(document, form);
            addTitlePage(document, form);
            addContent(document, form);

            document.close();
        } catch (Exception e) {
            error = e;
        }

        //Add data items to the favorite
        DataItem dataItem = new DataItem();
        dataItem.setParent(favoriteName);

        File file = new File(path);

        dataItem.setLocalPath(path);
        dataItem.setHumanReadableSize(FileMgr.humanReadableByteCount(file.length(), false));
        dataItem.setMimeType(FileMgr.getMimeType(file.getPath()));

        ArrayList<Descriptor> itemLevelMetadata = new ArrayList<Descriptor>();

        ArrayList<Descriptor> loadedDescriptors =
                FavoriteMgr.getBaseDescriptors(mContext);

        //If additional metadata is available, it should be added here
        for (Descriptor desc : loadedDescriptors) {
            String tag = desc.getTag();
            if (tag.equals(Utils.TITLE_TAG)) {
                desc.setValue(file.getName());
                itemLevelMetadata.add(desc);
            } else if (tag.equals(Utils.CREATED_TAG)) {
                desc.setValue("" + new Date());
                itemLevelMetadata.add(desc);
            } else if (tag.equals(Utils.DESCRIPTION_TAG)) {
                desc.setValue("");
                itemLevelMetadata.add(desc);
            }
        }

        dataItem.setFileLevelMetadata(itemLevelMetadata);

        FavoriteItem item = FavoriteMgr.getFavorite(mContext, favoriteName);
        item.addDataItem(dataItem);
        item.addFormItem(form);
        FavoriteMgr.updateFavoriteEntry(favoriteName, item, mContext);

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (error != null) {
            mHandler.onFailure(error);
        } else {
            mHandler.onSuccess(result);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        mHandler.onProgressUpdate(values[0]);
    }

    private static void addMetaData(Document document, Form form) {
        document.addTitle(form.getFormName());
        document.addSubject(form.getFormDescription());
        //document.addKeywords("Java, PDF, iText");
        document.addAuthor("LabTablet");
        document.addCreator("LabTablet");
    }

    private static void addTitlePage(Document document, Form form)
            throws DocumentException {
        Paragraph preface = new Paragraph();
        preface.setAlignment(Element.ALIGN_CENTER);
        addEmptyLine(preface, 1);

        //PDF Header
        preface.add(new Paragraph(form.getFormName(), catFont));

        addEmptyLine(preface, 1);

        //Report generated by: name, date
        preface.add(new Paragraph("By: "
                + "LabTablet (c), "
                + new Date(), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                smallBold));

        addEmptyLine(preface, 3);
        preface.add(new Paragraph(form.getFormDescription(),
                smallBold));

        addEmptyLine(preface, 8);

        Paragraph footer = new Paragraph(
                "Document generated with LabTablet application and the iTextPDF java library",
                redFont);

        footer.setAlignment(Element.ALIGN_BOTTOM);
        preface.add(footer);

        document.add(preface);
        document.newPage();
    }

    private static void addContent(Document document, Form form) throws DocumentException {
        Anchor anchor = new Anchor("Answers", catFont);
        anchor.setName("Answers");

        // Second parameter is the number of the chapter
        Chapter catPart = new Chapter(new Paragraph(anchor), 1);

        ArrayList<FormQuestion> fqs = form.getFormQuestions();
        for (FormQuestion fq : fqs) {
            Paragraph subPara = new Paragraph(fq.getQuestion(), subFont);
            Section subCatPart = catPart.addSection(subPara);
            subCatPart.add(new Paragraph("R: " + fq.getValue()));
            if(fq.isMandatory()) {
                subCatPart.add(new Paragraph("(required)"));
            }
        }

        // now add all this to the document
        document.add(catPart);

        // Build metrics section
        anchor = new Anchor("Gathered Metrics", catFont);
        anchor.setName("Gathered Metrics");
        Paragraph metricsParagraph = new Paragraph(anchor);
        addEmptyLine(metricsParagraph, 2);
        catPart = new Chapter(metricsParagraph, 2);

        createMetrics(catPart, form);

        // now add all this to the document
        document.add(catPart);
    }

    private static void createMetrics(Section subCatPart, Form form) {

        PdfPTable table = new PdfPTable(3);
        PdfPCell c1 = new PdfPCell(new Phrase("Metric"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Value"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);

        c1 = new PdfPCell(new Phrase("Unit"));
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(c1);
        table.setHeaderRows(1);

        table.addCell("Date");
        table.addCell("" + new Date());
        table.addCell("N/A");

        table.addCell("Elapsed time");
        table.addCell(form.getElapsedTime());
        table.addCell("Seconds");

        table.addCell("Estimated time");
        table.addCell("" + form.getDuration());
        table.addCell("Seconds");

        table.addCell("# Questions");
        table.addCell("" + form.getFormQuestions().size());
        table.addCell("N/A");

        ArrayList<FormQuestion> formQuestions = form.getFormQuestions();
        int answeredCount = 0;
        for (FormQuestion fq : formQuestions) {
                if (fq.getValue() != null &&
                        !fq.getValue().equals("")) {

                    ++answeredCount;
                }
        }

        table.addCell("# Answered questions");
        table.addCell("" + answeredCount);
        table.addCell("N/A");

        subCatPart.add(table);
    }

    private static void addEmptyLine(Paragraph paragraph, int number) {
        for (int i = 0; i < number; i++) {
            paragraph.add(new Paragraph(" "));
        }
    }
}