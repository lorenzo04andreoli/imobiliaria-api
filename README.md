# Imobiliaria API

API REST para catalogo de imoveis e painel administrativo de uma corretora.

O projeto faz parte de uma aplicacao full stack composta por:

- site publico em HTML, CSS e JavaScript;
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

Se o seu MySQL tiver senha, defina pelo ambiente:

```powershell
$env:DB_PASSWORD="sua-senha"
```

O banco `imobiliaria_db` pode ser criado automaticamente por causa do parametro `createDatabaseIfNotExist=true`. Se preferir criar manualmente:

```sql
CREATE DATABASE imobiliaria_db;
```

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

## Executar Localmente

Na raiz do projeto:

```powershell
.\mvnw.cmd spring-boot:run
```

Por padrao, a API sobe em:

```txt
http://localhost:8080
```

## Build

```powershell
.\mvnw.cmd -DskipTests package
```

O arquivo `.jar` sera gerado em:

```txt
target/imobiliaria-api-0.0.1-SNAPSHOT.jar
```

## Testes

Os testes cobrem o contrato dos endpoints publicos de imoveis, do login administrativo e das rotas administrativas de imoveis.

```powershell
.\mvnw.cmd test
```

## Swagger

Com a aplicacao rodando, acesse:

```txt
http://localhost:8080/swagger-ui.html
```

O documento OpenAPI em JSON fica em:

```txt
http://localhost:8080/v3/api-docs
```

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
GET /api/imoveis?cidade=Presidente Prudente&tipo=CASA&precoMin=300000&precoMax=700000&quartosMin=2
```

Filtros disponiveis:

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
PATCH  /api/admin/imoveis/{id}/imagens/{imagemId}/capa
```

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
app.cors.allowed-origins=http://localhost:4200,http://localhost:5500,http://127.0.0.1:5500
```

Antes do deploy, substitua ou complemente essa lista com o dominio real do frontend.

## Commits

Este projeto usa commits pequenos no padrao Conventional Commits:

```txt
feat(imoveis): adiciona cadastro administrativo
fix(security): libera cors para frontends locais
docs(api): adiciona swagger openapi
```
