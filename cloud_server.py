"""Account and shared-ledger server. No external dependencies. See docs/SERVER.md."""
import argparse,base64,copy,datetime,hashlib,hmac,http.cookies,http.server,json,os,pathlib,re,secrets,sqlite3,threading,time,urllib.parse,urllib.request
ROOT=pathlib.Path(__file__).resolve().parent
DB=ROOT/'cloud.sqlite3';SECRET=b'';DEV_SMS=False;PUBLIC_URL='';LOCK=threading.RLock();LIMITS={}
class APIError(Exception):
 def __init__(self,status,message):self.status=status;self.message=message
def fail(status,message):raise APIError(status,message)
def now():return int(time.time())
def connect():
 c=sqlite3.connect(DB);c.row_factory=sqlite3.Row;c.execute('PRAGMA foreign_keys=ON');return c
def initialize():
 with connect() as c:
  c.executescript('''PRAGMA journal_mode=WAL;
CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY,phone TEXT UNIQUE NOT NULL,name TEXT NOT NULL,password TEXT NOT NULL,created INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS sessions(token TEXT PRIMARY KEY,user TEXT NOT NULL REFERENCES users(id),expires INTEGER NOT NULL);
CREATE TABLE IF NOT EXISTS otp(phone TEXT NOT NULL,purpose TEXT NOT NULL,digest TEXT NOT NULL,expires INTEGER NOT NULL,attempts INTEGER NOT NULL DEFAULT 0,PRIMARY KEY(phone,purpose));
CREATE TABLE IF NOT EXISTS spaces(id TEXT PRIMARY KEY,name TEXT NOT NULL,owner TEXT NOT NULL REFERENCES users(id),shared INTEGER NOT NULL,payload TEXT,revision INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS members(space TEXT NOT NULL REFERENCES spaces(id),user TEXT NOT NULL REFERENCES users(id),role TEXT NOT NULL,PRIMARY KEY(space,user));
CREATE TABLE IF NOT EXISTS invites(id TEXT PRIMARY KEY,space TEXT NOT NULL REFERENCES spaces(id),phone TEXT NOT NULL,role TEXT NOT NULL,expires INTEGER NOT NULL,status TEXT NOT NULL DEFAULT 'pending');
CREATE TABLE IF NOT EXISTS audit(id TEXT PRIMARY KEY,space TEXT NOT NULL,user TEXT NOT NULL,action TEXT NOT NULL,created INTEGER NOT NULL);
''')
def phone(value):
 s=str(value or '').strip().replace(' ','').replace('-','')
 if re.fullmatch(r'1[3-9]\d{9}',s):s='+86'+s
 if not re.fullmatch(r'\+[1-9]\d{7,14}',s):fail(400,'请输入有效手机号，国际号码需包含国家区号')
 return s
def password_hash(value):
 if not isinstance(value,str) or not 12<=len(value)<=128:fail(400,'密码需为 12—128 个字符')
 salt=secrets.token_hex(16);digest=hashlib.scrypt(value.encode(),salt=bytes.fromhex(salt),n=16384,r=8,p=1).hex();return salt+':'+digest
def password_ok(value,stored):
 try:
  if not isinstance(value,str) or len(value)>128:return False
  salt,digest=stored.split(':');return hmac.compare_digest(hashlib.scrypt(value.encode(),salt=bytes.fromhex(salt),n=16384,r=8,p=1).hex(),digest)
 except (ValueError,TypeError):return False
def rate(key,count,seconds):
 t=time.time();q=[x for x in LIMITS.get(key,[]) if t-x<seconds]
 if len(q)>=count:fail(429,'操作过于频繁，请稍后再试')
 LIMITS[key]=q+[t]
 if len(LIMITS)>10000:
  for k in list(LIMITS):
   if not LIMITS[k] or t-LIMITS[k][-1]>3600:LIMITS.pop(k,None)
def code_digest(p,purpose,code):return hmac.new(SECRET,(p+'|'+purpose+'|'+code).encode(),hashlib.sha256).hexdigest()
def consume_code(c,p,purpose,code):
 row=c.execute('SELECT * FROM otp WHERE phone=? AND purpose=?',(p,purpose)).fetchone()
 if not row or row['expires']<now() or row['attempts']>=5:fail(400,'验证码无效或已过期，请重新获取')
 c.execute('UPDATE otp SET attempts=attempts+1 WHERE phone=? AND purpose=?',(p,purpose));c.commit()
 if not hmac.compare_digest(row['digest'],code_digest(p,purpose,str(code))):fail(400,'验证码无效或已过期')
 c.execute('DELETE FROM otp WHERE phone=? AND purpose=?',(p,purpose));c.commit()
