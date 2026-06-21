param(
    [string]$Database = "pokefichas",
    [string]$User = "postgres",
    [string]$Password = "2011",
    [string]$HostName = "localhost",
    [int]$Port = 5432,
    [string]$JdbcJar = "$env:USERPROFILE\.m2\repository\org\postgresql\postgresql\42.7.4\postgresql-42.7.4.jar"
)

$ErrorActionPreference = "Stop"

if ($Database -notmatch '^[A-Za-z0-9_]+$') {
    throw "Nome do banco invalido: $Database"
}

if (-not (Test-Path $JdbcJar)) {
    throw "Driver JDBC nao encontrado em: $JdbcJar. Rode mvn install primeiro ou informe -JdbcJar."
}

$javaCommand = Get-Command java -ErrorAction SilentlyContinue
$javacCommand = Get-Command javac -ErrorAction SilentlyContinue

if (-not $javaCommand -or -not $javacCommand) {
    $javaHomeCandidate = "F:\Java\bin"
    if (Test-Path "$javaHomeCandidate\java.exe" -and Test-Path "$javaHomeCandidate\javac.exe") {
        $java = "$javaHomeCandidate\java.exe"
        $javac = "$javaHomeCandidate\javac.exe"
    } else {
        throw "Java/Javac nao encontrados. Adicione o JDK ao PATH ou ajuste o script."
    }
} else {
    $java = $javaCommand.Source
    $javac = $javacCommand.Source
}

$workDir = Join-Path $env:TEMP ("pokefichas-db-reset-" + [guid]::NewGuid())
New-Item -ItemType Directory -Path $workDir | Out-Null

$sourcePath = Join-Path $workDir "ResetLocalDb.java"

@'
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class ResetLocalDb {
    public static void main(String[] args) throws Exception {
        String host = args[0];
        String port = args[1];
        String user = args[2];
        String password = args[3];
        String database = args[4];

        String url = "jdbc:postgresql://" + host + ":" + port + "/postgres";

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            connection.setAutoCommit(true);

            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ? AND pid <> pg_backend_pid()")) {
                statement.setString(1, database);
                statement.execute();
            }

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP DATABASE IF EXISTS \"" + database + "\"");
                statement.executeUpdate("CREATE DATABASE \"" + database + "\"");
            }
        }

        System.out.println("Banco '" + database + "' recriado com sucesso.");
    }
}
'@ | Set-Content -Path $sourcePath -Encoding ASCII

try {
    & $javac -cp $JdbcJar $sourcePath
    & $java -cp "$JdbcJar;$workDir" ResetLocalDb $HostName $Port $User $Password $Database
}
finally {
    Remove-Item -LiteralPath $workDir -Recurse -Force -ErrorAction SilentlyContinue
}
