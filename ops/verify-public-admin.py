"""Read-only HTTPS administrator checks; never print passwords or tokens."""
import json
import argparse
from pathlib import Path
import time
import urllib.error
import urllib.request

parser = argparse.ArgumentParser()
parser.add_argument('--base-url', choices=['https://54.94.105.56',
    'https://elianecarneiroimoveis.com.br'], default='https://elianecarneiroimoveis.com.br')
base = parser.parse_args().base_url

def request(path, body=None, headers=None):
    req = urllib.request.Request(base + path, data=body, headers=headers or {})
    try:
        with urllib.request.urlopen(req, timeout=10) as response:
            return response.status, response.read()
    except urllib.error.HTTPError as error:
        return error.code, b''

for attempt in range(60):
    try:
        if request('/api/imoveis')[0] == 200:
            break
    except urllib.error.URLError:
        pass
    time.sleep(2)
else:
    raise RuntimeError('API did not become ready')

assert request('/admin/login')[0] == 200
assert request('/api/admin/imoveis')[0] in (401, 403)
lines = Path('/root/imobiliaria-credentials/admin.txt').read_text().splitlines()
email, password = [line.split(': ', 1)[1] for line in lines]
body = json.dumps({'email': email, 'senha': password}).encode()
status, response = request('/api/auth/login', body,
                           {'Content-Type': 'application/json', 'Origin': base})
assert status == 200, f'HTTPS login status: {status}'
token = json.loads(response)['token']
assert request('/api/admin/imoveis', headers={
    'Authorization': 'Bearer ' + token, 'Origin': base})[0] == 200
assert request('/api/admin/imoveis', headers={
    'Authorization': 'Bearer ' + token, 'Origin': 'https://invalid.example'})[0] == 403
statuses = [request('/api/auth/login', b'{}', {'Content-Type': 'application/json'})[0]
            for _ in range(8)]
assert 429 in statuses, 'Login rate limit did not activate'
print('HTTPS admin page, login, authenticated read, anonymous denial, CORS and rate limit: OK')