def send_sms(p,code):
 if DEV_SMS:return
 sid=os.environ.get('TWILIO_ACCOUNT_SID');token=os.environ.get('TWILIO_AUTH_TOKEN');sender=os.environ.get('TWILIO_FROM')
 if not sid or not token or not sender:fail(503,'短信服务尚未配置，请联系服务器管理员')
 data=urllib.parse.urlencode({'To':p,'From':sender,'Body':'简账验证码 '+code+'，5 分钟有效。请勿向他人泄露。'}).encode()
 request=urllib.request.Request('https://api.twilio.com/2010-04-01/Accounts/'+urllib.parse.quote(sid,safe='')+'/Messages.json',data,{'Authorization':'Basic '+base64.b64encode((sid+':'+token).encode()).decode()})
 try:
  with urllib.request.urlopen(request,timeout=15) as response:
   if response.status not in (200,201):fail(503,'短信发送失败，请稍后重试')
 except Exception:fail(503,'短信发送失败，请检查服务商配置及地区支持')
def public_user(u):return {'id':u['id'],'phone':u['phone'],'name':u['name']}
def new_session(c,uid):
 token=secrets.token_urlsafe(32);c.execute('DELETE FROM sessions WHERE expires<?',(now(),));c.execute('INSERT INTO sessions VALUES(?,?,?)',(hashlib.sha256(token.encode()).hexdigest(),uid,now()+7*86400));return token
def membership(c,uid,sid,write=False,owner=False):
 row=c.execute('SELECT s.*,m.role FROM spaces s JOIN members m ON s.id=m.space WHERE s.id=? AND m.user=?',(sid,uid)).fetchone()
 if not row:fail(404,'账本不存在或你没有访问权限')
 if owner and row['owner']!=uid:fail(403,'只有账本创建者可以管理成员')
 if write and row['role']=='viewer':fail(403,'当前为只读成员，不能修改账本')
 return row
def validate_state(d,shared=False):
 if not isinstance(d,dict) or d.get('version')!=1:fail(400,'账本格式无效')
 fields=['entries','books','accounts','categories'];ids={}
 for k in fields:
  a=d.get(k)
  if not isinstance(a,list) or len(a)>50000:fail(400,'账本数据格式或大小无效')
  ids[k]=set()
  for x in a:
   if not isinstance(x,dict) or not isinstance(x.get('id'),str) or not 1<=len(x['id'])<=100 or x['id'] in ids[k]:fail(400,'账本编号无效或重复')
   ids[k].add(x['id'])
   if k!='entries' and (not isinstance(x.get('name'),str) or not 1<=len(x['name'].strip())<=40):fail(400,'名称格式无效')
 if not d['books'] or not d['accounts'] or not d['categories'] or (shared and len(d['books'])!=1):fail(400,'共享空间内仅允许一个账本，请从协作菜单新建另一个共享账本')
 cats={x['id']:x for x in d['categories']}
 for a in d['accounts']:
  if type(a.get('opening')) is not int or abs(a['opening'])>10**14:fail(400,'初始余额无效')
 for cat in d['categories']:
  if cat.get('type') not in ('income','expense'):fail(400,'分类类型无效')
  parent=cat.get('parentId')
  if parent is not None and not isinstance(parent,str):fail(400,'上级分类编号无效')
  if parent and (parent not in cats or parent==cat['id'] or cats[parent].get('parentId') or cats[parent].get('type')!=cat.get('type')):fail(400,'分类仅支持同类型的两级结构')
 for e in d['entries']:
  if type(e.get('amount')) is not int or not 0<e['amount']<=99999999999 or e.get('type') not in ('expense','income'):fail(400,'账单金额或类型无效')
  if e.get('book') not in ids['books'] or e.get('account') not in ids['accounts'] or e.get('category') not in ids['categories'] or cats[e['category']]['type']!=e['type']:fail(400,'账单引用无效')
  try:datetime.date.fromisoformat(e['date'])
  except (ValueError,TypeError,KeyError):fail(400,'日期无效')
  if not isinstance(e.get('note'),str) or len(e['note'])>120:fail(400,'备注无效')
 budgets=d.get('budgets')
 if not isinstance(budgets,dict) or any(type(v) is not int or v<0 or v>10**14 for v in budgets.values()):fail(400,'预算无效')
 transfers=d.get('transfers',[])
 if not isinstance(transfers,list) or len(transfers)>50000:fail(400,'转账格式无效')
 seen=set()
 for t in transfers:
  if not isinstance(t,dict) or not isinstance(t.get('id'),str) or t['id'] in seen or t.get('from') not in ids['accounts'] or t.get('to') not in ids['accounts'] or t['from']==t['to'] or type(t.get('amount')) is not int or not 0<t['amount']<=99999999999:fail(400,'转账无效')
  seen.add(t['id'])
  try:datetime.date.fromisoformat(t['date'])
  except (ValueError,TypeError,KeyError):fail(400,'转账日期无效')
 return d
