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
    heapId              int          not null,
    javaClassId         bigint       not null,
    name                varchar(512) not null,
    allInstancesSize    bigint       not null,
    array               bit(1)       not null,
    instanceSize        int          not null,
    instancesCount      int          not null,
    retainedSizeByClass bigint       not null,
    superClassId        bigint,
    primary key JavaClass_PK (heapId, javaClassId),
    unique JavaClass_IX_HIN (heapId, name)
) engine InnoDB;

alter table JavaClass
    add foreign key JavaClass_H (heapId) references Heap (id);

create table Field
(
    id               int          not null auto_increment primary key,
    heapId           int          not null,
    declaringClassId bigint       not null,
    name             varchar(256) not null,
    staticFlag       bit(1)       not null,
    typeId           int          not null
) engine InnoDB;

alter table Field
    add foreign key Field_DC (heapId, declaringClassId) references JavaClass (heapId, javaClassId);

create table Type
(
    id     int          not null auto_increment primary key,
    heapId int          not null,
    name   varchar(512) not null,
    unique JavaClass_IX_HIN (heapId, name)
) engine InnoDB;

alter table Type
    add foreign key Type_H (heapId) references Heap (id);

create table Instance
(
    heapId         int    not null,
    instanceId     bigint not null,
    instanceNumber int    not null,
    javaClassId    bigint not null,
    gcRoot         bit(1) not null,
    size           bigint not null,
    retainedSize   bigint not null,
    reachableSize  bigint not null,
    primary key Instance_PK (heapId, instanceId)
) engine InnoDB;

alter table Instance
    add foreign key Instance_H (heapId) references Heap (id);

create table FieldValue
(
    javaClassId        bigint not null,
    definingInstanceId bigint not null,
    fieldId            int    not null,
    staticFlag         bit(1) not null,
    value              text,
    valueInstanceId    bigint,
    primary key FieldValue_PK (javaClassId, definingInstanceId, fieldId)
) engine InnoDB;

create index FieldValue_IX_DII on FieldValue (definingInstanceId);

