package bbb.vertx_demo.main.http_server;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;
import static java.text.NumberFormat.getIntegerInstance;
import static java.util.Locale.US;
import static java.util.Objects.isNull;

public enum FormattingHelper {

  ;

  public static String longCompact(Long number) {
    if (isNull(number))
      return "N/A";
    if (number >= 1_000_000_000)
      return String.format("%.1fB", number / 1_000_000_000.0);
    if (number >= 1_000_000)
      return String.format("%.1fM", number / 1_000_000.0);
    if (number >= 1_000)
      return String.format("%.1fK", number / 1_000.0);
    return Long.toString(number);
  }

  public static String bigDecimalCompact(BigDecimal number) {
    if (number == null)
      return "N/A";
    var billion = BigDecimal.valueOf(1_000_000_000);
    var million = BigDecimal.valueOf(1_000_000);
    var thousand = BigDecimal.valueOf(1_000);
    if (number.compareTo(billion) >= 0)
      return String.format("%.1fB", number.divide(billion, 1, HALF_UP));
    if (number.compareTo(million) >= 0)
      return String.format("%.1fM", number.divide(million, 1, HALF_UP));
    if (number.compareTo(thousand) >= 0)
      return String.format("%.1fK", number.divide(thousand, 1, HALF_UP));
    return number.stripTrailingZeros().toPlainString();
  }

  public static String bigDecimalCompactPrice(BigDecimal number) {
    if (number == null)
      return "N/A";
    var billion = BigDecimal.valueOf(1_000_000_000);
    var million = BigDecimal.valueOf(1_000_000);
    var thousand = BigDecimal.valueOf(1_000);
    if (number.compareTo(billion) >= 0)
      return String.format("$%.1fB", number.divide(billion, 1, HALF_UP));
    if (number.compareTo(million) >= 0)
      return String.format("$%.1fM", number.divide(million, 1, HALF_UP));
    if (number.compareTo(thousand) >= 0)
      return String.format("$%.1fK", number.divide(thousand, 1, HALF_UP));
    return String.format("$%.1f", number);
  }

  public static String longWithCommas(Long number) {
    if (isNull(number))
      return "N/A";
    var formatter = getIntegerInstance(US);
    return formatter.format(number);
  }
}
