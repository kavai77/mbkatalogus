package com.himadri;

import com.himadri.model.Box;
import com.himadri.model.Page;
import com.himadri.renderer.PageRenderer;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DColorMapper;
import de.rototor.pdfbox.graphics2d.IPdfBoxGraphics2DImageEncoder;
import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

@Component
public class RenderingTest {

    @Autowired
    private PageRenderer pageRenderer;

    public String testRendering() throws IOException {

        Box[] boxes = new Box[2];
        boxes[0]  = new Box("3645.psd", null, "mélykúti csőszivattyú, 750W, 3\"",
                "CS 2", 0, Collections.singletonList(new Box.Article("8895010", "56400", "3\" testátmérő, szállító teljesítmény: 1,9m3/h, max.száll. 102m, csőcsonk: 1\"")));

        boxes[1]  = new Box("19101a.psd", null, "NiCd pótakku 14,4V a 402313 akkus csavarhúzókhoz",
                "CS 2", 1, Collections.singletonList(new Box.Article("8895200", "43900", "max./névleges nyomás: 140/100 Bar, max. 400 liter/óra, 5m tömlő, max. 40 °C, mosószer tartály, 12,7 kg, papír doboz")));
        Page[] pages = new Page[2];
        pages[0] = new Page("Kéziszerszám Katalógus", "Munkaruházat", "16", Page.Orientation.LEFT,
                new Random().ints(16, 0, boxes.length).mapToObj(i -> boxes[i]).collect(Collectors.toList()));
        pages[1] = new Page("Kéziszerszám Katalógus", "Pneumatikus gépek", "17", Page.Orientation.RIGHT,
                new Random().ints(16, 0, boxes.length).mapToObj(i -> boxes[i]).collect(Collectors.toList()));

        PDDocument doc = new PDDocument();
        for (Page page: pages) {
            PDPage pdPage = new PDPage(PDRectangle.A4);
            PdfBoxGraphics2D g2 = new PdfBoxGraphics2D(doc, PDRectangle.A4.getWidth(), PDRectangle.A4.getHeight());
            doc.addPage(pdPage);
            setCommonGraphics(g2);
            pageRenderer.drawPage(g2, page);
            g2.dispose();
            PDPageContentStream contentStream = new PDPageContentStream(doc, pdPage);
            contentStream.drawForm(g2.getXFormObject());
            contentStream.close();
        }
        File pdfFile = new File(Settings.RENDERING_LOCATION, "out.pdf");
        doc.save(pdfFile);
        doc.close();
        return pdfFile.getName();
    }

    private static void setCommonGraphics(PdfBoxGraphics2D g2) {
        g2.setColorMapper(new IPdfBoxGraphics2DColorMapper() {
            @Override
            public PDColor mapColor(PDPageContentStream pdPageContentStream, Color color) {
                if (color == null)
                    return new PDColor(new float[] { 0, 0, 0, 1f }, PDDeviceCMYK.INSTANCE);

                float[] c = color.getRGBColorComponents(null);
                float k = 1 - Math.max(c[0], Math.max(c[1], c[2]));
                if (k == 1) {
                    return new PDColor(new float[]{0, 0, 0, 1}, PDDeviceCMYK.INSTANCE);
                } else {
                    return new PDColor(new float[]{
                            (1 - c[0] - k) / (1 - k),
                            (1 - c[1] - k) / (1 - k),
                            (1 - c[2] - k) / (1 - k),
                            k}, PDDeviceCMYK.INSTANCE);
                }
            }
        });
        g2.setImageEncoder(new IPdfBoxGraphics2DImageEncoder() {
            @Override
            public PDImageXObject encodeImage(PDDocument document, PDPageContentStream contentStream, Image image) {
                try {
                    return LosslessFactory.createFromImage(document, (BufferedImage) image);
                } catch (IOException e) {
                    throw new RuntimeException("Could not encode Image", e);
                }
            }
        });
    }
}
