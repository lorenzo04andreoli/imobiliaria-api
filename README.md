# Imobiliaria API

API REST para catalogo de imoveis e painel administrativo de uma corretora.

O projeto faz parte de uma aplicacao full stack composta por:

- site publico em Angular;
- painel administrativo em Angular;
- backend em Java com Spring Boot;
- banco de dados MySQL.

## Tecnologias

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- JWT
- MySQL
- Flyway
- Lombok
- Swagger/OpenAPI

## Requisitos

- JDK 17 ou superior
- MySQL instalado e em execucao ou Docker
- Git

Nao e necessario instalar Maven globalmente, pois o projeto usa Maven Wrapper.

## Configuracao

O arquivo principal de configuracao fica em:

```txt
src/main/resources/application.properties
```

Configuracao atual do banco:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/imobiliaria_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
```

Por padrao, quando nenhum perfil e informado, a aplicacao usa o perfil `dev`.

Se o seu MySQL tiver senha, defina pelo ambiente:

```powershell
$env:DB_PASSWORD="sua-senha"
```

O banco `imobiliaria_db` pode ser criado automaticamente por causa do parametro `createDatabaseIfNotExist=true`. Se preferir criar manualmente:

```sql
CREATE DATABASE imobiliaria_db;
```

## Perfis de Ambiente

O projeto possui configuracoes separadas por perfil:

```txt
src/main/resources/application.properties
src/main/resources/application-dev.properties
src/main/resources/application-prod.properties
```

O arquivo `application.properties` concentra as configuracoes comuns. Os arquivos `dev` e `prod` ajustam comportamento de banco, logs SQL e carga inicial.

As tabelas do banco sao versionadas pelo Flyway em:

```txt
src/main/resources/db/migration
```

Perfil `dev`:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.root=INFO
logging.level.com.lorenzo.imobiliaria_api=DEBUG
app.seed.enabled=${APP_SEED_ENABLED:false}
```

Perfil `prod`:

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
logging.level.root=INFO
logging.level.com.lorenzo.imobiliaria_api=INFO
logging.level.org.hibernate.SQL=OFF
logging.level.org.hibernate.orm.jdbc.bind=OFF
app.seed.enabled=false
```

Rodar em desenvolvimento:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

Rodar em desenvolvimento com DevTools:

```powershell
.\mvnw.cmd -Plocal-devtools spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=dev"
```

Rodar em producao:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=prod"
```

Em producao, defina as variaveis sensiveis no ambiente:

```env
DB_URL=jdbc:mysql://host:3306/imobiliaria_db
DB_USERNAME=usuario
DB_PASSWORD=senha
JWT_SECRET=chave-grande-com-mais-de-32-caracteres
APP_CORS_ALLOWED_ORIGINS=https://seudominio.com
APP_UPLOAD_IMOVEIS_DIR=/caminho/persistente/uploads/imoveis
```

O perfil `prod` usa `ddl-auto=validate`, entao o banco precisa estar com as tabelas criadas antes da aplicacao subir.

Em bancos novos, o Flyway executa as migrations automaticamente ao iniciar a aplicacao. Em bancos que ja tinham tabelas antes da adocao do Flyway, `spring.flyway.baseline-on-migrate=true` cria uma linha inicial de controle sem apagar dados.

## MySQL com Docker Compose

O projeto possui um `docker-compose.yml` para subir MySQL local.

Crie um arquivo `.env` na raiz do projeto usando `.env.example` como referencia. O arquivo `.env` nao deve ser commitado.

Exemplo de variaveis:

```env
IMOBILIARIA_MYSQL_ROOT_PASSWORD=troque-a-senha-root
IMOBILIARIA_MYSQL_DATABASE=imobiliaria_db
IMOBILIARIA_MYSQL_USER=imobiliaria_user
IMOBILIARIA_MYSQL_PASSWORD=troque-a-senha-do-banco
IMOBILIARIA_MYSQL_PORT=3306

DB_URL=jdbc:mysql://localhost:3306/imobiliaria_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo
DB_USERNAME=imobiliaria_user
DB_PASSWORD=troque-a-senha-do-banco

JWT_SECRET=troque-por-uma-chave-grande-com-mais-de-32-caracteres
APP_CORS_ALLOWED_ORIGINS=http://localhost:4200,http://localhost:5500,http://127.0.0.1:5500
APP_UPLOAD_IMOVEIS_DIR=uploads/imoveis
```

As variaveis do Docker Compose usam o prefixo `IMOBILIARIA_` para evitar conflito com variaveis globais do sistema.

Subir o banco:

```powershell
docker compose up -d
```

Parar o banco:

```powershell
docker compose down
```

## Variaveis Sensiveis

O JWT usa uma chave secreta configurada por ambiente:

```properties
app.jwt.secret=${JWT_SECRET:}
```

Defina `JWT_SECRET` antes de rodar a aplicacao. Use uma chave longa, com pelo menos 32 caracteres.