class Handler(http.server.SimpleHTTPRequestHandler):
 def __init__(self,*args,**kw):super().__init__(*args,directory=str(ROOT/'web'),**kw)
 def log_message(self,*args):pass
 def end_headers(self):
  self.send_header('Cache-Control','no-store');self.send_header('X-Content-Type-Options','nosniff');self.send_header('Referrer-Policy','no-referrer');self.send_header('X-Frame-Options','DENY');super().end_headers()
 def reply(self,status,data,cookie=None):
  b=json.dumps(data,ensure_ascii=False).encode();self.send_response(status);self.send_header('Content-Type','application/json; charset=utf-8');self.send_header('Content-Length',str(len(b)))
  if cookie is not None:self.send_header('Set-Cookie','ledger_session='+cookie+'; HttpOnly; SameSite=Strict; Path=/; Max-Age='+('604800' if cookie else '0')+(' ; Secure' if PUBLIC_URL.startswith('https://') else ''))
  self.end_headers();self.wfile.write(b)
 def do_GET(self):
  if self.path.startswith('/api/'):self.handle_api()
  else:super().do_GET()
 def do_POST(self):self.handle_api()
 def do_PUT(self):self.handle_api()
 def token(self):
  jar=http.cookies.SimpleCookie()
  try:jar.load(self.headers.get('Cookie',''))
  except http.cookies.CookieError:pass
  return jar['ledger_session'].value if 'ledger_session' in jar else ''
 def handle_api(self):
  try:
   if self.command!='GET':
    origin=self.headers.get('Origin');allowed=PUBLIC_URL or 'http://'+self.headers.get('Host','')
    if self.headers.get('X-Requested-With')!='PlainLedger' or (origin is not None and origin!=allowed):fail(403,'请求来源验证失败')
    try:size=int(self.headers.get('Content-Length','0'))
    except ValueError:fail(400,'请求长度无效')
    if not 0<size<=10_000_000:fail(413,'请求数据过大或为空')
    try:d=json.loads(self.rfile.read(size))
    except (ValueError,UnicodeError):fail(400,'请求格式无效')
    if not isinstance(d,dict):fail(400,'请求格式无效')
   else:d={}
   with LOCK:
    c=connect()
    try:
     result,cookie=self.route(c,self.path.split('?')[0],d);c.commit()
    except Exception:c.rollback();raise
    finally:c.close()
   self.reply(200,result,cookie)
  except APIError as e:self.reply(e.status,{'error':e.message})
  except Exception:self.reply(500,{'error':'服务器处理失败，请稍后重试。未确认保存的修改请保留。'})
 def route(self,c,path,d):
  method=self.command;ip=self.client_address[0]
  if path=='/api/config' and method=='GET':return {'devSms':DEV_SMS,'mode':'shared'},None
  if path=='/api/auth/code' and method=='POST':
   p=phone(d.get('phone'));purpose=d.get('purpose','register')
   if purpose not in ('register','reset'):fail(400,'验证码用途无效')
   rate('smsip:'+ip,10,3600);rate('sms:'+p,1,60);rate('smsday:'+p,10,86400)
   code=str(secrets.randbelow(1000000)).zfill(6);send_sms(p,code)
   c.execute('INSERT OR REPLACE INTO otp VALUES(?,?,?,?,0)',(p,purpose,code_digest(p,purpose,code),now()+300))
   return dict({'ok':True},**({'devCode':code,'notice':'仅本机开发短信，未发送真实短信'} if DEV_SMS else {})),None
  if path=='/api/auth/register' and method=='POST':
   rate('register:'+ip,15,3600);p=phone(d.get('phone'));hashed=password_hash(d.get('password'));consume_code(c,p,'register',d.get('code'))
   if c.execute('SELECT 1 FROM users WHERE phone=?',(p,)).fetchone():fail(400,'无法注册，请尝试登录或重置密码')
   uid=secrets.token_hex(16);name=str(d.get('name','新用户')).strip()[:40] or '新用户';c.execute('INSERT INTO users VALUES(?,?,?,?,?)',(uid,p,name,hashed,now()));sid=secrets.token_hex(16)
   c.execute('INSERT INTO spaces VALUES(?,?,?,?,?,0)',(sid,'个人账本',uid,0,None));c.execute('INSERT INTO members VALUES(?,?,?)',(sid,uid,'owner'))
   return {'user':{'id':uid,'phone':p,'name':name}},new_session(c,uid)
  if path=='/api/auth/login' and method=='POST':
   rate('login:'+ip,30,900);p=phone(d.get('phone'));rate('loginphone:'+p,10,900);u=c.execute('SELECT * FROM users WHERE phone=?',(p,)).fetchone()
   stored=u['password'] if u else '00'*16+':'+'00'*64
   ok=password_ok(d.get('password'),stored)
   if not u or not ok:fail(401,'手机号或密码不正确')
   return {'user':public_user(u)},new_session(c,u['id'])
  if path=='/api/auth/reset' and method=='POST':
   rate('reset:'+ip,10,3600);p=phone(d.get('phone'));hashed=password_hash(d.get('password'));consume_code(c,p,'reset',d.get('code'));u=c.execute('SELECT id FROM users WHERE phone=?',(p,)).fetchone()
   if u:c.execute('UPDATE users SET password=? WHERE id=?',(hashed,u['id']));c.execute('DELETE FROM sessions WHERE user=?',(u['id'],))
   return {'ok':True},''
  token=self.token();session=c.execute('SELECT u.* FROM users u JOIN sessions s ON u.id=s.user WHERE s.token=? AND s.expires>?',(hashlib.sha256(token.encode()).hexdigest(),now())).fetchone()
  if not session:fail(401,'登录已失效，请重新登录')
  uid=session['id']
  if path=='/api/me' and method=='GET':
   spaces=[dict(r) for r in c.execute('SELECT s.id,s.name,s.shared,m.role,s.revision FROM spaces s JOIN members m ON s.id=m.space WHERE m.user=?',(uid,))]
   invitations=[dict(r) for r in c.execute("SELECT i.id,i.space,s.name,i.role,i.expires FROM invites i JOIN spaces s ON s.id=i.space WHERE i.phone=? AND i.status='pending' AND i.expires>?",(session['phone'],now()))]
   return {'user':public_user(session),'spaces':spaces,'invitations':invitations},None
  if path=='/api/auth/logout' and method=='POST':c.execute('DELETE FROM sessions WHERE token=?',(hashlib.sha256(token.encode()).hexdigest(),));return {'ok':True},''
  if path=='/api/account' and method=='POST':
   name=str(d.get('name','')).strip()
   if not 1<=len(name)<=40:fail(400,'昵称需为 1—40 个字符')
   c.execute('UPDATE users SET name=? WHERE id=?',(name,uid));return {'ok':True},None
  if path=='/api/auth/password' and method=='POST':
   rate('password:'+uid,10,3600)
   if not password_ok(d.get('oldPassword'),session['password']):fail(400,'原密码不正确')
   hashed=password_hash(d.get('password'));c.execute('UPDATE users SET password=? WHERE id=?',(hashed,uid));c.execute('DELETE FROM sessions WHERE user=?',(uid,));return {'ok':True},new_session(c,uid)
  if path=='/api/spaces' and method=='POST':
   rate('spaces:'+uid,30,3600);name=str(d.get('name','')).strip()
   if not 1<=len(name)<=20:fail(400,'账本名称需为 1—20 字')
   initial=validate_state(d.get('state'),True)
   if initial['entries'] or initial.get('transfers'):fail(400,'新共享账本需为空，不会自动上传个人历史账单')
   sid=secrets.token_hex(16);initial=copy.deepcopy(initial);initial['books']=[{'id':sid,'name':name}];c.execute('INSERT INTO spaces VALUES(?,?,?,?,?,0)',(sid,name,uid,1,json.dumps(initial,ensure_ascii=False)));c.execute('INSERT INTO members VALUES(?,?,?)',(sid,uid,'owner'));return {'id':sid},None
  if path=='/api/invitations/respond' and method=='POST':
   inv=c.execute("SELECT * FROM invites WHERE id=? AND phone=? AND status='pending' AND expires>?",(d.get('id'),session['phone'],now())).fetchone()
   if not inv:fail(404,'邀请不存在或已过期')
   accepted=d.get('accept') is True
   if accepted:c.execute('INSERT OR IGNORE INTO members VALUES(?,?,?)',(inv['space'],uid,inv['role']))
   c.execute('UPDATE invites SET status=? WHERE id=?',('accepted' if accepted else 'declined',inv['id']));return {'ok':True},None
  match=re.fullmatch(r'/api/spaces/([a-f0-9]{32})/(state|members|invite|remove)',path)
  if match:
   sid,action=match.groups();s=membership(c,uid,sid,write=method!='GET' and action=='state',owner=action in ('invite','remove'))
   if action=='state' and method=='GET':return {'state':json.loads(s['payload']) if s['payload'] else None,'revision':s['revision'],'role':s['role']},None
   if action=='state' and method=='PUT':
    state=validate_state(d.get('state'),bool(s['shared']))
    if type(d.get('revision')) is not int or d['revision']!=s['revision']:fail(409,'其他成员已更新账本。你的修改尚未保存，请导出待保存副本后刷新，再重新应用修改。')
    if s['shared'] and state['books'][0]['id']!=sid:fail(400,'共享账本编号不能改变')
    updated=c.execute('UPDATE spaces SET payload=?,name=?,revision=revision+1 WHERE id=? AND revision=?',(json.dumps(state,ensure_ascii=False),state['books'][0]['name'] if s['shared'] else s['name'],sid,s['revision']))
    if updated.rowcount!=1:fail(409,'账本版本已改变，请保留副本后刷新')
    c.execute('INSERT INTO audit VALUES(?,?,?,?,?)',(secrets.token_hex(16),sid,uid,'save',now()));return {'revision':s['revision']+1},None
   if action=='members' and method=='GET':
    members=[{'id':r['id'],'name':r['name'],'phone':r['phone'] if s['owner']==uid or r['id']==uid else r['phone'][:3]+'****'+r['phone'][-4:],'role':r['role']} for r in c.execute('SELECT u.id,u.name,u.phone,m.role FROM users u JOIN members m ON u.id=m.user WHERE m.space=?',(sid,))]
    pending=[{'id':r['id'],'phone':r['phone'],'role':r['role'],'expires':r['expires']} for r in c.execute("SELECT * FROM invites WHERE space=? AND status='pending' AND expires>?",(sid,now()))] if s['owner']==uid else []
    return {'members':members,'pending':pending},None
   if action=='invite' and method=='POST':
    if not s['shared']:fail(400,'个人空间不能邀请，请新建共享账本')
    rate('invite:'+uid,30,3600);p=phone(d.get('phone'));role=d.get('role')
    if role not in ('editor','viewer'):fail(400,'请选择编辑或只读权限')
    target=c.execute('SELECT id FROM users WHERE phone=?',(p,)).fetchone()
    if not target:fail(400,'请让对方先注册简账账号，再发送邀请')
    if c.execute('SELECT 1 FROM members WHERE space=? AND user=?',(sid,target['id'])).fetchone():fail(400,'对方已经是账本成员')
    c.execute("UPDATE invites SET status='superseded' WHERE space=? AND phone=? AND status='pending'",(sid,p));c.execute('INSERT INTO invites VALUES(?,?,?,?,?,?)',(secrets.token_hex(16),sid,p,role,now()+7*86400,'pending'));return {'ok':True},None
   if action=='remove' and method=='POST':
    if d.get('user')==s['owner']:fail(400,'不能移除账本创建者')
    c.execute('DELETE FROM members WHERE space=? AND user=?',(sid,d.get('user')));c.execute("UPDATE invites SET status='revoked' WHERE space=? AND id=? AND status='pending'",(sid,d.get('invitation')));return {'ok':True},None
  fail(404,'接口不存在')
if __name__=='__main__':
 parser=argparse.ArgumentParser();parser.add_argument('--port',type=int,default=8766);parser.add_argument('--dev-sms',action='store_true');parser.add_argument('--db',type=pathlib.Path,default=DB);args=parser.parse_args();DB=args.db;DEV_SMS=args.dev_sms;PUBLIC_URL=os.environ.get('LEDGER_PUBLIC_URL','').rstrip('/')
 if PUBLIC_URL and not PUBLIC_URL.startswith('https://'):raise SystemExit('LEDGER_PUBLIC_URL must use HTTPS')
 if DEV_SMS and PUBLIC_URL:raise SystemExit('Development SMS cannot be used with a public URL')
 if os.name!='nt':os.umask(0o077)
 keyfile=DB.with_suffix('.secret')
 if not keyfile.exists():keyfile.write_bytes(secrets.token_bytes(32))
 SECRET=keyfile.read_bytes()
 if len(SECRET)<32:raise SystemExit('Invalid server secret file; restore the configured key')
 initialize();print('Shared ledger listening on loopback port',args.port,flush=True);http.server.ThreadingHTTPServer(('127.0.0.1',args.port),Handler).serve_forever()
