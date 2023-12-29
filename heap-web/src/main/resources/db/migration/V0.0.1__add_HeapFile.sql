create table HeapFile
(
    id           int            not null auto_increment primary key,
    path         nvarchar(1024) not null,
    status       varchar(24)    not null,
    loadStart    datetime       not null,
    loadFinish   datetime,
    loadProgress float,
    loadMessage  text           not null,
    loadError    text,
    tablePrefix  varchar(32)    not null,
    unique index Heap_UI_TP (tablePrefix)
) engine InnoDB;
