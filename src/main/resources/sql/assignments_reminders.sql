create table if not exists assignments_reminders
(
    id              bigserial    not null
        constraint assignments_reminders_pkey
            primary key,
    "assignment_Id" integer   not null
        constraint assignment_id
            references assignments,
    remind_time     timestamp not null
);

