create table if not exists assignments
(
    id              bigserialserial    not null
        constraint assignments_pkey
            primary key,
    name            text      not null,
    due_date        timestamp not null,
    type            text      not null,
    professor_id    integer   not null
        constraint professor_id
            references professors,
    points_possible integer   not null,
    description     text      not null,
    class_id        integer   not null
        constraint class_id
            references classes
);

