# UI login

`docker run --cap-add SYS_PTRACE -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=veryStrong123' -p 1433:1433 --name azuresqledge -d mcr.microsoft.com/azure-sql-edge`

-   url: http://localhost:8080/task-ui
-   user: admin
-   password: admin


# Read metric by status

- http://localhost:8080/actuator/metrics/persistent_tasks.task.failingBuildVehicleTask?tag=status:FAILED
