-- FUNCTION: public.notify_calendar_ipo_change()

-- DROP FUNCTION IF EXISTS public.notify_calendar_ipo_change();

CREATE OR REPLACE FUNCTION finnhub.notify_calendar_ipo_change()
  RETURNS trigger
  LANGUAGE 'plpgsql'
  COST 100
  VOLATILE NOT LEAKPROOF
AS $BODY$
BEGIN
  PERFORM pg_notify('calendar_ipo_change_channel', TG_TABLE_NAME);
  RETURN NEW;
END;
$BODY$;

ALTER FUNCTION finnhub.notify_calendar_ipo_change()
  OWNER TO vertx_demo_admin;

-- Trigger: calendar_ipo_change

-- DROP TRIGGER IF EXISTS calendar_ipo_change ON finnhub.calendar_ipo;

CREATE OR REPLACE TRIGGER calendar_ipo_change
  AFTER INSERT OR DELETE OR UPDATE
  ON finnhub.calendar_ipo
  FOR EACH ROW
EXECUTE FUNCTION finnhub.notify_calendar_ipo_change();
