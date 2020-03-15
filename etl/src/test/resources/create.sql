CREATE SCHEMA if not exists meetup;
CREATE TABLE if not exists meetup.meetup_by_event_id
(
    event_id      nvarchar(256) PRIMARY KEY ,
    group_country nvarchar(256),
    group_city    nvarchar(256),
    group_id      nvarchar(256),
    group_name    nvarchar(256),
    group_lon     float,
    group_lat     float,
    event_name    nvarchar(256),
    yes           int,
    no            int
);

CREATE TABLE if not exists meetup.trending
(
    country  nvarchar(256),
    topic_name nvarchar(256),
    count    int
);