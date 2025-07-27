* Set up
  ```shell
  export BASE_URL="https://finnhub.io/api/v1"
  export API_KEY="d1uqv0pr01qletnb7080d1uqv0pr01qletnb708g"
  export AUTH="X-Finnhub-Token: ${API_KEY}"
  ```

# IPO calendar

* Record count
  ```shell
  jq '.ipoCalendar | length' ./output/IPO-Calendar-entire.json
  ```
* Unique exchanges
  ```shell
  jq -r '.ipoCalendar[].exchange' ./output/IPO-Calendar-entire.json | sort -u
  ```
* Only date starting with
  ```shell
  jq '.ipoCalendar[] | select(.date | startswith("2025-07"))' ./output/IPO-Calendar-entire.json

  ```
  * Rows
    * 2025
      ```shell
      jq -r '
        .ipoCalendar[]
        | select(.date | startswith("2025"))
        | "<tr><td>\(.date)</td><td>\(.name)</td><td>\(.exchange // "-")</td><td>\(.symbol // "-")</td><td>\(.numberOfShares // "-")</td><td>\(.price // "-")</td></tr>"
      ' ./output/IPO-Calendar-entire.json
      ```
    * 2020 - 2024
      ```shell
      jq -r '
      .ipoCalendar[]
      | select(.date >= "2020-01-01" and .date < "2025-01-01")
      | "<tr><td>\(.date)</td><td>\(.name)</td><td>\(.exchange // "-")</td><td>\(.symbol // "-")</td><td>\(.numberOfShares // "-")</td><td>\(.price // "-")</td></tr>"
      ' ./output/IPO-Calendar-entire.json
      ```
    * Rows again
      * NASDAQ Capital
      ```shell
      jq -r '
        .ipoCalendar
        | sort_by(.date) | reverse
        | map(select(.exchange == "NASDAQ Capital"))
        | .[:5]
        | map("<tr><td>\(.date)</td><td>\(.name)</td><td>\(.symbol // "–")</td><td>\(.numberOfShares // "–")</td><td>\(.price // "–")</td></tr>")
        | .[]
      ' ./output/IPO-Calendar-entire.json
      ```
      * NASDAQ Global
      ```shell
      jq -r '
        .ipoCalendar
        | sort_by(.date) | reverse
        | map(select(.exchange == "NASDAQ Global"))
        | .[:5]
        | map("<tr><td>\(.date)</td><td>\(.name)</td><td>\(.symbol // "–")</td><td>\(.numberOfShares // "–")</td><td>\(.price // "–")</td></tr>")
        | .[]
      ' ./output/IPO-Calendar-entire.json
      ```
      * NASDAQ Global Select
      ```shell
      jq -r '
        .ipoCalendar
        | sort_by(.date) | reverse
        | map(select(.exchange == "NASDAQ Global Select"))
        | .[:5]
        | map("<tr><td>\(.date)</td><td>\(.name)</td><td>\(.symbol // "–")</td><td>\(.numberOfShares // "–")</td><td>\(.price // "–")</td></tr>")
        | .[]
      ' ./output/IPO-Calendar-entire.json
      ```
      * NYSE
      ```shell
      jq -r '
        .ipoCalendar
        | sort_by(.date) | reverse
        | map(select(.exchange == "NYSE"))
        | .[:5]
        | map("<tr><td>\(.date)</td><td>\(.name)</td><td>\(.symbol // "–")</td><td>\(.numberOfShares // "–")</td><td>\(.price // "–")</td></tr>")
        | .[]
      ' ./output/IPO-Calendar-entire.json
      ```
      * NYSE MKT
      ```shell
      jq -r '
        .ipoCalendar
        | sort_by(.date) | reverse
        | map(select(.exchange == "NYSE MKT"))
        | .[:5]
        | map("<tr><td>\(.date)</td><td>\(.name)</td><td>\(.symbol // "–")</td><td>\(.numberOfShares // "–")</td><td>\(.price // "–")</td></tr>")
        | .[]
      ' ./output/IPO-Calendar-entire.json
      ```
