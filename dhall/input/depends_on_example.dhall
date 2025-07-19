depends_on:
  postgres:
    condition: service_healthy
    restart: false
  psql:
    condition: service_completed_successfully
    restart: false
  qpid:
    condition: service_healthy
    restart: false
  config-server-nginx:
    condition: service_healthy
    restart: false
