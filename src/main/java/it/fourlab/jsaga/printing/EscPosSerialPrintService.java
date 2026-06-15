package it.fourlab.jsaga.printing;

import com.fazecast.jSerialComm.SerialPort;
import com.github.anastaciocintra.escpos.EscPos;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;



@Service
@EnableConfigurationProperties(EscPosSerialProperties.class)
public class EscPosSerialPrintService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final EscPosSerialProperties properties;

    public EscPosSerialPrintService(EscPosSerialProperties properties) {
        this.properties = properties;
    }

    public void printSimpleReceipt(String title, List<String> lines, BigDecimal totalAmount) {
        if (!properties.isEnabled()) {
            throw new EscPosPrinterException("ESC/POS serial printing is disabled. Set jsaga.print.escpos.serial.enabled=true");
        }

        SerialPort serialPort = openSerialPort();
        try (OutputStream outputStream = serialPort.getOutputStream(); EscPos escPos = new EscPos(outputStream)) {
            escPos.writeLF(title);
            escPos.writeLF("Data: " + LocalDateTime.now().format(DATE_TIME_FORMATTER));
            escPos.writeLF("------------------------------");

            for (String line : lines) {
                escPos.writeLF(line);
            }

            escPos.writeLF("------------------------------");
            escPos.writeLF("Totale: EUR " + totalAmount);
            escPos.feed(4).cut(resolveCutMode());
            escPos.close();
        } catch (IOException ex) {
            throw new EscPosPrinterException("Error while printing receipt on serial ESC/POS", ex);
        } finally {
            serialPort.closePort();
        }
    }

    private SerialPort openSerialPort() {
        SerialPort serialPort = SerialPort.getCommPort(properties.getPort());
        serialPort.setComPortParameters(
                properties.getBaudRate(),
                properties.getDataBits(),
                resolveStopBits(properties.getStopBits()),
                resolveParity(properties.getParity()));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (!serialPort.openPort()) {
            throw new EscPosPrinterException("Cannot open serial port: " + properties.getPort());
        }

        return serialPort;
    }

    private static int resolveStopBits(int stopBits) {
        return switch (stopBits) {
            case 1 -> SerialPort.ONE_STOP_BIT;
            case 2 -> SerialPort.TWO_STOP_BITS;
            default -> throw new EscPosPrinterException("Unsupported stop bits value: " + stopBits);
        };
    }

    private static int resolveParity(String parity) {
        if (parity == null) {
            return SerialPort.NO_PARITY;
        }

        return switch (parity.trim().toLowerCase()) {
            case "none" -> SerialPort.NO_PARITY;
            case "odd" -> SerialPort.ODD_PARITY;
            case "even" -> SerialPort.EVEN_PARITY;
            default -> throw new EscPosPrinterException("Unsupported parity value: " + parity);
        };
    }

    private EscPos.CutMode resolveCutMode() {
        if ("partial".equalsIgnoreCase(properties.getCutMode())) {
            return EscPos.CutMode.PART;
        }
        return EscPos.CutMode.FULL;
    }
}
