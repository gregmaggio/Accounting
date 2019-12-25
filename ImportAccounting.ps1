$DataPath = 'C:\Data\DataMagic\Accounting'
$LoadedPath = [string]::Format('{0}\Loaded', $DataPath)
$DataDirectory = [System.IO.Directory]::CreateDirectory($DataPath)
$DataFiles = $DataDirectory.GetFiles('accounting.csv.*')
foreach ($DataFile in $DataFiles)
{
    $FileName = $DataFile.Name
    Write-Host "FileName: $FileName"
    $LoadedName = [string]::Format('{0}\{1}', $LoadedPath, $FileName)
    $FullName = $DataFile.FullName
    $CommandLine = [string]::Format('-cp C:\Users\Greg\.m2\repository\datamagic\accounting\1.0.0\accounting-1.0.0-jar-with-dependencies.jar ca.datamagic.accounting.importer.Importer --fileName {0}', $FullName)
    $Process = [System.Diagnostics.Process]::Start('java.exe', $CommandLine)
    $Process.WaitForExit()
    [System.IO.File]::Move($FullName, $LoadedName)
}
