# Operacao do servidor

O painel atual e publico via HTTPS: https://elianecarneiroimoveis.com.br/admin/login.
O tunel SSH e opcional. Consulte [o guia HTTPS](../DEPLOY_HTTPS_IP.md) para
os tres arquivos Compose que devem ser usados nas atualizacoes.

## Backups

`backup.sh` deve ser instalado como `/usr/local/sbin/imobiliaria-backup`,
root:root e modo 700. Instale os arquivos `.service` e `.timer` em
`/etc/systemd/system/`, execute `systemctl daemon-reload` e habilite o timer.

O timer executa diariamente as 06:00 UTC (03:00 em Brasilia), com ate cinco
minutos de atraso aleatorio. A API e interrompida brevemente para que banco
e fotos sejam consistentes. Ha indisponibilidade da API durante o backup e
sua inicializacao. Os arquivos ficam em `/var/backups/imobiliaria`, com
acesso somente de root e retencao de 14 dias para backups completos.

Cada backup inclui SQL, fotos, configuracao privada, checksums e um marcador
COMPLETE. Quando presentes, os certificados e chaves em `/etc/letsencrypt`
tambem sao incluidos. Esses arquivos CONTEM SEGREDOS e nunca devem entrar no Git.
Falhas podem ser consultadas com `systemctl status imobiliaria-backup.service`
e `journalctl -u imobiliaria-backup.service`. Nao ha alertas externos.

Um backup na mesma maquina nao protege contra perda do servidor. A copia
externa atual foi feita manualmente para o PC do operador. Uma rotina de
copia externa automatica ainda deve ser configurada.

## Teste de restauracao

```bash
sudo bash ops/test-restore.sh /var/backups/imobiliaria/DIRETORIO_COMPLETO
```

O teste cria um MySQL isolado, sem rede e sem volumes de producao, importa
o SQL, verifica as tabelas e remove apenas o container descartavel e seu
volume anonimo. Precisa de aproximadamente 512 MB adicionais de memoria.
As fotos sao validadas pelos checksums e pela leitura do arquivo tar no backup.

Para recuperacao real, pare as escritas, preserve os volumes atuais e restaure
em volumes novos. Recupere `.env.prod` do arquivo privado de configuracao,
inicialize o MySQL com essas credenciais, importe o SQL e extraia as fotos em
`/app/uploads` com as permissoes do usuario da API. Valide em ambiente privado
antes de trocar o trafego. Nao execute `down -v` na stack de producao.

## Credenciais

A migracao inicial de credenciais foi concluida. Os arquivos privados em
`/root/imobiliaria-credentials` devem ser preservados fora do Git; snapshots
antigos nao representam necessariamente as senhas atuais.

Alterar apenas o `.env` nao troca a senha de usuarios existentes no banco.
Use `verify-private-login.py` para validar o login pelo tunel e
`verify-public-admin.py` para validar HTTPS, autenticacao e limite de login.

Execute esses scripts com `sudo`: eles leem o arquivo privado
`/root/imobiliaria-credentials/admin.txt`. O teste publico faz leituras e
requisicoes de login para verificar HTTP 429, consumindo temporariamente
a cota de login do IP de origem; nao modifica imoveis.

## Certificados

O timer `snap.certbot.renew.timer` cuida da renovacao. O hook
`/etc/letsencrypt/renewal-hooks/deploy/imobiliaria-nginx` valida e recarrega
o Nginx. Mantenha a porta 80 aberta para ACME mesmo com o site em HTTPS.
Nao ha alerta externo automatico para falhas de renovacao ou backup.

## Atualizacoes

Atualizacoes automaticas de seguranca do Ubuntu estao habilitadas. Reinicios
necessarios devem ser acompanhados pelo operador; nao ha reinicio automatico
configurado por estes scripts. Imagens Docker e dependencias da aplicacao
precisam de atualizacao propria, nao sao corrigidas pelo apt do host.
