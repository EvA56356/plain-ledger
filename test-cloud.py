"""Integration tests use temporary databases and synthetic, unsent phone codes."""
import copy,http.cookiejar,json,pathlib,sys,tempfile,threading,unittest,urllib.request,urllib.error
from unittest.mock import patch
import cloud_server as server
def fresh():return {'version':1,'books':[{'id':'daily','name':'Test'}],'accounts':[{'id':'cash','name':'Cash','opening':0}],'categories':[{'id':'food','name':'Food','type':'expense','icon':'x'}],'entries':[],'budgets':{}}
class Client:
 def __init__(self,url):self.url=url;self.client=urllib.request.build_opener(urllib.request.HTTPCookieProcessor(http.cookiejar.CookieJar()))
 def call(self,path,body=None,method=None):
  req=urllib.request.Request(self.url+path,None if body is None else json.dumps(body).encode(),{'Content-Type':'application/json','X-Requested-With':'PlainLedger'},method=method or ('GET' if body is None else 'POST'))
  try:
   with self.client.open(req) as r:return r.status,json.load(r)
  except urllib.error.HTTPError as e:return e.code,json.load(e)
 def register(self,number):
  status,r=self.call('/api/auth/code',{'phone':number});assert status==200,r
  status,r=self.call('/api/auth/register',{'phone':number,'code':r['devCode'],'password':'Sufficient-Test-Password1','name':'Tester'});assert status==200,r
  return r['user']['id']
