INSERT INTO waka_craft_time (unique_id, player_name, measure_time) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE measure_time = ?;
