import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.krysalis.barcode4j.impl.pdf417.PDF417Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@Service
public class GeneratePdfService {

    public ByteArrayOutputStream generatePdf() {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            File template = new File("templates/pdf_template.pdf");
            PDDocument pDDocument = PDDocument.load(template);
            // Set text field
            setField(pDDocument, "label_first_name", "Michael Wilson");
            // Add barcode
            PDImageXObject barcode = getBarcode("A123456789");
            setField(pDDocument, "label_barcode", barcode);

            pDDocument.save(outStream);
            pDDocument.close();
        } catch (IOException e) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "Failed to generate pdf");
        } catch (NullPointerException e) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "Template Not Found");
        }
        return outStream;
    }

    private void setField(PDDocument document, String name, String value) throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField(name);
        if (field instanceof PDTextField) {
            ((PDTextField) field).setValue(value);
        }
    }

    private void setField(PDDocument document, String name, PDImageXObject image) throws IOException {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        PDField field = acroForm.getField(name);
        if (field != null) {
            PDRectangle rectangle = getFieldArea(field);
            float size = rectangle.getHeight();
            float x = rectangle.getLowerLeftX();
            float y = rectangle.getLowerLeftY();

            try (PDPageContentStream contentStream = new PDPageContentStream(document, document.getPage(0),
                    PDPageContentStream.AppendMode.APPEND, true)) {
                contentStream.drawImage(image, x, y, size, size);
            } catch(IOException ioe) {
                throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "MESSAGE");
            }
        }
    }

    private PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }

    public PDImageXObject getBarcode(String receiptNumber) {
        PDImageXObject image = null;
        try {
            int dpi = 300;
            BitmapCanvasProvider canvas = new BitmapCanvasProvider(dpi, BufferedImage.TYPE_BYTE_BINARY, false, 0);
            PDF417Bean pdf417Bean = new PDF417Bean();
            pdf417Bean.generateBarcode(canvas, receiptNumber.trim());
            canvas.finish();
            BufferedImage bImage = canvas.getBufferedImage();
            image = JPEGFactory.createFromImage(new PDDocument(), bImage);

        } catch (Exception e) {
            throw new HttpServerErrorException(HttpStatus.BAD_REQUEST, "Failed to generate barcode");
        }
        return image;
    }
}
