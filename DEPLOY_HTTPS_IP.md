# HTTPS no dominio e no IP fixo

Endereco principal: `https://elianecarneiroimoveis.com.br`.
O `www` redireciona para o dominio principal, preservando caminho e query.
O painel esta em `https://elianecarneiroimoveis.com.br/admin/login`.
O IP `54.94.105.56` continua disponivel como acesso alternativo.
Ha dois certificados Let's Encrypt gerenciados pelo Certbot via webroot:
`imobiliaria-dominio` para os dois nomes e `imobiliaria-ip` (perfil shortlived)
para o IP. O nome deste guia e dos arquivos Compose foi mantido por compatibilidade.
O backend exige JWT para operacoes administrativas. O proxy limita o login
a cinco requisicoes por minuto por IP, com burst de cinco; excesso recebe 429.
O tunel SSH permanece como acesso alternativo. CORS permite apenas o endereco
HTTPS do dominio, www e IP, alem das duas origens locais do tunel.

## Operacao normal

Use os TRES arquivos para nao remover HTTPS em uma atualizacao:

```bash
sudo docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ip.yml -f docker-compose.ip-https.yml up -d --no-build
```

Portas publicas: 80 para validacao ACME e redirecionamento, 443 para HTTPS.
8081 permanece somente em 127.0.0.1. Nao e preciso abrir 8080 ou 3306.

## Emissao inicial / recuperacao

Instale o Certbot oficial via `sudo snap install --classic certbot`.
Crie `/var/www/letsencrypt` e inicie primeiro apenas os dois arquivos
`docker-compose.prod.yml` e `docker-compose.ip.yml`. Isso permite validar
o IP antes de existir um certificado. Depois emita o certificado:

```bash
sudo /snap/bin/certbot certonly --non-interactive --agree-tos --register-unsafely-without-email --preferred-profile shortlived --webroot -w /var/www/letsencrypt --ip-address 54.94.105.56 --cert-name imobiliaria-ip
```

Leia e aceite os termos do Let's Encrypt antes de usar `--agree-tos`.
Para ensaio, use `--staging` e diretorios separados para config, work e logs;
nao sirva certificados de staging ao publico. Depois da emissao real, inicie
com os tres arquivos somente quando AMBOS os certificados existirem.
Nginx usa `/etc/letsencrypt/live/imobiliaria-ip` e
`/etc/letsencrypt/live/imobiliaria-dominio`.

Para emitir o certificado do dominio, confirme antes que os registros A
do nome principal e do www apontam para `54.94.105.56`:

```bash
sudo /snap/bin/certbot certonly --non-interactive --agree-tos --register-unsafely-without-email --webroot -w /var/www/letsencrypt -d elianecarneiroimoveis.com.br -d www.elianecarneiroimoveis.com.br --cert-name imobiliaria-dominio
```

## Renovacao

O timer `snap.certbot.renew.timer` verifica automaticamente a renovacao.
Instale `ops/reload-certificate.sh` como root, modo 700, em
`/etc/letsencrypt/renewal-hooks/deploy/imobiliaria-nginx`.
O hook valida e recarrega o Nginx depois da emissao; nao reinicia o banco.

```bash
sudo /snap/bin/certbot renew --dry-run --run-deploy-hooks --no-random-sleep-on-renew
sudo systemctl list-timers snap.certbot.renew.timer
```

Nao feche a porta 80: a renovacao depende do desafio HTTP-01. Monitore
falhas com `journalctl -u snap.certbot.renew.service` e verifique a validade
do certificado externamente. Ainda nao ha alertas externos de expiracao.
O backup diario inclui certificados e chaves privadas; mantenha-o restrito.

## DNS e verificacao

Os dois registros A no Registro.br apontam para `54.94.105.56`; nao publique
AAAA sem configurar e validar o acesso IPv6. O frontend usa `/api` e
`/uploads`, sem precisar de rebuild para a troca de dominio.

`sudo python3 ops/verify-public-admin.py` testa o dominio principal.
Acrescente `--base-url https://54.94.105.56` para testar o acesso alternativo.
Nao inicie o Caddy do override EC2 junto com o Nginx publico: ambos usam
as portas 80 e 443.

Referencia: https://letsencrypt.org/2026/03/11/shorter-certs-certbot/
