create table if not exists guild_settings
(
    guild_id bigint NOT NULL,
    prefix   text,
    PRIMARY KEY (guild_id)
);
