drop table if exists post_images;
drop table if exists comments;
drop table if exists posts_tags_link;
drop table if exists posts;
drop table if exists tags;

create table if not exists tags(
    id bigserial primary key,
    tag varchar(20) unique not null,
    created_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC'
);

create table if not exists posts(
    id bigserial primary key,
    title varchar(100) not null,
    text varchar(5000) not null,
    likes_count integer not null default 0,
    created_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    modified_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC'
);

create table if not exists posts_tags_link(
    post_id bigint not null,
    tag_id bigint not null,
    primary key (post_id, tag_id),
    constraint fk_posts_tags_link_post foreign key (post_id) references posts(id) on delete cascade,
    constraint fk_posts_tags_link_tag foreign key (tag_id) references tags(id) on delete cascade
);

create table if not exists comments(
    id bigserial primary key,
    text varchar(2000) not null,
    post_id bigint not null,
    created_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    modified_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    constraint fk_comments_post foreign key (post_id) references posts(id) on delete cascade
);

create table if not exists post_images(
    post_id bigint primary key,
    origin_filename varchar(100) not null,
    storage_file_id varchar(36) not null,
    created_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    modified_at timestamp with time zone not null default CURRENT_TIMESTAMP AT TIME ZONE 'UTC',
    constraint fk_post_images_post foreign key (post_id) references posts(id) on delete cascade
);


insert into tags(tag)
values
    ('tag_1'),
    ('tag_2');

insert into posts(title, text)
values
    ('Название поста 1', 'Текст поста в формате Markdown...'),
    ('Название поста 2', 'Текст поста в формате Markdown...'),
    ('Название поста 3', 'Текст поста в формате Markdown...');

insert into posts_tags_link(post_id, tag_id)
values
    (1, 1),
    (1, 2),
    (3, 1),
    (3, 2);

insert into comments(text, post_id)
values
    ('Комментарий к посту 1', 1),
    ('Ещё один комментарий к посту 1', 1);