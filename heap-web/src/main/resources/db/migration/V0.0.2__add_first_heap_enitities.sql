create table Heap
(
    id         int not null auto_increment primary key,
    fileId int not null
) engine InnoDb;


alter table Heap
    Add foreign key Heap_HP (fileId) references HeapFile (id);