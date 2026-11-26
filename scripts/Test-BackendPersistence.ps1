$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$evidence = Join-Path $root ('build/ev4-persistence-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $evidence | Out-Null
$env:DATA_DIR = (Join-Path $evidence 'data').Replace('\', '/')
$env:BIND_ADDRESS = '127.0.0.1'
$env:PROJECTS_URL = 'http://127.0.0.1:18081'
$projectUrl = "$env:PROJECTS_URL/api/v1/projects"
$taskUrl = 'http://127.0.0.1:18082/api/v1/projects/ev4-persistent/tasks'
$java = (Get-Command java.exe).Source
$processes = @()

function Start-ServiceJar([string]$Service, [int]$Port, [string]$Run) {
    $jar = Join-Path $root "backend/$Service/build/libs/$Service.jar"
    if (!(Test-Path -LiteralPath $jar)) { throw "Primero compila $jar" }
    $env:PORT = $Port.ToString()
    Start-Process -FilePath $java -ArgumentList '-jar', ('"' + $jar + '"') -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput (Join-Path $evidence "$Service-$Run.log") `
        -RedirectStandardError (Join-Path $evidence "$Service-$Run.err")
}

function Wait-Http([string]$Url) {
    for ($attempt = 0; $attempt -lt 60; $attempt++) {
        try {
            Invoke-RestMethod -Uri $Url -TimeoutSec 2 | Out-Null
            return
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    throw "El servicio no respondió en $Url"
}

try {
    $processes += Start-ServiceJar projects 18081 first
    $processes += Start-ServiceJar tasks 18082 first
    Wait-Http $projectUrl
    Wait-Http $taskUrl
    $project = @{ id = 'ev4-persistent'; name = 'Proyecto antes de reiniciar'; description = 'Evaluación'; client = 'DeepSource'; totalMinutes = 120; iconIndex = 3; isCompleted = $false }
    $task = @{ id = 'ev4-task'; projectId = 'ev4-persistent'; name = 'Tarea antes de reiniciar'; description = ''; estimatedMinutes = 60 }
    Invoke-RestMethod $projectUrl -Method Post -ContentType 'application/json' -Body ($project | ConvertTo-Json) | Out-Null
    Invoke-RestMethod $taskUrl -Method Post -ContentType 'application/json' -Body ($task | ConvertTo-Json) | Out-Null
    foreach ($process in $processes) { Stop-Process -InputObject $process; $process.WaitForExit() }
    $processes = @()
    $processes += Start-ServiceJar projects 18081 second
    $processes += Start-ServiceJar tasks 18082 second
    Wait-Http $projectUrl
    Wait-Http $taskUrl
    $savedProject = Invoke-RestMethod "$projectUrl/ev4-persistent"
    $savedTask = Invoke-RestMethod "$taskUrl/ev4-task"
    if ($savedProject.name -ne $project.name -or $savedTask.name -ne $task.name) {
        throw 'Los archivos H2 no conservaron los registros.'
    }
    $result = [pscustomobject]@{ project = $savedProject; task = $savedTask; persistedAfterRestart = $true }
    $result | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $evidence 'result.json')
    $result | ConvertTo-Json -Depth 4
    Write-Host "Evidencia: $evidence"
} finally {
    foreach ($process in $processes) {
        if (!$process.HasExited) { Stop-Process -InputObject $process }
    }
}
