create table if not exists gx_system_config_parameter (

	name_ varchar(50) not null,
	
	value varchar(100) not null,

	constraint "name_fk" primary key (name_)
);