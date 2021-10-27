create database APPDB collate SQL_Latin1_General_CP1_CI_AS
go

use APPDB
go

grant connect on database :: APPDB to dbo
go

grant view any column encryption key definition, view any column master key definition on database :: APPDB to [public]
go

create table records
(
	record_id uniqueidentifier,
	datetime datetime,
	amount float,
	record_type tinyint,
    PRIMARY KEY (record_id)
)
go