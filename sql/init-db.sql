CREATE TABLE IF NOT EXISTS public.users
(
  name    character varying(30) COLLATE pg_catalog."default" NOT NULL,
  surname character varying(30) COLLATE pg_catalog."default" NOT NULL
) TABLESPACE pg_default;
ALTER TABLE IF EXISTS public.users
  OWNER to vertx_demo_dev_user;

INSERT INTO users (name, surname)
VALUES ('Liam', 'O''Connell');
INSERT INTO users (name, surname)
VALUES ('Ava', 'Ramirez');
INSERT INTO users (name, surname)
VALUES ('Noah', 'Fischer');

SELECT *
FROM users;

CREATE OR REPLACE FUNCTION notify_table_change() RETURNS TRIGGER AS
'
  BEGIN
    PERFORM pg_notify(''my_channel'', TG_TABLE_NAME);
    RETURN NEW;
  END;
' LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER table_change
  AFTER INSERT OR UPDATE OR DELETE
  ON users
  FOR EACH ROW
EXECUTE PROCEDURE notify_table_change();
