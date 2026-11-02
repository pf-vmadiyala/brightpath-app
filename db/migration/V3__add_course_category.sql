-- Expand step: additive and nullable-with-default, so the previous app version
-- (which never reads or writes this column) keeps working during a rolling deploy.
ALTER TABLE courses ADD COLUMN category VARCHAR(50) NOT NULL DEFAULT 'general';
