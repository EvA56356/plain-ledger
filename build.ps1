param(
 [string]$AndroidSdkRoot=$env:ANDROID_SDK_ROOT,
 [string]$JavaHome=$env:JAVA_HOME,
 [string]$GradleHome=$env:GRADLE_HOME,
 [string]$BuildToolsVersion='35.0.0',
 [string]$BuildToolsDirectory,
 [string]$AndroidJar,
 [string]$KeyStore,
 [string]$KeyAlias
)
$ErrorActionPreference='Stop'
$appRoot=$PSScriptRoot
if($JavaHome){$env:JAVA_HOME=$JavaHome}
if(!$env:JAVA_HOME){throw 'Provide -JavaHome pointing to a JDK (17 or newer, tested with 24).'}
if(!$AndroidSdkRoot){$AndroidSdkRoot=$env:ANDROID_HOME}
if(!$AndroidSdkRoot -and $AndroidJar){$AndroidSdkRoot=(Get-Item -LiteralPath $AndroidJar).Directory.Parent.Parent.FullName}
if($AndroidSdkRoot){$env:ANDROID_HOME=$AndroidSdkRoot;[IO.File]::WriteAllText((Join-Path $appRoot 'local.properties'),'sdk.dir='+$AndroidSdkRoot.Replace('\','/')+[Environment]::NewLine)}
if(!$GradleHome){
 $cache=Join-Path $env:LOCALAPPDATA 'PlainLedger/build-tools'
 $GradleHome=Join-Path $cache 'gradle-8.14.3'
 if(!(Test-Path -LiteralPath "$GradleHome/bin/gradle.bat")){
  New-Item -ItemType Directory -Force $cache | Out-Null
  $zip=Join-Path $cache 'gradle-8.14.3-bin.zip'
  Invoke-WebRequest 'https://services.gradle.org/distributions/gradle-8.14.3-bin.zip' -OutFile $zip
  if((Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLower() -ne 'bd71102213493060956ec229d946beee57158dbd89d0e62b91bca0fa2c5f3531'){throw 'Gradle checksum mismatch'}
  Expand-Archive -LiteralPath $zip -DestinationPath $cache -Force
 }
}
$gradleArgs=@('-p',$appRoot,'assembleRelease','--console=plain')
if($KeyStore){$gradleArgs+="-PledgerKey=$KeyStore"}
if($KeyAlias){$gradleArgs+="-PledgerKeyAlias=$KeyAlias"}
# Passwords are read by Gradle from local properties or LEDGER_* environment
# variables, so they do not appear in this script's process arguments or logs.
& "$GradleHome/bin/gradle.bat" @gradleArgs
if($LASTEXITCODE){throw 'Android build failed'}
$manifestText=[IO.File]::ReadAllText((Join-Path $appRoot 'android/AndroidManifest.xml'))
$version=[regex]::Match($manifestText,'versionName="([^"]+)"').Groups[1].Value
$id=[regex]::Match($manifestText,'package="([^"]+)"').Groups[1].Value
$name=switch($id){'cn.plainledger.paper'{'简账-paper'};'cn.plainledger.sky'{'简账-sky'};'cn.plainledger.shared'{'简账-协作版'};default{'简账'}}
New-Item -ItemType Directory -Force "$appRoot/dist" | Out-Null
$output=Join-Path $appRoot "dist/$name-$version.apk"
Copy-Item -LiteralPath "$appRoot/build/outputs/apk/release/PlainLedger-release.apk" -Destination $output -Force
Write-Output "APK: $output"