Exemplo no PowerShell:

```powershell
$env:JWT_SECRET="troque-por-uma-chave-grande-com-mais-de-32-caracteres"
```

## Usuario Administrador Inicial

O projeto possui um inicializador opcional de usuario administrador.

Ele so cria o admin se estas propriedades forem informadas:

```properties
app.admin.nome=
app.admin.email=
app.admin.senha=
```

Exemplo no PowerShell:

```powershell
$env:JWT_SECRET="troque-por-uma-chave-grande-com-mais-de-32-caracteres"
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--app.admin.nome=Administrador --app.admin.email=admin@seudominio.com --app.admin.senha=sua-senha-segura"
```

Nao deixe senha real commitada no repositorio.

## Dados de Exemplo

O projeto possui uma carga inicial opcional para ambiente local. Ela cria imoveis demonstrativos com imagens por URL quando o banco ainda nao possui nenhum imovel.

Para ativar:

```powershell
$env:APP_SEED_ENABLED="true"
```

Ou no arquivo `.env`:

```env
APP_SEED_ENABLED=true
```

Por padrao, a carga inicial fica desativada:

```properties
app.seed.enabled=${APP_SEED_ENABLED:false}
```

## Upload de Imagens

O projeto aceita imagens enviadas pelo painel administrativo. Por padrao, os arquivos sao salvos em:

```txt
uploads/imoveis
```

A pasta `uploads/` fica fora do Git por ser dado de runtime.

Configuracoes:

```properties
app.upload.imoveis-dir=${APP_UPLOAD_IMOVEIS_DIR:uploads/imoveis}
app.upload.public-path=/uploads/imoveis
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
```

Formatos permitidos:

- JPG
- PNG
- WEBP

As imagens locais ficam publicas em:

```txt
/uploads/imoveis/{nome-do-arquivo}
```

## Executar Localmente

Na raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrao, a API sobe em:

```txt
http://localhost:8080
```

Como o perfil padrao e `dev`, o comando acima usa as configuracoes de desenvolvimento.

## Build

```powershell
.\mvnw.cmd -DskipTests package
```

O arquivo `.jar` sera gerado em:

```txt
target/imobiliaria-api-0.0.1-SNAPSHOT.jar
```

## Docker

Build da imagem:

```powershell
docker build -t imobiliaria-api .
```

Rodar a API em container:

```powershell
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e DB_URL="jdbc:mysql://host.docker.internal:3306/imobiliaria_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo" `
  -e DB_USERNAME="imobiliaria_user" `
  -e DB_PASSWORD="troque-a-senha-do-banco" `
  -e JWT_SECRET="troque-por-uma-chave-grande-com-mais-de-32-caracteres" `
  -e APP_CORS_ALLOWED_ORIGINS="https://seudominio.com" `
  -v "${PWD}/uploads:/app/uploads" `
  imobiliaria-api
```

O volume em `/app/uploads` preserva as imagens enviadas mesmo se o container for recriado.

## Docker Compose de Producao

O arquivo `docker-compose.prod.yml` sobe a stack de producao:

- frontend Angular servido por Nginx;
- API Spring Boot;
- MySQL;
- volumes persistentes para banco e uploads.

Crie um arquivo `.env.prod` usando `.env.prod.example` como referencia:

```powershell
Copy-Item .env.prod.example .env.prod
```

Suba os containers:

