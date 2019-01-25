ALTER TABLE [dbo].[DataCenterConfig]
	ADD [id] int IDENTITY NOT NULL
GO
ALTER TABLE [dbo].[DataCenterConfig]
	ADD CONSTRAINT [id]
	PRIMARY KEY CLUSTERED ([id])
GO
