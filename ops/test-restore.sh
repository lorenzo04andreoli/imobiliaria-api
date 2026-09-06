#!/usr/bin/env bash
set -Eeuo pipefail
backup=${1:?Provide an absolute completed backup directory}
test -f "$backup/COMPLETE"
(cd "$backup" && sha256sum -c SHA256SUMS)
name="imobiliaria-restore-test-$(date +%s)"
cleanup() { docker rm -fv "$name" >/dev/null; }
# No network, no production volumes, and only an anonymous disposable volume.
docker run -d --name "$name" --network none --memory 512m \
    -e MYSQL_ALLOW_EMPTY_PASSWORD=yes -e MYSQL_DATABASE=restore_test \
    mysql:8.4 --innodb-buffer-pool-size=64M --performance-schema=OFF >/dev/null
trap cleanup EXIT
ready=false
for attempt in $(seq 1 60); do
    if docker exec "$name" mysql -h127.0.0.1 -uroot -D restore_test -e 'SELECT 1' >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 2
done
test "$ready" = true
gzip -dc "$backup/database.sql.gz" | docker exec -i "$name" mysql -uroot restore_test
docker exec "$name" mysql -uroot restore_test -e 'SELECT COUNT(*) AS properties FROM imoveis; SELECT COUNT(*) AS images FROM imagens_imovel; SELECT COUNT(*) AS users FROM usuarios; CHECK TABLE imoveis, imagens_imovel, usuarios;'
printf 'Isolated SQL restore: OK\n'
