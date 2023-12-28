create table Heap
(
    id          int         not null auto_increment primary key,
    fileId      int         not null,
    tm          datetime    not null,
    tablePrefix varchar(32) not null,
    unique index Heap_UI_TP (tablePrefix)
) engine InnoDb;

alter table Heap
    add foreign key Heap_HP (fileId) references HeapFile (id);
