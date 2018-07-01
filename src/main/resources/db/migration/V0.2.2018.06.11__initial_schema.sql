-- tables
CREATE TABLE comment_thread (
  id    BIGSERIAL PRIMARY KEY,
  url   VARCHAR(256) UNIQUE NOT NULL,
  title VARCHAR(512)        NULL
);

CREATE TABLE comment (
  id                     BIGSERIAL PRIMARY KEY,
  thread_id              BIGINT       NOT NULL,
  parent_id              BIGINT       NULL,
  creation_date          TIMESTAMP    NOT NULL,
  last_modification_date TIMESTAMP    NOT NULL,
  status                 VARCHAR(32)  NOT NULL,
  text_source            TEXT         NOT NULL,
  text_html              TEXT         NOT NULL,
  author                 VARCHAR(128) NOT NULL,
  url                    VARCHAR(256) NULL,
  author_hash            BYTEA        NOT NULL,
  FOREIGN KEY (thread_id) REFERENCES comment_thread (id),
  FOREIGN KEY (parent_id) REFERENCES comment (id)
);

CREATE TABLE property (
  key   VARCHAR(32) PRIMARY KEY,
  value VARCHAR(256) NOT NULL
);

-- indexes
CREATE INDEX idx__comment__thread_id
  ON comment (thread_id);
CREATE INDEX idx__comment__creation_date
  ON comment (creation_date);
CREATE INDEX idx__comment__status
  ON comment (status);

