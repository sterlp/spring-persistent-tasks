mvn versions:display-dependency-updates
mvn versions:set -DnewVersion=1.6.0 -DgenerateBackupPoms=false
git tag -a v1.5.3 -m "v1.5.3 release"
mvn versions:set -DnewVersion=1.6.0-SNAPSHOT -DgenerateBackupPoms=false

## postgres

docker run -e POSTGRES_USER=sa -e POSTGRES_PASSWORD=veryStrong123 -p 5432:5432 -d postgres

## azure-sql-edge

docker run --cap-add SYS_PTRACE -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=veryStrong123' -p 1433:1433 --name azuresqledge -d mcr.microsoft.com/azure-sql-edge

## MariaDB

docker run -e MYSQL_ROOT_PASSWORD=veryStrong123 -e MYSQL_DATABASE=testdb -e MYSQL_USER=sa -e MYSQL_PASSWORD=veryStrong123 -p 3306:3306 -d mariadb:latest

## MySQL

docker run -e MYSQL_ROOT_PASSWORD=veryStrong123 -e MYSQL_DATABASE=testdb -e MYSQL_USER=sa -e MYSQL_PASSWORD=veryStrong123 -p 3306:3306 -d mysql
