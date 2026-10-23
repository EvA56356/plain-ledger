"""Verify the published text bundle with Python's standard library only."""
from pathlib import Path
import hashlib,json,re,xml.etree.ElementTree as ET

root=Path(__file__).resolve().parent.parent
def require(condition,message):
    if not condition:raise SystemExit(message)
files={p.relative_to(root).as_posix():p for p in root.rglob('*') if p.is_file() and '.git' not in p.relative_to(root).parts}
files.pop('evidence/FILE_SHA256.json',None)
files={k:p for k,p in files.items() if '__pycache__' not in p.parts}
index=json.loads((root/'evidence/FILE_SHA256.json').read_text(encoding='utf-8'))
require(set(files)==set(index),'File inventory differs from the published manifest')
for rel,p in files.items():
    require(hashlib.sha256(p.read_bytes()).hexdigest()==index[rel],'File digest mismatch: '+rel)
    require(p.suffix.lower() not in {'.apk','.aab','.dex','.so','.jar','.bin','.bks','.keystore','.jks','.db','.sqlite','.sqlite3'},'Unexpected binary/private file: '+rel)
    require(p.name not in {'keys.txt','AGENTS.md'} and not p.name.startswith('.env'),'Unexpected private/config file: '+rel)
    text=p.read_text(encoding='utf-8')
    if p.suffix=='.xml':ET.fromstring(text)
    if p.suffix=='.json':json.loads(text)
    patterns=[r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',r'AKIA[0-9A-Z]{16}',r'gh[pousr]_[A-Za-z0-9]{30,}',r'github_pat_[A-Za-z0-9_]{40,}',r'AIza[0-9A-Za-z_-]{35}',r'(?i)bearer\s+[A-Za-z0-9._~-]{24,}',r'(?i)[A-Z]:[\\/]Users[\\/][^\s/\\]+']
    require(not any(re.search(pattern,text) for pattern in patterns),'Potential credential/local-path pattern: '+rel)
require(len(list((root/'decompiled/java').rglob('*.java')))==7,'Expected seven Java outputs')
require(len(list((root/'decompiled/smali').rglob('*.smali')))==5,'Expected five smali outputs')
require(len(list((root/'decoded/res').rglob('*.xml')))==3356,'Resource XML inventory differs')
manifest=ET.parse(root/'decoded/AndroidManifest.xml').getroot()
require(manifest.get('package')=='com.youqi.miaomiao','Wrong target package')
summary=json.loads((root/'evidence/analysis-summary.json').read_text(encoding='utf-8'))
require(summary['business_code_recovered'] is False,'Coverage must not claim complete business recovery')
print(f'Verified {len(files)} text files, SHA-256 inventory, 7 Java / 5 smali / 3356 resource XML, parseability and selected privacy patterns.')
print('This does not prove business-code recovery, rebuildability or absence of all possible sensitive data.')
