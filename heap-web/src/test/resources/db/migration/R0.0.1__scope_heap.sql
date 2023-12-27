drop table if exists FieldValue;
drop table if exists Field;
drop table if exists Instance;
drop table if exists JavaClass;
drop table if exists Type;

create table JavaClass
(
    javaClassId         bigint       not null,
    name                varchar(512) not null,
    allInstancesSize    bigint       not null,
    array               bit(1)       not null,
    instanceSize        int          not null,
    instancesCount      int          not null,
    retainedSizeByClass bigint       not null,
    superClassId        bigint,
    primary key JavaClass_PK (javaClassId),
    unique JavaClass_IX_HIN (name)
) engine InnoDB;

create table Field
(
    id               int          not null auto_increment primary key,
    declaringClassId bigint       not null,
    name             varchar(256) not null,
    staticFlag       bit(1)       not null,
    typeId           int          not null
) engine InnoDB;

alter table Field
    add foreign key Field_DC (declaringClassId) references JavaClass (javaClassId);

create table Type
(
    id   int          not null auto_increment primary key,
    name varchar(512) not null,
    unique JavaClass_IX_HIN (name)
) engine InnoDB;

create table Instance
(
    instanceId     bigint not null primary key,
    instanceNumber int    not null,
    javaClassId    bigint not null,
    gcRoot         bit(1) not null,
    size           bigint not null,
    retainedSize   bigint not null,
    reachableSize  bigint not null
) engine InnoDB;

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