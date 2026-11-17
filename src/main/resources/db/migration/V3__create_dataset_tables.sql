CREATE TABLE input_dataset (
                               id BIGSERIAL PRIMARY KEY,
                               admin_id BIGINT NOT NULL,
                               dataset_name VARCHAR(150) NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_dataset_admin
                                   FOREIGN KEY (admin_id)
                                       REFERENCES admin_user(id)
);

CREATE INDEX idx_dataset_admin_id
    ON input_dataset(admin_id);


CREATE TABLE lane (
                      id BIGSERIAL PRIMARY KEY,
                      dataset_id BIGINT NOT NULL,
                      lane_code VARCHAR(100) NOT NULL,
                      origin VARCHAR(150) NOT NULL,
                      destination VARCHAR(150) NOT NULL,

                      CONSTRAINT fk_lane_dataset
                          FOREIGN KEY (dataset_id)
                              REFERENCES input_dataset(id),

                      CONSTRAINT uk_lane_dataset_code
                          UNIQUE (dataset_id, lane_code)
);


CREATE TABLE transporter (
                             id BIGSERIAL PRIMARY KEY,
                             dataset_id BIGINT NOT NULL,
                             transporter_code VARCHAR(100) NOT NULL,
                             transporter_name VARCHAR(150) NOT NULL,

                             CONSTRAINT fk_transporter_dataset
                                 FOREIGN KEY (dataset_id)
                                     REFERENCES input_dataset(id),

                             CONSTRAINT uk_transporter_dataset_code
                                 UNIQUE (dataset_id, transporter_code)
);


CREATE TABLE lane_quote (
                            id BIGSERIAL PRIMARY KEY,
                            lane_id BIGINT NOT NULL,
                            transporter_id BIGINT NOT NULL,
                            quote_amount NUMERIC(14, 2) NOT NULL,

                            CONSTRAINT fk_quote_lane
                                FOREIGN KEY (lane_id)
                                    REFERENCES lane(id),

                            CONSTRAINT fk_quote_transporter
                                FOREIGN KEY (transporter_id)
                                    REFERENCES transporter(id),

                            CONSTRAINT uk_lane_transporter_quote
                                UNIQUE (lane_id, transporter_id),

                            CONSTRAINT chk_quote_amount
                                CHECK (quote_amount >= 0)
);

CREATE INDEX idx_lane_dataset_id ON lane(dataset_id);
CREATE INDEX idx_transporter_dataset_id ON transporter(dataset_id);
CREATE INDEX idx_lane_quote_lane_id ON lane_quote(lane_id);
CREATE INDEX idx_lane_quote_transporter_id ON lane_quote(transporter_id);