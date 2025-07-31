CREATE OR REPLACE FUNCTION notify_calendar_ipo_change() RETURNS TRIGGER AS
'
  BEGIN
    PERFORM pg_notify(''calendar_ipo_change_channel'', TG_TABLE_NAME);
    RETURN NEW;
  END;
' LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER calendar_ipo_change
  AFTER INSERT OR UPDATE OR DELETE
  ON finnhub.calendar_ipo
  FOR EACH ROW
EXECUTE PROCEDURE notify_calendar_ipo_change();
