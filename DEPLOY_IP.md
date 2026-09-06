# Catalogo temporario pelo IP

Este modo publica somente leitura via HTTP na porta 80. HTTP nao protege
o conteudo contra interceptacao ou alteracao em transito. Nao e substituto
para HTTPS em producao. Login e escrita ficam bloqueados no proxy publico.

Mantenha o acesso administrativo pelo tunel SSH em localhost:8081.
Nao abra as portas 8081, 8080 ou 3306 no firewall publico.

Com as imagens ja carregadas e .env.prod configurado:

```bash
sudo docker compose -p imobiliaria --env-file .env.prod -f docker-compose.prod.yml -f docker-compose.ip.yml up -d --no-build
```

Use sempre ambos os arquivos neste modo. Os volumes existentes sao preservados.
O override exige Docker Compose 2.24.4 ou mais recente.

Quando houver dominio, siga DEPLOY_EC2.md para configurar HTTPS e credenciais
definitivas. Remova o servico public-ip antes de iniciar o Caddy, que tambem
usa a porta 80. Nao remova volumes do banco ou das fotos.

Antes da publicacao definitiva, configure backups e aplique as atualizacoes
de seguranca do sistema. As credenciais do ensaio local nao sao de producao.
