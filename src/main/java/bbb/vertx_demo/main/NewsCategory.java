package bbb.vertx_demo.main;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NewsCategory {

  GENERAL("general"),
  FOREX("forex"),
  CRYPTO("crypto"),
  MERGER("merger"),
  ;

  public final String value;
}
