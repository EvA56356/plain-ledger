param(
    [string]$AndroidSdkRoot=$env:ANDROID_SDK_ROOT,
    [string]$JavaHome=$env:JAVA_HOME,
    [string]$BuildToolsVersion='35.0.0',
    [string]$BuildToolsDirectory,
    [string]$AndroidJar
)
$ErrorActionPreference='Stop'
$appRoot=$PSScriptRoot
if(!$AndroidSdkRoot){$AndroidSdkRoot=$env:ANDROID_HOME}
if(!$AndroidSdkRoot -and (!$BuildToolsDirectory -or !$AndroidJar)){throw 'Set ANDROID_SDK_ROOT or provide -BuildToolsDirectory and -AndroidJar.'}
if($BuildToolsDirectory){$buildTools=$BuildToolsDirectory}else{$buildTools=Join-Path $AndroidSdkRoot "build-tools/$BuildToolsVersion"}
if($JavaHome){$javaRoot=Join-Path $JavaHome 'bin'}else{$javaRoot=Split-Path (Get-Command javac -ErrorAction Stop).Source}
if($AndroidJar){$platformJar=$AndroidJar}else{$platformJar=Join-Path $AndroidSdkRoot 'platforms/android-35/android.jar'}
foreach($required in @("$buildTools/aapt2.exe","$buildTools/zipalign.exe","$buildTools/lib/d8.jar","$buildTools/lib/apksigner.jar","$javaRoot/javac.exe",$platformJar)){
    if(!(Test-Path -LiteralPath $required)){throw "Missing build dependency: $required"}
}
$buildRoot=Join-Path $appRoot 'build'
New-Item -ItemType Directory -Force "$buildRoot/classes","$buildRoot/dex","$buildRoot/assets", "$appRoot/dist" | Out-Null
Copy-Item "$appRoot/web/*" "$buildRoot/assets" -Force
function Run-Checked([string]$exe,[string[]]$arguments){ & $exe @arguments; if($LASTEXITCODE -ne 0){throw "Build failed: $exe"} }
Run-Checked "$buildTools/aapt2.exe" @('compile','--dir',"$appRoot/android/res",'-o',"$buildRoot/resources.zip")
Run-Checked "$buildTools/aapt2.exe" @('link','-o',"$buildRoot/base.apk",'-I',$platformJar,'--manifest',"$appRoot/android/AndroidManifest.xml",'-A',"$buildRoot/assets",'--auto-add-overlay',"$buildRoot/resources.zip")
Run-Checked "$javaRoot/javac.exe" @('-encoding','UTF-8','-source','8','-target','8','-classpath',$platformJar,'-d',"$buildRoot/classes","$appRoot/android/src/cn/plainledger/MainActivity.java","$appRoot/android/src/cn/plainledger/PaymentParser.java","$appRoot/android/src/cn/plainledger/AutoBillService.java")
$classFiles=@(Get-ChildItem "$buildRoot/classes" -Recurse -Filter '*.class' | ForEach-Object {$_.FullName})
Run-Checked "$javaRoot/java.exe" (@('-cp',"$buildTools/lib/d8.jar",'com.android.tools.r8.D8','--lib',$platformJar,'--min-api','26','--output',"$buildRoot/dex")+$classFiles)
Copy-Item "$buildRoot/base.apk" "$buildRoot/unsigned.apk" -Force
Add-Type -AssemblyName System.IO.Compression.FileSystem
$archive=[System.IO.Compression.ZipFile]::Open("$buildRoot/unsigned.apk",[System.IO.Compression.ZipArchiveMode]::Update)
try{[System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile($archive,"$buildRoot/dex/classes.dex",'classes.dex') | Out-Null}finally{$archive.Dispose()}
Run-Checked "$buildTools/zipalign.exe" @('-f','4',"$buildRoot/unsigned.apk","$buildRoot/aligned.apk")
$keyPath=Join-Path $appRoot 'local-debug.keystore'
if(!(Test-Path $keyPath)){Run-Checked "$javaRoot/keytool.exe" @('-genkeypair','-keystore',$keyPath,'-storepass','android','-keypass','android','-alias','plainledger','-keyalg','RSA','-keysize','2048','-validity','10000','-dname','CN=Plain Ledger Local Development')}
Run-Checked "$javaRoot/java.exe" @('-jar',"$buildTools/lib/apksigner.jar",'sign','--ks',$keyPath,'--ks-pass','pass:android','--key-pass','pass:android','--out',"$appRoot/dist/简账-sky-1.1.0.apk","$buildRoot/aligned.apk")
Run-Checked "$javaRoot/java.exe" @('-jar',"$buildTools/lib/apksigner.jar",'verify','--verbose',"$appRoot/dist/简账-sky-1.1.0.apk")
Write-Output "APK: $appRoot/dist/简账-sky-1.1.0.apk"
