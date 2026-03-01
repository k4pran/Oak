package io.ryanjames.oak.Document;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfOutput {

    private static final Logger LOG = LoggerFactory.getLogger(PdfOutput.class);

    @Inject
    public PdfOutput() {}

    public void writePdf(String outputDest, List<BufferedImage> images) {
        if (images == null || images.isEmpty()) {
            LOG.warn("No images provided, not writing PDF");
            return;
        }

        try (FileOutputStream fos = new FileOutputStream(outputDest)) {
            LOG.info("Writing PDF to {}", outputDest);

            float pageW = images.get(0).getWidth();
            float pageH = images.get(0).getHeight();
            Rectangle pageSize = new Rectangle(pageW, pageH);

            // Create with page size + zero margins
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter.getInstance(document, fos);

            document.open();

            for (int i = 0; i < images.size(); i++) {
                BufferedImage bi = images.get(i);

                // Encode to PNG
                byte[] pngBytes;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    ImageIO.write(bi, "png", baos);
                    baos.flush();
                    pngBytes = baos.toByteArray();
                }

                Image img = Image.getInstance(pngBytes);

                // Force exact fit and origin placement
                img.setAbsolutePosition(0, 0);
                img.scaleAbsolute(pageW, pageH);

                document.add(img);

                if (i < images.size() - 1) {
                    document.newPage();
                }
            }

            document.close();
            LOG.info("PDF has been written to {}", outputDest);

        } catch (IOException | DocumentException e) {
            LOG.error("Failed to output pdf file to {}", outputDest, e);
        }
    }
}