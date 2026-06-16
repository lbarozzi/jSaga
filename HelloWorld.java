import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import javax.print.PrintService;

public class HelloWorld {
    public static void main(String[] args) throws IOException {
        if(args.length!=1){
            System.out.println("Usage: java -jar escpos-simple.jar (\"printer name\")");
            System.out.println("Printer list to use:");
            String[] printServicesNames = PrinterOutputStream.getListPrintServicesNames();
            for(String printServiceName: printServicesNames){
                System.out.println(printServiceName);
            }

            System.exit(0);
        }

        PrintService printService = PrinterOutputStream.getPrintServiceByName(args[0]);
        PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
        EscPos escpos = new EscPos(printerOutputStream);
            
        Bitonal algorithm = new BitonalThreshold(150); 
        File imageFile = new File("/tmp/logobn.png");
        if(!imageFile.exists()) {
            throw new IOException("Immagine non trovata: " + imageFile.getAbsolutePath());
        }

        BufferedImage image = ImageIO.read(imageFile);
        if(image == null) {
            throw new IOException("Formato immagine non supportato: " + imageFile.getAbsolutePath());
        }

        image = resizeIfTooWide(image, 384);
        EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(image), algorithm);
        RasterBitImageWrapper bitImageWrapper = new RasterBitImageWrapper();
        
        BarCode barcode = new BarCode();
        
        //escpos.writeLF("barcode default options CODE93 system");
        escpos.feed(2);
            
        for (int i = 0; i < 2; i++) {
            escpos.writeLF("******* $$ *******");
            escpos.write(bitImageWrapper, escposImage);
            escpos.writeLF("");
            for(int riga=0; riga<15; riga++) {
                BufferedImage rigaScontrino = createLineItemImage(riga, "Caffe Arabica 100% - macinato", 1.90, 384);
                EscPosImage escposLineImage = new EscPosImage(new CoffeeImageImpl(rigaScontrino), algorithm);
                escpos.write(bitImageWrapper, escposLineImage);
            }
            escpos.writeLF("");
            escpos.writeLF("******* $$ *******");
            //*
            //BarCode barcode = new BarCode();
            escpos.write(barcode,"800225088050");
            escpos.feed(1);
            //*/
            //escpos.feed(2);
        }
        escpos.feed(3).cut(EscPos.CutMode.PART);
        // Il full pianta la stapante
        //.cut(EscPos.CutMode.FULL);
        
        escpos.flush();

        escpos.close();
        printerOutputStream.close();
        printerOutputStream = null;
        printService = null;    
        
    }

    private static BufferedImage resizeIfTooWide(BufferedImage src, int maxWidth) {
        if(src.getWidth() <= maxWidth) {
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

    private static String fitTextToWidth(Graphics2D g2d, String text, int maxWidthPx) {
        if(text == null || text.isEmpty()) {
            return "";
        }

        FontMetrics fm = g2d.getFontMetrics();
        if(fm.stringWidth(text) <= maxWidthPx) {
            return text;
        }

        String ellipsis = "...";
        int allowed = maxWidthPx - fm.stringWidth(ellipsis);
        if(allowed <= 0) {
            return ellipsis;
        }

        int i = text.length();
        while(i > 0 && fm.stringWidth(text.substring(0, i)) > allowed) {
            i--;
        }

        return text.substring(0, i) + ellipsis;
    }
}
