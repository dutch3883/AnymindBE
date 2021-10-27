create database APPDB collate SQL_Latin1_General_CP1_CI_AS
go

use APPDB
go

grant connect on database :: APPDB to dbo
go

grant view any column encryption key definition, view any column master key definition on database :: APPDB to [public]
go

use APPDB
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

insert into records (record_id, datetime, amount, record_type) values ('974461fa-3676-11ec-8d3d-0242ac130003', '2021-10-26 16:06:59', 10, 1);

go