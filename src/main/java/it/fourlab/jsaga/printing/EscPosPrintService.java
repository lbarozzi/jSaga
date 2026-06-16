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
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import java.io.File;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;



@Service
@EnableConfigurationProperties(EscPosProperties.class)
public class EscPosPrintService {

    private static final Logger log = LoggerFactory.getLogger(EscPosPrintService.class);
    private static final String CLASSPATH_LOGO_PATH = "static/logobn.png";
    private static final String TMP_LOGO_PATH = "/tmp/logobn.png";

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
            escPos.initializePrinter();
            escPos.flush();
            RasterBitImageWrapper bitImageWrapper = new RasterBitImageWrapper();
            Bitonal algorithm = new BitonalThreshold(150); 
            writeLogoIfAvailable(escPos, bitImageWrapper, algorithm);

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
            BufferedImage rigaScontrino = createLineItemImage( 0, "Totale scontrino", totalAmount.doubleValue(), 384);

            EscPosImage escposLineImage = new EscPosImage(new CoffeeImageImpl(rigaScontrino), algorithm);
            escPos.write(bitImageWrapper, escposLineImage);

            //escPos.writeLF("");
            //escPos.write(barcode,String.format("%d %s",orderId, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))));
            escPos.feed(2).cut(resolveCutMode());
            escPos.flush();
        } catch (IOException ex) {
            String details = ex.getMessage() == null ? "unknown error" : ex.getMessage();
            throw new EscPosPrinterException("Error while printing receipt: " + details, ex);
        } finally {
            releaseOutputStream();
        }
    }

    protected OutputStream openOutputStream() throws IOException {
        if (properties.isSystemPrintEnabled() && isLinux()) {
            try {
                return openCommandOutputStream("lp", "-d", properties.getPrinterName(), "-o", "raw", "-t", "Java Printing");
            } catch (IOException lpError) {
                try {
                    return openCommandOutputStream("lpr", "-P", properties.getPrinterName(), "-J", "Java Printing", "-l", "-");
                } catch (IOException lprError) {
                    lpError.addSuppressed(lprError);
                    throw lpError;
                }
            }
        }
        return new PrinterOutputStream(PrinterOutputStream.getPrintServiceByName(properties.getPrinterName()));
    }

    private static boolean isLinux() {
        String osName = System.getProperty("os.name", "");
        return osName.toLowerCase().contains("linux");
    }

    private OutputStream openCommandOutputStream(String command, String... args) throws IOException {
        List<String> cmd = new ArrayList<>();
        cmd.add(command);
        cmd.addAll(Arrays.asList(args));

        Process process = new ProcessBuilder(cmd).start();
        return new ProcessBackedOutputStream(process, cmd);
    }

    protected void releaseOutputStream() {
        // no-op for system printer
    }

    private void writeLogoIfAvailable(EscPos escPos, RasterBitImageWrapper bitImageWrapper, Bitonal algorithm) throws IOException {
        ClassPathResource logoResource = new ClassPathResource(CLASSPATH_LOGO_PATH);
        if (logoResource.exists()) {
            try (InputStream logoStream = logoResource.getInputStream()) {
                BufferedImage image = ImageIO.read(logoStream);
                if (image == null) {
                    log.warn("Formato immagine non supportato, logo classpath saltato: {}", CLASSPATH_LOGO_PATH);
                } else {
                    writeImage(escPos, bitImageWrapper, algorithm, image);
                }
            }
            return;
        }

        File imageFile = new File(TMP_LOGO_PATH);
        if (imageFile.exists()) {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                log.warn("Formato immagine non supportato, logo saltato: {}", imageFile.getAbsolutePath());
            } else {
                writeImage(escPos, bitImageWrapper, algorithm, image);
            }
        } else {
            log.warn("Logo non trovato, stampa senza immagine. Cercati: classpath {} oppure {}", CLASSPATH_LOGO_PATH, imageFile.getAbsolutePath());
        }
    }

    private static void writeImage(EscPos escPos, RasterBitImageWrapper bitImageWrapper, Bitonal algorithm, BufferedImage image) throws IOException {
        BufferedImage resized = resizeIfTooWide(image, 384);
        EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(resized), algorithm);
        escPos.write(bitImageWrapper, escposImage);
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
        int colonnaQta = 28;
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

        String qta = quantita >0 ? String.valueOf(quantita): "";
        String prezzo = new DecimalFormat("0.00").format(prezzoUnitario*(quantita>0?quantita:1));
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
        int colonnaQta = 8;
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

    private static final class ProcessBackedOutputStream extends OutputStream {
        private final Process process;
        private final List<String> command;
        private final OutputStream delegate;

        private ProcessBackedOutputStream(Process process, List<String> command) {
            this.process = process;
            this.command = command;
            this.delegate = process.getOutputStream();
        }

        @Override
        public void write(int b) throws IOException {
            delegate.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            delegate.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            IOException writeError = null;
            try {
                delegate.close();
            } catch (IOException ex) {
                writeError = ex;
            }

            try {
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8).trim();
                    String message = "Print command failed (" + String.join(" ", command) + ") with exit code " + exitCode;
                    if (!stderr.isEmpty()) {
                        message += ": " + stderr;
                    }

                    IOException processError = new IOException(message);
                    if (writeError != null) {
                        processError.addSuppressed(writeError);
                    }
                    throw processError;
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                IOException interrupted = new IOException("Interrupted while waiting for print command to complete", ex);
                if (writeError != null) {
                    interrupted.addSuppressed(writeError);
                }
                throw interrupted;
            }

            if (writeError != null) {
                throw writeError;
            }
        }
    }
}
