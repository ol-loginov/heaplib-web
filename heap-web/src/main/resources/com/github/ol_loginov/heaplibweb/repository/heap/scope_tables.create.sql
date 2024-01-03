create table Name
(
    id   int          not null auto_increment primary key,
    name varchar(128) not null collate utf8mb4_0900_as_cs,
    unique index Name_IX_N (name)
) engine InnoDB;

create table Class
(
    id                  bigint       not null primary key,
    classLoaderObjectId bigint,
    name                varchar(512) not null collate utf8mb4_0900_as_cs,
    allInstancesSize    bigint,
    array               bit(1),
    instanceSize        int,
    instancesCount      int          not null,
    retainedSizeByClass bigint,
    superClassId        bigint,
    index Class_IX_N (name)
) engine InnoDB;

create table Field
(
    id               int     not null auto_increment primary key,
    declaringClassId bigint  not null,
    nameId           int     not null,
    staticFlag       bit(1)  not null,
    typeTag          tinyint not null,
    foreign key Field_DC (declaringClassId) references Class (id),
    foreign key Field_N (nameId) references Name (id)
) engine InnoDB;

create table Instance
(
    instanceId     bigint    not null primary key,
    instanceNumber int       not null,
    javaClassId    bigint    not null,
    rootTag        mediumint not null,
    size           bigint    not null,
    arrayTypeTag   tinyint   not null,
    arrayLength    int       not null,
    retainedSize   bigint    null,
    reachableSize  bigint    null
) engine InnoDB;

create table FieldValue
(
    instanceId      bigint not null,
    fieldId         int    not null,
    value           varchar(32),
    valueInstanceId bigint,
    primary key FieldValue_PK (instanceId, fieldId),
    index FieldValue_IX_DII (instanceId)
) engine InnoDB;

create table PrimitiveArray
(
    instanceId bigint not null,
    itemIndex  int    not null,
    itemValue  text,
    primary key PrimitiveArray_PK (instanceId, itemIndex)
) engine InnoDB;

create table ObjectArray
(
    instanceId     bigint not null,
    itemIndex      int    not null,
    itemInstanceId bigint not null,
    primary key PrimitiveArray_PK (instanceId, itemIndex)
) engine InnoDB;
