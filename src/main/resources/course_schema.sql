-- MySQL 8.x 기준 course / course_item 테이블

CREATE TABLE course (
                        course_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
                        user_id       BIGINT NOT NULL,
                        course_name   VARCHAR(100) NOT NULL,
                        course_type   ENUM('USER', 'AI') NOT NULL,
                        people_count  INT NOT NULL,
                        with_child    BOOLEAN NOT NULL,
                        start_date    DATE NOT NULL,
                        end_date      DATE NOT NULL,
                        created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        deleted_at    DATETIME NULL,
                        CONSTRAINT fk_course_user FOREIGN KEY (user_id) REFERENCES user(user_id)
) ENGINE=InnoDB;

CREATE TABLE course_item (
                             course_item_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
                             course_id       BIGINT NOT NULL,
                             activity_id     BIGINT NULL,
                             reservation_id  BIGINT NULL,
                             title           VARCHAR(100) NOT NULL,
                             day_no          INT NOT NULL,
                             start_time      TIME NOT NULL,
                             end_time        TIME NOT NULL,
                             memo            TEXT NULL,
                             sort_order      INT NOT NULL,
                             created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             CONSTRAINT fk_course_item_course FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE,
                             CONSTRAINT fk_course_item_activity FOREIGN KEY (activity_id) REFERENCES activity(activity_id),
                             CONSTRAINT fk_course_item_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id),
                             INDEX idx_course_item_course_day (course_id, day_no)
) ENGINE=InnoDB;