```powershell
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

Acompanhe os logs:

```powershell
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml logs -f
```

Pare os containers:

```powershell
docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml down
```

Use `down -v` apenas quando quiser apagar tambem os volumes do banco e dos uploads.

Para deploy sem custo em uma VM Oracle Cloud Always Free, veja:

```txt
DEPLOY_ORACLE.md
```

## Testes

Os testes cobrem o contrato dos endpoints publicos de imoveis, do login administrativo e das rotas administrativas de imoveis.

```powershell
.\mvnw.cmd test
```

## Swagger

No perfil `dev`, com a aplicacao rodando, acesse:

```txt
http://localhost:8080/swagger-ui.html
```

O documento OpenAPI em JSON fica em:

```txt
http://localhost:8080/v3/api-docs
```

No perfil `prod`, Swagger UI e OpenAPI JSON ficam desabilitados.

## Autenticacao

Login:

```http
POST /api/auth/login
```

Body:

```json
{
  "email": "admin@seudominio.com",
  "senha": "sua-senha"
}
```

Resposta:

```json
{
  "id": 1,
  "nome": "Administrador",
  "email": "admin@seudominio.com",
  "role": "ADMIN",
  "token": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 7200
}
```

Para acessar endpoints administrativos, envie o token:

```http
Authorization: Bearer seu-token
```

## Endpoints Publicos

```http
GET /api/imoveis
GET /api/imoveis/{id}
```

Os endpoints publicos retornam apenas imoveis com status `PUBLICADO`.

A listagem publica aceita filtros por query string:

```http
GET /api/imoveis?q=quintal&cidade=Presidente Prudente&tipo=CASA&precoMin=300000&precoMax=700000&quartosMin=2
```

Filtros disponiveis:

- `q`: busca em titulo, descricao, cidade e bairro
- `cidade`
- `bairro`
- `tipo`
- `precoMin`
- `precoMax`
- `quartosMin`
- `banheirosMin`
- `vagasMin`

A listagem publica tambem aceita paginacao:

```http
GET /api/imoveis?page=0&size=12
```

E ordenacao:

```http
GET /api/imoveis?sort=preco&direction=asc
```

Campos de ordenacao disponiveis:

- `criadoEm`
- `preco`
- `area`
- `quartos`
- `banheiros`
- `vagas`

Direcoes disponiveis:

- `asc`
- `desc`

Regras dos parametros:

- `page` deve ser maior ou igual a `0`.
- `size` deve estar entre `1` e `50`.
- `precoMin` deve ser menor ou igual a `precoMax`.
- `sort` deve ser um dos campos permitidos.
- `direction` deve ser `asc` ou `desc`.

Resposta da listagem publica:

```json
{
  "content": [],
  "page": 0,
  "size": 12,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

## Endpoints Administrativos

```http
GET    /api/admin/imoveis
GET    /api/admin/imoveis/{id}
POST   /api/admin/imoveis
PUT    /api/admin/imoveis/{id}
PATCH  /api/admin/imoveis/{id}/publicar
PATCH  /api/admin/imoveis/{id}/vender
PATCH  /api/admin/imoveis/{id}/rascunho
PATCH  /api/admin/imoveis/{id}/inativar
GET    /api/admin/imoveis/{id}/imagens
POST   /api/admin/imoveis/{id}/imagens
POST   /api/admin/imoveis/{id}/imagens/upload
PUT    /api/admin/imoveis/{id}/imagens/ordem
PATCH  /api/admin/imoveis/{id}/imagens/{imagemId}/capa
DELETE /api/admin/imoveis/{id}/imagens/{imagemId}
```

Todos os endpoints administrativos exigem token JWT.

A listagem administrativa aceita os mesmos filtros da listagem publica e tambem o filtro `status`:

```http
GET /api/admin/imoveis?status=RASCUNHO&q=quintal&page=0&size=12&sort=criadoEm&direction=desc
```

Filtros disponiveis:

- `q`: busca em titulo, descricao, cidade e bairro
- `cidade`
- `bairro`
- `tipo`
- `status`
- `precoMin`
- `precoMax`
- `quartosMin`
- `banheirosMin`
- `vagasMin`

A resposta da listagem administrativa tambem usa o envelope paginado:

```json
{
  "content": [],
  "page": 0,
  "size": 12,
  "totalElements": 0,
  "totalPages": 0,
  "first": true,
  "last": true
}
```

Cadastrar imagem por URL:

```http
POST /api/admin/imoveis/{id}/imagens
```

```json
{
  "url": "https://example.com/imovel.jpg",
  "ordem": 0,
  "capa": true
}
```

Enviar imagem local:

```http
POST /api/admin/imoveis/{id}/imagens/upload
Content-Type: multipart/form-data
```

Campos do formulario:

- `arquivo`: arquivo JPG, PNG ou WEBP
- `ordem`: opcional
- `capa`: opcional

Reordenar imagens:

```http
PUT /api/admin/imoveis/{id}/imagens/ordem
```

```json
{
  "imagemIds": [12, 11, 10]
}
```

Para reordenar, envie todos os IDs de imagem daquele imovel, sem repeticao, na ordem desejada.

Remover imagem:

```http
DELETE /api/admin/imoveis/{id}/imagens/{imagemId}
```

Se a imagem removida for local, o arquivo fisico tambem sera removido. Se ela for capa, outra imagem do imovel sera promovida automaticamente quando existir.

## Status de Imovel

- `RASCUNHO`: cadastrado, mas ainda nao aparece no site publico.
- `PUBLICADO`: aparece no site publico.
- `VENDIDO`: marcado como vendido.
- `INATIVO`: removido do site publico sem apagar do banco.

## Tipos de Imovel

- `CASA`
- `APARTAMENTO`
- `TERRENO`
- `COMERCIAL`
- `CHACARA`
- `OUTRO`

## CORS

Origens locais liberadas atualmente:

```properties
app.cors.allowed-origins=${APP_CORS_ALLOWED_ORIGINS:http://localhost:4200,http://localhost:5500,http://127.0.0.1:5500}
```

Antes do deploy, substitua ou complemente essa lista com o dominio real do frontend.

## Commits

Este projeto usa commits pequenos no padrao Conventional Commits:

```txt
feat(imoveis): adiciona cadastro administrativo
fix(security): libera cors para frontends locais
docs(api): adiciona swagger openapi
```
