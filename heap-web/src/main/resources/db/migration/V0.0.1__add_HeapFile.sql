create table HeapFile
(
    id           int            not null auto_increment primary key,
    relativePath nvarchar(1024) not null,
    status       varchar(24)    not null,
    loadStart    datetime       not null,
    loadFinish   datetime,
    loadProgress float,
    loadMessage  text           not null,
    loadError    text
) engine InnoDB;
