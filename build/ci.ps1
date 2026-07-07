#!/usr/bin/env pwsh
<#
.SYNOPSIS
    Run the CI pipeline locally on Windows.
    Mirrors build/ci.sh so issues can be caught before pushing.
#>
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (Get-Command wsl -ErrorAction SilentlyContinue) {
    wsl bash ./build/ci.sh
    exit $LASTEXITCODE
}

Write-Host "WSL not found - running native PowerShell steps" -ForegroundColor Yellow

./gradlew.bat --no-daemon clean check buildPlugin
