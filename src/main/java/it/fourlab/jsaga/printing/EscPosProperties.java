package it.fourlab.jsaga.printing;

import org.springframework.boot.context.properties.ConfigurationProperties;
/*
jsaga.print.escpos.enabled=false
jsaga.print.escpos.serial-port=/dev/rfcomm0
jsaga.print.escpos.serial-baud-rate=9600
jsaga.print.escpos.serial-data-bits=8
jsaga.print.escpos.serial-stop-bits=1
jsaga.print.escpos.serial-parity=none
jsaga.print.escpos.system-print.enabled=true
jsaga.print.escpos.printer-name=pt-210
 */
@ConfigurationProperties(prefix = "jsaga.print.escpos")
public class EscPosProperties {

    private boolean enabled;
    private boolean serialEnabled = false;
    private String serialPort = "/dev/ttyUSB0";
    private int serialBaudRate = 9600;
    private int serialDataBits = 8;
    private int serialStopBits = 1;
    private String serialParity = "none";
    private String cutMode = "half";
    private boolean systemPrintEnabled = true;
    private String printerName = "pt-210";


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSerialEnabled() {
        return serialEnabled;
    }

    public void setSerialEnabled(boolean serialEnabled) {
        this.serialEnabled = serialEnabled;
    }

    public String getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(String serialPort) {
        this.serialPort = serialPort;
    }

    public int getSerialBaudRate() {
        return serialBaudRate;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    public int getSerialDataBits() {
        return serialDataBits;
    }

    public void setSerialDataBits(int serialDataBits) {
        this.serialDataBits = serialDataBits;
    }

    public int getSerialStopBits() {
        return serialStopBits;
    }

    public void setSerialStopBits(int serialStopBits) {
        this.serialStopBits = serialStopBits;
    }

    public String getSerialParity() {
        return serialParity;
    }

    public void setSerialParity(String serialParity) {
        this.serialParity = serialParity    ;
    }

    public String getCutMode() {
        return cutMode;
    }

    public void setCutMode(String cutMode) {
        this.cutMode = cutMode;
    }
    public boolean isSystemPrintEnabled() {
        return systemPrintEnabled;
    }
    public void setSystemPrintEnabled(boolean systemPrintEnabled) {
        this.systemPrintEnabled = systemPrintEnabled;
    }
    public String getPrinterName() {
        return printerName;
    }
    public void setPrinterName(String printerName) {
        this.printerName = printerName;
    }
}
