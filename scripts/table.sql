create database platform default character set utf8mb4 collate utf8mb4_unicode_ci;

use platform;

drop table if exists info_group;
create table info_group (
  group_name   varchar(64)     not null comment '组名',
  system_id    int             not null comment '系统id',
  system_name  varchar(64)     not null comment '系统名',
  current_ip   varbinary(1024) not null comment '当前版本所有请求过的ip',
  version_info varchar(128)    not null comment '版本信息',
  created_at   timestamp       not null default current_timestamp comment '添加时间',
  updated_at   timestamp       not null default current_timestamp on update current_timestamp comment '更新时间',
  deleted_at   timestamp,
  primary key (group_name, system_id)
) engine = innodb default charset = utf8mb4 comment = '分组表';


drop table if exists info_clazz;
create table info_clazz (
  group_name      varchar(64)  not null comment '组名',
  full_name       varchar(128) not null comment '全名',
  system_id       int          not null comment '系统id',
  is_generic      boolean   default false comment '是否定义了泛型',
  is_interface    boolean   default false comment '是否是接口',
  is_primitive    boolean   default false comment '字段是否都收基本类',
  simple_name     varchar(32) comment '简名',
  simple_comment  varchar(128) comment '简单信息',
  comment_info    tinyblob comment '注释信息',
  field_info      blob comment '字段信息',
  generic_info    varbinary(512) comment '泛型信息',
  superclass_info varbinary(1024) comment '父类信息',
  import_info     varbinary(4096) comment '导入信息',
  version_info    varchar(128) not null comment '版本信息',
  created_at      timestamp default current_timestamp comment '添加时间',
  updated_at      timestamp default current_timestamp on update current_timestamp comment '更新时间',
  deleted_at      timestamp,
  primary key (group_name, full_name)
) engine = innodb default charset = utf8mb4 comment = '类型表';
create index idx_public_clazz_system_id
  on info_clazz (group_name, system_id);

drop table if exists info_method;
create table info_method (
  group_name       varchar(64)  not null comment '组名',
  clazz_name       varchar(128) not null comment '所属类名',
  path_name        varchar(64)  not null comment '路径名',
  invoke_name      varchar(64)  not null comment '调用名',
  invoke_length    int          not null comment '参数长度',
  system_id        int          not null comment '系统id',
  is_whitelist     boolean   default false comment '是否在白名单中',
  is_background    boolean   default false comment '是否在是后台接口',
  simple_name      varchar(32) comment '简单方法名',
  simple_parameter varbinary(2048) comment '简单参数',
  simple_comment   varchar(128) comment '简单信息',
  comment_info     tinyblob comment '注释信息',
  parameter_info   blob comment '输入信息',
  return_info      blob comment '返回信息',
  injection_info   blob comment '需要注入的信息',
  permission_info  varbinary(512) comment '权限信息',
  method_data      blob comment '缓存返回值',
  param_mock       text comment '参数mock信息',
  return_mock      text comment '返回mock信息',
  created_at       timestamp default current_timestamp comment '添加时间',
  updated_at       timestamp default current_timestamp on update current_timestamp comment '更新时间',
  deleted_at       timestamp,
  primary key (group_name, invoke_name, invoke_length)
) engine = innodb default charset = utf8mb4 comment = '方法表';
create index idx_public_method_clazz_name
  on info_method (group_name, clazz_name);


drop table if exists system_;
create table system_ (
  system_id     int             not null comment '系统id',
  system_secret varbinary(2048) not null comment '系统key',
  source_init   blob            not null comment '可用的资源',
  invoke_limit  int       default -1                      comment '接口调用限制(每天)，-1不限制',
  created_at    timestamp default current_timestamp comment '添加时间',
  updated_at    timestamp default current_timestamp on update CURRENT_TIMESTAMP comment '更新时间',
  deleted_at    timestamp,
  primary key (system_id)
) engine = innodb default charset = utf8mb4 comment = '系统表';
