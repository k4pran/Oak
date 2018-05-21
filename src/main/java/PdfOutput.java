import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PdfOutput {

    private String outputDest;
    private ArrayList<BufferedImage> images;

    public PdfOutput(String outputDest, ArrayList<BufferedImage> images) {
        this.outputDest = outputDest + ".pdf";
        this.images = images;
    }

    public void writePdf() {
        try {
            Document document = new Document();
            Rectangle pageDim = new Rectangle(images.get(0).getWidth(), images.get(0).getHeight());
            document.setPageSize(pageDim);
            PdfWriter.getInstance(document, new FileOutputStream(outputDest));
            document.open();
            for (BufferedImage image : images) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                Image singlePageImage = Image.getInstance(baos.toByteArray());
                document.add(singlePageImage);
            }
            document.close();
        }
        catch (IOException | DocumentException e) {
            System.out.println("Failed to output pdf file to " + outputDest);
            return;
        }
    }
}
