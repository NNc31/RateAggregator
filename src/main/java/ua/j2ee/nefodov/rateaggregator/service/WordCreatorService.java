package ua.j2ee.nefodov.rateaggregator.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ua.j2ee.nefodov.rateaggregator.model.Rate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class WordCreatorService implements FileCreatorService {

    private static final Logger logger = LoggerFactory.getLogger(WordCreatorService.class);

    @Override
    public Object createFile(List<Rate> rateList) {
        logger.debug("Creating .docx file");
        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();

        XWPFRun run = paragraph.createRun();
        run.setFontFamily("Times New Roman");
        run.setFontSize(12);
        run.setText("Date: " + rateList.get(0).getDate());
        run.addBreak();
        run.setText("Currency: " + rateList.get(0).getCurrency());
        run.addBreak();
        run.addBreak();

        for (Rate rate : rateList) {
            run.setText("Service: " + rate.getService());
            run.addBreak();
            run.setText("Sell rate: " + rate.getSellRate());
            run.addBreak();
            run.setText("Purchase rate: " + rate.getPurchaseRate());
            run.addBreak();
            run.addBreak();
        }

        return document;
    }

    @Override
    public byte[] fileToBytes(Object file) {
        logger.debug("Converting .docx to byte array");

        XWPFDocument document;
        if (file instanceof XWPFDocument) {
            document = (XWPFDocument) file;
        } else {
            throw new IllegalArgumentException("Cannot convert .docx document");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()){
            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            logger.warn("Caught IOException for .docx file");
            throw new IllegalStateException("Error while converting .docx document");
        }
    }
}
