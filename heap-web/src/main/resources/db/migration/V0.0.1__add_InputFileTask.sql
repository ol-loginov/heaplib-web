create table InputFileLoad
(
    id           int            not null auto_increment primary key,
    relativePath nvarchar(1024) not null,
    status       varchar(24)    not null,
    loadStart    datetime       not null,
    loadFinish   datetime,
    loadProgress float,
    loadError    text
) engine InnoDB;

create table InputFileLoadEvent
(
    id              int      not null auto_increment primary key,
    tm              datetime not null,
    inputFileLoadId int      not null,
    progress        float    not null,
    message         text     not null
) engine InnoDB;

alter table InputFileLoadEvent
    Add foreign key InputFileLoadEvent_IFL (inputFileLoadId) references InputFileLoad (id);