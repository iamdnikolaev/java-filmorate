CREATE TABLE "users" (
  "user_id" bigint PRIMARY KEY,
  "email" varchar(255) NOT NULL,
  "login" varchar(255) UNIQUE NOT NULL,
  "name" varchar(255),
  "birthday" date,
  "last_update" timestamp,
  PRIMARY KEY ("user_id")
);

CREATE TABLE "friends" (
  "user_id" bigint NOT NULL,
  "friend_id" bigint NOT NULL,
  "last_udpate" timestamp,
  PRIMARY KEY ("user_id", "friend_id")
);

CREATE TABLE "films" (
  "film_id" bigint PRIMARY KEY,
  "name" varchar(255) NOT NULL,
  "description" varchar(200),
  "release_date" date NOT NULL,
  "duration" integer NOT NULL,
  "rating_id" bigint,
  "last_update" timestamp,
  PRIMARY KEY ("film_id")
);

CREATE TABLE "genres" (
  "genre_id" bigint PRIMARY KEY,
  "name" varchar(255) NOT NULL,
  "last_update" timestamp,
  PRIMARY KEY ("genre_id")
);

CREATE TABLE "film_genres" (
  "film_id" bigint NOT NULL,
  "genre_id" bigint NOT NULL,
  "last_update" timestamp,
  PRIMARY KEY ("film_id", "genre_id")
);

CREATE TABLE "ratings" (
  "rating_id" bigint PRIMARY KEY,
  "name" varchar(255) NOT NULL,
  "last_update" timestamp,
  PRIMARY KEY ("rating_id")
);

CREATE TABLE "film_likes" (
  "film_id" bigint NOT NULL,
  "user_id" bigint NOT NULL,
  "last_update" timestamp,
  PRIMARY KEY ("film_id", "user_id")
);

CREATE UNIQUE INDEX "idx_by_login" ON "users" ("login");

CREATE INDEX "idx_by_friend" ON "friends" ("friend_id");

CREATE INDEX "idx_by_release_date" ON "films" ("release_date");

CREATE INDEX "idx_by_rating" ON "films" ("rating_id");

CREATE INDEX "idx_by_genre" ON "film_genres" ("genre_id");

CREATE INDEX "idx_by_user" ON "film_likes" ("user_id");

ALTER TABLE "film_likes" ADD FOREIGN KEY ("film_id") REFERENCES "films" ("film_id");

ALTER TABLE "film_likes" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "film_genres" ADD FOREIGN KEY ("film_id") REFERENCES "films" ("film_id");

ALTER TABLE "films" ADD FOREIGN KEY ("rating_id") REFERENCES "ratings" ("rating_id");

ALTER TABLE "film_genres" ADD FOREIGN KEY ("genre_id") REFERENCES "genres" ("genre_id");

ALTER TABLE "friends" ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "friends" ADD FOREIGN KEY ("friend_id") REFERENCES "users" ("user_id");
