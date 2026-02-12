CREATE TABLE estimations (
                             id SERIAL PRIMARY KEY,
                             item_name VARCHAR(255) NOT NULL,
                             category VARCHAR(100),
                             brand VARCHAR(100),
                             year INT NOT NULL,
                             condition_rating INT CHECK (condition_rating BETWEEN 1 AND 10),
                             estimated_price NUMERIC(10,2),
                             ai_description TEXT,
                             created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_estimations_item_name ON estimations(item_name);
CREATE INDEX idx_estimations_created_at ON estimations(created_at);
