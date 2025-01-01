mvn versions:display-dependency-updates
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false
mvn versions:set -DnewVersion=1.2.1-SNAPSHOT -DgenerateBackupPoms=false

docker run --name pg-container -e POSTGRES_USER=sa -e POSTGRES_PASSWORD=veryStrong123 -p 5432:5432 -d postgres

docker run --cap-add SYS_PTRACE -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=veryStrong123' -p 1433:1433 --name azuresqledge -d mcr.microsoft.com/azure-sql-edge
