"""One-time rotation of local-test credentials. Run as root on the server."""
import json
import os
from pathlib import Path
import re
import secrets
import subprocess

os.umask(0o077)
os.chdir('/opt/imobiliaria/imobiliaria-api')
compose = ['docker', 'compose', '-p', 'imobiliaria', '--env-file', '.env.prod',
           '-f', 'docker-compose.prod.yml', '-f', 'docker-compose.ip.yml']
config = json.loads(subprocess.check_output(compose + ['config', '--format', 'json']))
mysql = config['services']['mysql-prod']['environment']
api = config['services']['api-prod']['environment']
recovery = Path('/root/imobiliaria-credentials')
recovery.mkdir(mode=0o700, exist_ok=False)
env_path = Path('.env.prod')
original = env_path.read_text()
(recovery / 'previous.env').write_text(original)
new_root, new_db, new_admin = [secrets.token_hex(24) for _ in range(3)]
new_jwt = secrets.token_hex(48)
password_hash = subprocess.check_output(
    ['htpasswd', '-niBC', '12', 'admin'], input=(new_admin + '\n').encode()
).decode().strip().split(':', 1)[1]
email = api['APP_ADMIN_EMAIL']

def sql_literal(value):
    return "'" + value.replace('\\', '\\\\').replace("'", "''") + "'"

def query(sql):
    return subprocess.check_output(
        ['docker', 'exec', '-i', 'imobiliaria-mysql-prod', 'sh', '-c',
         'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" exec mysql -uroot -N -B'],
        input=sql.encode()).decode()

database = mysql['MYSQL_DATABASE']
if not re.fullmatch(r'[A-Za-z0-9_]+', database):
    raise RuntimeError('Unexpected database identifier')
if query(f'SELECT COUNT(*) FROM `{database}`.usuarios WHERE email={sql_literal(email)};').strip() != '1':
    raise RuntimeError('Expected exactly one configured administrator')
accounts = [line.split('\t') for line in query(
    'SELECT User, Host FROM mysql.user WHERE User IN (' +
    sql_literal(mysql['MYSQL_USER']) + ", 'root');").splitlines()]
replacements = {
    'IMOBILIARIA_MYSQL_ROOT_PASSWORD': new_root,
    'IMOBILIARIA_MYSQL_PASSWORD': new_db,
    'JWT_SECRET': new_jwt,
    'APP_ADMIN_SENHA': new_admin,
}
updated = original
for key, value in replacements.items():
    updated, count = re.subn(r'^' + key + r'=.*$', key + '=' + value,
                            updated, flags=re.MULTILINE)
    if count != 1:
        raise RuntimeError('Expected exactly one setting: ' + key)
# Save recovery material before any database change; never print secrets.
(recovery / 'new.env').write_text(updated)
(recovery / 'admin.txt').write_text('Email: ' + email + '\nSenha: ' + new_admin + '\n')
subprocess.run(['docker', 'stop', 'imobiliaria-api-prod'], check=True, stdout=subprocess.DEVNULL)
statements = [f'UPDATE `{database}`.usuarios SET senha={sql_literal(password_hash)} WHERE email={sql_literal(email)};']
for user, host in accounts:
    password = new_root if user == 'root' else new_db
    statements.append('ALTER USER ' + sql_literal(user) + '@' + sql_literal(host) +
                      ' IDENTIFIED BY ' + sql_literal(password) + ';')
query('\n'.join(statements))
temporary = Path('.env.prod.new')
temporary.write_text(updated)
temporary.chmod(0o600)
temporary.replace(env_path)
subprocess.run(compose + ['up', '-d', '--no-build'], check=True)
print('Credentials rotated. Recovery material: /root/imobiliaria-credentials')
