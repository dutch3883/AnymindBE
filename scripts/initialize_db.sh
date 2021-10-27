docker exec btcbillionaire_mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P P@ssw0rd -Q "$(cat ./SQL/create_db.sql)"
