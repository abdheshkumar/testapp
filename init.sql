CREATE SCHEMA if not exists meetup;

CREATE TABLE if not exists meetup.meetup_by_event_id
(
    event_id      text,
    group_country text,
    group_city    text,
    group_id      text,
    group_name    text,
    group_lon     float,
    group_lat     float,
    event_name    text,
    yes           int,
    no            int
);

CREATE TABLE meetup.trending
(
    country  text,
    topic_name   text,
    count    int
);

CREATE TABLE if not exists meetup.trending_events
(
    event_id      text PRIMARY KEY,
    group_country text,
    group_city    text,
    group_id      text,
    group_name    text,
    group_lon     float,
    group_lat     float,
    event_name    text,
    yes           int,
    no            int
);

CREATE TABLE meetup.trending_topic
(
    country    text,
    topic_name text,
    count      int,
    PRIMARY KEY ( country, topic_name )
);