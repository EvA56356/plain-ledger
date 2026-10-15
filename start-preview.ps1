param([string]$Python='python')
$ErrorActionPreference='Stop'
$appRoot=$PSScriptRoot
try { $null=Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8765/api/state' -TimeoutSec 2 }
catch {
    $pythonPath=(Get-Command $Python -ErrorAction Stop).Source
    Start-Process -FilePath $pythonPath -ArgumentList @(('"'+(Join-Path $appRoot 'server.py')+'"')) -WorkingDirectory $appRoot -WindowStyle Hidden
    $previewReady=$false
    for($attempt=0;$attempt -lt 20;$attempt++) {
        Start-Sleep -Milliseconds 200
        try { $null=Invoke-WebRequest -UseBasicParsing -Uri 'http://127.0.0.1:8765/api/state' -TimeoutSec 1; $previewReady=$true; break } catch {}
    }
    if(!$previewReady){throw 'Preview did not start. Run python server.py to inspect the error.'}
}
Start-Process 'http://127.0.0.1:8765'
