package it.fourlab.jsaga.printing;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.output.PrinterOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;
import java.io.File;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;



@Service
@EnableConfigurationProperties(EscPosProperties.class)
public class EscPosPrintService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    protected final EscPosProperties properties;

    public EscPosPrintService(EscPosProperties properties) {
        this.properties = properties;
    }

    public void printOrder(String title, List<PrintLine> lines, BigDecimal totalAmount,long orderId) {
        if (!properties.isEnabled()) {
            throw new EscPosPrinterException("ESC/POS printing is disabled. Set jsaga.print.escpos.enabled=true");
        }

        try (OutputStream outputStream = openOutputStream(); EscPos escPos = new EscPos(outputStream)) {
            RasterBitImageWrapper bitImageWrapper = new RasterBitImageWrapper();
            Bitonal algorithm = new BitonalThreshold(150); 
            File imageFile = new File("/tmp/logobn.png");
            if(!imageFile.exists()) {
                throw new IOException("Immagine non trovata: " + imageFile.getAbsolutePath());
            }

            BufferedImage image = ImageIO.read(imageFile);
            if(image == null) {
                throw new IOException("Formato immagine non supportato: " + imageFile.getAbsolutePath());
            } else {
                image = resizeIfTooWide(image, 384);
                EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(image), algorithm);
                escPos.write(bitImageWrapper, escposImage);
            }

            BarCode barcode = new BarCode();
            //escPos.writeLF(title);
            escPos.writeLF("Data: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
            escPos.writeLF("------------------------------");

            for (PrintLine line : lines) {
                BufferedImage rigaScontrino = createLineItemImage(
                        line.qty(), line.name(), line.unitPrice().doubleValue(), 384);
                EscPosImage escposLineImage = new EscPosImage(new CoffeeImageImpl(rigaScontrino), algorithm);
                escPos.write(bitImageWrapper, escposLineImage);
            }

            escPos.writeLF("------------------------------");
            //escPos.writeLF("Totale: EUR " + totalAmount);
            BufferedImage rigaScontrino = createLineItemImage(
                        "Totale: € " + totalAmount);
            EscPosImage escposLineImage = new EscPosImage(new CoffeeImageImpl(rigaScontrino), algorithm);
            escPos.write(bitImageWrapper, escposLineImage);

            //
            escPos.write(barcode,String.format("%012d/%s",orderId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
            escPos.feed(4).cut(resolveCutMode());
            escPos.close();
        } catch (IOException ex) {
            throw new EscPosPrinterException("Error while printing receipt", ex);
        } finally {
            releaseOutputStream();
        }
    }

    protected OutputStream openOutputStream() throws IOException {
        return new PrinterOutputStream(PrinterOutputStream.getPrintServiceByName(properties.getPrinterName()));
    }

    protected void releaseOutputStream() {
        // no-op for system printer
    }

    //*************************
    private static BufferedImage resizeIfTooWide(BufferedImage src, int maxWidth) {
        if (src.getWidth() <= maxWidth) {
            return src;
        }

        int newWidth = maxWidth;
        int newHeight = (src.getHeight() * newWidth) / src.getWidth();
        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(src, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        return resized;
    }

    private static BufferedImage createLineItemImage(int quantita, String descrizione, double prezzoUnitario, int larghezzaPx) {
        int altezzaPx = 30;
        int padding = 1;
        int colonnaQta = 48;
        int colonnaEuro = 18;
        int colonnaPrezzo = 84;
        int colonnaDescrizione = larghezzaPx - (padding * 2) - colonnaQta - colonnaPrezzo - colonnaEuro;

        BufferedImage lineImage = new BufferedImage(larghezzaPx, altezzaPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = lineImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, larghezzaPx, altezzaPx);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        int baseline = (altezzaPx - fm.getHeight()) / 2 + fm.getAscent();

        String qta = String.valueOf(quantita);
        String prezzo = new DecimalFormat("0.00").format(prezzoUnitario);
        String desc = fitTextToWidth(g2d, descrizione, colonnaDescrizione);

        int xQta = padding;
        int xDesc = xQta + colonnaQta;
        int xPriceRight = xDesc + colonnaDescrizione + colonnaPrezzo;
        int xEuro = larghezzaPx - padding - colonnaEuro;

        g2d.drawString(qta, xQta, baseline);
        g2d.drawString(desc, xDesc, baseline);
        g2d.drawString(prezzo, xPriceRight - fm.stringWidth(prezzo), baseline);
        g2d.drawString("€", xEuro, baseline);

        g2d.dispose();
        return lineImage;
    }
    private static BufferedImage createLineItemImage(String testo){
        int altezzaPx = 30;
        int padding = 1;
        int colonnaQta = 48;
        int colonnaEuro = 18;
        int colonnaPrezzo = 84;
        int colonnaDescrizione = 384 - (padding * 2) - colonnaQta - colonnaPrezzo - colonnaEuro;

        BufferedImage lineImage = new BufferedImage(384, altezzaPx, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = lineImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, 384, altezzaPx);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        int baseline = (altezzaPx - fm.getHeight()) / 2 + fm.getAscent();

        String desc = fitTextToWidth(g2d, testo, colonnaDescrizione);

        int xDesc = padding;

        g2d.drawString(desc, xDesc, baseline);

        g2d.dispose();
        return lineImage;
    }

    private static String fitTextToWidth(Graphics2D g2d, String text, int maxWidthPx) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        FontMetrics fm = g2d.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidthPx) {
            return text;
        }

        String ellipsis = "...";
        int allowed = maxWidthPx - fm.stringWidth(ellipsis);
        if (allowed <= 0) {
            return ellipsis;
        }

        int i = text.length();
        while (i > 0 && fm.stringWidth(text.substring(0, i)) > allowed) {
            i--;
        }

        return text.substring(0, i) + ellipsis;
    }
    //*************************

    private EscPos.CutMode resolveCutMode() {
        if ("partial".equalsIgnoreCase(properties.getCutMode())) {
            return EscPos.CutMode.PART;
        }
        return EscPos.CutMode.FULL;
    }
}
