# Installe Java, Maven et Git si besoin, recupere le jeu My Little Pony Maze et le lance.
#
# Utilisation (dans PowerShell) :
#   irm https://raw.githubusercontent.com/Calicles/my_little_pony_maze_2/master/installer-et-lancer.ps1 | iex
#
# Le script est enveloppe dans un bloc pour que ses variables et reglages
# ne restent pas dans la session PowerShell apres son execution.

& {
    $ErrorActionPreference = 'Stop'

    $MavenVersion = '3.9.11'
    $MavenUrl     = "https://archive.apache.org/dist/maven/maven-3/$MavenVersion/binaries/apache-maven-$MavenVersion-bin.zip"
    $MavenDir     = Join-Path $env:LOCALAPPDATA "Programs\apache-maven-$MavenVersion"
    $RepoUrl      = 'https://github.com/Calicles/my_little_pony_maze_2.git'
    $ProjectDir   = Join-Path $HOME 'my_little_pony_maze_2'

    function Write-Step($message) { Write-Host "`n==> $message" -ForegroundColor Cyan }

    # Recharge le PATH apres une installation, sans devoir rouvrir le terminal.
    function Update-SessionPath {
        $machine = [Environment]::GetEnvironmentVariable('Path', 'Machine')
        $user    = [Environment]::GetEnvironmentVariable('Path', 'User')
        $env:Path = "$machine;$user"
    }

    function Test-Command($name) { [bool](Get-Command $name -ErrorAction SilentlyContinue) }

    # Renvoie la version majeure de Java (8, 11, 17, 21...) ou 0 si Java est absent.
    function Get-JavaMajorVersion {
        if (-not (Test-Command 'java')) { return 0 }
        $output = (& cmd /c 'java -version 2>&1') -join ' '
        if ($output -match 'version "1\.(\d+)') { return [int]$Matches[1] }
        if ($output -match 'version "(\d+)')    { return [int]$Matches[1] }
        return 0
    }

    function Assert-Winget {
        if (-not (Test-Command 'winget')) {
            throw "winget est introuvable. Installe 'App Installer' depuis le Microsoft Store, puis relance ce script."
        }
    }

    # --- Java ---------------------------------------------------------------------
    Write-Step 'Verification de Java'
    if ((Get-JavaMajorVersion) -ge 8) {
        Write-Host "Java $(Get-JavaMajorVersion) est deja installe."
    } else {
        Assert-Winget
        Write-Host 'Installation de Java 21 (Eclipse Temurin)...'
        winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements
        Update-SessionPath
        if ((Get-JavaMajorVersion) -lt 8) {
            throw "Java a ete installe mais n'est pas encore visible. Ferme ce terminal, rouvre-le et relance le script."
        }
    }

    # Maven utilise JAVA_HOME s'il est defini : on s'assure qu'il pointe vers un vrai JDK.
    if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
        $javaExe = (Get-Command java).Source
        $env:JAVA_HOME = Split-Path (Split-Path $javaExe -Parent) -Parent
    }

    # --- Maven --------------------------------------------------------------------
    Write-Step 'Verification de Maven'
    if (Test-Command 'mvn') {
        Write-Host 'Maven est deja installe.'
    } else {
        if (-not (Test-Path (Join-Path $MavenDir 'bin\mvn.cmd'))) {
            Write-Host "Telechargement de Maven $MavenVersion..."
            $zip = Join-Path $env:TEMP "apache-maven-$MavenVersion-bin.zip"
            Invoke-WebRequest -Uri $MavenUrl -OutFile $zip -UseBasicParsing
            Expand-Archive -Path $zip -DestinationPath (Split-Path $MavenDir -Parent) -Force
            Remove-Item $zip
        }
        $mavenBin = Join-Path $MavenDir 'bin'
        $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
        if (($userPath -split ';') -notcontains $mavenBin) {
            [Environment]::SetEnvironmentVariable('Path', "$userPath;$mavenBin", 'User')
        }
        Update-SessionPath
        Write-Host "Maven installe dans $MavenDir"
    }

    # --- Git ----------------------------------------------------------------------
    Write-Step 'Verification de Git'
    if (Test-Command 'git') {
        Write-Host 'Git est deja installe.'
    } else {
        Assert-Winget
        Write-Host 'Installation de Git...'
        winget install --id Git.Git -e --accept-source-agreements --accept-package-agreements
        Update-SessionPath
        if (-not (Test-Command 'git')) {
            throw "Git a ete installe mais n'est pas encore visible. Ferme ce terminal, rouvre-le et relance le script."
        }
    }

    # --- Projet -------------------------------------------------------------------
    Write-Step 'Recuperation du projet'
    if (Test-Path (Join-Path $ProjectDir '.git')) {
        git -C $ProjectDir checkout master
        git -C $ProjectDir pull
    } else {
        git clone $RepoUrl $ProjectDir
    }

    # --- Lancement ----------------------------------------------------------------
    Write-Step 'Compilation et lancement du jeu (le premier lancement telecharge les dependances Maven)'
    Set-Location $ProjectDir
    mvn -q compile exec:java
}
