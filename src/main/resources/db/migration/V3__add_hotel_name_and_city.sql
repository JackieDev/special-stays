DROP TABLE special_deals;

CREATE TABLE IF NOT EXISTS special_deals(
  special_id VARCHAR NOT NULL,
  description VARCHAR NOT NULL,
  hotel_name VARCHAR NOT NULL,
  city VARCHAR NOT NULL,
  total_nights INT,
  discount_percentage_off DECIMAL(3,2) NOT NULL,
  available_from TIMESTAMP WITH TIME ZONE,
  available_to TIMESTAMP WITH TIME ZONE,
  added_on TIMESTAMP WITH TIME ZONE DEFAULT now(),
  PRIMARY KEY (special_id)
);