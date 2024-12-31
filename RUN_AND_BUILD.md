mvn versions:display-dependency-updates
mvn versions:set -DnewVersion=1.2.0 -DgenerateBackupPoms=false

docker run -e "ACCEPT_EULA=Y" -e "MSSQL_SA_PASSWORD=veryStrong123" -p 1433:1433 -d mcr.microsoft.com/mssql/server:2022-latest

docker run --cap-add SYS_PTRACE -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=veryStrong123' -p 1433:1433 --name azuresqledge -d mcr.microsoft.com/azure-sql-edge
