#!/usr/bin/env bash
set -Eeuo pipefail
cd /opt/imobiliaria/imobiliaria-api
compose=(docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ip.yml -f docker-compose.ip-https.yml)
"${compose[@]}" exec -T public-ip nginx -t
"${compose[@]}" exec -T public-ip nginx -s reload
