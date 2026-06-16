package it.fourlab.jsaga.printing;

import java.math.BigDecimal;

public record PrintLine(int qty, String name, BigDecimal unitPrice) {}
