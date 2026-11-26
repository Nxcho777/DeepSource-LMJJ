param([string]$Report)
$ErrorActionPreference = 'Stop'
if (!$Report) {
    $root = Split-Path -Parent $PSScriptRoot
    $Report = Get-ChildItem (Join-Path $root 'app/build/reports') -Recurse -File -Filter '*.xml' |
        Where-Object { $_.FullName -match '[\\/]coverage[\\/]' } |
        Sort-Object LastWriteTime -Descending | Select-Object -First 1 -ExpandProperty FullName
}
if (!$Report) { throw 'Ejecuta :app:ev4UnitCoverage e indica la ruta XML si es necesario.' }

[xml]$coverage = Get-Content -LiteralPath $Report -Raw
function Get-LineRows($nodes) {
    foreach ($node in $nodes) {
        $lines = $node.counter | Where-Object { $_.type -eq 'LINE' }
        if ($lines) {
            $covered = [int]$lines.covered
            $total = $covered + [int]$lines.missed
            [pscustomobject]@{ Package = $node.name; Covered = $covered; Total = $total; Percent = [math]::Round(100 * $covered / $total, 2) }
        }
    }
}
$packages = @(Get-LineRows $coverage.report.package)
$packages | Format-Table -AutoSize
$logicalPrefix = '^cl/duoc/deepsource(?:$|/ui$|/(data|domain|ui/viewmodel|ui/animation|navigation|notification|widget)(/|$))'
$logicalPackages = $packages | Where-Object { $_.Package -match $logicalPrefix }
$authoredLogicalSources = $coverage.report.package | Where-Object { $_.name -match $logicalPrefix } |
    ForEach-Object { $_.sourcefile } | Where-Object {
        $_.name -notin @('DeepSourceDao_Impl.java', 'DeepSourceDatabase_Impl.java')
    }
foreach ($summary in @(
    @{ Name = 'Global (incluye Compose)'; Rows = $packages },
    @{ Name = 'Paquetes de lógica Android (incluye Room generado)'; Rows = $logicalPackages },
    @{ Name = 'Fuentes de lógica escritas en el proyecto'; Rows = @(Get-LineRows $authoredLogicalSources) }
)) {
    $covered = ($summary.Rows | Measure-Object -Property Covered -Sum).Sum
    $total = ($summary.Rows | Measure-Object -Property Total -Sum).Sum
    if ($total) { '{0}: {1}/{2} líneas ({3:P2})' -f $summary.Name, $covered, $total, ($covered / $total) }
}
"XML: $Report"
