create table if not exists schools
(
    id bigserial not null
        constraint schools_pkey
            primary key,
    name           text    not null,
    role_id        bigint,
    guild_id       bigint  not null,
    is_pitt_campus boolean not null,
    url            text    not null,
    email_suffix   text
);

