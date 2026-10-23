"""Loopback-only development preview with a local SQLite store."""
import json, sqlite3, pathlib, http.server, argparse, threading, re
ROOT = pathlib.Path(__file__).resolve().parent
DB = ROOT / 'preview.sqlite3'
LOCK = threading.Lock()
class ClosingConnection(sqlite3.Connection):
    def __exit__(self,*args):
        try: return super().__exit__(*args)
        finally: self.close()
def connect():
    c=sqlite3.connect(DB,factory=ClosingConnection)
    c.execute('CREATE TABLE IF NOT EXISTS state(id INTEGER PRIMARY KEY CHECK(id=1), payload TEXT NOT NULL)')
    c.execute('CREATE TABLE IF NOT EXISTS entries(id TEXT PRIMARY KEY,type TEXT NOT NULL,amount INTEGER NOT NULL,category TEXT NOT NULL,account TEXT NOT NULL,book TEXT NOT NULL,date TEXT NOT NULL,note TEXT NOT NULL)')
    return c
class Handler(http.server.SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header("Cache-Control","no-store")
        super().end_headers()
    def log_message(self,format,*args): pass
    def __init__(self,*args,**kwargs): super().__init__(*args,directory=str(ROOT/'web'),**kwargs)
    def allowed_request(self):
        port=self.server.server_port
        hosts={f'127.0.0.1:{port}',f'localhost:{port}'}
        if port==80: hosts.update(('127.0.0.1','localhost'))
        supplied_hosts=self.headers.get_all('Host',[])
        origins=self.headers.get_all('Origin',[])
        allowed_origins={f'http://{host}' for host in hosts}
        if len(supplied_hosts)!=1 or supplied_hosts[0] not in hosts or len(origins)>1 or (origins and origins[0] not in allowed_origins):
            self.send_error(403); return False
        return True
    def do_HEAD(self):
        if self.allowed_request(): super().do_HEAD()
    def do_GET(self):
        if not self.allowed_request(): return
        if self.path=='/api/state':
            with LOCK,connect() as c: row=c.execute('SELECT payload FROM state WHERE id=1').fetchone()
            body=(row[0] if row else 'null').encode()
            self.send_response(200); self.send_header('Content-Type','application/json; charset=utf-8'); self.send_header('Cache-Control','no-store'); self.end_headers(); self.wfile.write(body)
        else: super().do_GET()
    def do_POST(self):
        if not self.allowed_request(): return
        if self.path!='/api/state': self.send_error(404); return
        try:
            lengths=self.headers.get_all('Content-Length',[])
            if self.headers.get_all('Transfer-Encoding') or len(lengths)!=1 or not re.fullmatch(r'[0-9]+',lengths[0]): raise ValueError()
            size=int(lengths[0])
            if size<1: raise ValueError()
            if size>10_000_000: self.send_error(413); return
            data=json.loads(self.rfile.read(size))
            if not isinstance(data,dict) or not isinstance(data['entries'],list): raise ValueError()
            for e in data['entries']:
                if not isinstance(e,dict) or type(e['amount']) is not int or not 0<e['amount']<=99999999999: raise ValueError()
                if e['type'] not in ('income','expense') or any(not isinstance(e[k],str) for k in ('id','type','category','account','book','date','note')): raise ValueError()
            payload=json.dumps(data,ensure_ascii=False,allow_nan=False)
            with LOCK,connect() as c:
                c.execute('INSERT INTO state VALUES(1,?) ON CONFLICT(id) DO UPDATE SET payload=excluded.payload',(payload,))
                c.execute('DELETE FROM entries')
                for e in data['entries']:
                    c.execute('INSERT INTO entries VALUES(?,?,?,?,?,?,?,?)',tuple(e[k] for k in ('id','type','amount','category','account','book','date','note')))
        except (ValueError,KeyError,TypeError,RecursionError,sqlite3.IntegrityError,sqlite3.DataError,sqlite3.ProgrammingError): self.send_error(400); return
        self.send_response(200); self.send_header('Content-Type','application/json'); self.end_headers(); self.wfile.write(b'{"ok":true}')
if __name__=='__main__':
    parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=8765);args=parser.parse_args()
    connect().close()
    http.server.ThreadingHTTPServer(('127.0.0.1',args.port),Handler).serve_forever()
