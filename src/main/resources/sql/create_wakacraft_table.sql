CREATE TABLE IF NOT EXISTS waka_craft_time(
    unique_id CHAR(36) NOT NULL,
    player_name VARCHAR(16),
    measure_time BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (unique_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
