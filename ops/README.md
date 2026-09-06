# Operacao do servidor

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
COMPLETE. Esses arquivos CONTEM SEGREDOS e nunca devem entrar no Git.
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

`rotate-credentials.py` e uma rotina de uso UNICO para migrar as credenciais
do ensaio local. Atualiza a senha do administrador existente, as contas do
MySQL e JWT. Gera recuperacao privada em `/root/imobiliaria-credentials` e
recusa uma segunda execucao. Nao altera o email administrativo.

Se houver falha parcial, nao execute novamente nem restaure apenas o `.env`:
compare o estado do MySQL com `previous.env` e `new.env` na pasta privada de
recuperacao e ajuste as contas antes de reiniciar os servicos. ALTER USER nao
e transacional. `verify-private-login.py` testa o novo login sem imprimir JWT.

Atualizacoes automaticas de seguranca do Ubuntu estao habilitadas. Reinicios
necessarios devem ser acompanhados pelo operador; nao ha reinicio automatico
configurado por estes scripts. Imagens Docker e dependencias da aplicacao
precisam de atualizacao propria, nao sao corrigidas pelo apt do host.
