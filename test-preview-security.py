"""Exercise preview HTTP boundaries without touching a real ledger database."""
import copy
import http.client
import json
import pathlib
import tempfile
import threading
import unittest

import server


class PreviewSecurityTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.original_db = server.DB
        server.DB = pathlib.Path(self.tmp.name) / 'preview-test.sqlite3'
        server.connect().close()
        self.http = server.http.server.ThreadingHTTPServer(('127.0.0.1', 0), server.Handler)
        self.thread = threading.Thread(target=self.http.serve_forever, daemon=True)
        self.thread.start()
        self.host = f'127.0.0.1:{self.http.server_port}'
        self.data = {'version': 1, 'entries': [{'id': 'entry', 'type': 'expense', 'amount': 100,
                     'category': 'food', 'account': 'cash', 'book': 'daily',
                     'date': '2026-10-24', 'note': 'synthetic', 'autoSource': 'synthetic source'}]}
        self.assertEqual(self.request('POST', json.dumps(self.data).encode())[0], 200)

    def tearDown(self):
        self.http.shutdown()
        self.http.server_close()
        self.thread.join()
        server.DB = self.original_db
        self.tmp.cleanup()

    def request(self, method, body=b'', hosts=None, origins=(), lengths=None, extra=(), path='/api/state'):
        conn = http.client.HTTPConnection('127.0.0.1', self.http.server_port, timeout=3)
        conn.putrequest(method, path, skip_host=True, skip_accept_encoding=True)
        for value in [self.host] if hosts is None else hosts:
            conn.putheader('Host', value)
        for value in origins:
            conn.putheader('Origin', value)
        if method == 'POST':
            for value in [str(len(body))] if lengths is None else lengths:
                conn.putheader('Content-Length', value)
        for key, value in extra:
            conn.putheader(key, value)
        conn.endheaders(body)
        response = conn.getresponse()
        result = response.status, response.read()
        conn.close()
        return result

    def test_loopback_hosts_and_origins(self):
        for hostname in ('127.0.0.1', 'localhost'):
            host = f'{hostname}:{self.http.server_port}'
            for method in ('GET', 'POST'):
                with self.subTest(host=hostname, method=method):
                    self.assertEqual(self.request(method, json.dumps(self.data).encode(),
                                     hosts=[host], origins=[f'http://{host}'])[0], 200)

    def test_rebinding_and_cross_origin_requests_are_rejected(self):
        invalid_hosts = ([], ['attacker.invalid'], [f'attacker.invalid:{self.http.server_port}'],
                         [self.host + '.attacker.invalid'], [self.host, self.host],
                         ['localhost'], [f'localhost:{self.http.server_port + 1}'])
        for method in ('GET', 'HEAD', 'POST'):
            for hosts in invalid_hosts:
                with self.subTest(method=method, case='host'):
                    self.assertEqual(self.request(method, b'{}', hosts=hosts)[0], 403)
            for origins in (['https://attacker.invalid'], ['null'],
                            [f'http://localhost:{self.http.server_port + 1}'],
                            [f'http://{self.host}', f'http://{self.host}']):
                with self.subTest(method=method, case='origin'):
                    self.assertEqual(self.request(method, b'{}', origins=origins)[0], 403)
        self.assertEqual(self.request('GET', hosts=['attacker.invalid'], path='/index.html')[0], 403)

    def test_malformed_request_lengths_return_client_errors(self):
        for lengths in ([], ['invalid'], ['-1'], ['0'], ['1.2'], ['2', '2']):
            with self.subTest(lengths=lengths):
                self.assertEqual(self.request('POST', b'{}', lengths=lengths)[0], 400)
        self.assertEqual(self.request('POST', b'{}', lengths=['10000001'])[0], 413)
        self.assertEqual(self.request('POST', b'{}', extra=[('Transfer-Encoding', 'chunked')])[0], 400)
        self.assertEqual(json.loads(self.request('GET')[1]), self.data)

    def test_malformed_fields_roll_back_and_server_keeps_serving(self):
        invalid = [[], {}, {'entries': None}, {'entries': [None]}, {'entries': [[]]}]
        for key, value in [('amount', True), ('amount', 1.5), ('amount', 0),
                           ('type', []), ('id', {}), ('note', []), ('category', None),
                           ('account', 1), ('book', False), ('date', {})]:
            data = copy.deepcopy(self.data)
            data['entries'][0][key] = value
            invalid.append(data)
        duplicate = copy.deepcopy(self.data)
        duplicate['entries'] *= 2
        invalid.append(duplicate)
        for data in invalid:
            with self.subTest(case=type(data).__name__):
                self.assertEqual(self.request('POST', json.dumps(data).encode())[0], 400)
                self.assertEqual(json.loads(self.request('GET')[1]), self.data)
        self.assertEqual(self.request('POST', b'not json')[0], 400)
        deeply_nested = b'{"entries":' + b'[' * 1200 + b'0' + b']' * 1200 + b'}'
        self.assertEqual(self.request('POST', deeply_nested)[0], 400)
        self.assertEqual(json.loads(self.request('GET')[1]), self.data)


if __name__ == '__main__':
    unittest.main()
