"""Verify the new administrator password without logging credentials or JWT."""
import json
from pathlib import Path
import time
import urllib.error
import urllib.request

lines = Path('/root/imobiliaria-credentials/admin.txt').read_text().splitlines()
email, password = [line.split(': ', 1)[1] for line in lines]
body = json.dumps({'email': email, 'senha': password}).encode()
for attempt in range(30):
    try:
        request = urllib.request.Request('http://127.0.0.1:8081/api/auth/login',
                                         data=body, headers={'Content-Type': 'application/json'})
        with urllib.request.urlopen(request, timeout=5) as response:
            assert response.status == 200
        print('New administrator login: OK')
        break
    except (urllib.error.URLError, TimeoutError):
        if attempt == 29:
            raise RuntimeError('Login validation failed') from None
        time.sleep(2)
