create table users
(
    id         uuid primary key      default gen_random_uuid(),
    username   varchar(30)  not null unique,
    email      varchar(254) not null unique,
    passwd     varchar(60)  not null,
    created_at timestamptz  not null default timezone('UTC', now()),
    updated_at timestamptz  not null default timezone('UTC', now())
);
