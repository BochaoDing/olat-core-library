create table o_cer_template (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_name varchar(256) not null,
   c_path varchar(1024) not null,
   c_public bool not null,
   c_format varchar(16),
   c_orientation varchar(16),
   primary key (id)
);

create table o_cer_certificate (
   id int8 not null,
   creationdate timestamp not null,
   lastmodified timestamp not null,
   c_status varchar(16) not null default 'pending',
   c_email_status varchar(16),
   c_uuid varchar(36) not null,
   c_path varchar(1024),
   c_last bool not null default true,
   c_course_title varchar(255),
   c_archived_resource_id int8 not null,
   fk_olatresource int8,
   fk_identity int8 not null,
   primary key (id)
);

alter table o_cer_certificate add constraint cer_to_identity_idx foreign key (fk_identity) references o_bs_identity (id);
create index cer_identity_idx on o_cer_certificate (fk_identity);
alter table o_cer_certificate add constraint cer_to_resource_idx foreign key (fk_olatresource) references o_olatresource (resource_id);
create index cer_resource_idx on o_cer_certificate (fk_olatresource);
create index cer_archived_resource_idx on o_cer_certificate (c_archived_resource_id);
create index cer_uuid_idx on o_cer_certificate (c_uuid);


alter table o_gp_business add column allowtoleave bool not null default true;


drop view o_qp_item_shared_v;
drop view o_qp_item_pool_v;
drop view o_qp_item_author_v;
drop view o_qp_item_v;

drop view o_gp_member_v;