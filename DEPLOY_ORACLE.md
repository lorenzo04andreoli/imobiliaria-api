# Deploy na Oracle Cloud Always Free

Guia para publicar a aplicação em uma VM Oracle Cloud usando Docker Compose.

## Arquitetura

Na VM, os dois repositórios devem ficar lado a lado:

```text
/opt/imobiliaria/imobiliaria-api
/opt/imobiliaria/imobiliaria-front
```

O `docker-compose.prod.yml` do backend sobe:

- `front-prod`: Nginx servindo o Angular na porta 80
- `api-prod`: Spring Boot disponível apenas dentro da rede Docker
- `mysql-prod`: MySQL interno
- volumes Docker para banco e uploads

O frontend usa `/api` e `/uploads`, e o Nginx encaminha essas rotas para a API.

## Preparar a VM

Atualize o sistema:

```bash
sudo apt update
sudo apt upgrade -y
```

Instale Docker:

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
```

Depois saia e entre novamente na sessão SSH.

## Clonar os projetos

```bash
sudo mkdir -p /opt/imobiliaria
sudo chown -R $USER:$USER /opt/imobiliaria
cd /opt/imobiliaria

git clone https://github.com/lorenzo04andreoli/imobiliaria-api.git
git clone https://github.com/lorenzo04andreoli/imobiliaria-front.git
```

## Configurar ambiente

```bash
cd /opt/imobiliaria/imobiliaria-api
cp .env.prod.example .env.prod
nano .env.prod
```

Valores mínimos:

```env
FRONTEND_PATH=../imobiliaria-front
FRONTEND_PORT=80

IMOBILIARIA_MYSQL_ROOT_PASSWORD=troque-por-uma-senha-forte
IMOBILIARIA_MYSQL_DATABASE=imobiliaria_db
IMOBILIARIA_MYSQL_USER=imobiliaria_user
IMOBILIARIA_MYSQL_PASSWORD=troque-por-outra-senha-forte

JWT_SECRET=troque-por-uma-chave-grande-com-mais-de-32-caracteres
APP_CORS_ALLOWED_ORIGINS=https://seudominio.com

APP_ADMIN_NOME=Eliane
APP_ADMIN_EMAIL=admin@seudominio.com
APP_ADMIN_SENHA=troque-por-uma-senha-inicial-segura
```

Se ainda não houver domínio, use temporariamente:

```env
APP_CORS_ALLOWED_ORIGINS=http://IP_PUBLICO_DA_VM
```

O admin inicial só é criado se `APP_ADMIN_NOME`, `APP_ADMIN_EMAIL` e `APP_ADMIN_SENHA` estiverem preenchidos. Depois que o usuário já existir no banco, o inicializador não troca a senha automaticamente.

## Ensaio local

Para testar a mesma stack de produção na sua máquina:

```powershell
Copy-Item .env.prod.local.example .env.prod
docker compose -p imobiliaria-local-prod --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Acesse:

```text
http://localhost:8081
http://localhost:8081/admin/login
```

Login local do exemplo:

```text
admin@imobiliaria.com
admin123456
```

Para parar:

```powershell
docker compose -p imobiliaria-local-prod --env-file .env.prod -f docker-compose.prod.yml down
```

## Subir a aplicação

```bash
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Ver logs:

```bash
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml logs -f
```

Ver status:

```bash
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml ps
```

## Atualizar produção

```bash
cd /opt/imobiliaria/imobiliaria-api
git pull

cd /opt/imobiliaria/imobiliaria-front
git pull

cd /opt/imobiliaria/imobiliaria-api
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

## Backup básico

Exportar banco:

```bash
docker exec imobiliaria-mysql-prod mysqldump -u imobiliaria_user -p imobiliaria_db > backup-imobiliaria.sql
```

Compactar uploads:

```bash
docker run --rm -v imobiliaria_api_uploads:/uploads -v "$PWD":/backup alpine tar czf /backup/uploads-imobiliaria.tar.gz /uploads
```

## Firewall

Na Oracle Cloud, libere entrada HTTP na porta 80 na subnet/security list.

Na VM, se usar `ufw`:

```bash
sudo ufw allow 80/tcp
sudo ufw enable
```

Quando configurar HTTPS com domínio, libere também a porta 443.
