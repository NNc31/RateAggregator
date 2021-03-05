package ua.j2ee.nefodov.rateaggregator.model;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class WordCreator {

    private static final Logger logger = LoggerFactory.getLogger(WordCreator.class);

    public static XWPFDocument createWord(List<CommonDto> dtoList) {
        logger.debug("Creating .docx file");
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();

        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("Date: " + dtoList.get(0).getDate());
        run.addBreak();
        run.setText("Currency: " + dtoList.get(0).getCurrency());
        run.addBreak();
        run.addBreak();

        for (CommonDto dto : dtoList) {
            run.setText("Service: " + dto.getService());
            run.addBreak();
            run.setText("Sell rate: " + dto.getSellRate());
            run.addBreak();
            run.setText("Purchase rate: " + dto.getPurchaseRate());
            run.addBreak();
            run.addBreak();
        }

        return document;
    }

    public static byte[] wordToBytes(XWPFDocument document) {
        logger.debug("Converting .docx to byte array");
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            logger.warn("Caught IOException for .docx file");
            throw new IllegalStateException("Error while converting .docx document");
            //throw new ("Error while sending .docx document");
        }
    }
}