class CloudTests(unittest.TestCase):
 def test_two_level_categories(self):
  d=fresh();d['categories'].append({'id':'lunch','name':'Lunch','type':'expense','parentId':'food'})
  server.validate_state(d)
  for parent in ('missing','lunch'):
   bad=copy.deepcopy(d);bad['categories'].append({'id':'bad','name':'Bad','type':'expense','parentId':parent})
   with self.assertRaises(Exception):server.validate_state(bad)
 @classmethod
 def setUpClass(cls):
  cls.temp=tempfile.TemporaryDirectory();server.DB=pathlib.Path(cls.temp.name)/'cloud.sqlite3';server.DEV_SMS=True;server.SECRET=b'test-secret-not-for-production';server.PUBLIC_URL='';server.LIMITS.clear();server.initialize();cls.http=server.http.server.ThreadingHTTPServer(('127.0.0.1',0),server.Handler);cls.thread=threading.Thread(target=cls.http.serve_forever,daemon=True);cls.thread.start();cls.url='http://127.0.0.1:'+str(cls.http.server_port)
 @classmethod
 def tearDownClass(cls):cls.http.shutdown();cls.http.server_close();cls.thread.join();cls.temp.cleanup()
 def test_accounts_sharing_and_conflicts(self):
  a,b,c,viewer=[Client(self.url) for _ in range(4)];aid=a.register('13800000001');bid=b.register('13800000002');c.register('13800000003');vid=viewer.register('13800000004')
  self.assertEqual(a.call('/api/auth/password',{'oldPassword':'wrong','password':'New-Sufficient-Password'})[0],400)
  personal=a.call('/api/me')[1]['spaces'][0]['id'];path='/api/spaces/'+personal+'/state'
  self.assertEqual(a.call(path,{'state':fresh(),'revision':0},'PUT')[0],200)
  self.assertEqual(b.call(path)[0],404)
  code=a.call('/api/spaces',{'name':'Shared Test','state':fresh()})[1]['id'];base='/api/spaces/'+code
  self.assertEqual(a.call(base+'/invite',{'phone':'13800000002','role':'editor'})[0],200)
  self.assertEqual(b.call(base+'/state')[0],404)
  invite=b.call('/api/me')[1]['invitations'][0]['id'];self.assertEqual(c.call('/api/invitations/respond',{'id':invite,'accept':True})[0],404)
  self.assertEqual(b.call('/api/invitations/respond',{'id':invite,'accept':True})[0],200)
  snapshot=b.call(base+'/state')[1];changed=copy.deepcopy(snapshot['state']);changed['entries']=[{'id':'entry1','type':'expense','amount':1234,'category':'food','account':'cash','book':code,'date':'2026-10-18','note':'Synthetic test'}]
  self.assertEqual(b.call(base+'/state',{'state':changed,'revision':snapshot['revision']},'PUT')[0],200)
  self.assertEqual(a.call(base+'/state')[1]['state']['entries'][0]['amount'],1234)
  self.assertEqual(a.call(base+'/state',{'state':snapshot['state'],'revision':snapshot['revision']},'PUT')[0],409)
  bad=copy.deepcopy(changed);bad['entries'][0]['amount']=-1;self.assertEqual(b.call(base+'/state',{'state':bad,'revision':1},'PUT')[0],400)
  self.assertEqual(b.call(base+'/invite',{'phone':'13800000003','role':'editor'})[0],403)
  a.call(base+'/invite',{'phone':'13800000004','role':'viewer'});inv=viewer.call('/api/me')[1]['invitations'][0]['id'];viewer.call('/api/invitations/respond',{'id':inv,'accept':True})
  self.assertEqual(viewer.call(base+'/state')[0],200);self.assertEqual(viewer.call(base+'/state',{'state':changed,'revision':1},'PUT')[0],403)
  self.assertEqual(a.call(base+'/remove',{'user':bid})[0],200);self.assertEqual(b.call(base+'/state')[0],404)
  self.assertEqual(a.call(base+'/remove',{'user':aid})[0],400)
  second=Client(self.url);self.assertEqual(second.call('/api/auth/login',{'phone':'13800000001','password':'Sufficient-Test-Password1'})[0],200)
  self.assertEqual(a.call('/api/auth/password',{'oldPassword':'Sufficient-Test-Password1','password':'New-Sufficient-Password'})[0],200)
  self.assertEqual(second.call('/api/me')[0],401);self.assertEqual(a.call('/api/me')[0],200)
  self.assertEqual(a.call('/api/auth/logout',{})[0],200);self.assertEqual(a.call('/api/me')[0],401)
 def test_otp_replay_reset_and_csrf(self):
  d=Client(self.url);p='13800000005';status,r=d.call('/api/auth/code',{'phone':p});self.assertEqual(status,200);code=r['devCode']
  self.assertEqual(d.call('/api/auth/register',{'phone':p,'code':'WRONG','password':'Sufficient-Test-Password1'})[0],400)
  self.assertEqual(d.call('/api/auth/register',{'phone':p,'code':code,'password':'Sufficient-Test-Password1'})[0],200)
  self.assertEqual(d.call('/api/auth/register',{'phone':p,'code':code,'password':'Sufficient-Test-Password1'})[0],400)
  server.LIMITS.pop('sms:'+server.phone(p),None);r=d.call('/api/auth/code',{'phone':p,'purpose':'reset'})[1]
  self.assertEqual(d.call('/api/auth/reset',{'phone':p,'code':r['devCode'],'password':'Reset-Sufficient-Password'})[0],200)
  self.assertEqual(d.call('/api/me')[0],401);self.assertEqual(d.call('/api/auth/login',{'phone':p,'password':'Reset-Sufficient-Password'})[0],200)
  req=urllib.request.Request(self.url+'/api/auth/logout',b'{}',{'Content-Type':'application/json','Origin':'https://evil.example','X-Requested-With':'PlainLedger'})
  with self.assertRaises(urllib.error.HTTPError) as error:urllib.request.urlopen(req)
  self.assertEqual(error.exception.code,403)
 def test_invalid_states_and_sms_production_guard(self):
  for bad in [{},dict(fresh(),accounts=[]),dict(fresh(),budgets={'daily|2026-10':-1})]:
   with self.assertRaises(server.APIError):server.validate_state(bad)
  self.assertFalse(server.password_ok('bad','broken'));self.assertNotIn('Password',server.password_hash('Long-Synthetic-Password'))
  old=server.DEV_SMS;server.DEV_SMS=False
  try:
   with patch.dict(server.os.environ,{},clear=True):
    with self.assertRaises(server.APIError):server.send_sms('+8613800000099','000000')
  finally:server.DEV_SMS=old
if __name__=='__main__':unittest.main(verbosity=2)
