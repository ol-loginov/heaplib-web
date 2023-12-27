create table Heap
(
    id     int      not null auto_increment primary key,
    fileId int      not null,
    tm     datetime not null
) engine InnoDb;


alter table Heap
    add foreign key Heap_HP (fileId) references HeapFile (id);


