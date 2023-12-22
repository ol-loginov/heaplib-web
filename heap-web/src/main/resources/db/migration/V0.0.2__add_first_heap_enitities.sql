create table Heap
(
    id     int      not null auto_increment primary key,
    fileId int      not null,
    tm     datetime not null
) engine InnoDb;


alter table Heap
    add foreign key Heap_HP (fileId) references HeapFile (id);

create table JavaClass
(
    id                  int    not null auto_increment primary key,
    heapId              int    not null,
    name                text   not null,
    allInstancesSize    long   not null,
    array               bit(1) not null,
    instanceSize        int    not null,
    instancesCount      int    not null,
    retainedSizeByClass long   not null,
    javaClassId         long   not null,
    superClassId        int
) engine InnoDB;

alter table JavaClass
    add foreign key JavaClass_H (heapId) references Heap (id);
alter table JavaClass
    add foreign key JavaClass_SC (superClassId) references JavaClass (id);
