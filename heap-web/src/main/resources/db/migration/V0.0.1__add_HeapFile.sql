create table HeapFile
(
    id           int            not null auto_increment primary key,
    relativePath nvarchar(1024) not null,
    status       varchar(24)    not null,
    loadStart    datetime       not null,
    loadFinish   datetime,
    loadProgress float,
    loadMessage  text           not null,
    loadError    text,
) engine InnoDB;

create table HeapFileLoadEvent
(
    id         int      not null auto_increment primary key,
    tm         datetime not null,
    heapFileId int      not null,
    progress   float    not null,
    message    text     not null
) engine InnoDB;

alter table HeapFileLoadEvent
    Add foreign key HeapFileLoadEvent_HF (heapFileId) references HeapFile (id);