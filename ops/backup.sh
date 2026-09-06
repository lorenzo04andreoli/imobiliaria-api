#!/usr/bin/env bash
set -Eeuo pipefail
umask 077
exec 9>/run/lock/imobiliaria-backup.lock
flock -n 9 || exit 1
root=/var/backups/imobiliaria
mkdir -p "$root"
chmod 700 "$root"
destination=$(mktemp -d "$root/$(date -u +%Y%m%dT%H%M%SZ)-XXXXXX")
restart_api() { docker start imobiliaria-api-prod >/dev/null; }
# Pause writes so the SQL dump and photo archive refer to the same state.
trap restart_api EXIT
docker stop imobiliaria-api-prod >/dev/null
docker exec imobiliaria-mysql-prod sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysqldump -uroot --single-transaction --quick --no-tablespaces --set-gtid-purged=OFF "$MYSQL_DATABASE"' | gzip > "$destination/database.sql.gz"
uploads=$(docker volume inspect imobiliaria_api_uploads --format '{{.Mountpoint}}')
tar -czf "$destination/uploads.tar.gz" -C "$uploads" .
tar -czf "$destination/config.tar.gz" -C /opt/imobiliaria/imobiliaria-api .env.prod docker-compose.prod.yml docker-compose.ip.yml nginx-public-ip.conf
gzip -t "$destination/database.sql.gz"
tar -tzf "$destination/uploads.tar.gz" >/dev/null
tar -tzf "$destination/config.tar.gz" >/dev/null
(cd "$destination" && sha256sum database.sql.gz uploads.tar.gz config.tar.gz > SHA256SUMS)
touch "$destination/COMPLETE"
restart_api
trap - EXIT
# Only prune completed backups in this fixed backup directory.
find "$root" -mindepth 2 -maxdepth 2 -name COMPLETE -mtime +14 -print0 | while IFS= read -r -d '' marker; do
    rm -rf -- "${marker%/COMPLETE}"
done
printf 'Backup verified: %s\n' "$destination"
