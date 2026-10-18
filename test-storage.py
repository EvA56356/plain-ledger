import unittest,tempfile,pathlib,threading,urllib.request,urllib.error,json,sqlite3
import server
class StorageTest(unittest.TestCase):
    def test_sqlite_roundtrip_and_rollback(self):
        with tempfile.TemporaryDirectory() as tmp:
            original=server.DB
            server.DB=pathlib.Path(tmp)/'test.sqlite3'
            server.connect().close()
            http=server.http.server.ThreadingHTTPServer(('127.0.0.1',0),server.Handler)
            thread=threading.Thread(target=http.serve_forever,daemon=True);thread.start()
            url=f'http://127.0.0.1:{http.server_port}/api/state'
            def post(data):
                req=urllib.request.Request(url,json.dumps(data).encode(),{'Content-Type':'application/json'},method='POST')
                return urllib.request.urlopen(req).read()
            try:
                data={'version':1,'entries':[{'id':'x','type':'expense','amount':1234,'category':'e0','account':'cash','book':'daily','date':'2026-10-15','note':'test','projectId':'dalian'}],'projects':[{'id':'travel','name':'Travel','book':'daily'},{'id':'dalian','name':'Dalian','book':'daily','parentId':'travel','dropdownVisible':False}]}
                post(data)
                self.assertEqual(json.load(urllib.request.urlopen(url)),data)
                with server.connect() as c:self.assertEqual(c.execute('SELECT amount FROM entries').fetchone()[0],1234)
                data['entries'][0]['amount']=0
                with self.assertRaises(urllib.error.HTTPError):post(data)
                self.assertEqual(json.load(urllib.request.urlopen(url))['entries'][0]['amount'],1234)
                with server.connect() as c:self.assertEqual(c.execute('SELECT amount FROM entries').fetchone()[0],1234)
            finally:
                http.shutdown();http.server_close();thread.join();server.DB=original
if __name__=='__main__':unittest.main()
