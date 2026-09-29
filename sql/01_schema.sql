WHENEVER SQLERROR EXIT SQL.SQLCODE

CREATE TABLE PRESSING_MACHINE (
  machine_id NUMBER(9) NOT NULL,
  machine_name VARCHAR2(80) NOT NULL,
  model_number VARCHAR2(60) NOT NULL,
  installed_date DATE NOT NULL,
  last_maintenance_date DATE,
  status VARCHAR2(25) NOT NULL,
  PRIMARY KEY (machine_id),
  CHECK (status IN ('Operational', 'Under Maintenance', 'Faulty', 'Retired')),
  CHECK (last_maintenance_date IS NULL OR last_maintenance_date >= installed_date),
  CHECK (machine_id > 0)
);

CREATE TABLE RAW_MATERIAL (
  material_id NUMBER(9) NOT NULL,
  material_name VARCHAR2(100) NOT NULL,
  supplier_name VARCHAR2(100) NOT NULL,
  quantity_in_stock NUMBER(12,3) NOT NULL,
  unit VARCHAR2(12) NOT NULL,
  last_restocked_date DATE,
  storage_location VARCHAR2(100) NOT NULL,
  PRIMARY KEY (material_id),
  CHECK (unit IN ('Kilograms', 'Sheets', 'Rolls', 'Pieces')),
  UNIQUE (material_id, unit),
  CHECK (quantity_in_stock >= 0),
  CHECK (material_id > 0)
);

CREATE TABLE ARTIST (
  artist_id NUMBER(9) NOT NULL,
  artist_name VARCHAR2(100) NOT NULL,
  genre VARCHAR2(60) NOT NULL,
  contact_person VARCHAR2(100) NOT NULL,
  phone VARCHAR2(25) NOT NULL,
  email VARCHAR2(120) NOT NULL,
  contract_start_date DATE NOT NULL,
  contract_end_date DATE,
  PRIMARY KEY (artist_id),
  CHECK (contract_end_date IS NULL OR contract_end_date >= contract_start_date),
  CHECK (artist_id > 0)
);

CREATE TABLE VINYL_RELEASE (
  release_id NUMBER(9) NOT NULL,
  artist_id NUMBER(9) NOT NULL,
  album_title VARCHAR2(150) NOT NULL,
  format VARCHAR2(10) NOT NULL,
  color_variant VARCHAR2(60) NOT NULL,
  speed_rpm NUMBER(2) NOT NULL,
  total_tracks NUMBER(3) NOT NULL,
  estimated_runtime_minutes NUMBER(6,2) NOT NULL,
  master_vinyl_disc_id NUMBER(9),
  PRIMARY KEY (release_id),
  FOREIGN KEY (artist_id) REFERENCES ARTIST (artist_id),
  CHECK (format IN ('7 Inch', '10 Inch', '12 Inch', 'LP', 'EP')),
  CHECK (speed_rpm IN (33, 45, 78)),
  CHECK (total_tracks > 0),
  CHECK (estimated_runtime_minutes > 0),
  CHECK (release_id > 0)
);

CREATE TABLE MASTER_DISC (
  disc_id NUMBER(9) NOT NULL,
  release_id NUMBER(9) NOT NULL,
  disc_name VARCHAR2(100) NOT NULL,
  created_date DATE NOT NULL,
  created_by VARCHAR2(100) NOT NULL,
  storage_location VARCHAR2(100) NOT NULL,
  disc_condition VARCHAR2(10) NOT NULL,
  PRIMARY KEY (disc_id),
  FOREIGN KEY (release_id) REFERENCES VINYL_RELEASE (release_id),
  CHECK (disc_condition IN ('New', 'Good', 'Worn', 'Damaged')),
  UNIQUE (disc_id, release_id),
  CHECK (disc_id > 0)
);

ALTER TABLE VINYL_RELEASE ADD CONSTRAINT fk_selected_master
  FOREIGN KEY (master_vinyl_disc_id, release_id)
  REFERENCES MASTER_DISC (disc_id, release_id);

CREATE TABLE PRODUCTION_BATCH (
  batch_id NUMBER(9) NOT NULL,
  release_id NUMBER(9) NOT NULL,
  machine_id NUMBER(9) NOT NULL,
  batch_size NUMBER(9) NOT NULL,
  started_at TIMESTAMP,
  completed_at TIMESTAMP,
  supervisor_name VARCHAR2(100) NOT NULL,
  status VARCHAR2(15) NOT NULL,
  PRIMARY KEY (batch_id),
  FOREIGN KEY (release_id) REFERENCES VINYL_RELEASE (release_id),
  FOREIGN KEY (machine_id) REFERENCES PRESSING_MACHINE (machine_id),
  CHECK (status IN ('Scheduled', 'In Progress', 'Completed', 'Quality Hold', 'Rejected')),
  UNIQUE (batch_id, release_id),
  CHECK (batch_size > 0),
  CHECK (completed_at IS NULL OR (started_at IS NOT NULL AND completed_at >= started_at)),
  CHECK (status NOT IN ('In Progress', 'Completed') OR started_at IS NOT NULL),
  CHECK (status <> 'Completed' OR completed_at IS NOT NULL),
  CHECK (batch_id > 0)
);

CREATE TABLE BATCH_MATERIAL_USAGE (
  usage_id NUMBER(9) NOT NULL,
  batch_id NUMBER(9) NOT NULL,
  material_id NUMBER(9) NOT NULL,
  quantity_used NUMBER(12,3) NOT NULL,
  unit VARCHAR2(12) NOT NULL,
  PRIMARY KEY (usage_id),
  FOREIGN KEY (batch_id) REFERENCES PRODUCTION_BATCH (batch_id),
  FOREIGN KEY (material_id) REFERENCES RAW_MATERIAL (material_id),
  CHECK (unit IN ('Kilograms', 'Sheets', 'Rolls', 'Pieces')),
  CHECK (quantity_used > 0),
  CHECK (usage_id > 0),
  CONSTRAINT fk_usage_material_unit FOREIGN KEY (material_id, unit)
    REFERENCES RAW_MATERIAL (material_id, unit)
);

CREATE TABLE QUALITY_CHECK (
  check_id NUMBER(9) NOT NULL,
  batch_id NUMBER(9) NOT NULL,
  sample_size NUMBER(9) NOT NULL,
  checked_by VARCHAR2(100) NOT NULL,
  checked_at TIMESTAMP NOT NULL,
  visual_defects_found VARCHAR2(25) NOT NULL,
  audio_defects_found VARCHAR2(15) NOT NULL,
  overall_result VARCHAR2(20) NOT NULL,
  notes VARCHAR2(500),
  PRIMARY KEY (check_id),
  FOREIGN KEY (batch_id) REFERENCES PRODUCTION_BATCH (batch_id),
  CHECK (visual_defects_found IN ('Scratches', 'Warping', 'Bubbles', 'Color Inconsistency', 'None')),
  CHECK (audio_defects_found IN ('Pops', 'Clicks', 'Skips', 'Distortion', 'None')),
  CHECK (overall_result IN ('Pass', 'Conditional Pass', 'Fail')),
  CHECK (sample_size > 0),
  CHECK (check_id > 0)
);

CREATE TABLE FINISHED_RECORD (
  record_id NUMBER(9) NOT NULL,
  batch_id NUMBER(9) NOT NULL,
  release_id NUMBER(9) NOT NULL,
  serial_number VARCHAR2(60) NOT NULL,
  sleeve_type VARCHAR2(25) NOT NULL,
  jacket_condition VARCHAR2(12) NOT NULL,
  current_location VARCHAR2(100) NOT NULL,
  PRIMARY KEY (record_id),
  FOREIGN KEY (batch_id) REFERENCES PRODUCTION_BATCH (batch_id),
  FOREIGN KEY (release_id) REFERENCES VINYL_RELEASE (release_id),
  CHECK (sleeve_type IN ('Standard', 'Gatefold', 'Picture Disc Sleeve')),
  CHECK (jacket_condition IN ('Mint', 'Near Mint', 'Good', 'Fair')),
  UNIQUE (serial_number),
  CHECK (record_id > 0),
  CONSTRAINT fk_record_batch_release FOREIGN KEY (batch_id, release_id)
    REFERENCES PRODUCTION_BATCH (batch_id, release_id)
);

CREATE TABLE CUSTOMER_ORDER (
  order_id NUMBER(9) NOT NULL,
  release_id NUMBER(9) NOT NULL,
  customer_name VARCHAR2(100) NOT NULL,
  quantity_ordered NUMBER(9) NOT NULL,
  order_date DATE NOT NULL,
  status VARCHAR2(15) NOT NULL,
  shipping_address VARCHAR2(250) NOT NULL,
  shipped_date DATE,
  tracking_number VARCHAR2(80),
  PRIMARY KEY (order_id),
  FOREIGN KEY (release_id) REFERENCES VINYL_RELEASE (release_id),
  CHECK (status IN ('Pending', 'In Production', 'Quality Hold', 'Ready to Ship', 'Shipped', 'Delivered')),
  CHECK (quantity_ordered > 0),
  CHECK (shipped_date IS NULL OR shipped_date >= order_date),
  CHECK (status NOT IN ('Shipped', 'Delivered') OR (shipped_date IS NOT NULL AND tracking_number IS NOT NULL)),
  CHECK (order_id > 0)
);

