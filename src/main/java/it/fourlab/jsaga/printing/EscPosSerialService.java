package it.fourlab.jsaga.printing;

import com.fazecast.jSerialComm.SerialPort;

import java.io.OutputStream;
//@Service // Uncomment if you want to use this implementation as a Spring service
public class EscPosSerialService extends EscPosPrintService {

    private SerialPort currentPort;

    public EscPosSerialService(EscPosProperties properties) {
        super(properties);
    }

    @Override
    protected OutputStream openOutputStream() {
        currentPort = openSerialPort();
        return currentPort.getOutputStream();
    }

    @Override
    protected void releaseOutputStream() {
        if (currentPort != null) {
            currentPort.closePort();
            currentPort = null;
        }
    }

    private SerialPort openSerialPort() {
        SerialPort serialPort = SerialPort.getCommPort(properties.getSerialPort());
        serialPort.setComPortParameters(
                properties.getSerialBaudRate(),
                properties.getSerialDataBits(),
                resolveStopBits(properties.getSerialStopBits()),
                resolveParity(properties.getSerialParity()));
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);

        if (!serialPort.openPort()) {
            throw new EscPosPrinterException("Cannot open serial port: " + properties.getSerialPort());
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
}
