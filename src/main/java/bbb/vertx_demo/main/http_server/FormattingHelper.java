package bbb.vertx_demo.main.http_server;

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

  public static String longWithCommas(Long number) {
    if (isNull(number))
      return "N/A";
    var formatter = getIntegerInstance(US);
    return formatter.format(number);
  }
}
