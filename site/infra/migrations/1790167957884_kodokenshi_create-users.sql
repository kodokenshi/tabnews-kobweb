create table users
(
    id         uuid primary key default gen_random_uuid(),
    username   varchar(30)  not null unique,
    email      varchar(254) not null unique,
    passwd     varchar(72)  not null,
    created_at timestamptz      default now(),
    updated_at timestamptz      default now()
);
