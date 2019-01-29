CREATE TABLE [dbo].[AuditLog]  (
	[Id]      	int IDENTITY(1,1) NOT NULL,
	[DateTime]	datetime NOT NULL,
	[UserId]  	varchar(40) NOT NULL,
	[Activity] 	varchar(40) NOT NULL,
	[Details] 	varchar(4096) NULL
	)
GO
CREATE NONCLUSTERED INDEX [audit_date_user_activity_ndx]
	ON [dbo].[AuditLog]([DateTime], [UserId], [Activity])
GO
