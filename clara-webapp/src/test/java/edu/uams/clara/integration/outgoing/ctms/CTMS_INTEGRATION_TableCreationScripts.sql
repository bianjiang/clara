USE [ctms_integration]
GO
/****** Object:  Table [dbo].[clara_protocol]    Script Date: 05/13/2013 23:18:30 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[clara_protocol](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[irb_number] [varchar](50) NOT NULL,
	[irb_approval_date] [varchar](50) NULL,
	[title] [varchar](8000) NULL,
	[phases] [varchar](255) NULL,
	[local_accrual_gaol] [varchar](50) NULL,
	[irb_status] [varchar](50) NULL,
 CONSTRAINT [PK_clara_protocol] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF

USE [ctms_integration]
GO
/****** Object:  Table [dbo].[clara_protocoldrug]    Script Date: 05/13/2013 23:19:44 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[clara_protocoldrug](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[clara_protocol_id] [bigint] NOT NULL,
	[drug_name] [varchar](1000) NOT NULL,
	[drug_id] [varchar](255) NULL,
 CONSTRAINT [PK_clara_protocoldrug] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF

USE [ctms_integration]
GO
/****** Object:  Table [dbo].[clara_protocoluser]    Script Date: 05/13/2013 23:20:00 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[clara_protocoluser](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[clara_user_id] [bigint] NOT NULL,
	[clara_protocol_id] [bigint] NOT NULL,
	[clara_protocoluser_role] [varchar](50) NULL,
 CONSTRAINT [PK_clara_protocoluser] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF


USE [ctms_integration]
GO
/****** Object:  Table [dbo].[clara_user]    Script Date: 05/13/2013 23:20:12 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[clara_user](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[user_type] [varchar](50) NULL,
	[user_id] [bigint] NOT NULL,
	[username] [varchar](255) NOT NULL,
	[first_name] [varchar](255) NULL,
	[last_name] [varchar](255) NULL,
	[middle_name] [varchar](255) NULL,
	[phone] [varchar](50) NULL,
	[email] [varchar](100) NULL,
 CONSTRAINT [PK_clara_users] PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX  = OFF, STATISTICS_NORECOMPUTE  = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS  = ON, ALLOW_PAGE_LOCKS  = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF