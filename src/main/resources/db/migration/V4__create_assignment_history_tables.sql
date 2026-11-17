CREATE TABLE assignment_run (
                                id BIGSERIAL PRIMARY KEY,
                                admin_id BIGINT NOT NULL,
                                dataset_id BIGINT NOT NULL,
                                max_transporters INTEGER NOT NULL,
                                selected_transporter_count INTEGER NOT NULL,
                                total_cost NUMERIC(18, 2) NOT NULL,
                                solver_status VARCHAR(30) NOT NULL,
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_assignment_admin
                                    FOREIGN KEY (admin_id)
                                        REFERENCES admin_user(id),

                                CONSTRAINT fk_assignment_dataset
                                    FOREIGN KEY (dataset_id)
                                        REFERENCES input_dataset(id),

                                CONSTRAINT chk_max_transporters
                                    CHECK (max_transporters >= 1),

                                CONSTRAINT chk_selected_transporter_count
                                    CHECK (selected_transporter_count >= 0),

                                CONSTRAINT chk_total_cost
                                    CHECK (total_cost >= 0)
);


CREATE TABLE assignment_result (
                                   id BIGSERIAL PRIMARY KEY,
                                   assignment_run_id BIGINT NOT NULL,
                                   lane_id BIGINT NOT NULL,
                                   transporter_id BIGINT NOT NULL,
                                   quote_amount NUMERIC(14, 2) NOT NULL,

                                   CONSTRAINT fk_result_assignment
                                       FOREIGN KEY (assignment_run_id)
                                           REFERENCES assignment_run(id),

                                   CONSTRAINT fk_result_lane
                                       FOREIGN KEY (lane_id)
                                           REFERENCES lane(id),

                                   CONSTRAINT fk_result_transporter
                                       FOREIGN KEY (transporter_id)
                                           REFERENCES transporter(id),

                                   CONSTRAINT uk_assignment_lane
                                       UNIQUE (assignment_run_id, lane_id),

                                   CONSTRAINT chk_result_quote
                                       CHECK (quote_amount >= 0)
);

CREATE INDEX idx_assignment_admin_id
    ON assignment_run(admin_id);

CREATE INDEX idx_assignment_dataset_id
    ON assignment_run(dataset_id);

CREATE INDEX idx_assignment_created_at
    ON assignment_run(created_at DESC);

CREATE INDEX idx_assignment_result_run_id
    ON assignment_result(assignment_run_id);