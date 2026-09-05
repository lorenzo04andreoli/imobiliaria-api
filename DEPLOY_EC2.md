# Deploy EC2

## Infraestrutura

- Ubuntu 24.04 x86_64; ponto de partida: t3.small (2 GiB), com consumo monitorado.
- EBS gp3 de 30 GiB, criptografado. Orce instancia, disco, IPv4 e backups na regiao escolhida.
- Subnet publica com rota para Internet Gateway e Elastic IP associado.
- Security Group: TCP 22 apenas do seu IP; TCP 80 e 443 para visitantes.
- Nao abrir 3306 ou 8080. MySQL e API ficam internos ao Docker.
- Instalar Docker Engine e Compose >= 2.24.4 conforme https://docs.docker.com/engine/install/ubuntu/.
- Habilitar o Docker no boot. Se usar firewall local, permitir SSH antes de ativa-lo.

## Preparar imagens

No PC, com Docker Desktop em containers Linux e os repositorios lado a lado,
execute no diretorio do backend. O arquivo de exemplo contem apenas placeholders:

```powershell
docker compose --env-file .env.ec2.example -f docker-compose.prod.yml -f docker-compose.ec2.yml build
docker save -o imobiliaria-images.tar imobiliaria-api:latest imobiliaria-front:latest
scp -i caminho-da-chave.pem imobiliaria-images.tar ubuntu@IP_PUBLICO:/home/ubuntu/
```

As imagens devem ser linux/amd64 para a VM x86_64. O build da API pula testes:
execute `./mvnw test` (Docker necessario para integracao) e `npm ci` / `npm run build`
no frontend antes de produzir as imagens. Nao envie arquivos .env ou senhas ao Git.

## Preparar servidor

Conecte via SSH e mantenha os repositorios lado a lado:

```bash
sudo mkdir -p /opt/imobiliaria
sudo chown ubuntu:ubuntu /opt/imobiliaria
cd /opt/imobiliaria
git clone https://github.com/lorenzo04andreoli/imobiliaria-api.git
git clone https://github.com/lorenzo04andreoli/imobiliaria-front.git
docker load -i /home/ubuntu/imobiliaria-images.tar
cd imobiliaria-api
cp .env.ec2.example .env.prod
chmod 600 .env.prod
nano .env.prod
```

Os arquivos deste deploy precisam estar publicados no Git antes de clonar.
Substitua TODOS os placeholders, inclusive senhas do MySQL, email/senha do admin e JWT.
Use `openssl rand -hex 32` para gerar o segredo JWT, mantendo a saida privada.
SITE_DOMAIN recebe apenas o hostname, por exemplo elianecorretora.com.br.
APP_CORS_ALLOWED_ORIGINS recebe https:// seguido desse mesmo hostname.
O admin inicial e criado somente se o email nao existir. Alterar a variavel nao troca a senha existente.

## Dominio e HTTPS

Crie o registro DNS A do hostname escolhido apontando para o Elastic IP.
Nao crie AAAA sem configurar IPv6. Esta configuracao atende apenas SITE_DOMAIN;
www exige configurar tambem esse hostname no Caddy e no DNS.
Caddy emite/renova certificados e redireciona HTTP para HTTPS automaticamente.
Isso exige DNS correto, portas 80/443 acessiveis e volume caddy_data persistente.
Referencia: https://caddyserver.com/docs/automatic-https

```bash
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ec2.yml config --quiet
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ec2.yml up -d --no-build
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ec2.yml ps
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ec2.yml logs --tail=100
docker stats --no-stream
```

Os limites de memoria sao iniciais, nao uma garantia de capacidade. Monitore reinicios,
OOM e consumo durante uploads. Os containers devem voltar apos reiniciar a VM.
O Compose base continua disponivel para ensaio HTTP local; use sempre os DOIS arquivos no EC2.

## Validacao antes de divulgar

- HTTPS valido e redirecionamento HTTP.
- Pagina inicial, filtros, detalhes e recarregamento direto de /admin/login.
- Login, cadastro, edicao, publicacao e status vendido/inativo.
- Foto de 1 a 5 MB deve passar pelo proxy; acima de 5 MB deve ser recusada pela API.
- Multiplas fotos, capa, reordenacao e exclusao, incluindo uso no celular.
- Reiniciar os containers e conferir persistencia dos imoveis e fotos.
- Conferir telefone WhatsApp, CRECI e dados reais.

## Dados, backup e recuperacao

Um deploy novo cria banco vazio via Flyway. Para aproveitar imoveis locais,
migre um dump do banco e uploads juntos, em janela sem edicoes, testando primeiro numa stack isolada.
Preserve caminhos relativos /uploads/imoveis e permissoes de escrita do usuario da API.

Os volumes com projeto `imobiliaria` sao imobiliaria_mysql_prod_data e imobiliaria_api_uploads.
Nunca use `down -v` em producao: remove os volumes. Volumes nao substituem backup.
Antes de publicar, programe backup diario do MySQL (dump consistente) e dos uploads,
com copia criptografada fora da VM e retencao definida. Nao coloque senhas na linha de comando.
Teste restaurar o dump em MySQL 8.4 e os uploads numa stack separada antes de confiar no backup.
Inclua caddy_data e uma copia protegida das configuracoes no plano de recuperacao.
Snapshots EBS sao complementares; nao substituem o teste de restauracao da aplicacao.

## Atualizacao e rollback

Faca backup antes de atualizar. Guarde imagens da versao anterior com tag distinta de latest.
Publique os commits, gere/envie novas imagens no PC, atualize os repositorios no servidor,
carregue as imagens e repita `up -d --no-build` com os mesmos arquivos e nome de projeto.
Em falha, restaure as imagens anteriores; migrations incompativeis exigem tambem recuperar
o banco do backup, com possivel perda das alteracoes posteriores. Teste migrations antes do deploy.
Configure alerta de gastos AWS e monitoramento externo de disponibilidade.
