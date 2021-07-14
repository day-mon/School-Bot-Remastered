create table if not exists class_reminders
(
    id          bigserial    not null
        constraint class_reminder_pkey
            primary key,
    class_id    integer   not null
        constraint "class_Id"
            references classes,
    remind_time timestamp not null
);