CREATE TABLE WAREHOUSE_LOCATION (
  location_id NUMBER(9) NOT NULL,
  shelf_number VARCHAR2(20) NOT NULL,
  row_label VARCHAR2(20) NOT NULL,
  section VARCHAR2(40) NOT NULL,
  current_capacity_used NUMBER(9) NOT NULL,
  max_capacity NUMBER(9) NOT NULL,
  PRIMARY KEY (location_id),
  UNIQUE (shelf_number, row_label, section),
  CHECK (current_capacity_used >= 0),
  CHECK (max_capacity > 0),
  CHECK (current_capacity_used <= max_capacity),
  CHECK (location_id > 0)
);

CREATE TABLE EMPLOYEE (
  employee_id NUMBER(9) NOT NULL,
  name VARCHAR2(100) NOT NULL,
  role VARCHAR2(20) NOT NULL,
  shift VARCHAR2(10) NOT NULL,
  phone VARCHAR2(25) NOT NULL,
  date_joined DATE NOT NULL,
  PRIMARY KEY (employee_id),
  CHECK (role IN ('Press Operator', 'Quality Inspector', 'Warehouse Staff', 'Supervisor', 'Admin')),
  CHECK (shift IN ('Morning', 'Evening', 'Night')),
  CHECK (employee_id > 0)
);

CREATE TABLE MAINTENANCE_LOG (
  log_id NUMBER(9) NOT NULL,
  machine_id NUMBER(9) NOT NULL,
  maintenance_date DATE NOT NULL,
  issue_reported VARCHAR2(250) NOT NULL,
  action_taken VARCHAR2(250) NOT NULL,
  performed_by VARCHAR2(100) NOT NULL,
  next_due_date DATE,
  PRIMARY KEY (log_id),
  FOREIGN KEY (machine_id) REFERENCES PRESSING_MACHINE (machine_id),
  CHECK (next_due_date IS NULL OR next_due_date >= maintenance_date),
  CHECK (log_id > 0)
);

CREATE TABLE RESTOCK_LOG (
  restock_id NUMBER(9) NOT NULL,
  material_id NUMBER(9) NOT NULL,
  quantity_added NUMBER(12,3) NOT NULL,
  restocked_date DATE NOT NULL,
  supplier_name VARCHAR2(100) NOT NULL,
  invoice_number VARCHAR2(60) NOT NULL,
  received_by VARCHAR2(100) NOT NULL,
  PRIMARY KEY (restock_id),
  FOREIGN KEY (material_id) REFERENCES RAW_MATERIAL (material_id),
  CHECK (quantity_added > 0),
  CHECK (restock_id > 0)
);

CREATE SEQUENCE seq_pressing_machine START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_maintenance_log START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_raw_material START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_restock_log START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_artist START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_vinyl_release START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_master_disc START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_production_batch START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_batch_material_usage START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_quality_check START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_finished_record START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_customer_order START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_warehouse_location START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_employee START WITH 1000 INCREMENT BY 1 NOCACHE NOCYCLE;

CREATE INDEX idx_maintenance_machine ON MAINTENANCE_LOG (machine_id);
CREATE INDEX idx_restock_material ON RESTOCK_LOG (material_id);
CREATE INDEX idx_release_artist ON VINYL_RELEASE (artist_id);
CREATE INDEX idx_master_release ON MASTER_DISC (release_id);
CREATE INDEX idx_batch_release ON PRODUCTION_BATCH (release_id);
CREATE INDEX idx_batch_machine ON PRODUCTION_BATCH (machine_id);
CREATE INDEX idx_usage_batch ON BATCH_MATERIAL_USAGE (batch_id);
CREATE INDEX idx_usage_material ON BATCH_MATERIAL_USAGE (material_id);
CREATE INDEX idx_quality_batch ON QUALITY_CHECK (batch_id);
CREATE INDEX idx_record_batch ON FINISHED_RECORD (batch_id);
CREATE INDEX idx_record_release ON FINISHED_RECORD (release_id);
CREATE INDEX idx_order_release ON CUSTOMER_ORDER (release_id);
