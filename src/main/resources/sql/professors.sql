create table if not exists professors
(
    id           serial not null
        constraint professors_pkey
            primary key,
    first_name   text,
    last_name    text,
    full_name    text,
    email_prefix text,
    school_id    integer
        constraint school_id
            references schools
);

