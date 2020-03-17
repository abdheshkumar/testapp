CREATE TABLE meetup.trending_topic
(
    country    text,
    topic_name text,
    count      int,
    PRIMARY KEY ( country, topic_name )
);

select * from meetup.trending_topic;
drop table meetup.trending_topic;

select * from meetup.trending;
select * from meetup.meetup_by_event_id order by yes desc ;
select * from meetup.trending_topic;
select * from meetup.trending_events order by yes desc ;
select * from meetup.trending_events where event_id='269322143';
select * from meetup.trending_topic where country='gb' order by count desc;
CREATE TABLE meetup.trending_topic
(
    country    text,
    topic_name text,
    count      int,
    PRIMARY KEY ( country, topic_name )
);

drop table meetup.customers;
CREATE TABLE meetup.customers (
                                  customer_id text,
                                  name VARCHAR, str text, PRIMARY KEY (customer_id,name)
);

INSERT INTO meetup.customers  (customer_id, name, str)
VALUES
(
    'Microsoft',
    'hotline@microsoft.com2',
    'str3'
)
    ON CONFLICT (customer_id, name)
    DO
UPDATE
SET str = EXCLUDED.str || ';' || customers.str;

select * from meetup.customers;